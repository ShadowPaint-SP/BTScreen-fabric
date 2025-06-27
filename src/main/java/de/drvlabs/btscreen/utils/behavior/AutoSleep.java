package de.drvlabs.btscreen.utils.behavior;

import java.util.ArrayList;
import java.util.List;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.utils.BotStatus;
import de.drvlabs.btscreen.utils.CommandUtils;
import de.drvlabs.btscreen.utils.Waiter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoSleep {
	private static MinecraftClient mc = MinecraftClient.getInstance();
	private static boolean sucess = false;
	private static boolean currTrying = false;

	public static boolean isNight() {
		long curTime = mc.world.getTimeOfDay() % 24000;
		return (curTime >= 13000 && curTime < 23000);
	}

	public static boolean isDay() {
		long curTime = mc.world.getTimeOfDay() % 24000;
		return (curTime < 13000 || curTime >= 23000);
	}

	public static void tryToSleep() {

		if (DataManager.getBotStatus() == BotStatus.MINING
				&& isNight()) {

			if (mc.world.getDimension().hasCeiling()) { // in the nether
				Configs.Generic.AUTO_SLEEP.setBooleanValue(false);
				BTScreen.debugLog("Cannot sleep in the nether... Disabling auto sleep");
				return;
			}
			CommandUtils.pause(BotStatus.SLEEPING);
			CommandUtils.setHome(Configs.Generic.MINE_HOME.getStringValue());
			CommandUtils.tpTo(Configs.Generic.SLEEP_HOME.getStringValue());
		}

		if (DataManager.getBotStatus() == BotStatus.SLEEPING && isNight()) {
			tryNewSleeping();
		}

		if (DataManager.getBotStatus() == BotStatus.SLEEPING && isDay()) {
			sucess = false;
			CommandUtils.tpTo(Configs.Generic.MINE_HOME.getStringValue());
			CommandUtils.resume();
		}
	}

	private static void tryNewSleeping() {
		if (currTrying || sucess) {
			return;
		}
		currTrying = true;
		hitBeds();
		Waiter.wait(10, () -> {
			if (mc.player.isSleeping()) {
				sucess = true;
				currTrying = false;
				return;
			}
			BTScreen.debugLog("Failed to sleep, trying again");
			currTrying = false;
		});
	}

	private static void hitBeds() {
		Vec3d pos = mc.player.getPos();
		List<BlockPos> positions = new ArrayList<>();
		positions.add(new BlockPos((int) pos.x + 1, (int) pos.y, (int) pos.z));
		positions.add(new BlockPos((int) pos.x - 1, (int) pos.y, (int) pos.z));
		positions.add(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z + 1));
		positions.add(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z - 1));

		for (BlockPos position : positions) {
			BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(position), Direction.DOWN, position,
					false);
			BTScreen.debugLog("Hitting Bed at: " + position);
			if (mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit).isAccepted()) {
				mc.player.swingHand(Hand.MAIN_HAND);
			}
		}
	}

}
