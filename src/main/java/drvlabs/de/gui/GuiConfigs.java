package drvlabs.de.gui;

import java.util.List;
import java.util.Objects;

import drvlabs.de.Reference;
import drvlabs.de.config.Configs;
import drvlabs.de.config.Hotkeys;
import drvlabs.de.data.DataManager;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;

public class GuiConfigs extends GuiConfigsBase {

	public GuiConfigs() {
		super(10, 50, Reference.MOD_ID, null, "btscreen.gui.title.configs", String.format("%s", Reference.MOD_VERSION));
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
		button.setEnabled(DataManager.getConfigGuiTab() != tab);
		this.addButton(button, new ButtonListener(tab, this));
		return button.getWidth() + 2;
	}

	@Override
	public List<ConfigOptionWrapper> getConfigs() {
		List<? extends IConfigBase> configs;
		ConfigGuiTab tab = DataManager.getConfigGuiTab();
		configs = switch (tab) {
			case GENERIC -> Configs.Generic.OPTIONS;
			case LISTS -> Configs.Lists.OPTIONS;
			case HOTKEYS -> Hotkeys.HOTKEY_LIST;
		};
		return ConfigOptionWrapper.createFor(configs);
	}

	private record ButtonListener(ConfigGuiTab tab, GuiConfigs parent) implements IButtonActionListener {
		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			DataManager.setConfigGuiTab(tab);
			this.parent.reCreateListWidget();
			Objects.requireNonNull(this.parent.getListWidget()).resetScrollbarPosition();
			this.parent.initGui();
		}
	}

	public enum ConfigGuiTab {
		GENERIC("btscreen.gui.button.config_gui.generic"),
		LISTS("btscreen.gui.button.config_gui.lists"),
		HOTKEYS("btscreen.gui.button.config_gui.hotkeys");

		private final String translationKey;

		private ConfigGuiTab(String translationKey) {
			this.translationKey = translationKey;
		}

		public String getDisplayName() {
			return StringUtils.translate(this.translationKey);
		}
	}
}
