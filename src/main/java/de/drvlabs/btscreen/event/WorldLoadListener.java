package de.drvlabs.btscreen.event;

import org.jetbrains.annotations.Nullable;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.data.DataManager;
import fi.dy.masa.malilib.interfaces.IWorldLoadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public class WorldLoadListener implements IWorldLoadListener {

	@SuppressWarnings("null")
	@Override
	public void onWorldLoadPre(@Nullable ClientWorld worldBefore, @Nullable ClientWorld worldAfter, MinecraftClient mc) {

		if (worldBefore != null) {
			DataManager.save();
		}
	}

	@SuppressWarnings("null")
	@Override
	public void onWorldLoadPost(@Nullable ClientWorld worldBefore, @Nullable ClientWorld worldAfter, MinecraftClient mc) {

		if (worldAfter != null) {
			DataManager.load();
			BTScreen.LOGGER.error("Loaded settings");
		} else {
			DataManager.clear();
		}
	}
}
