package de.drvlabs.btscreen;

import static de.drvlabs.btscreen.config.Configs.Generic.DEBUG_LOGGING;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.drvlabs.btscreen.event.EventHandler;
import de.drvlabs.btscreen.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BTScreen implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing BTScreen");
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
                Text.literal(Reference.MOD_NAME).formatted(Formatting.LIGHT_PURPLE),
                Text.literal("] ").formatted(Formatting.DARK_PURPLE)));
    }
}