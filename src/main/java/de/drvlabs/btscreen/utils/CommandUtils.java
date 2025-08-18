package de.drvlabs.btscreen.utils;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import net.minecraft.client.MinecraftClient;

public class CommandUtils {
	private static final MinecraftClient MC = MinecraftClient.getInstance();
	private static final IBaritone BT = BaritoneAPI.getProvider().getPrimaryBaritone();

	public static void execute(String command) {
		BT.getCommandManager().execute(command);
	}

	public static void executeBuild(String command) {
		if (Configs.Generic.REPEAT_ACTION.getBooleanValue()) {
			RepeatAction.trackCommand(command);
		}
		execute(command);
		DataManager.setBotStatus(BotStatus.MINING);
	}

	public static void pause(BotStatus newStatus) {
		BT.getBuilderProcess().pause();
		// execute("pause");
		DataManager.setBotStatus(newStatus);
	}

	public static void resume() {
		BT.getBuilderProcess().resume();
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
		if (MC.player != null) {
			if (Configs.Generic.HOME_COMMAND.getStringValue().equals("tp")
					&& homeName.equals(Configs.Generic.DROP_HOME.getStringValue())) {
				MC.player.networkHandler.sendChatCommand(Configs.Generic.HOME_COMMAND.getStringValue()
						+ " " + MC.player.getNameForScoreboard() + " " + homeName + " 180 0");
				return;
			}
			MC.player.networkHandler
					.sendChatCommand(Configs.Generic.HOME_COMMAND.getStringValue() + " " + homeName);
		}
	}

	public static void sendCommand(String command) {
		if (MC.player != null) {
			MC.getNetworkHandler().sendChatCommand(command);
		}
	}

	public static void setHome(String homeName) {
		if (MC.player != null) {
			if (Configs.Generic.HOME_COMMAND.getStringValue().equals("tp")) {
				Configs.Generic.MINE_HOME.setValueFromString(MC.player.getBlockPos().getX() + " "
						+ MC.player.getBlockPos().getY() + " " + MC.player.getBlockPos().getZ());
				return;
			}
			MC.player.networkHandler
					.sendChatCommand(Configs.Generic.SETHOME_COMMAND.getStringValue() + " " + homeName);
		}
	}
}
