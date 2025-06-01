package drvlabs.de.gui;

import drvlabs.de.Reference;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
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
		int width = this.getButtonWidth();

		x += this.createButton(x, y, width, ButtonType.CONFIGURATION);
	}

	private int createButton(int x, int y, int width, ButtonType tab) {
		ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.getDisplayName());
		return button.getWidth() + 2;
	}

	private int getButtonWidth() {
		int width = 0;

		for (ButtonType type : ButtonType.values()) {
			width = Math.max(width, this.getStringWidth(type.getDisplayName()) + 30);
		}

		return width;
	}

	public enum ButtonType {
		// In-game Configuration GUI
		CONFIGURATION("btscreen.gui.button.change_menu.configuration_menu", ButtonIcons.CONFIGURATION),
		// Switch to the btscreen main menu
		MAIN_MENU("btscreen.gui.button.change_menu.to_main_menu", null);

		private final String labelKey;
		private final ButtonIcons icon;

		ButtonType(String labelKey, ButtonIcons icon) {
			this.labelKey = labelKey;
			this.icon = icon;
		}

		public String getLabelKey() {
			return this.labelKey;
		}

		public String getDisplayName() {
			return StringUtils.translate(this.getLabelKey());
		}

		public ButtonIcons getIcon() {
			return this.icon;
		}
	}
}
