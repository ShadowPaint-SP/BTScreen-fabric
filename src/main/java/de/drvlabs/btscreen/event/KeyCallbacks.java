package de.drvlabs.btscreen.event;

import baritone.api.BaritoneAPI;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.gui.GuiCommandList;
import de.drvlabs.btscreen.gui.GuiConfigs;
import de.drvlabs.btscreen.gui.GuiMainMenu;
import de.drvlabs.btscreen.utils.BotStatus;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;

public class KeyCallbacks {
	public static boolean openGuiMainMenu(KeyAction action, IKeybind key) {
		GuiBase.openGui(new GuiMainMenu());
		return true;
	}

	public static boolean openGuiSettings(KeyAction action, IKeybind key) {
		GuiBase.openGui(new GuiConfigs());
		return true;
	}

	public static boolean openGuiCustomCommands(KeyAction action, IKeybind key) {
		GuiBase.openGui(new GuiCommandList());
		return true;
	}

	public static boolean pauseResume(KeyAction action, IKeybind key) {
		if (BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().isPaused()) {
			BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().resume();
			DataManager.setBotStatus(BotStatus.MINING);
		} else {
			BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().pause();
			DataManager.setBotStatus(BotStatus.IDLE);
		}
		return true;
	}
}
