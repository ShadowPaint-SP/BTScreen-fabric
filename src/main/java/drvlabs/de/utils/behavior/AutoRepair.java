package drvlabs.de.utils.behavior;

import java.util.function.Consumer;

import drvlabs.de.BTScreen;
import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.CommandUtils;
import drvlabs.de.utils.IMinecraftClientInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import fi.dy.masa.malilib.config.IConfigInteger;
import fi.dy.masa.malilib.util.GuiUtils;

public class AutoRepair {
	private static final KeybindState KEY_STATE_ATTACK = new KeybindState(MinecraftClient.getInstance().options.attackKey,
			(mc) -> ((IMinecraftClientInvoker) mc).btscreen_invokeDoAttack());

	private static int swordSlot = -1;

	private static class KeybindState {
		private final Consumer<MinecraftClient> clickFunc;

		private int intervalCounter;

		public KeybindState(KeyBinding keybind, Consumer<MinecraftClient> clickFunc) {
			this.clickFunc = clickFunc;
		}

		public void reset() {
			this.intervalCounter = 0;
		}

		public void handlePeriodicClick(int interval, MinecraftClient mc) {
			if (++this.intervalCounter >= interval) {
				BTScreen.debugLog("Attacking");
				if (swordSlot != -1) {
					int tmpSlot = mc.player.getInventory().getSelectedSlot();
					mc.player.getInventory().setSelectedSlot(swordSlot);
					this.clickFunc.accept(mc);
					mc.player.getInventory().setSelectedSlot(tmpSlot);
				} else {
					this.clickFunc.accept(mc);
				}
				this.intervalCounter = 0;
			}
		}
	}

	public static void onTick(MinecraftClient mc) {
		ClientPlayerEntity player = mc.player;
		if (player == null) {
			return;
		}

		if (!player.getStackInHand(Hand.MAIN_HAND).isDamaged()) {
			BTScreen.debugLog("FINISHED REPAIR");
			CommandUtils.tpTo(Configs.Generic.MINE_HOME.getStringValue());
			DataManager.setBotStatus(BotStatus.MINING);
			CommandUtils.execute("resume");
			AutoDrop.checkInventory();
			return;
		}
		doPeriodicClicks(mc);

	}

	private static void doPeriodicClicks(MinecraftClient mc) {
		if (GuiUtils.getCurrentScreen() == null) {
			handlePeriodicClicks(
					KEY_STATE_ATTACK,
					DataManager.getBotStatus(),
					Configs.Generic.PERIODIC_ATTACK_INTERVAL, mc);
		} else {
			KEY_STATE_ATTACK.reset();
		}
	}

	private static void handlePeriodicClicks(
			KeybindState keyState,
			BotStatus status,
			IConfigInteger cfgClickInterval,
			MinecraftClient mc) {
		if (status == BotStatus.REPAIRING) {
			int interval = cfgClickInterval.getIntegerValue();
			keyState.handlePeriodicClick(interval, mc);
		} else {
			keyState.reset();
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	public static void tryStartingRepairIfNearlyBroken() {
		PlayerEntity player = MinecraftClient.getInstance().player;
		ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
		if (stack.isEmpty() == false) {
			int minDurability = Configs.Generic.ITEM_DURABILITY_THRESHOLD.getIntegerValue();

			if (isItemAtLowDurability(stack, minDurability)) {
				BTScreen.debugLog("STARTING REPAIR");
				CommandUtils.execute("pause");
				DataManager.setBotStatus(BotStatus.REPAIRING);
				swordSlot = getSwordSlotInHotbar();
				CommandUtils.setHome(Configs.Generic.MINE_HOME.getStringValue());
				CommandUtils.tpTo(Configs.Generic.REPAIR_HOME.getStringValue());
			}
		}
	}

	private static boolean isItemAtLowDurability(ItemStack stack, int minDurability) {
		return stack.isDamageable() && (stack.getMaxDamage() - stack.getDamage()) <= minDurability;
	}

	public static int getSwordSlotInHotbar() {
		MinecraftClient mc = MinecraftClient.getInstance();
		PlayerInventory inventory = mc.player.getInventory();

		for (int i = 0; i < 9; i++) {
			ItemStack stack = inventory.getStack(i);
			if (isSword(stack)) {
				return i;
			}
		}

		return -1;
	}

	public static boolean isSword(ItemStack stack) {
		return stack.isOf(Items.WOODEN_SWORD) ||
				stack.isOf(Items.STONE_SWORD) ||
				stack.isOf(Items.IRON_SWORD) ||
				stack.isOf(Items.GOLDEN_SWORD) ||
				stack.isOf(Items.DIAMOND_SWORD) ||
				stack.isOf(Items.NETHERITE_SWORD);
	}
}