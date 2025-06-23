package drvlabs.de.event;

import baritone.api.BaritoneAPI;
import drvlabs.de.config.Hotkeys;
import drvlabs.de.data.DataManager;
import drvlabs.de.gui.GuiCommandList;
import drvlabs.de.gui.GuiConfigs;
import drvlabs.de.gui.GuiMainMenu;
import drvlabs.de.utils.BotStatus;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class KeyCallbacks {
	public static void init(MinecraftClient mc) {
		IHotkeyCallback callbackHotkeys = new KeyCallbackHotkeys(mc);

		Hotkeys.OPEN_GUI_MAIN_MENU.getKeybind().setCallback(callbackHotkeys);
		Hotkeys.OPEN_GUI_SETTINGS.getKeybind().setCallback(callbackHotkeys);
		Hotkeys.OPEN_GUI_CUSTOM_COMMANDS.getKeybind().setCallback(callbackHotkeys);
		Hotkeys.PAUSE_RESUME.getKeybind().setCallback(callbackHotkeys);
	}

	private static class KeyCallbackHotkeys implements IHotkeyCallback {
		private final MinecraftClient mc;

		public KeyCallbackHotkeys(MinecraftClient mc) {
			this.mc = mc;
		}

		@Override
		public boolean onKeyAction(KeyAction action, IKeybind key) {
			if (this.mc.player == null || this.mc.world == null) {
				return false;
			}

			if (key == Hotkeys.OPEN_GUI_MAIN_MENU.getKeybind()) {
				GuiBase.openGui(new GuiMainMenu());
				return true;
			} else if (key == Hotkeys.OPEN_GUI_SETTINGS.getKeybind()) {
				GuiBase.openGui(new GuiConfigs());
				return true;
			} else if (key == Hotkeys.OPEN_GUI_CUSTOM_COMMANDS.getKeybind()) {
				GuiBase.openGui(new GuiCommandList());
				return true;
			} else if (key == Hotkeys.PAUSE_RESUME.getKeybind()) {
				if (BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().isPaused()) {
					BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().resume();
					DataManager.setBotStatus(BotStatus.MINING);
				} else {
					BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().pause();
					DataManager.setBotStatus(BotStatus.IDLE);
				}
				return true;
			}
			return false;
		}
	}

	public static class FeatureCallbackHold implements IValueChangeCallback<IConfigBoolean> {
		private final KeyBinding keyBind;

		public FeatureCallbackHold(KeyBinding keyBind) {
			this.keyBind = keyBind;
		}

		@Override
		public void onValueChanged(IConfigBoolean config) {
			if (config.getBooleanValue()) {
				KeyBinding.setKeyPressed(InputUtil.fromTranslationKey(this.keyBind.getBoundKeyTranslationKey()), true);
				KeyBinding.onKeyPressed(InputUtil.fromTranslationKey(this.keyBind.getBoundKeyTranslationKey()));
			} else {
				KeyBinding.setKeyPressed(InputUtil.fromTranslationKey(this.keyBind.getBoundKeyTranslationKey()), false);
			}
		}
	}
}
