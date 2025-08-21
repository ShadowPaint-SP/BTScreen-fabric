package de.drvlabs.btscreen.utils.behavior;

import java.util.function.Consumer;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.utils.BotStatus;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.config.IConfigInteger;
import fi.dy.masa.malilib.util.GuiUtils;
import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;

public class AutoRepair {
	private static final KeybindState KEY_STATE_ATTACK = new KeybindState(
			MinecraftClient.getInstance().options.attackKey, MinecraftClient::doAttack);

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
			BTScreen.debugLog("Finished Repairing");
			Utils.tpTo(Configs.Generic.MINE_HOME.getStringValue());
			Utils.resume();
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
				BTScreen.debugLog("Start Repairing");
				Utils.pause(BotStatus.REPAIRING);
				swordSlot = getSwordSlotInHotbar();
				Utils.setHome(Configs.Generic.MINE_HOME.getStringValue());
				Utils.tpTo(Configs.Generic.REPAIR_HOME.getStringValue());
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
		return TagUtil.isIn(ItemTags.SWORDS, stack.getItem());
	}
}