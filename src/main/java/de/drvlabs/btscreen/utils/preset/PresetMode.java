package de.drvlabs.btscreen.utils.preset;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public enum PresetMode implements StringIdentifiable, IConfigOptionListEntry {
    DEFAULT {
        @Override
        public void setSettings() {
            bt.allowBreak.value = true;
            bt.allowPlace.value = true;
            bt.buildInLayers.value = true;
            bt.blockBreakSpeed.value = 0;
            bt.layerHeight.value = 5;
            bt.layerOrder.value = true;
            bt.itemSaver.value = true;
            bt.itemSaverThreshold.value = 10;
            bt.randomLooking.value = (double) 0;
            bt.randomLooking113.value = (double) 0;
            List<Block> blocksToDisallowBreaking = Configs.Lists.DEFAULT_BLOCKS_TO_DISALLOW_BREAKING
                    .getStrings().stream().map(string -> Registries.BLOCK.get(Identifier.tryParse(string)))
                    .filter(Objects::nonNull).toList();
            List<Block> blocksToIgnore = Stream.concat(blocksToDisallowBreaking.stream(),
                    Configs.Lists.DEFAULT_BLOCKS_TO_IGNORE.getStrings().stream()
                            .map(string -> Registries.BLOCK.get(Identifier.tryParse(string))).filter(Objects::nonNull)
                            .toList().stream())
                    .toList();
            bt.blocksToDisallowBreaking.value = blocksToDisallowBreaking; // Blocks that cant be mined
            // Blocks that should be ignored in the selection
            if (Configs.Generic.AUTO_TORCH.getBooleanValue()) {
                bt.buildIgnoreBlocks.value = Stream.concat(blocksToIgnore.stream(),
                        Stream.of(Blocks.TORCH, Blocks.WALL_TORCH)).toList();
            } else {
                bt.buildIgnoreBlocks.value = blocksToIgnore;
            }
            setAcceptableThrowawayItems();
        }
    },
    FARM {
        @Override
        public void setSettings() {
            bt.allowBreak.value = false;
            bt.allowPlace.value = false;
            bt.buildInLayers.value = false;
            bt.randomLooking.value = (double) 0;
            bt.randomLooking113.value = (double) 0;
            List<Block> blocksToDisallowBreaking = Configs.Lists.FARM_BLOCKS_TO_DISALLOW_BREAKING
                    .getStrings().stream().map(string -> Registries.BLOCK.get(Identifier.tryParse(string)))
                    .filter(Objects::nonNull).toList();
            List<Block> blocksToIgnore = Stream.concat(blocksToDisallowBreaking.stream(),
                    Configs.Lists.FARM_BLOCKS_TO_IGNORE.getStrings().stream()
                            .map(string -> Registries.BLOCK.get(Identifier.tryParse(string))).filter(Objects::nonNull)
                            .toList().stream())
                    .toList();
            bt.blocksToDisallowBreaking.value = blocksToDisallowBreaking; // Blocks that cant be mined
            bt.buildIgnoreBlocks.value = blocksToIgnore; // Blocks that should be ignored in the selection
            setAcceptableThrowawayItems();
        }
    },
    LIQUID {
        @Override
        public void setSettings() {
            bt.allowBreak.value = true;
            bt.okIfWater.value = false;
            bt.allowPlace.value = true;
            bt.buildInLayers.value = true;
            bt.layerOrder.value = true;
            bt.layerHeight.value = 1;
        }
    },
    BUILDING {
        @Override
        public void setSettings() {
            bt.buildInLayers.value = true;
            bt.layerOrder.value = false;
            bt.layerHeight.value = 1;
        }
    };

    private static final Settings bt = BaritoneAPI.getSettings();

    private final String configString;

    PresetMode() {
        this.configString = this.name().toLowerCase();
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
        return DEFAULT;
    }

    public abstract void setSettings();

    private static void setAcceptableThrowawayItems() {
        bt.acceptableThrowawayItems.value = Configs.Lists.ACCEPTABLE_THROWAWAY_ITEMS
                .getStrings().stream().map(string -> Registries.ITEM.get(Identifier.tryParse(string)))
                .filter(Objects::nonNull).toList();
    }

    public static final ImmutableList<String> DEFAULT_BLOCKS_TO_IGNORE = ImmutableList.of(
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

    public static final ImmutableList<String> DEFAULT_ACCEPTABLE_THROWAWAY_ITEMS = ImmutableList.of("grass_block",
            "dirt",
            "cobblestone",
            "stone",
            "deepslate",
            "cobbled_deepslate",
            "netherrack",
            "soul_sand",
            "soul_soil",
            "basalt");

    public static final ImmutableList<String> DEFAULT_BLOCKS_TO_DISALLOW_BREAKING = ImmutableList.of(
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

    public static final ImmutableList<String> FARM_BLOCKS_TO_IGNORE = ImmutableList.of(
            "budding_amethyst",
            "dragon_egg");

}
