package drvlabs.de.data;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import drvlabs.de.BTScreen;
import drvlabs.de.Reference;
import drvlabs.de.gui.GuiConfigs.ConfigGuiTab;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.customcommands.CommandsManager;
import drvlabs.de.utils.preset.PresetMode;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;

public class DataManager {

	private static final DataManager INSTANCE = new DataManager();

	private static CommandsManager commandsManager = new CommandsManager();
	private static ConfigGuiTab configGuiTab = ConfigGuiTab.GENERIC;
	private static boolean canSave;
	private static PresetMode operationMode = PresetMode.DEFAULT;
	private static BotStatus botStatus = BotStatus.IDLE;
	private static boolean isActive = false;
	private static boolean needsToEat = false;

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

	public static PresetMode getPresetMode() {
		return operationMode;
	}

	public static void setPresetMode(PresetMode mode) {
		operationMode = mode;
	}

	public static BotStatus getBotStatus() {
		return botStatus;
	}

	public static void setBotStatus(BotStatus status) {
		BTScreen.debugLog("New Bot Status: " + status);
		botStatus = status;
	}

	public static boolean getActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public static boolean getNeedsToEat() {
		return needsToEat;
	}

	public static void setNeedsToEat(boolean bool) {
		BTScreen.debugLog("Eating: " + bool);
		needsToEat = bool;
	}

	public static CommandsManager getCommandsManager() {
		return commandsManager;
	}

	public static Path getCurrentConfigDirectory() {
		return FileUtils.getConfigDirectoryAsPath().resolve(Reference.MOD_ID);
	}

	private static Path getCurrentStorageFile(boolean globalData) {
		Path dir = getCurrentConfigDirectory();

		if (!Files.exists(dir)) {
			FileUtils.createDirectoriesIfMissing(dir);
		}

		if (!Files.isDirectory(dir)) {
			BTScreen.LOGGER.warn("Failed to create the config directory '{}'", dir.toAbsolutePath());
		}

		return dir.resolve(StringUtils.getStorageFileName(globalData, Reference.MOD_ID + "_", ".json", "default"));
	}

	public static void load() {
		getInstance().loadPerDimensionData();
		Path file = getCurrentStorageFile(true);
		JsonElement element = JsonUtils.parseJsonFileAsPath(file);

		if (element != null && element.isJsonObject()) {

			JsonObject root = element.getAsJsonObject();

			if (JsonUtils.hasString(root, "config_gui_tab")) {
				try {
					configGuiTab = ConfigGuiTab.valueOf(root.get("config_gui_tab").getAsString());
				} catch (Exception ignored) {
					BTScreen.LOGGER.error("Failed to load config gui tab");
				}

				if (configGuiTab == null) {
					configGuiTab = ConfigGuiTab.GENERIC;
				}
			}
			if (JsonUtils.hasObject(root, "commands")) {
				commandsManager.loadFromJson(root.get("commands").getAsJsonObject());
			}
		}

		canSave = true;
	}

	public static void save() {
		save(false);
	}

	public static void save(boolean forceSave) {
		if (canSave == false && forceSave == false) {
			return;
		}
		BTScreen.debugLog("Saving data");
		getInstance().savePerDimensionData();

		JsonObject root = new JsonObject();

		root.add("commands", commandsManager.toJson());
		root.add("config_gui_tab", new JsonPrimitive(configGuiTab.name()));

		Path file = getCurrentStorageFile(true);
		JsonUtils.writeJsonToFileAsPath(root, file);

		canSave = false;
	}

	public static void clear() {
		BTScreen.debugLog("Clearing data");
		botStatus = BotStatus.IDLE;
		getInstance().setActive(false);
		needsToEat = false;
	}

	// dimension specific storage

	private void savePerDimensionData() {
		JsonObject root = this.toJson();
		Path file = getCurrentStorageFile(false);
		JsonUtils.writeJsonToFileAsPath(root, file);
	}

	private void loadPerDimensionData() {
		Path file = getCurrentStorageFile(false);
		JsonElement element = JsonUtils.parseJsonFileAsPath(file);

		if (element != null && element.isJsonObject()) {
			JsonObject root = element.getAsJsonObject();
			this.fromJson(root);
		}
	}

	private void fromJson(JsonObject obj) {
		if (JsonUtils.hasString(obj, "operation_mode")) {
			try {
				operationMode = PresetMode.valueOf(obj.get("operation_mode").getAsString());
			} catch (Exception ignored) {
			}

			if (operationMode == null) {
				operationMode = PresetMode.DEFAULT;
			}
			operationMode.setSettings();
		}
	}

	private JsonObject toJson() {
		JsonObject obj = new JsonObject();

		obj.add("operation_mode", new JsonPrimitive(operationMode.name()));

		return obj;
	}
}
