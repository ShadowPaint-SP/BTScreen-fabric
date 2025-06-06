package drvlabs.de.utils.behavior;

import java.util.ArrayList;
import java.util.List;

import drvlabs.de.BTScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoSleep {
	private static MinecraftClient mc = MinecraftClient.getInstance();
	private static int sleepTimer = 0;
	private static boolean sucess = false;

	public static boolean isNight() {
		return mc.world.isNight();
	}

	public static boolean isDay() {
		return mc.world.isDay();
	}

	public static void trySleeping() {

		if (mc.player.isSleeping()) {
			++sleepTimer;
			BTScreen.LOGGER.info("timer: " + sleepTimer);
			if (sleepTimer > 100) {
				sleepTimer = 100;
			}
		} else if (sleepTimer > 0) {

			++sleepTimer;
			BTScreen.LOGGER.info("timer NOT:" + sleepTimer);
			if (sleepTimer >= 130) {
				sleepTimer = 0;
				sucess = false;
			}
		}

		if (!mc.player.isSleeping() && mc.currentScreen == null && !sucess) {
			Vec3d pos = mc.player.getPos();
			List<BlockPos> positions = new ArrayList<>();
			positions.add(new BlockPos((int) pos.x + 1, (int) pos.y, (int) pos.z));
			positions.add(new BlockPos((int) pos.x - 1, (int) pos.y, (int) pos.z));
			positions.add(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z + 1));
			positions.add(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z - 1));

			for (BlockPos position : positions) {
				BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(position), Direction.DOWN, position,
						false);
				if (mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit).isAccepted()) {
					mc.player.swingHand(Hand.MAIN_HAND);
					sucess = true;
				}
			}
			return;
		}

	}

}
