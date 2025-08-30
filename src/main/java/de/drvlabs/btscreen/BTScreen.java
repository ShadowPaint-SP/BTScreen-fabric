package de.drvlabs.btscreen;

import static de.drvlabs.btscreen.config.Configs.Generic.DEBUG_LOGGING;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.event.EventHandler;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BTScreen implements ClientModInitializer {
    public static final String MOD_ID = "btscreen";
    public static final ModMetadata MOD_META;
    public static final String MOD_NAME;
    public static final String MOD_VERSION;

    static {
        MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata();
        MOD_NAME = MOD_META.getName();
        MOD_VERSION = MOD_META.getVersion().getFriendlyString();
    }

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing " + MOD_NAME + " " + MOD_VERSION);
        // Configs
        ConfigManager.getInstance().registerConfigHandler(BTScreen.MOD_ID, new Configs());
        // Events
        final EventHandler eventHandler = new EventHandler();
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(eventHandler);
        ClientTickEvents.END_WORLD_TICK.register(eventHandler);
        ClientLifecycleEvents.CLIENT_STARTED.register(eventHandler);
    }

    public static void debugLog(String msg, Object... args) {
        if (DEBUG_LOGGING.getBooleanValue()) {
            LOGGER.info(msg, args);
        }
    }

    public static void chatMessage(Text... message) {
        Utils.chatMessage(ArrayUtils.insert(0, message,
                Text.literal("[").formatted(Formatting.DARK_PURPLE),
                Text.literal(MOD_NAME).formatted(Formatting.LIGHT_PURPLE),
                Text.literal("] ").formatted(Formatting.DARK_PURPLE)));
    }
}