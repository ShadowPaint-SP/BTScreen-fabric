package drvlabs.de.config;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import drvlabs.de.BTScreen;
import drvlabs.de.Reference;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;

public class Configs implements IConfigHandler {
	private static final String CONFIG_FILE_NAME = Reference.MOD_ID + ".json";
	private static final String GENERIC_KEY = Reference.MOD_ID + ".config.generic";

	public static class Generic {
		public static final ConfigBoolean DEBUG_LOGGING = new ConfigBoolean("debugLogging", true).apply(GENERIC_KEY);

		public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
				DEBUG_LOGGING);
	}

	public static void loadFromFile() {
		Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE_NAME);

		if (Files.exists(configFile) && Files.isReadable(configFile)) {
			JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);

			if (element != null && element.isJsonObject()) {
				JsonObject root = element.getAsJsonObject();

				ConfigUtils.readConfigBase(root, "Generic", Generic.OPTIONS);
				// ConfigUtils.readConfigBase(root, "Hotkeys", Hotkeys.HOTKEY_LIST);

				// BTScreen.debugLog("loadFromFile(): Successfully loaded config file '{}'.",
				// configFile.toAbsolutePath());
			} else {
				BTScreen.LOGGER.error("loadFromFile(): Failed to load config file '{}'.", configFile.toAbsolutePath());
			}
		}
	}

	public static void saveToFile() {
		Path dir = FileUtils.getConfigDirectoryAsPath();

		if (!Files.exists(dir)) {
			FileUtils.createDirectoriesIfMissing(dir);
			// BTScreen.debugLog("saveToFile(): Creating directory '{}'.",
			// dir.toAbsolutePath());
		}

		if (Files.isDirectory(dir)) {
			JsonObject root = new JsonObject();

			ConfigUtils.writeConfigBase(root, "Generic", Generic.OPTIONS);
			// ConfigUtils.writeConfigBase(root, "Hotkeys", Hotkeys.HOTKEY_LIST);

			JsonUtils.writeJsonToFileAsPath(root, dir.resolve(CONFIG_FILE_NAME));
		} else {
			BTScreen.LOGGER.error("saveToFile(): Config Folder '{}' does not exist!", dir.toAbsolutePath());
		}
	}

	@Override
	public void load() {
		loadFromFile();
	}

	@Override
	public void save() {
		saveToFile();
	}

}
