package drvlabs.de.utils.behavior;

import drvlabs.de.BTScreen;
import drvlabs.de.config.Configs;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.CommandUtils;
import drvlabs.de.utils.Waiter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoTorch {
	private static boolean currTrying = false;
	private static boolean success = false;

	public static boolean blockNeedsTorch(MinecraftClient mc) {
		return mc.world.getLightLevel(mc.player.getBlockPos()) <= Configs.Generic.MIN_LIGHT_LEVEL.getIntegerValue();
	}

	private static int getTorchSlotInHotbar(MinecraftClient mc) {
		PlayerInventory inventory = mc.player.getInventory();
		for (int i = 0; i < 9; i++) {
			ItemStack stack = inventory.getStack(i);
			if (stack.isOf(Items.TORCH)) {
				return i;
			}
		}
		return -1;
	}

	public static void prepare(MinecraftClient mc) {
		CommandUtils.pause(BotStatus.LIGHTING);
		mc.player.setPitch(90);
	}

	private static void tryPlacingTorch(MinecraftClient mc, int torchSlot) {
		mc.player.getInventory().setSelectedSlot(torchSlot);
		BlockPos pos = mc.player.getBlockPos().down();

		if (!mc.world.getBlockState(pos).allowsSpawning(mc.world, pos, EntityType.ZOMBIE)) {
			BTScreen.debugLog("No spawnable Block Disabeling for 600 Ticks");
			Configs.Generic.AUTO_TORCH.setBooleanValue(false);
			CommandUtils.resume();
			Waiter.wait(600, () -> {
				Configs.Generic.AUTO_TORCH.setBooleanValue(true);
			});
			return;
		}

		if (currTrying) {
			return;
		}
		currTrying = true;
		mc.player.setPitch(90);
		Waiter.wait(5, () -> {
			BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP,
					pos, false);
			for (int i = 0; i <= 4; i++) {
				mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
				mc.player.swingHand(Hand.MAIN_HAND);
			}
			Waiter.wait(10, () -> {
				if (!blockNeedsTorch(mc)) {
					success = true;
				}
				currTrying = false;
			});
		});
	}

	public static void onTick(MinecraftClient mc) {
		if (success) {
			success = false;
			CommandUtils.resume();
			return;
		}
		int torchSlot = getTorchSlotInHotbar(mc);
		if (torchSlot != -1) {
			tryPlacingTorch(mc, torchSlot);
		} else {
			BTScreen.debugLog("No Torch in Hotbar Disabeling");
			Configs.Generic.AUTO_TORCH.setBooleanValue(false);
			CommandUtils.resume();
		}
	}
}