package drvlabs.de.utils;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
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
			if (Configs.Generic.HOME_COMMAND.getStringValue().equals("tp")) {
				if (homeName.equals(Configs.Generic.DROP_HOME.getStringValue())) {
					client.player.networkHandler.sendChatCommand(
							Configs.Generic.HOME_COMMAND.getStringValue() + " " + client.player.getNameForScoreboard() + " "
									+ homeName
									+ " 180 0");
					return;
				}
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
			if (Configs.Generic.HOME_COMMAND.getStringValue().equals("tp")) {
				Configs.Generic.MINE_HOME.setValueFromString(client.player.getBlockPos().getX() + " "
						+ client.player.getBlockPos().getY() + " " + client.player.getBlockPos().getZ());
				return;
			}
			client.player.networkHandler.sendChatCommand(Configs.Generic.SETHOME_COMMAND.getStringValue() + " " + homeName);
		}
	}
}
