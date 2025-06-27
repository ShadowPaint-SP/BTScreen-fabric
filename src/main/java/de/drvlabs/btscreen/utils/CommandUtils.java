package de.drvlabs.btscreen.utils;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import net.minecraft.client.MinecraftClient;

public class CommandUtils {

	private static IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

	public static void execute(String command) {
		baritone.getCommandManager().execute(command);
	}

	public static void executeBuild(String command) {
		if (Configs.Generic.REPEAT_ACTION.getBooleanValue()) {
			RepeatAction.trackCommand(command);
		}
		execute(command);
		DataManager.setBotStatus(BotStatus.MINING);
	}

	public static void pause(BotStatus newStatus) {
		baritone.getBuilderProcess().pause();
		// execute("pause");
		DataManager.setBotStatus(newStatus);
	}

	public static void resume() {
		baritone.getBuilderProcess().resume();
		// execute("resume");
		DataManager.setBotStatus(BotStatus.MINING);
	}

	public static void stop() {
		CommandUtils.execute("stop");
		DataManager.getInstance().setActive(false);
		RepeatAction.cancelRepeatAction();
		DataManager.setBotStatus(BotStatus.IDLE);
	}

	public static void tpTo(String homeName) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			if (Configs.Generic.HOME_COMMAND.getStringValue().equals("tp")
					&& homeName.equals(Configs.Generic.DROP_HOME.getStringValue())) {
				client.player.networkHandler.sendChatCommand(Configs.Generic.HOME_COMMAND.getStringValue()
						+ " " + client.player.getNameForScoreboard() + " " + homeName + " 180 0");
				return;
			}
			client.player.networkHandler
					.sendChatCommand(Configs.Generic.HOME_COMMAND.getStringValue() + " " + homeName);
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
			client.player.networkHandler
					.sendChatCommand(Configs.Generic.SETHOME_COMMAND.getStringValue() + " " + homeName);
		}
	}
}
