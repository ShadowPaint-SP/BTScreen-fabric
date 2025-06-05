package drvlabs.de.utils.preset;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import baritone.api.BaritoneAPI;
import drvlabs.de.BTScreen;
import drvlabs.de.config.Configs;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.StringIdentifiable;

public enum PresetMode implements StringIdentifiable {
	DEFAULT(
			"default",
			"btscreen.preset_mode.name.default"),
	FARM(
			"farm",
			"btscreen.preset_mode.name.farm");

	public static final StringIdentifiable.EnumCodec<PresetMode> CODEC = StringIdentifiable
			.createCodec(PresetMode::values);
	public static final ImmutableList<PresetMode> VALUES = ImmutableList.copyOf(values());

	private final String configString;
	private final String translationKey;

	private PresetMode(String configString, String translationKey) {
		this.configString = configString;
		this.translationKey = translationKey;
	}

	public Codec<PresetMode> codec() {
		return CODEC;
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
			BaritoneAPI.getSettings().allowBreak.value = true;
			BaritoneAPI.getSettings().allowPlace.value = true;
			BaritoneAPI.getSettings().buildInLayers.value = true;
			BaritoneAPI.getSettings().blockBreakSpeed.value = 0;
			BaritoneAPI.getSettings().layerHeight.value = 5;
			BaritoneAPI.getSettings().layerOrder.value = true;
			BaritoneAPI.getSettings().itemSaver.value = true;
			BaritoneAPI.getSettings().itemSaverThreshold.value = 10;
			BaritoneAPI.getSettings().randomLooking.value = (double) 0;
			BaritoneAPI.getSettings().randomLooking113.value = (double) 0;

			BTScreen.LOGGER.info("Updated settings do default");
			// TODO: Update Blacklist
		}
		if (this.asString().equals(FARM.asString())) {
			BaritoneAPI.getSettings().allowBreak.value = false;
			BaritoneAPI.getSettings().allowPlace.value = false;
			BaritoneAPI.getSettings().buildInLayers.value = false;
			BaritoneAPI.getSettings().randomLooking.value = (double) 0;
			BaritoneAPI.getSettings().randomLooking113.value = (double) 0;
			BTScreen.LOGGER.info("Updated settings do farm");
			// TODO: Update Blacklist
			Configs.Lists.BLOCK_TYPE_BREAK_RESTRICTION_BLACKLIST.setStrings(ImmutableList.of("minecraft:farmland"));

		}
	}
}
