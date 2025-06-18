package drvlabs.de.event;

import baritone.api.BaritoneAPI;
import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.Waiter;
import drvlabs.de.utils.behavior.AutoRepair;
import drvlabs.de.utils.behavior.AutoSleep;
import drvlabs.de.utils.behavior.AutoTorch;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public final class ClientTickHandler {
	private static MinecraftClient mc = MinecraftClient.getInstance();

	public static void onEndTick(ClientWorld world) {
		if (world != null && mc.player != null) {
			Waiter.tickAll();
			if (DataManager.getActive() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager()
					.mostRecentInControl().isPresent()) {
				if (DataManager.getBotStatus() == BotStatus.IDLE) {
					return;
				}
				if (DataManager.getBotStatus() == BotStatus.REPAIRING) {
					AutoRepair.onTick(mc);
				}
				if (Configs.Generic.AUTO_SLEEP.getBooleanValue()) {
					AutoSleep.tryToSleep();
				}
				if (Configs.Generic.AUTO_TORCH.getBooleanValue() && DataManager.getBotStatus() == BotStatus.MINING) {
					AutoTorch.onTick(mc);
				}
			} else {
				if (DataManager.getBotStatus() != BotStatus.IDLE) {
					DataManager.getInstance().setActive(false);
					DataManager.setBotStatus(BotStatus.IDLE);
				}
			}
		}
	}

}
