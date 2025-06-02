package drvlabs.de.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import drvlabs.de.BTScreen;
import drvlabs.de.Reference;
import drvlabs.de.gui.GuiConfigs.ConfigGuiTab;
import fi.dy.masa.malilib.util.*;

public class DataManager {

	private static final DataManager INSTANCE = new DataManager();

	private static final Map<String, Path> LAST_DIRECTORIES = new HashMap<>();
	private static ConfigGuiTab configGuiTab = ConfigGuiTab.GENERIC;
	private static boolean canSave;
	private static long clientTickStart;

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

	public static void load() {
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

	public static void onClientTickStart() {
		clientTickStart = System.nanoTime();
	}

	public static long getClientTickStartTime() {
		return clientTickStart;
	}
}
