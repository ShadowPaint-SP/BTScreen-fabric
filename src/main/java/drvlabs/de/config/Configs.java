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
	private static final String LISTS_KEY = Reference.MOD_ID + ".config.lists";

	public static class Generic {
		public static final ConfigBoolean DEBUG_LOGGING = new ConfigBoolean("debugLogging", true).apply(GENERIC_KEY);
		public static final ConfigBoolean RSD_SETTINGS = new ConfigBoolean("rsdSettings", false).apply(GENERIC_KEY);
		public static final ConfigBoolean AUTO_SLEEP = new ConfigBoolean("autoSleep", false).apply(GENERIC_KEY);
		public static final ConfigBoolean AUTO_REPAIR = new ConfigBoolean("autoRepair", false).apply(GENERIC_KEY);
		public static final ConfigBoolean AUTO_EAT = new ConfigBoolean("autoEat", false).apply(GENERIC_KEY);
		public static final ConfigBoolean AUTO_HASTE = new ConfigBoolean("autoHaste", false).apply(GENERIC_KEY);
		public static final ConfigBoolean AUTO_DROP = new ConfigBoolean("autoDrop", false).apply(GENERIC_KEY);
		public static final ConfigString HOME_COMMAND = new ConfigString("homeCommand", "home").apply(GENERIC_KEY);
		public static final ConfigString SETHOME_COMMAND = new ConfigString("setHomeCommand", "sethome").apply(GENERIC_KEY);
		public static final ConfigString SLEEP_HOME = new ConfigString("sleepHome", "sleep").apply(GENERIC_KEY);
		public static final ConfigString DROP_HOME = new ConfigString("dropHome", "drop").apply(GENERIC_KEY);
		public static final ConfigString HASTE_HOME = new ConfigString("hasteHome", "haste").apply(GENERIC_KEY);
		public static final ConfigString REPAIR_HOME = new ConfigString("repairHome", "repair").apply(GENERIC_KEY);
		public static final ConfigString MINE_HOME = new ConfigString("mineHome", "mine").apply(GENERIC_KEY);
		public static final ConfigInteger PERIODIC_ATTACK_INTERVAL = new ConfigInteger("periodicAttackInterval", 200, 0,
				Integer.MAX_VALUE).apply(GENERIC_KEY);
		public static final ConfigInteger ITEM_DURABILITY_THRESHOLD = new ConfigInteger("itemDurabilityThreshold", 40, 20,
				Integer.MAX_VALUE).apply(GENERIC_KEY);

		public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
				AUTO_SLEEP,
				AUTO_REPAIR,
				AUTO_EAT,
				AUTO_HASTE,
				AUTO_DROP,
				PERIODIC_ATTACK_INTERVAL,
				ITEM_DURABILITY_THRESHOLD,
				HOME_COMMAND,
				SETHOME_COMMAND,
				MINE_HOME,
				SLEEP_HOME,
				DROP_HOME,
				HASTE_HOME,
				REPAIR_HOME,
				RSD_SETTINGS,
				DEBUG_LOGGING);
	}

	public static class Lists {
		public static final ConfigStringList INV_PRESERVE_ITEM_BLACKLIST = new ConfigStringList(
				"invPreserveItemBlackList", ImmutableList.of("minecraft:resin_clump")).apply(LISTS_KEY);

		public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
				INV_PRESERVE_ITEM_BLACKLIST);
	}

	public static void loadFromFile() {
		Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE_NAME);

		if (Files.exists(configFile) && Files.isReadable(configFile)) {
			JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);

			if (element != null && element.isJsonObject()) {
				JsonObject root = element.getAsJsonObject();

				ConfigUtils.readConfigBase(root, "Generic", Generic.OPTIONS);
				ConfigUtils.readConfigBase(root, "Lists", Lists.OPTIONS);
				ConfigUtils.readConfigBase(root, "Hotkeys", Hotkeys.HOTKEY_LIST);

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
		}

		if (Files.isDirectory(dir)) {
			JsonObject root = new JsonObject();

			ConfigUtils.writeConfigBase(root, "Generic", Generic.OPTIONS);
			ConfigUtils.writeConfigBase(root, "Lists", Lists.OPTIONS);
			ConfigUtils.writeConfigBase(root, "Hotkeys", Hotkeys.HOTKEY_LIST);

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
