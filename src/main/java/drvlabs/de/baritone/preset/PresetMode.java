package drvlabs.de.baritone.preset;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

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
}
