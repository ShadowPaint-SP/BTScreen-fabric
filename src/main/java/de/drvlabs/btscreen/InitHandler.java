package de.drvlabs.btscreen;

import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.event.ClientTickHandler;
import de.drvlabs.btscreen.event.InputHandler;
import de.drvlabs.btscreen.event.WorldLoadListener;
import de.drvlabs.btscreen.gui.GuiConfigs;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.event.WorldLoadHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class InitHandler implements IInitializationHandler {

	@Override
	public void registerModHandlers() {
		ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, new Configs());
		Registry.CONFIG_SCREEN.registerConfigScreenFactory(
				new ModInfo(Reference.MOD_ID, Reference.MOD_NAME, GuiConfigs::new));

		InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());

		WorldLoadListener listener = new WorldLoadListener();
		WorldLoadHandler.getInstance().registerWorldLoadPreHandler(listener);
		WorldLoadHandler.getInstance().registerWorldLoadPostHandler(listener);

		ClientTickEvents.END_WORLD_TICK.register(ClientTickHandler::onEndTick);
	}

}
