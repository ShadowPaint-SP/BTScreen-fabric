package de.drvlabs.btscreen.gui;

import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.screens.Screen;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.config.Hotkeys;
import de.drvlabs.btscreen.config.LangKeys;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;

public class GuiConfigs extends GuiConfigsBase {
	private ConfigGuiTab tab = ConfigGuiTab.GENERIC;

	public GuiConfigs() {
		this(null);
	}

	public GuiConfigs(Screen parent) {
		super(10, 50, BTScreen.MOD_ID, parent, LangKeys.GUI_TITLE + ".configs",
				BTScreen.MOD_NAME, BTScreen.MOD_VERSION);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.clearOptions();

		int x = 10;
		int y = 26;

		x += this.createButton(x, y, -1, ConfigGuiTab.GENERIC);
		x += this.createButton(x, y, -1, ConfigGuiTab.LISTS);
		x += this.createButton(x, y, -1, ConfigGuiTab.HOTKEYS);
	}

	private int createButton(int x, int y, int width, ConfigGuiTab tab) {
		ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.getDisplayName());
		button.setEnabled(this.tab != tab);
		this.addButton(button, (t, mouseButton) -> {
			this.tab = tab;
			this.reCreateListWidget();
			Objects.requireNonNull(this.getListWidget()).resetScrollbarPosition();
			this.initGui();
		});
		return button.getWidth() + 2;
	}

	@Override
	public List<ConfigOptionWrapper> getConfigs() {
		List<? extends IConfigBase> configs = switch (this.tab) {
			case GENERIC -> Configs.Generic.OPTIONS;
			case LISTS -> Configs.Lists.OPTIONS;
			case HOTKEYS -> Hotkeys.HOTKEY_LIST;
		};
		return ConfigOptionWrapper.createFor(configs);
	}

	public enum ConfigGuiTab {
		GENERIC, LISTS, HOTKEYS;

		private final String configString;

		ConfigGuiTab() {
			this.configString = this.name().toLowerCase();
		}

		public String getDisplayName() {
			return StringUtils.translate(LangKeys.GUI_BUTTON + ".config_gui." + this.configString);
		}
	}
}
