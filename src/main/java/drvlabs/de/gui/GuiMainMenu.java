package drvlabs.de.gui;

import drvlabs.de.Reference;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;

public class GuiMainMenu extends GuiBase {
	public GuiMainMenu() {
		String version = String.format("v%s", Reference.MOD_VERSION);
		this.title = StringUtils.translate("btscreen.gui.title.btscreen_main_menu", version);
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = 12;
		int y = 30;

		x += this.createButton(x, y, -1, ButtonListener.Type.CONFIGURATION);
	}

	private int createButton(int x, int y, int width, ButtonListener.Type type) {
		ButtonListener listener = new ButtonListener(type, this);
		String label = StringUtils.translate(type.getTranslationKey());

		if (width == -1) {
			width = this.getStringWidth(label) + 10;
		}

		ButtonGeneric button = new ButtonGeneric(x, y, width, 20, label, type.getIcon());

		if (type == ButtonListener.Type.CONFIGURATION) {
			button.setHoverStrings(StringUtils.translate("btscreen.gui.button.hover.config_info_text"));
		}

		this.addButton(button, listener);

		return width;
	}

	private static class ButtonListener implements IButtonActionListener {
		private final Type type;
		private final GuiMainMenu gui;

		public ButtonListener(Type type, GuiMainMenu gui) {
			this.type = type;
			this.gui = gui;
		}

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			if (this.type == Type.CONFIGURATION) {
				GuiBase.openGui(new GuiConfigs());
				this.gui.addMessage(MessageType.SUCCESS, "btscreen.info.main_menu.CONFIGURATION");
			}

		}

		public enum Type {
			CONFIGURATION("btscreen.gui.button.configuration_menu", ButtonIcons.CONFIGURATION),
			BUTTON1("btscreen.gui.button.material_list", null);

			private final String translationKey;
			private final ButtonIcons icon;

			Type(String translationKey, ButtonIcons icon) {
				this.translationKey = translationKey;
				this.icon = icon;
			}

			public String getTranslationKey() {
				return this.translationKey;
			}

			public ButtonIcons getIcon() {
				return this.icon;
			}
		}
	}
}
