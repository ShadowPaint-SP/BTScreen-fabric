package drvlabs.de.utils.preset;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import drvlabs.de.BTScreen;
import drvlabs.de.config.Configs;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public enum PresetMode implements StringIdentifiable {
	DEFAULT(
			"default",
			"btscreen.preset_mode.name.default"),
	FARM(
			"farm",
			"btscreen.preset_mode.name.farm");

	public static final ImmutableList<PresetMode> VALUES = ImmutableList.copyOf(values());
	private static final Settings bt = BaritoneAPI.getSettings();

	private final String configString;
	private final String translationKey;

	private PresetMode(String configString, String translationKey) {
		this.configString = configString;
		this.translationKey = translationKey;
	}

	@Override
	public String asString() {
		return this.configString;
	}

	public String getName() {
		return StringUtils.translate(this.translationKey);
	}

	/**
	 * Cycles to the next or previous PresetMode in the enum.
	 *
	 * @param player  Unused in this context, kept for signature compatibility if
	 *                needed elsewhere.
	 * @param forward If true, cycles to the next mode; if false, cycles to the
	 *                previous mode.
	 * @return The next (or previous) PresetMode in the enumeration.
	 */
	public PresetMode cycle(PlayerEntity player, boolean forward) {
		PresetMode[] values = PresetMode.values();
		final int numModes = values.length;
		final int currentIndex = this.ordinal();
		int nextIndex;

		if (forward) {
			nextIndex = (currentIndex + 1) % numModes;
		} else {
			nextIndex = (currentIndex - 1 + numModes) % numModes;
		}

		return values[nextIndex];
	}

	public void setSettings() {
		if (this.asString().equals(DEFAULT.asString())) {
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
			bt.buildIgnoreBlocks.value = blocksToIgnore; // Blocks that should be ignored in the selection
			setAcceptableThrowawayItems();
			BTScreen.debugLog("Updated settings do default");
		}
		if (this.asString().equals(FARM.asString())) {
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
			BTScreen.debugLog("Updated settings do farm");
		}
	}

	public static ImmutableList<String> getDefaultIgnoreList() {
		return ImmutableList.of(
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
	}

	private static void setAcceptableThrowawayItems() {
		bt.acceptableThrowawayItems.value = Configs.Lists.ACCEPTABLE_THROWAWAY_ITEMS
				.getStrings().stream().map(string -> Registries.ITEM.get(Identifier.tryParse(string)))
				.filter(Objects::nonNull).toList();
	}

	public static ImmutableList<String> getAcceptableThrowawayItems() {
		return ImmutableList.of("grass_block",
				"dirt",
				"cobblestone",
				"stone",
				"deepslate",
				"cobbled_deepslate",
				"netherrack",
				"soul_sand",
				"soul_soil",
				"basalt");
	}

	public static ImmutableList<String> getFarmIgnoreList() {
		return ImmutableList.of(
				"budding_amethyst",
				"dragon_egg");
	}

	public static ImmutableList<String> getGlobalDisallowBreakingList() {
		return ImmutableList.of(
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
	}
}
