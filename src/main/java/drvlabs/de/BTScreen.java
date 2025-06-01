package drvlabs.de;

import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import drvlabs.de.config.Configs;
import fi.dy.masa.malilib.event.InitializationHandler;

public class BTScreen implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
		LOGGER.info("BTScreen is running!!");

	}

	public static void debugLog(String msg, Object... args) {
		if (Configs.Generic.DEBUG_LOGGING.getBooleanValue()) {
			BTScreen.LOGGER.info(msg, args);
		}
	}
}