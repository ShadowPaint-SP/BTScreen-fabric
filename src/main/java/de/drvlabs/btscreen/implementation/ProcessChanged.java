package de.drvlabs.btscreen.implementation;

import static de.drvlabs.btscreen.config.Configs.Generic.SHOW_PROCESS_CHANGES;
import static de.drvlabs.btscreen.config.Configs.Lists.PROCESS_CHANGES_BLACKLIST;

import java.util.List;

import baritone.api.process.IBaritoneProcess;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.text.Text;

public final class ProcessChanged {
    private ProcessChanged() {
    }

    private static boolean shouldDisplayProcesses(IBaritoneProcess... processes) {
        if (!SHOW_PROCESS_CHANGES.getBooleanValue()) {
            return false;
        }
        final List<String> strings = PROCESS_CHANGES_BLACKLIST.getStrings();
        for (IBaritoneProcess process : processes) {
            String processName = process == null ? "IDLE" : process.getClass().getSimpleName();
            if (strings.contains(processName)) {
                return false;
            }
        }
        return true;
    }

    private static String toDebugString(IBaritoneProcess process) {
        return process == null ? "IDLE"
                : String.format("%s[%s, isTemporary: %s, priority: %s, toString: %s]",
                        process.getClass().getSimpleName(), process.displayName0(), process.isTemporary(),
                        process.priority(), process.toString());
    }

    private static String toString(IBaritoneProcess process) {
        return process == null ? "IDLE"
                : String.format("%s[%s]", process.getClass().getSimpleName(), process.displayName0());
    }

    private static void baritoneProcessChanged(IBaritoneProcess oldProcess, IBaritoneProcess newProcess) {
        if (shouldDisplayProcesses(oldProcess, newProcess)) {
            BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".processChanged",
                    toString(oldProcess), toString(newProcess)));
        }
        BTScreen.debugLog("Baritone Process changed: oldProcess: {}, newProcess: {}",
                toDebugString(oldProcess), toDebugString(newProcess));
    }

    private static IBaritoneProcess lastProcess = null;

    public static void onTick() {
        IBaritoneProcess currentProcess = Utils.getActiveProcess();
        if (lastProcess == currentProcess)
            return;
        baritoneProcessChanged(lastProcess, currentProcess);
        lastProcess = currentProcess;
    }
}
