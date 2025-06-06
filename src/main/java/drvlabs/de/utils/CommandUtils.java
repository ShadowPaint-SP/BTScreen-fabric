package drvlabs.de.utils;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import drvlabs.de.BTScreen;
import drvlabs.de.config.Configs;
import net.minecraft.client.MinecraftClient;

public class CommandUtils {

	private static IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

	public static void execute(String command) {
		baritone.getCommandManager().execute(command);
	}

	public static void tpTo(String homeName) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			if (homeName.equals(Configs.Generic.DROP_HOME.getStringValue())) { // TODO DEBUG MODE
				client.player.networkHandler.sendChatCommand(
						Configs.Generic.HOME_COMMAND.getStringValue() + " " + client.player.getNameForScoreboard() + " " + homeName
								+ " 180 0");
				return;
			}
			client.player.networkHandler.sendChatCommand(Configs.Generic.HOME_COMMAND.getStringValue() + " " + homeName);
		}
	}

	public static void sendCommand(String command) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.player.networkHandler.sendChatCommand(command);
		}
	}

	public static void setHome(String homeName) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.player.networkHandler.sendChatCommand(Configs.Generic.SETHOME_COMMAND.getStringValue() + " " + homeName);
		}
	}

	public static void debugHome(String homeName) { // TODO DEBUG MODE
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			Configs.Generic.MINE_HOME.setValueFromString(homeName);
			BTScreen.LOGGER.info("Mine Home: " + Configs.Generic.MINE_HOME.getStringValue());
		}
	}

}
