package de.drvlabs.btscreen;

import static de.drvlabs.btscreen.config.Configs.Generic.DEBUG_LOGGING;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import baritone.api.pathing.calc.IPathingControlManager;
import de.drvlabs.btscreen.btprocess.AutoDrop;
import de.drvlabs.btscreen.btprocess.AutoEat;
import de.drvlabs.btscreen.btprocess.AutoHaste;
import de.drvlabs.btscreen.btprocess.AutoRepair;
import de.drvlabs.btscreen.btprocess.AutoSleep;
import de.drvlabs.btscreen.btprocess.AutoTorch;
import de.drvlabs.btscreen.btprocess.BTActiveListener;
import de.drvlabs.btscreen.btprocess.BedrockCleaner;
import de.drvlabs.btscreen.btprocess.LocationCheck;
import de.drvlabs.btscreen.btprocess.ClearAreaPlus;
import de.drvlabs.btscreen.btprocess.SelectionOrchestrator;
import de.drvlabs.btscreen.btprocess.SmartWaterClear;
import de.drvlabs.btscreen.btprocess.Teleport;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.event.EventHandler;
import de.drvlabs.btscreen.event.InputHandler;
import de.drvlabs.btscreen.gui.GuiConfigs;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class BTScreen implements ClientModInitializer {
    public static final String MOD_ID = "btscreen";
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID).get();
    public static final ModMetadata MOD_META = MOD_CONTAINER.getMetadata();
    public static final String MOD_NAME = MOD_META.getName();
    public static final String MOD_VERSION = MOD_META.getVersion().getFriendlyString();

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing " + MOD_NAME + " " + MOD_VERSION);
        // Configs
        ConfigManager.getInstance().registerConfigHandler(BTScreen.MOD_ID, new Configs());
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(BTScreen.MOD_ID, BTScreen.MOD_NAME, GuiConfigs::new));
        // Events
        EventHandler.register();
        InputHandler.register();
        // Processes
        final IPathingControlManager controlManager = Utils.BT.getPathingControlManager();
        controlManager.registerProcess(BTActiveListener.INSTANCE);
        controlManager.registerProcess(Teleport.INSTANCE);
        controlManager.registerProcess(AutoDrop.INSTANCE);
        controlManager.registerProcess(AutoEat.INSTANCE);
        controlManager.registerProcess(AutoHaste.INSTANCE);
        controlManager.registerProcess(AutoRepair.INSTANCE);
        controlManager.registerProcess(AutoSleep.INSTANCE);
        controlManager.registerProcess(AutoTorch.INSTANCE);
        controlManager.registerProcess(LocationCheck.INSTANCE);
        controlManager.registerProcess(SelectionOrchestrator.INSTANCE);
        controlManager.registerProcess(ClearAreaPlus.INSTANCE);
        controlManager.registerProcess(SmartWaterClear.INSTANCE);
        controlManager.registerProcess(BedrockCleaner.INSTANCE);
    }

    public static void debugLog(String msg, Object... args) {
        if (DEBUG_LOGGING.getBooleanValue()) {
            LOGGER.info(msg, args);
        }
    }

    public static void chatMessage(Component... message) {
        Utils.chatMessage(ArrayUtils.insert(0, message,
                Component.literal("[").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal(MOD_NAME).withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal("] ").withStyle(ChatFormatting.DARK_PURPLE)));
    }
}
