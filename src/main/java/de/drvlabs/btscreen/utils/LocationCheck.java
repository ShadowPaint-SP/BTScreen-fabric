package de.drvlabs.btscreen.utils;

import static de.drvlabs.btscreen.config.Configs.Generic.SAFETY;

import de.drvlabs.btscreen.BTScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LocationCheck {
	private static Vec3d lastLocation = null;
	private static World lastWorld = null;
	private static MinecraftClient mc = MinecraftClient.getInstance();

	private static boolean inRange() {
		Vec3d currentLocation = mc.player.getPos();
		World currentWorld = mc.world;
		boolean result = true;
		if (lastWorld != null && lastLocation != null) {
			result = currentWorld == lastWorld && currentLocation.isInRange(lastLocation, 5);
		}
		lastLocation = currentLocation;
		lastWorld = currentWorld;
		return result;
	}

	public static void checkLocation() {
		if (!inRange()) {
			if (SAFETY.getBooleanValue()) {

				BTScreen.debugLog("Player has moved too far from the last recorded location.");
				Utils.cancel();
			}
		}
	}

}
