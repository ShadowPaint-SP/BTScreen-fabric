package drvlabs.de.utils.behavior;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

import java.util.ArrayList;
import java.util.List;

import baritone.api.BaritoneAPI;
import drvlabs.de.BTScreen;
import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.CommandUtils;
import drvlabs.de.utils.Waiter;

public class AutoDrop {
	private static final MinecraftClient mc = MinecraftClient.getInstance();

	private static List<Integer> workingSlots = new ArrayList<>();

	public static void checkInventory() {
		PlayerInventory inventory = mc.player.getInventory();
		boolean hasFreeSlot = false;
		if (DataManager.getBotStatus() != BotStatus.MINING)
			return;
		if (workingSlots.isEmpty())
			return;

		for (int slot : workingSlots) {
			if (inventory.getStack(slot).isEmpty()) {
				hasFreeSlot = true;
				break;
			}
		}

		if (!hasFreeSlot) {
			DataManager.setBotStatus(BotStatus.DROPPING);
			BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
			BTScreen.debugLog("Inventory full, pausing bot");
			CommandUtils.setHome(Configs.Generic.MINE_HOME.getStringValue());
			CommandUtils.tpTo(Configs.Generic.DROP_HOME.getStringValue());
			Waiter.wait("drop_wait", 200, () -> {
				Waiter.cancel("drop_wait");
				mc.setScreen(new InventoryScreen(mc.player));
				dropInventory();
				// dropWaitFinished = true;
				mc.currentScreen.close();
				CommandUtils.tpTo(Configs.Generic.MINE_HOME.getStringValue());
				Waiter.wait("resume_drop_wait", 100, () -> {
					Waiter.cancel("resume_drop_wait");
					DataManager.setBotStatus(BotStatus.MINING);
					BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
					checkInventory(); // To make sure the inventory has space now
				});
			});
		}
	}

	public static void updateMaxSlots() {
		List<Integer> emptySlots = new ArrayList<>();

		for (int i = 9; i <= 35; i++) {
			ItemStack stack = mc.player.getInventory().getStack(i);
			if (stack.isEmpty()) {
				emptySlots.add(i);
			}
		}

		BTScreen.debugLog("Empty Slots: " + emptySlots.toString());
		AutoDrop.workingSlots = emptySlots;
	}

	public static void dropInventory() {
		for (Integer slot : workingSlots) {
			// if (slot <= 8) {
			// mc.player.getInventory().setSelectedSlot(slot);
			// mc.player.dropSelectedItem(true);
			// continue;
			// }
			if (isNotAllowedToDrop(slot)) {
				continue;
			}
			BTScreen.debugLog("Dropping slot: " + slot);
			mc.interactionManager.clickSlot(0, slot, 1, SlotActionType.THROW, mc.player);
		}
	}

	public static boolean isNotAllowedToDrop(int slot) {
		for (String item : Configs.Lists.INV_PRESERVE_ITEM_BLACKLIST.getStrings()) {
			if (mc.player.getInventory().getStack(slot).getItem().toString().equals(item))
				return true;
		}
		return false;
	}
}