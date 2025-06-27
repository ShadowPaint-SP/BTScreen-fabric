package de.drvlabs.btscreen.event;

import baritone.api.BaritoneAPI;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.utils.BotStatus;
import de.drvlabs.btscreen.utils.Waiter;
import de.drvlabs.btscreen.utils.behavior.AutoRepair;
import de.drvlabs.btscreen.utils.behavior.AutoSleep;
import de.drvlabs.btscreen.utils.behavior.AutoTorch;
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
				if (Configs.Generic.AUTO_TORCH.getBooleanValue()) {
					if (DataManager.getBotStatus() == BotStatus.MINING && AutoTorch.blockNeedsTorch(mc)) {
						AutoTorch.prepare(mc);
					}
					if (DataManager.getBotStatus() == BotStatus.LIGHTING) {
						AutoTorch.onTick(mc);
					}
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
