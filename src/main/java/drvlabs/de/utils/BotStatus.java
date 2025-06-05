package drvlabs.de.utils;

import net.minecraft.util.StringIdentifiable;

public enum BotStatus implements StringIdentifiable {
	IDLE("idle"),
	MINING("mining"),
	DROPPING("dropping"),
	SLEEPING("sleeping"),
	HASTING("hasting"),
	REPAIRING("repairing");

	private final String configString;

	private BotStatus(String configString) {
		this.configString = configString;
	}

	@Override
	public String asString() {
		return this.configString;
	}

}
