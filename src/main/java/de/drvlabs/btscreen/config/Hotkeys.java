package de.drvlabs.btscreen.config;

import java.util.List;
import java.util.function.BiFunction;

import de.drvlabs.btscreen.event.KeyCallbacks;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;

public enum Hotkeys implements IHotkeyCallback {
    OPEN_GUI_MAIN_MENU(new ConfigHotkey("openGuiMainMenu", "P", KeybindSettings.RELEASE_EXCLUSIVE),
            KeyCallbacks::openGuiMainMenu),
    OPEN_GUI_SETTINGS(new ConfigHotkey("openGuiSettings", "P,C"), KeyCallbacks::openGuiSettings),
    OPEN_GUI_CUSTOM_COMMANDS(new ConfigHotkey("openGuiCustomCommands", ""), KeyCallbacks::openGuiCustomCommands),
    PAUSE_RESUME(new ConfigHotkey("pauseResume", "", KeybindSettings.RELEASE_EXCLUSIVE), KeyCallbacks::pauseResume);

    Hotkeys(ConfigHotkey hotkey, BiFunction<KeyAction, IKeybind, Boolean> callback) {
        hotkey.apply(LangKeys.CONFIG_HOTKEYS).getKeybind().setCallback(this);
        this.hotkey = hotkey;
        this.callback = callback;
    }

    public final ConfigHotkey hotkey;
    private final BiFunction<KeyAction, IKeybind, Boolean> callback;

    public static final List<ConfigHotkey> HOTKEY_LIST = List.of(values()).stream().map(h -> h.hotkey).toList();

    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key) {
        if (Utils.MC.player == null || Utils.MC.world == null) {
            return false;
        }
        return callback.apply(action, key);
    }
}