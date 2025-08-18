package de.drvlabs.btscreen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.calc.IPathingControlManager;
import de.drvlabs.btscreen.btprocess.AutoRepair;
import de.drvlabs.btscreen.config.Configs;
import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ModInitializer;

public class BTScreen implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing BTScreen");
		InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
		IPathingControlManager controlManager = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager();
		controlManager.registerProcess(new AutoRepair());
	}

	public static void debugLog(String msg, Object... args) {
		if (Configs.Generic.DEBUG_LOGGING.getBooleanValue()) {
			BTScreen.LOGGER.info(msg, args);
		}
	}
}