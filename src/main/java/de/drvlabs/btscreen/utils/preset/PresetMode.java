package de.drvlabs.btscreen.utils.preset;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.utils.SettingsUtil;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public enum PresetMode implements StringIdentifiable, IConfigOptionListEntry {
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
            resetSettings();
            SETTINGS.allowBreak.value = true;
            SETTINGS.allowPlace.value = true;
            SETTINGS.buildInLayers.value = true;
            SETTINGS.buildRepeatCount.value = 0;
            SETTINGS.blockBreakSpeed.value = 0;
            SETTINGS.layerHeight.value = 5;
            SETTINGS.layerOrder.value = true;
            SETTINGS.itemSaver.value = true;
            SETTINGS.itemSaverThreshold.value = 10;
            SETTINGS.randomLooking.value = (double) 0;
            SETTINGS.randomLooking113.value = (double) 0;
            List<Block> blocksToDisallowBreaking = Configs.Lists.CLEAR_AREA_BLOCKS_TO_DISALLOW_BREAKING
                    .getStrings().stream().map(string -> Registries.BLOCK.get(Identifier.tryParse(string)))
                    .filter(Objects::nonNull).toList();
            List<Block> blocksToIgnore = Stream.concat(blocksToDisallowBreaking.stream(),
                    Configs.Lists.CLEAR_AREA_BLOCKS_TO_IGNORE.getStrings().stream()
                            .map(string -> Registries.BLOCK.get(Identifier.tryParse(string))).filter(Objects::nonNull)
                            .toList().stream())
                    .toList();
            SETTINGS.blocksToDisallowBreaking.value = blocksToDisallowBreaking; // Blocks that cant be mined
            // Blocks that should be ignored in the selection
            if (Configs.Generic.AUTO_TORCH.getBooleanValue()) {
                SETTINGS.buildIgnoreBlocks.value = Stream.concat(blocksToIgnore.stream(),
                        Stream.of(Blocks.TORCH, Blocks.WALL_TORCH)).toList();
            } else {
                SETTINGS.buildIgnoreBlocks.value = blocksToIgnore;
            }
            setAcceptableThrowawayItems();
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
            resetSettings();
            // SETTINGS.buildInLayers.value = true;
            SETTINGS.blockBreakSpeed.value = 0;
            // SETTINGS.layerHeight.value = 1;
            // SETTINGS.layerOrder.value = true;
            SETTINGS.itemSaver.value = true;
            SETTINGS.itemSaverThreshold.value = 10;
            SETTINGS.randomLooking.value = (double) 0;
            SETTINGS.randomLooking113.value = (double) 0;
            // SETTINGS.buildRepeatSneaky.value = false;
            // SETTINGS.buildRepeat.value = Vec3i.ZERO.down();
            // SETTINGS.buildRepeatCount.value = y - Utils.MC.world.getBottomY() - 3; // Ignore 4 layers of bedrock
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
                int count = Utils.MC.player.getInventory().count(item);
                if (count > maxCount) {
                    maxCount = count;
                    bestItem = item;
                }
            }

            if (bestItem != null) {
                Identifier id = Registries.ITEM.getId(bestItem);
                return "sel replace lava water " + id;
            } else {
                gui.addMessage(MessageType.ERROR, 1000, LangKeys.INFO + ".removeLiquid.noUsableItem");
                return null;
            }
        }
    },
    FARMING(true) {
        @Override
        public boolean setSettings() {
            resetSettings();
            SETTINGS.allowBreak.value = false;
            SETTINGS.allowPlace.value = false;
            SETTINGS.buildInLayers.value = false;
            SETTINGS.buildRepeatCount.value = 0;
            SETTINGS.randomLooking.value = (double) 0;
            SETTINGS.randomLooking113.value = (double) 0;
            List<Block> blocksToDisallowBreaking = Configs.Lists.FARM_BLOCKS_TO_DISALLOW_BREAKING
                    .getStrings().stream().map(string -> Registries.BLOCK.get(Identifier.tryParse(string)))
                    .filter(Objects::nonNull).toList();
            List<Block> blocksToIgnore = Stream.concat(blocksToDisallowBreaking.stream(),
                    Configs.Lists.FARM_BLOCKS_TO_IGNORE.getStrings().stream()
                            .map(string -> Registries.BLOCK.get(Identifier.tryParse(string))).filter(Objects::nonNull)
                            .toList().stream())
                    .toList();
            SETTINGS.blocksToDisallowBreaking.value = blocksToDisallowBreaking; // Blocks that cant be mined
            SETTINGS.buildIgnoreBlocks.value = blocksToIgnore; // Blocks that should be ignored in the selection
            setAcceptableThrowawayItems();
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
            resetSettings();
            SETTINGS.buildInLayers.value = true;
            SETTINGS.buildRepeatCount.value = 0;
            SETTINGS.layerOrder.value = false;
            SETTINGS.layerHeight.value = 1;
            return true;
        }

        @Override
        public String getCommand(GuiBase gui) {
            // TODO: Pass number of selected
            return "litematica";
        }
    };

    private static final Settings SETTINGS = BaritoneAPI.getSettings();

    private final String configString;
    public final boolean additionalControls;

    PresetMode(boolean additionalControls) {
        this.configString = this.name().toLowerCase();
        this.additionalControls = additionalControls;
    }

    @Override
    public String asString() {
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

    private static boolean shouldLoadPreviousSettings = false;

    public static boolean overwroteSettings() {
        return shouldLoadPreviousSettings;
    }

    private static void resetSettings() {
        shouldLoadPreviousSettings = true;
        SettingsUtil.modifiedSettings(SETTINGS).forEach(Settings.Setting::reset);
    }

    public static void loadPreviousSettings() {
        if (!shouldLoadPreviousSettings)
            return;
        resetSettings();
        shouldLoadPreviousSettings = false;
        SettingsUtil.readAndApply(SETTINGS, SettingsUtil.SETTINGS_DEFAULT_NAME);
    }

    private static void setAcceptableThrowawayItems() {
        SETTINGS.acceptableThrowawayItems.value = Configs.Lists.ACCEPTABLE_THROWAWAY_ITEMS
                .getStrings().stream().map(string -> Registries.ITEM.get(Identifier.tryParse(string)))
                .filter(Objects::nonNull).toList();
    }

    public static final ImmutableList<String> CLEAR_AREA_BLOCKS_TO_IGNORE = ImmutableList.of(
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

    public static final ImmutableList<String> CLEAR_AREA_ACCEPTABLE_THROWAWAY_ITEMS = ImmutableList.of("grass_block",
            "dirt",
            "cobblestone",
            "stone",
            "deepslate",
            "cobbled_deepslate",
            "netherrack",
            "soul_sand",
            "soul_soil",
            "basalt");

    public static final ImmutableList<String> CLEAR_AREA_BLOCKS_TO_DISALLOW_BREAKING = ImmutableList.of(
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

    public static final ImmutableList<String> FARMING_BLOCKS_TO_IGNORE = ImmutableList.of(
            "budding_amethyst",
            "dragon_egg");
}
