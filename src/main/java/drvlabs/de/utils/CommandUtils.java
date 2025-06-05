package drvlabs.de.utils;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import net.minecraft.client.MinecraftClient;

public class CommandUtils {

	private static IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

	public static void execute(String command) {
		baritone.getCommandManager().execute(command);
	}

	public static void sendCommand(String command) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.player.networkHandler.sendChatCommand(command);
		}
	}
}
