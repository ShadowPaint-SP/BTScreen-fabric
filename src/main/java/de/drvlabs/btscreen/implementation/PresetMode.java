package de.drvlabs.btscreen.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import com.google.common.collect.ImmutableList;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.Settings.Setting;
import baritone.api.utils.SettingsUtil;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.btprocess.BedrockCleaner;
import de.drvlabs.btscreen.btprocess.SelectionOrchestrator;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.util.StringUtils;

public enum PresetMode implements StringRepresentable, IConfigOptionListEntry {
    NONE(true) {
        @Override
        public boolean setSettings() {
            return true;
        }

        @Override
        public String getCommand(GuiBase gui) {
            return "sel cleararea";
        }
    },
    CLEAR_AREA(false) {
        @Override
        public boolean setSettings() {
            SETTINGS_MANAGER.init().addCommonMiningSettings()
                    .add(SETTINGS.buildInLayers, true)
                    .add(SETTINGS.buildRepeatCount, 0)
                    .add(SETTINGS.layerHeight, 5)
                    .add(SETTINGS.layerOrder, true)
                    .apply();
            return true;
        }

        @Override
        public String getCommand(GuiBase gui) {
            return "sel cleararea";
        }
    },
    REMOVE_LIQUID(false) {
        @Override
        public boolean setSettings() {
            SETTINGS_MANAGER.init().addCommonMiningSettings()
                    .add(SETTINGS.allowInventory, true)
                    .apply();
            return true;
        }

        private final Item[] possibleBlocks = {
                Items.NETHERRACK,
                Items.RESIN_BLOCK,
                Items.MOSS_BLOCK,
                Items.DIRT,
                Items.STONE,
        };

        @Override
        public String getCommand(GuiBase gui) {
            Item bestItem = null;
            int maxCount = 0;

            for (Item item : possibleBlocks) {
                int count = Utils.MC.player.getInventory().countItem(item);
                if (count > maxCount) {
                    maxCount = count;
                    bestItem = item;
                }
            }
            if (bestItem == null) {
                sendMessage(gui, MessageType.ERROR, LangKeys.INFO + ".removeLiquid.noUsableItem");
            }
            Identifier id = BuiltInRegistries.ITEM.getKey(bestItem);
            StringBuilder builder = new StringBuilder();
            builder.append("sel replace");
            BuiltInRegistries.BLOCK.stream().filter(b -> b instanceof BucketPickup || b instanceof LiquidBlockContainer)
                    .forEach(b -> {
                        builder.append(" " + BuiltInRegistries.BLOCK.getKey(b));
                        if (b instanceof SimpleWaterloggedBlock) {
                            builder.append("[waterlogged=true]");
                        }
                    });
            builder.append(" " + id);
            SelectionOrchestrator.activate(-1, builder.toString());
            setSettings();
            return null;
        }
    },
    CLEAN_BEDROCK(false) {
        @Override
        public boolean setSettings() {
            return true;
        }

        @Override
        public String getCommand(GuiBase gui) {
            BedrockCleaner.activate();
            return null;
        }
    },
    FARMING(true) {
        @Override
        public boolean setSettings() {
            SETTINGS_MANAGER.init().addCommonSettings()
                    .add(SETTINGS.allowBreak, false)
                    .add(SETTINGS.allowPlace, false)
                    .apply();
            return true;
        }

        @Override
        public String getCommand(GuiBase gui) {
            return "farm";
        }
    },
    BUILDING(false) {
        @Override
        public boolean setSettings() {
            SETTINGS_MANAGER.init().addCommonSettings()
                    .add(SETTINGS.buildInLayers, true)
                    .add(SETTINGS.layerOrder, false)
                    .add(SETTINGS.layerHeight, 1)
                    .apply();
            return true;
        }

        @Override
        public String getCommand(GuiBase gui) {
            // TODO: Pass number of selected
            return "litematica";
        }
    };

    private static final Settings SETTINGS = BaritoneAPI.getSettings();
    public static final SettingsManager SETTINGS_MANAGER = new SettingsManager();

    private final String configString;
    public final boolean additionalControls;

    PresetMode(boolean additionalControls) {
        this.configString = this.name().toLowerCase();
        this.additionalControls = additionalControls;
    }

    @Override
    public String getSerializedName() {
        return this.configString;
    }

    @Override
    public String getStringValue() {
        return this.configString;
    }

    @Override
    public String getDisplayName() {
        return StringUtils.translate(BTScreen.MOD_ID + ".preset_mode.name." + this.configString);
    }

    @Override
    public PresetMode cycle(boolean forward) {
        int id = this.ordinal();
        if (forward) {
            id = (id + 1) % values().length;
        } else {
            id = (id - 1 + values().length) % values().length;
        }
        return values()[id];
    }

    @Override
    public PresetMode fromString(String value) {
        return fromStringStatic(value);
    }

    public static PresetMode fromStringStatic(String value) {
        for (PresetMode mode : values()) {
            if (mode.configString.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return NONE;
    }

    public abstract boolean setSettings();

    public abstract String getCommand(GuiBase gui);

    private static <T> Stream<T> getStream(Registry<T> registry, ConfigStringList config) {
        return config.getStrings().stream().map(string -> registry.getValue(Identifier.tryParse(string)))
                .filter(Objects::nonNull).distinct();
    }

    private static void sendMessage(GuiBase gui, MessageType type, String messageKey, Object... args) {
        if (gui != null) {
            gui.addMessage(type, 1000, messageKey, args);
        } else {
            BTScreen.chatMessage(Component.translatable(messageKey, args).withStyle(switch (type) {
                case ERROR -> ChatFormatting.RED;
                case INFO -> ChatFormatting.WHITE;
                case SUCCESS -> ChatFormatting.GREEN;
                case WARNING -> ChatFormatting.GOLD;
            }));
        }
    }

    public static final ImmutableList<String> BLOCKS_TO_IGNORE = ImmutableList.of(
            "torch",
            "wall_torch",
            "vine",
            "fern",
            "large_fern",
            "brown_mushroom",
            "red_mushroom",
            "short_grass",
            "short_dry_grass",
            "tall_grass",
            "tall_dry_grass",
            "small_dripleaf",
            "big_dripleaf",
            "leaf_litter",
            "flowering_azalea",
            "azalea",
            "bamboo_sapling",
            "bamboo",
            "pointed_dripstone",
            "budding_amethyst",
            "small_amethyst_bud",
            "medium_amethyst_bud",
            "large_amethyst_bud",
            "amethyst_cluster",
            "dragon_egg");

    public static final ImmutableList<String> ACCEPTABLE_THROWAWAY_ITEMS = ImmutableList.of("grass_block",
            "dirt",
            "cobblestone",
            "stone",
            "deepslate",
            "cobbled_deepslate",
            "netherrack",
            "soul_sand",
            "soul_soil",
            "basalt");

    public static final ImmutableList<String> BLOCKS_TO_DISALLOW_BREAKING = ImmutableList.of(
            // other
            "vault",
            "trial_spawner",
            "spawner",
            "budding_amethyst",
            // block entities
            "dispenser",
            "dropper",
            "furnace",
            "smoker",
            "blast_furnace",
            "hopper",
            "barrel",
            "chest",
            "trapped_chest",
            "shulker_box",
            "white_shulker_box",
            "orange_shulker_box",
            "magenta_shulker_box",
            "light_blue_shulker_box",
            "yellow_shulker_box",
            "lime_shulker_box",
            "pink_shulker_box",
            "gray_shulker_box",
            "light_gray_shulker_box",
            "cyan_shulker_box",
            "purple_shulker_box",
            "blue_shulker_box",
            "brown_shulker_box",
            "green_shulker_box",
            "red_shulker_box",
            "black_shulker_box",
            // Beacon+Base
            "beacon",
            "iron_block",
            "gold_block",
            "emerald_block",
            "diamond_block",
            "netherite_block",
            // unbreakable
            "bedrock",
            "end_portal",
            "end_portal_frame",
            "end_gateway",
            "reinforced_deepslate",
            "command_block",
            "repeating_command_block",
            "chain_command_block",
            "structure_block",
            "structure_void",
            "jigsaw",
            "barrier");

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static class SettingsManager {
        private SettingsManager() {
        }

        private boolean applied = false;
        private final Map<Setting, Object> originalSettings = new HashMap<>();
        private final Map<Setting, Object> modifiedSettings = new HashMap<>();

        public SettingsManager init() {
            originalSettings.clear();
            modifiedSettings.clear();
            SettingsUtil.modifiedSettings(SETTINGS).forEach(setting -> {
                originalSettings.put(setting, setting.value);
            });
            return this;
        }

        public <T> SettingsManager add(Setting<T> setting, T value) {
            modifiedSettings.put((Setting) setting, value);
            return this;
        }

        public void apply() {
            modifiedSettings.forEach((setting, value) -> {
                setting.value = value;
            });
            Waiter.wait(2, w -> {
                if (!Utils.isActive()) {
                    reset();
                }
            });
            applied = true;
        }

        public void reset() {
            if (!applied)
                return;
            modifiedSettings.forEach((setting, value) -> {
                if (originalSettings.containsKey(setting)) {
                    setting.value = originalSettings.get(setting);
                } else {
                    setting.reset();
                }
            });
            modifiedSettings.clear();
            applied = false;
        }

        public boolean isApplied() {
            return applied;
        }

        public List<String> getModifiedSettings() {
            return modifiedSettings.keySet().stream().map(s -> s.getName().toLowerCase()).toList();
        }

        public SettingsManager addCommonSettings() {
            add(SETTINGS.blockBreakSpeed, 0);
            add(SETTINGS.itemSaver, true);
            add(SETTINGS.itemSaverThreshold, 10);
            add(SETTINGS.randomLooking, (double) 0);
            add(SETTINGS.randomLooking113, (double) 0);
            add(SETTINGS.acceptableThrowawayItems,
                    getStream(BuiltInRegistries.ITEM, Configs.Lists.ACCEPTABLE_THROWAWAY_ITEMS).toList());
            return this;
        }

        public SettingsManager addCommonMiningSettings() {
            addCommonSettings();
            add(SETTINGS.avoidUpdatingFallingBlocks, false);
            add(SETTINGS.blocksToDisallowBreaking,
                    getStream(BuiltInRegistries.BLOCK, Configs.Lists.BLOCKS_TO_DISALLOW_BREAKING).toList());
            add(SETTINGS.buildIgnoreBlocks, Stream.concat(SETTINGS.blocksToDisallowBreaking.value.stream(),
                    getStream(BuiltInRegistries.BLOCK, Configs.Lists.BLOCKS_TO_IGNORE)).distinct().toList());
            if (Configs.Generic.AUTO_TORCH.getBooleanValue()) {
                add(SETTINGS.buildIgnoreBlocks, Stream.concat(SETTINGS.buildIgnoreBlocks.value.stream(),
                        Stream.of(Blocks.TORCH, Blocks.WALL_TORCH)).distinct().toList());
            }
            return this;
        }
    }
}
