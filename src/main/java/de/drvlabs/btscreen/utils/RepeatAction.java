package de.drvlabs.btscreen.utils;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;

// TODO: fix for new system
public class RepeatAction {
	private static String lastCommand = "";
	private static boolean isWaiting = false;

	public static void trackCommand(String command) {
		if (!Configs.Generic.REPEAT_ACTION.getBooleanValue()) {
			return;
		}
		lastCommand = command;
	}

	public static void cancelRepeatAction() {
		if (!Configs.Generic.REPEAT_ACTION.getBooleanValue()) {
			return;
		}
		isWaiting = false;
		Configs.Generic.REPEAT_ACTION.setBooleanValue(false);
		BTScreen.debugLog("canceled repeat action");
	}

	public static void startWaitPeriod() {
		if (!Configs.Generic.REPEAT_ACTION.getBooleanValue()) {
			return;
		}
		isWaiting = true;
		BTScreen.debugLog("starting wait period for repeat action");
		Waiter.wait(Configs.Generic.REPEAT_ACTION_INTERVAL.getIntegerValue(), w -> {
			if (isWaiting) {
				BTScreen.debugLog("wait period over, executing last command");
				Utils.execute(lastCommand);
				isWaiting = false;
			}
		});
	}
}
