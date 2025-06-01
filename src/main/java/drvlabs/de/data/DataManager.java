package drvlabs.de.data;

import drvlabs.de.gui.GuiConfigs.ConfigGuiTab;

public class DataManager {

	private static final DataManager INSTANCE = new DataManager();
	private static ConfigGuiTab configGuiTab = ConfigGuiTab.GENERIC;

	private DataManager() {
	}

	public static DataManager getInstance() {
		return INSTANCE;
	}

	public static ConfigGuiTab getConfigGuiTab() {
		return configGuiTab;
	}

	public static void setConfigGuiTab(ConfigGuiTab tab) {
		configGuiTab = tab;
	}
}
