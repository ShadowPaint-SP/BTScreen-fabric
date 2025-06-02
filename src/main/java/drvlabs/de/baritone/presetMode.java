package drvlabs.de.baritone;

import net.minecraft.util.StringIdentifiable;

public enum presetMode implements StringIdentifiable {
	DEFAULT("default", "baritone.gui.label.preset_selection.mode.normal"),
	FARM("farm", "baritone.gui.label.preset_selection.mode.farm"),;

	presetMode(String configName, String translationKey) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String asString() {
		
	}
}
