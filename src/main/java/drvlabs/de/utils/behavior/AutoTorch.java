package drvlabs.de.utils.behavior;

import baritone.api.BaritoneAPI;
import drvlabs.de.BTScreen;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoTorch {

	public static boolean blockNeedsTorch(MinecraftClient mc) {
		return mc.world.getLightLevel(mc.player.getBlockPos()) <= 1;
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
		BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().pause();
		DataManager.setBotStatus(BotStatus.LIGHTING);
		mc.player.setPitch(90);
	}

	private static void tryPlacingTorch(MinecraftClient mc, int torchSlot) {
		mc.player.getInventory().setSelectedSlot(torchSlot);
		BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(mc.player.getBlockPos().down()), Direction.UP,
				mc.player.getBlockPos().down(), false);
		mc.player.setPitch(90);
		if (mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit).isAccepted()) {
			BTScreen.debugLog("Placed Torch");
			mc.player.swingHand(Hand.MAIN_HAND);
		}
	}

	public static void onTick(MinecraftClient mc) {
		if (blockNeedsTorch(mc)) {
			int torchSlot = getTorchSlotInHotbar(mc);
			if (torchSlot != -1) {
				tryPlacingTorch(mc, torchSlot);
			}
		} else {
			BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().resume();
			DataManager.setBotStatus(BotStatus.MINING);
		}
	}
}