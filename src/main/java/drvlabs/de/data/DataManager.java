package drvlabs.de.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import drvlabs.de.BTScreen;
import drvlabs.de.Reference;
import drvlabs.de.gui.GuiConfigs.ConfigGuiTab;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.preset.PresetMode;
import fi.dy.masa.malilib.gui.interfaces.IDirectoryCache;
import fi.dy.masa.malilib.util.*;

public class DataManager implements IDirectoryCache {

	private static final DataManager INSTANCE = new DataManager();

	private static final Map<String, Path> LAST_DIRECTORIES = new HashMap<>();
	private static ConfigGuiTab configGuiTab = ConfigGuiTab.GENERIC;
	private static boolean canSave;
	private static long clientTickStart;
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
		BTScreen.LOGGER.info("Bot Status: " + status);
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
		needsToEat = bool;
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
			BTScreen.LOGGER.warn("Failed to create the config directory '{}'",
					dir.toAbsolutePath());
		}

		return dir.resolve(StringUtils.getStorageFileName(globalData,
				Reference.MOD_ID + "_", ".json", "default"));
	}

	@Override
	@Nullable
	public Path getCurrentDirectoryForContext(String context) {
		return LAST_DIRECTORIES.get(context);
	}

	@Override
	public void setCurrentDirectoryForContext(String context, Path dir) {
		LAST_DIRECTORIES.put(context, dir);
	}

	public static void load() {
		getInstance().loadPerDimensionData();
		Path file = getCurrentStorageFile(true);
		JsonElement element = JsonUtils.parseJsonFileAsPath(file);

		if (element != null && element.isJsonObject()) {
			LAST_DIRECTORIES.clear();

			JsonObject root = element.getAsJsonObject();

			if (JsonUtils.hasObject(root, "last_directories")) {
				JsonObject obj = root.get("last_directories").getAsJsonObject();

				for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
					String name = entry.getKey();
					JsonElement el = entry.getValue();

					if (el.isJsonPrimitive()) {
						Path dir = Path.of(el.getAsString());

						if (Files.exists(dir) && Files.isDirectory(dir)) {
							LAST_DIRECTORIES.put(name, dir);
						}
					}
				}
			}

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
		getInstance().savePerDimensionData();

		JsonObject root = new JsonObject();
		JsonObject objDirs = new JsonObject();

		for (Map.Entry<String, Path> entry : LAST_DIRECTORIES.entrySet()) {
			objDirs.add(entry.getKey(), new JsonPrimitive(entry.getValue().toAbsolutePath().toString()));
		}

		root.add("last_directories", objDirs);

		root.add("config_gui_tab", new JsonPrimitive(configGuiTab.name()));

		Path file = getCurrentStorageFile(true);
		JsonUtils.writeJsonToFileAsPath(root, file);

		canSave = false;
	}

	public static void clear() {
		botStatus = BotStatus.IDLE;
		getInstance().setActive(false);
		needsToEat = false;
	}

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

	public static void onClientTickStart() {
		clientTickStart = System.nanoTime();
	}

	public static long getClientTickStartTime() {
		return clientTickStart;
	}

}
