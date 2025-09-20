package de.drvlabs.btscreen.btprocess;

import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import baritone.api.process.PathingCommand;
import baritone.api.selection.ISelection;
import baritone.api.selection.ISelectionManager;
import baritone.api.utils.BetterBlockPos;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SelectionOrchestrator extends BTProcessHelper {
    private static final ISelectionManager SEL_MGR = Utils.BT.getSelectionManager();
    private static String[] layerCommands;
    private static int layerHeight;
    private int currentY = Integer.MAX_VALUE;
    private ISelection[] selections;
    private Iterator<String> commandIterator;

    @Override
    public boolean isActive() {
        return layerCommands != null && !Utils.BT.getBuilderProcess().isActive();
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (isSafeToCancel) {
            if (currentY == Integer.MAX_VALUE) {
                selections = SEL_MGR.getSelections();
                BTScreen.debugLog("selections: {}", selections.toString());
                if (selections.length == 0) {
                    BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".selectionOrchestrator.noSelection").formatted(Formatting.RED));
                    onLostControl();
                    return DEFER;
                }
                IntStream allY = Stream.of(selections).mapMultiToInt((s, c) -> {
                    c.accept(s.min().y);
                    c.accept(s.max().y);
                });
                if (layerHeight > 0) {
                    currentY = allY.min().getAsInt();
                } else {
                    currentY = allY.max().getAsInt();
                }
            }
            BTScreen.debugLog("currentY: {}", currentY);
            if (commandIterator != null && commandIterator.hasNext()) {
                Utils.execute(commandIterator.next());
                return REQUEST_PAUSE;
            }
            SEL_MGR.removeAllSelections();
            for (ISelection selection : selections) {
                BetterBlockPos min = selection.min();
                BetterBlockPos max = selection.max();
                BTScreen.debugLog("min: {}, max: {}", min, max);
                if (min.y <= currentY && max.y >= currentY) {
                    BetterBlockPos pos1 = new BetterBlockPos(min.x, currentY, min.z);
                    int pos2Y = currentY + layerHeight + (layerHeight > 0 ? -1 : 1);
                    if (pos2Y < min.y) {
                        pos2Y = min.y;
                    }
                    if (pos2Y > max.y) {
                        pos2Y = max.y;
                    }
                    BetterBlockPos pos2 = new BetterBlockPos(max.x, pos2Y, max.z);;
                    BTScreen.debugLog("pos1: {}, pos2: {}", pos1, pos2);
                    SEL_MGR.addSelection(pos1, pos2);
                }
            }
            if (SEL_MGR.getSelections().length == 0) {
                BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".selectionOrchestrator.finished").formatted(Formatting.GREEN));
                onLostControl();
                return DEFER;
            }
            commandIterator = Stream.of(layerCommands).iterator();
            Utils.execute(commandIterator.next());
            currentY += (currentY > 0 ? -1 : 1);
        }
        return REQUEST_PAUSE;
    }

    @Override
    public void onLostControl() {
        layerCommands = null;
        currentY = Integer.MAX_VALUE;
        if (selections != null && selections.length > 0) {
            SEL_MGR.removeAllSelections();
            for (ISelection selection : selections) {
                SEL_MGR.addSelection(selection);
            }
        }
        selections = null;
        commandIterator = null;
    }

    @Override
    public double priority() {
        return DEFAULT_PRIORITY - 1;
    }

    public static boolean activate(int layerHeight, String... command) {
        if (layerHeight == 0 || command.length == 0 || SelectionOrchestrator.layerCommands != null)
            return false;
        SelectionOrchestrator.layerCommands = command;
        SelectionOrchestrator.layerHeight = layerHeight;
        return true;
    }
}
