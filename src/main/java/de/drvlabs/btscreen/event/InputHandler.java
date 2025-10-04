package de.drvlabs.btscreen.event;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Hotkeys;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;

public class InputHandler implements IKeybindProvider {
	private static InputHandler INSTANCE = null;

	private InputHandler() {
		InputEventHandler.getKeybindManager().registerKeybindProvider(this);
	}

	public static void register() {
		if (INSTANCE == null) {
			INSTANCE = new InputHandler();
		}
	}

	@Override
	public void addKeysToMap(IKeybindManager manager) {
		for (IHotkey hotkey : Hotkeys.HOTKEY_LIST) {
			manager.addKeybindToMap(hotkey.getKeybind());
		}
	}

	@Override
	public void addHotkeys(IKeybindManager manager) {
		manager.addHotkeysForCategory(BTScreen.MOD_NAME, BTScreen.MOD_ID + ".hotkeys.category.generic_hotkeys",
				Hotkeys.HOTKEY_LIST);
	}
}
