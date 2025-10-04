package de.drvlabs.btscreen.implementation;

import static de.drvlabs.btscreen.config.Configs.Generic.REPEAT_ACTION;
import static de.drvlabs.btscreen.config.Configs.Generic.REPEAT_ACTION_INTERVAL;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;

public class RepeatAction {
    private static String lastCommand = "";
    private static Waiter waiter = null;

    public static void trackCommand(String command) {
        lastCommand = command;
    }

    public static void baritoneStopped(boolean canceled) {
        if (canceled) {
            cancel();
        } else if (REPEAT_ACTION.getBooleanValue()) {
            start();
        }
    }

    public static void cancel() {
        if (waiter == null) {
            return;
        }
        waiter.cancel();
        waiter = null;
        REPEAT_ACTION.setBooleanValue(false);
        BTScreen.debugLog("canceled repeat action");
    }

    private static void start() {
        BTScreen.debugLog("starting wait period for repeat action");
        waiter = Waiter.wait(REPEAT_ACTION_INTERVAL.getIntegerValue(), w -> {
            BTScreen.debugLog("wait period over, executing last command");
            Utils.execute(lastCommand);
        });
    }
}
