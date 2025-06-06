package drvlabs.de.event;

import org.jetbrains.annotations.Nullable;

import drvlabs.de.BTScreen;
import drvlabs.de.data.DataManager;
import fi.dy.masa.malilib.interfaces.IWorldLoadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public class WorldLoadListener implements IWorldLoadListener {

	@SuppressWarnings("null")
	@Override
	public void onWorldLoadPre(@Nullable ClientWorld worldBefore, @Nullable ClientWorld worldAfter, MinecraftClient mc) {
		// Save the settings before the integrated server gets shut down
		if (worldBefore != null) {
			DataManager.save();
			DataManager.clear();
			BTScreen.LOGGER.info("Saved settings");
		}
	}

	@SuppressWarnings("null")
	@Override
	public void onWorldLoadPost(@Nullable ClientWorld worldBefore, @Nullable ClientWorld worldAfter, MinecraftClient mc) {

		if (worldBefore == null) {
			DataManager.load();
			BTScreen.LOGGER.error("Loaded settings");
		}

		// TODO: Seems like this isnt needed leaving it here for now if something doesnt
		// work
		// becasue of dimension chenges.
		// if (worldAfter != null) {
		// DataManager.load();
		// BTScreen.LOGGER.info("Loaded settings");
		// }
	}
}
