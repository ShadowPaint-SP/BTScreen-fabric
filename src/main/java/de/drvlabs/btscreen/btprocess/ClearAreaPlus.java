package de.drvlabs.btscreen.btprocess;

import baritone.api.process.IBaritoneProcess;
import baritone.api.process.IBuilderProcess;
import baritone.api.process.PathingCommand;
import baritone.api.selection.ISelection;
import baritone.api.selection.ISelectionManager;
import baritone.api.utils.BetterBlockPos;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Clears one selection in five-block slices from top to bottom.
 *
 * <p>
 * SmartWaterClear receives the five-layer slice plus the layer directly
 * below it. Once it returns, the normal clear-area command receives only the
 * five layers that belong to the current slice. This process restores the
 * original selection after the full job or after any cancellation.
 * </p>
 */
public final class ClearAreaPlus extends BTProcessHelper {
    public static final ClearAreaPlus INSTANCE = new ClearAreaPlus();

    private static final String TRANSLATABLE_PREFIX = BTScreen.MOD_ID + ".clearAreaPlus.";
    private static final int SLICE_HEIGHT = 5;
    private static final int CHILD_STOP_GRACE_TICKS = 2;
    private static final IBuilderProcess BUILD_PROC = Utils.BT.getBuilderProcess();
    private static final ISelectionManager SEL_MGR = Utils.BT.getSelectionManager();

    private Phase phase = Phase.IDLE;
    private ISelection originalSelection;
    private BetterBlockPos originalMin;
    private BetterBlockPos originalMax;
    private int currentTopY;
    private int finishedSliceCount;
    private int childStopGraceTicks;
    private boolean clearAreaFailureScheduled;

    private ClearAreaPlus() {
    }

    @Override
    public boolean isActive() {
        return phase != Phase.IDLE;
    }

    public static boolean isRunning() {
        return INSTANCE.isActive();
    }

    public static boolean activate() {
        ClearAreaPlus process = INSTANCE;
        if (process.isActive() || SmartWaterClear.isRunning()) {
            message("alreadyStarted", ChatFormatting.RED);
            return false;
        }

        ISelection selection = SEL_MGR.getOnlySelection();
        if (selection == null) {
            message("noSelection", ChatFormatting.RED);
            return false;
        }

        BetterBlockPos min = selection.min();
        BetterBlockPos max = selection.max();
        if (max.x - min.x + 1 < 5 || max.z - min.z + 1 < 5) {
            message("selectionTooNarrow", ChatFormatting.RED);
            return false;
        }

        process.originalSelection = selection;
        process.originalMin = min;
        process.originalMax = max;
        process.currentTopY = max.y;
        process.finishedSliceCount = 0;
        process.phase = Phase.START_LIQUID_REMOVAL;
        message("started", ChatFormatting.WHITE);
        return true;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (clearAreaFailureScheduled) {
            return REQUEST_PAUSE;
        }
        if (!isSafeToCancel || SmartWaterClear.isRunning() || BUILD_PROC.isActive()) {
            return REQUEST_PAUSE;
        }

        switch (phase) {
            case START_LIQUID_REMOVAL:
                applyLiquidRemovalSlice();
                SmartWaterClear.activate();
                childStopGraceTicks = CHILD_STOP_GRACE_TICKS;
                phase = Phase.WAIT_FOR_LIQUID_REMOVAL;
                return REQUEST_PAUSE;
            case WAIT_FOR_LIQUID_REMOVAL:
                if (childStopGraceTicks-- > 0) {
                    return REQUEST_PAUSE;
                }
                phase = Phase.START_CLEAR_AREA;
                return REQUEST_PAUSE;
            case START_CLEAR_AREA:
                applyClearAreaSlice();
                Utils.execute("sel cleararea");
                childStopGraceTicks = CHILD_STOP_GRACE_TICKS;
                phase = Phase.WAIT_FOR_CLEAR_AREA;
                return REQUEST_PAUSE;
            case WAIT_FOR_CLEAR_AREA:
                if (childStopGraceTicks-- > 0) {
                    return REQUEST_PAUSE;
                }
                finishCurrentSlice();
                return isActive() ? REQUEST_PAUSE : DEFER;
            default:
                return DEFER;
        }
    }

    private void applyLiquidRemovalSlice() {
        applySlice(SLICE_HEIGHT + 1, "liquid removal");
    }

    private void applyClearAreaSlice() {
        applySlice(SLICE_HEIGHT, "clear area");
    }

    private void applySlice(int height, String purpose) {
        int bottomY = sliceBottomY(height);
        BetterBlockPos sliceMin = new BetterBlockPos(originalMin.x, bottomY, originalMin.z);
        BetterBlockPos sliceMax = new BetterBlockPos(originalMax.x, currentTopY, originalMax.z);
        SEL_MGR.removeAllSelections();
        SEL_MGR.addSelection(sliceMin, sliceMax);
        BTScreen.debugLog("layered clear area {} slice: {} to {}", purpose, sliceMin, sliceMax);
    }

    private int sliceBottomY(int height) {
        return Math.max(originalMin.y, currentTopY - height + 1);
    }

    private void finishCurrentSlice() {
        finishedSliceCount++;
        message("layerFinished", ChatFormatting.GRAY, finishedSliceCount);
        currentTopY -= SLICE_HEIGHT;
        if (currentTopY < originalMin.y) {
            reset(true);
            message("finished", ChatFormatting.GREEN);
            return;
        }
        phase = Phase.START_LIQUID_REMOVAL;
    }

    public static boolean handleBaritoneLog(String logMessage) {
        ClearAreaPlus process = INSTANCE;
        if (process.phase != Phase.WAIT_FOR_CLEAR_AREA || process.clearAreaFailureScheduled) {
            return false;
        }

        boolean failed = logMessage.equals("Unreplaceable liquids at at least:")
                || logMessage.equals("Unable to do it. Pausing. resume to resume, cancel to cancel")
                || logMessage.equals("Missing materials for at least:");
        if (!failed) {
            return false;
        }

        process.clearAreaFailureScheduled = true;
        message("clearFailed", ChatFormatting.RED, process.finishedSliceCount + 1);
        Waiter.wait(1, waiter -> {
            if (process.isActive()) {
                process.reset(false);
                Utils.cancel();
            }
        });
        return true;
    }

    public static boolean shouldSuppressBaritoneChat(Component message) {
        return isRunning() && message.getString().endsWith("Done building");
    }

    public static void resetForWorldChange() {
        INSTANCE.reset(false);
    }

    @Override
    public void onLostControl() {
        IBaritoneProcess activeProcess = Utils.getActiveProcess();
        boolean childHasControl = activeProcess == SmartWaterClear.INSTANCE || activeProcess == BUILD_PROC;
        if (childHasControl) {
            return;
        }

        ISelection selectionToRestore = originalSelection;
        reset(false);
        if (selectionToRestore != null && activeProcess == null) {
            Waiter.wait(2, waiter -> {
                if (!isActive()) {
                    restoreSelection(selectionToRestore);
                }
            });
        }
    }

    private void reset(boolean completed) {
        boolean hadState = isActive() || originalSelection != null;
        restoreOriginalSelection();
        phase = Phase.IDLE;
        originalSelection = null;
        originalMin = null;
        originalMax = null;
        currentTopY = 0;
        finishedSliceCount = 0;
        childStopGraceTicks = 0;
        clearAreaFailureScheduled = false;
        if (hadState && !completed) {
            BTScreen.debugLog("layered clear area reset before completion");
        }
    }

    private void restoreOriginalSelection() {
        if (originalSelection == null) {
            return;
        }
        restoreSelection(originalSelection);
    }

    private static void restoreSelection(ISelection selection) {
        SEL_MGR.removeAllSelections();
        SEL_MGR.addSelection(selection);
    }

    @Override
    public double priority() {
        return DEFAULT_PRIORITY - 2;
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    private static void message(String key, ChatFormatting formatting, Object... args) {
        BTScreen.chatMessage(Component.translatable(TRANSLATABLE_PREFIX + key, args).withStyle(formatting));
    }

    private enum Phase {
        IDLE,
        START_LIQUID_REMOVAL,
        WAIT_FOR_LIQUID_REMOVAL,
        START_CLEAR_AREA,
        WAIT_FOR_CLEAR_AREA
    }
}
