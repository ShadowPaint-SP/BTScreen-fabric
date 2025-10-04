package de.drvlabs.btscreen.implementation;

import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;

public class RetryUnreplaceableLiquids {
    private static int retryLiquidCount = -1;

    public static void onBaritoneLog(String msg) {
        if (!msg.equals("Unreplaceable liquids at at least:") || retryLiquidCount >= 0)
            return;
        retryLiquidCount = 2;
        Waiter.wait(100, w -> {
            if (!Utils.isPaused()) {
                retryLiquidCount = 0;
            } else if (retryLiquidCount > 0) {
                Utils.resume();
                w.start(100);
            }
            retryLiquidCount--;
        });
    }
}