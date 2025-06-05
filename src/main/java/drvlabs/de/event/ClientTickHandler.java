package drvlabs.de.event;

import baritone.api.BaritoneAPI;
import drvlabs.de.BTScreen;
//import drvlabs.de.BTScreen;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
//import drvlabs.de.utils.behavior.AutoDrop;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import net.minecraft.client.MinecraftClient;

public class ClientTickHandler implements IClientTickHandler {

	/*
	 * This is only here so that if u manually or beacause of whatever reason achive
	 * to start or stop the bot in another way the bot will be in the correct mode
	 */
	@Override
	public void onClientTick(MinecraftClient mc) {
		if (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().mostRecentInControl().isPresent()) {
			if (DataManager.getBotStatus() == BotStatus.IDLE) {
				BTScreen.LOGGER.info("BOT SHOULD BE IN MINING");
				DataManager.setBotStatus(BotStatus.MINING);
			}
		} else {
			if (DataManager.getBotStatus() == BotStatus.MINING) {
				BTScreen.LOGGER.info("BOT SHOULD BE IN IDLE");
				DataManager.getInstance().setActive(false);
				DataManager.setBotStatus(BotStatus.IDLE);
			}
		}

	}

}
