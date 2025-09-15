package de.drvlabs.btscreen.utils;

import org.jetbrains.annotations.Nullable;

import baritone.api.selection.ISelection;
import baritone.api.selection.ISelectionManager;
import baritone.api.utils.BetterBlockPos;

/**
 * Simple orchestrator to iterate a Baritone selection layer-by-layer from top to bottom.
 * It seeds a one-layer selection at the top Y of the original selection and, whenever
 * Baritone finishes, it shifts the selection down by one and re-runs the command until
 * the bottom Y is reached.
 */
public final class LayeredSelection {
    private static boolean active = false;
    private static int minX, maxX, minZ, maxZ;
    private static int yBottom, yCurrent;
    private static String commandToRun;
    private static int graceTicksRemaining = 0;
    private static int consecutiveIdleChecks = 0;
    private static Waiter poller = null;

    private LayeredSelection() {}

    public static boolean isActive() {
        return active;
    }

    /**
     * Start a layer-by-layer run using the current single selection as the base.
     * Stores the original X/Z extents and bottom/top Y, then seeds a 1-layer selection at the top.
     */
    public static void startFromCurrentSelection(String command) {
        if (active) {
            // already running
            return;
        }
        ISelectionManager sm = Utils.BT.getSelectionManager();
        ISelection base = getSingleSelection(sm);
        if (base == null) {
            return;
        }

        BetterBlockPos min = base.min();
        BetterBlockPos max = base.max();

        minX = Math.min(min.getX(), max.getX());
        maxX = Math.max(min.getX(), max.getX());
        minZ = Math.min(min.getZ(), max.getZ());
        maxZ = Math.max(min.getZ(), max.getZ());
        int yTop = Math.max(min.getY(), max.getY());
        yBottom = Math.min(min.getY(), max.getY());

        yCurrent = yTop;
        commandToRun = command;
        active = true;

        // Seed a fresh, single-layer selection at the top and run the command once
        sm.removeAllSelections();
        sm.addSelection(new BetterBlockPos(minX, yCurrent, minZ), new BetterBlockPos(maxX, yCurrent, maxZ));
        runLayerCommand();
        startPolling();
    }

    /**
     * Legacy hook. We now rely on polling; keep cancel handling for safety.
     */
    public static void onBaritoneStopped(boolean canceled) {
        if (!active) {
            return;
        }
        if (canceled) {
            finish(false);
            return;
        }
    }

    private static void runLayerCommand() {
        // Ensure generic repeat action won't interfere with our orchestrated loop
        RepeatAction.cancel();
        graceTicksRemaining = 40; // ~2 seconds grace
        consecutiveIdleChecks = 0;
        Utils.executeBuild(commandToRun);
    }

    private static void startPolling() {
        if (poller != null) {
            poller.cancel();
        }
        // Poll every 20 ticks (~1 second)
        poller = Waiter.wait(20, w -> {
            if (!active) {
                return;
            }
            if (graceTicksRemaining > 0) {
                graceTicksRemaining -= 20;
            } else {
                if (isBotIdle()) {
                    consecutiveIdleChecks++;
                } else {
                    consecutiveIdleChecks = 0;
                }
                if (consecutiveIdleChecks >= 2) { // idle ~2s => advance
                    advanceLayerOrFinish();
                }
            }
            if (active) {
                w.start(20);
            }
        });
    }

    private static boolean isBotIdle() {
        return Utils.getActiveProcess() == null;
    }

    private static void advanceLayerOrFinish() {
        if (yCurrent <= yBottom) {
            finish(true);
            return;
        }
        yCurrent -= 1;
        ISelectionManager sm = Utils.BT.getSelectionManager();
        sm.removeAllSelections();
        sm.addSelection(new BetterBlockPos(minX, yCurrent, minZ), new BetterBlockPos(maxX, yCurrent, maxZ));
        runLayerCommand();
    }

    private static void finish(boolean completedAllLayers) {
        active = false;
        if (poller != null) {
            poller.cancel();
            poller = null;
        }
        if (completedAllLayers) {
            Utils.chatMessage(net.minecraft.text.Text.translatable("btscreen.info.layeredselection.done"));
        }
    }

    public static void cancel() {
        if (active) {
            finish(false);
        }
    }

    @Nullable
    private static ISelection getSingleSelection(ISelectionManager sm) {
        ISelection[] sels = sm.getSelections();
        if (sels == null || sels.length == 0) {
            return null;
        }
        if (sels.length == 1) {
            return sm.getOnlySelection() != null ? sm.getOnlySelection() : sm.getLastSelection();
        }
        return null; // multiple selections are not supported here
    }
}
