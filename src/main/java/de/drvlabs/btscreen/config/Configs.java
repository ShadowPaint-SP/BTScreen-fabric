package de.drvlabs.btscreen.config;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.btprocess.AutoEat;
import de.drvlabs.btscreen.utils.preset.PresetMode;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;

public class Configs implements IConfigHandler {
    private static final String CONFIG_FILE_NAME = BTScreen.MOD_ID + ".json";

    public static class Generic {
        public static final ConfigBoolean DEBUG_LOGGING = new ConfigBoolean("debugLogging", false);
        public static final ConfigBoolean SHOW_PROCESS_CHANGES = new ConfigBoolean("showProcessChanges", true);
        public static final ConfigBoolean AUTO_SLEEP = new ConfigBoolean("autoSleep", false);
        public static final ConfigBoolean AUTO_REPAIR = new ConfigBoolean("autoRepair", false);
        public static final ConfigBoolean AUTO_EAT = new ConfigBoolean("autoEat", false);
        public static final ConfigBoolean AUTO_HASTE = new ConfigBoolean("autoHaste", false);
        public static final ConfigBoolean AUTO_DROP = new ConfigBoolean("autoDrop", false);
        public static final ConfigBoolean AUTO_TORCH = new ConfigBoolean("autoTorch", false);
        public static final ConfigString HOME_COMMAND = new ConfigString("homeCommand", "home");
        public static final ConfigString SETHOME_COMMAND = new ConfigString("setHomeCommand", "sethome");
        public static final ConfigString SLEEP_HOME = new ConfigString("sleepHome", "sleep");
        public static final ConfigString DROP_HOME = new ConfigString("dropHome", "drop");
        public static final ConfigString HASTE_HOME = new ConfigString("hasteHome", "haste");
        public static final ConfigString REPAIR_HOME = new ConfigString("repairHome", "repair");
        public static final ConfigString SAFETY_HOME = new ConfigString("safetyHome", "");
        public static final ConfigString MINE_HOME = new ConfigString("mineHome", "mine");
        public static final ConfigString FINISHED_HOME = new ConfigString("finishedHome", "");
        public static final ConfigTicks PERIODIC_ATTACK_INTERVAL = new ConfigTicks("periodicAttackInterval", 25, 1,
                400);
        public static final ConfigInteger ITEM_DURABILITY_THRESHOLD = new ConfigInteger("itemDurabilityThreshold", 40,
                20, 100);
        public static final ConfigInteger FOOD_LEVEL = new ConfigInteger("foodLevel", AutoEat.MIN_FOOD_LEVEL,
                AutoEat.MIN_FOOD_LEVEL, 20);
        public static final ConfigInteger MIN_LIGHT_LEVEL = new ConfigInteger("minLightLevel", 1, 1, 15);
        public static final ConfigBoolean NO_BREAK_COOLDOWN = new ConfigBoolean("noBreakCooldown", true);
        public static final ConfigBoolean REPEAT_ACTION = new ConfigBoolean("repeatAction", false);
        public static final ConfigTicks REPEAT_ACTION_INTERVAL = new ConfigTicks("repeatActionInterval", 100, 0,
                1000000);
        public static final ConfigBoolean SAFETY_LOCATION = new ConfigBoolean("safetyLocation", true);
        public static final ConfigInteger SAFETY_MIN_HEALTH = new ConfigInteger("safetyMinHealth", 5, 0, 20);
        public static final ConfigTicks MAX_SLEEP_TICKS = new ConfigTicks("maxSleepTicks", 50, 0, 200);

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                AUTO_SLEEP,
                AUTO_REPAIR,
                AUTO_EAT,
                AUTO_HASTE,
                AUTO_DROP,
                AUTO_TORCH,
                PERIODIC_ATTACK_INTERVAL,
                ITEM_DURABILITY_THRESHOLD,
                HOME_COMMAND,
                SETHOME_COMMAND,
                MINE_HOME,
                SLEEP_HOME,
                DROP_HOME,
                HASTE_HOME,
                REPAIR_HOME,
                SAFETY_HOME,
                FINISHED_HOME,
                FOOD_LEVEL,
                MIN_LIGHT_LEVEL,
                NO_BREAK_COOLDOWN,
                REPEAT_ACTION,
                REPEAT_ACTION_INTERVAL,
                SAFETY_LOCATION,
                SAFETY_MIN_HEALTH,
                MAX_SLEEP_TICKS,
                SHOW_PROCESS_CHANGES,
                DEBUG_LOGGING);

        static {
            OPTIONS.forEach(o -> {
                if (o instanceof ConfigBase<?> cb) {
                    cb.apply(LangKeys.CONFIG_GENERIC);
                }
            });
        }
    }

    public static class Lists {
        public static final ConfigStringList PROCESS_CHANGES_BLACKLIST = new ConfigStringList("processChangesBlacklist", ImmutableList.of("Teleport"));
        public static final ConfigStringList INV_PRESERVE_ITEM_BLACKLIST = new ConfigStringList(
                "invPreserveItemBlackList", ImmutableList.of());
        public static final ConfigStringList BLOCKS_TO_GET_REPLACED = new ConfigStringList("blocksToGetReplaced",
                ImmutableList.of("small_amethyst_bud", " medium_amethyst_bud", "large_amethyst_bud",
                        "amethyst_cluster"));
        public static final ConfigString BLOCK_TO_REPLACE_WITH = new ConfigString("blockToReplaceWith", "air");
        public static final ConfigStringList BLOCKS_TO_DISALLOW_BREAKING = new ConfigStringList(
                "blocksToDisallowBreaking", PresetMode.BLOCKS_TO_DISALLOW_BREAKING);
        public static final ConfigStringList BLOCKS_TO_IGNORE = new ConfigStringList("blocksToIgnore",
                PresetMode.BLOCKS_TO_IGNORE);
        public static final ConfigStringList ACCEPTABLE_THROWAWAY_ITEMS = new ConfigStringList(
                "acceptableThrowawayItems", PresetMode.ACCEPTABLE_THROWAWAY_ITEMS);

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                PROCESS_CHANGES_BLACKLIST,
                INV_PRESERVE_ITEM_BLACKLIST,
                BLOCKS_TO_GET_REPLACED,
                BLOCK_TO_REPLACE_WITH,
                BLOCKS_TO_DISALLOW_BREAKING,
                BLOCKS_TO_IGNORE,
                ACCEPTABLE_THROWAWAY_ITEMS);

        static {
            OPTIONS.forEach(o -> {
                if (o instanceof ConfigBase<?> cb) {
                    cb.apply(LangKeys.CONFIG_LISTS);
                }
            });
        }
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

                BTScreen.debugLog("loadFromFile(): Successfully loaded config file '{}'.",
                        configFile.toAbsolutePath());
            } else {
                BTScreen.LOGGER.error("loadFromFile(): Failed to load config file '{}'.",
                        configFile.toAbsolutePath());
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
            BTScreen.LOGGER.error("saveToFile(): Config Folder '{}' does not exist!",
                    dir.toAbsolutePath());
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
