package drvlabs.de.event;

import baritone.api.BaritoneAPI;
import drvlabs.de.BTScreen;
import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.Waiter;
import drvlabs.de.utils.behavior.AutoRepair;
import drvlabs.de.utils.behavior.AutoSleep;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import net.minecraft.client.MinecraftClient;

public class ClientTickHandler implements IClientTickHandler {

	/*
	 * This is only here so that if u manually or beacause of whatever reason achive
	 * to start or stop the bot in another way the bot will be in the correct mode
	 */
	@Override
	public void onClientTick(MinecraftClient mc) {
		if (mc.world != null && mc.player != null) {
			Waiter.tickAll();
			if (DataManager.getActive() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager()
					.mostRecentInControl().isPresent()) {
				if (DataManager.getBotStatus() == BotStatus.IDLE) {
					BTScreen.LOGGER.info("BOT SHOULD BE IN MINING");
					DataManager.setBotStatus(BotStatus.MINING);
				}
				if (DataManager.getBotStatus() == BotStatus.REPAIRING) {
					AutoRepair.onTick(mc);
				}
				if (Configs.Generic.AUTO_SLEEP.getBooleanValue()) {
					AutoSleep.tryToSleep();
				}
			} else {
				if (DataManager.getBotStatus() != BotStatus.IDLE) {
					BTScreen.LOGGER.info("BOT SHOULD BE IN IDLE");
					DataManager.getInstance().setActive(false);
					DataManager.setBotStatus(BotStatus.IDLE);
				}
			}
		}
	}

}
