package drvlabs.de.event;

import drvlabs.de.config.Hotkeys;
import drvlabs.de.gui.GuiConfigs;
import drvlabs.de.gui.GuiMainMenu;
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
			}

			// else if (key == Hotkeys.LAYER_NEXT.getKeybind())
			// {
			// DataManager.getRenderLayerRange().moveLayer(1);
			// return true;
			// }
			// else if (key == Hotkeys.LAYER_PREVIOUS.getKeybind())
			// {
			// DataManager.getRenderLayerRange().moveLayer(-1);
			// return true;
			// }
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
