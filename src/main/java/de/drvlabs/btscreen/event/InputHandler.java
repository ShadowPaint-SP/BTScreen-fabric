package de.drvlabs.btscreen.event;

import de.drvlabs.btscreen.Reference;
import de.drvlabs.btscreen.config.Hotkeys;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;

public class InputHandler implements IKeybindProvider {
	public static final InputHandler INSTANCE = new InputHandler();

	@Override
	public void addKeysToMap(IKeybindManager manager) {
		for (IHotkey hotkey : Hotkeys.HOTKEY_LIST) {
			manager.addKeybindToMap(hotkey.getKeybind());
		}
	}

	@Override
	public void addHotkeys(IKeybindManager manager) {
		manager.addHotkeysForCategory(Reference.MOD_NAME, Reference.MOD_ID + ".hotkeys.category.generic_hotkeys",
				Hotkeys.HOTKEY_LIST);
	}
}
