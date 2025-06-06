package drvlabs.de.utils.behavior;

import drvlabs.de.BTScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoSleep {
	private static MinecraftClient mc = MinecraftClient.getInstance();
	private static int sleepTimer = 0;

	public static boolean isNight() {
		return mc.world.isNight();
	}

	public static void trySleeping() {

		if (mc.player.isSleeping()) {
			++sleepTimer;
			if (sleepTimer > 120) {
				sleepTimer = 120;
			}
			if (!mc.player.getWorld().isClient && mc.player.getWorld().isDay()) {
				mc.player.wakeUp();
				BTScreen.LOGGER.info("IT IS DAY AGAIN");
			}
		} else if (sleepTimer > 0) {
			++sleepTimer;
			if (sleepTimer >= 130) {
				sleepTimer = 0;
			}
		}

		if (!mc.player.isSleeping() && mc.currentScreen == null) {
			Vec3d pos = mc.player.getPos();
			BlockPos p = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
			BTScreen.LOGGER.info("Sleeping at: " + pos);
			BTScreen.LOGGER.info("Sleeping at: " + p);

			BTScreen.LOGGER.info("1");
			// mc.player.interactAt(mc.player, pos.offset(Direction.NORTH, 1),
			// Hand.MAIN_HAND);
			// BTScreen.LOGGER.info("2");
			// mc.player.interactAt(mc.player, pos.offset(Direction.SOUTH, 1),
			// Hand.MAIN_HAND);
			// BTScreen.LOGGER.info("3");
			// mc.player.interactAt(mc.player, pos.offset(Direction.WEST, 1),
			// Hand.MAIN_HAND);
			// BTScreen.LOGGER.info("4");
			// mc.player.interactAt(mc.player, pos.offset(Direction.EAST, 1),
			// Hand.MAIN_HAND);

			// mc.player.trySleep(p.offset(Axis.X, 1));
			// if (mc.player.trySleep(p.offset(Axis.X, 1)).right().isPresent()) {
			// // Sleep successful
			// BTScreen.LOGGER.info("SLEEPING1");
			// return;
			// }
			// BTScreen.LOGGER.info("2");
			// if (mc.player.trySleep(p.offset(Axis.X, -1)).right().isPresent()) {
			// // Sleep successful
			// BTScreen.LOGGER.info("SLEEPING2");
			// return;
			// }
			// BTScreen.LOGGER.info("3");
			// if (mc.player.trySleep(p.offset(Axis.Z, 1)).right().isPresent()) {
			// // Sleep successful
			// BTScreen.LOGGER.info("SLEEPING3");
			// return;
			// }
			// BTScreen.LOGGER.info("4");
			// if (mc.player.trySleep(p.offset(Axis.Z, -1)).right().isPresent()) {
			// // Sleep successful
			// BTScreen.LOGGER.info("SLEEPING4");
			// return;
			// }
			BTScreen.LOGGER.info("FAILED");

			return;
		}

	}

}
