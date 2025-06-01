package drvlabs.de.config;

import java.util.List;

import com.google.common.collect.ImmutableList;

import drvlabs.de.Reference;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;

public class Hotkeys {
	private static final String HOTKEYS_KEY = Reference.MOD_ID + ".config.hotkeys";

	public static final ConfigHotkey OPEN_GUI_MAIN_MENU = new ConfigHotkey("openGuiMainMenu", "P",
			KeybindSettings.RELEASE_EXCLUSIVE).apply(HOTKEYS_KEY);

	public static final List<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(
			OPEN_GUI_MAIN_MENU);
}
