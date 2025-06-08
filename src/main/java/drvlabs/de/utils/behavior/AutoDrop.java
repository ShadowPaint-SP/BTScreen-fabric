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

	// private static final IntervalStepper<Integer> dropStepper = new
	// IntervalStepper<>((i) -> dropNextSlot());

	public static void checkInventory() {
		PlayerInventory inventory = mc.player.getInventory();
		boolean hasFreeSlot = false;

		BTScreen.LOGGER.info("Checking Inventory");

		if (DataManager.getBotStatus() != BotStatus.MINING)
			return;
		if (workingSlots.isEmpty())
			return;

		for (int slot : workingSlots) {
			if (inventory.getStack(slot).isEmpty()) {
				hasFreeSlot = true;
				BTScreen.LOGGER.info("Found free slot: " + slot);
				break;
			}
		}

		BTScreen.LOGGER.info("Has Free Slot: " + hasFreeSlot);

		if (!hasFreeSlot) {
			DataManager.setBotStatus(BotStatus.DROPPING);
			BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
			BTScreen.LOGGER.info("Inventory full, pausing bot");
			BTScreen.LOGGER.info("Teleport to drop off location");
			CommandUtils.setHome(Configs.Generic.MINE_HOME.getStringValue());
			CommandUtils.tpTo(Configs.Generic.DROP_HOME.getStringValue());
			Waiter.wait("drop_wait", 200, () -> {
				Waiter.cancel("drop_wait");
				BTScreen.LOGGER.info("Wait over, starting to drop inventory");
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

		BTScreen.LOGGER.info("Free Slots: " + emptySlots.size());
		BTScreen.LOGGER.info("Empty Slots: " + emptySlots.toString());

		AutoDrop.workingSlots = emptySlots;
	}

	// private static void dropNextSlot() {
	// if (currentDropIndex >= workingSlots.size()) {
	// BTScreen.LOGGER.info("Finished dropping inventory");
	// // Reset state
	// currentDropIndex = 0;
	// dropWaitFinished = false;
	// // Resume mining
	// CommandUtils.tpTo(Configs.Generic.MINE_HOME.getStringValue());
	// Waiter.wait("resume_drop_wait", 100, () -> {
	// DataManager.setBotStatus(BotStatus.MINING);
	// BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
	// Waiter.cancel("resume_drop_wait");
	// });
	// return;
	// }
	// int slot = workingSlots.get(currentDropIndex);
	// BTScreen.LOGGER.info("Dropping slot: " + slot);
	// BTScreen.LOGGER.info("SyncID: " + syncId);
	// // if (slot <= 8) {
	// // mc.player.getInventory().setSelectedSlot(slot);
	// // mc.player.dropSelectedItem(true);
	// // mc.player.dropSelectedItem(false);
	// // } else {
	// // }
	// mc.interactionManager.clickSlot(syncId, slot, 1, SlotActionType.THROW,
	// mc.player);
	// currentDropIndex++;
	// }

	/**
	 * equivalent to hitting the numbers or f for swapping slots to hotbar
	 *
	 * @param slot
	 * @param hotbarSlot 0-8 or 40 for offhand
	 * @return
	 * @since 1.6.5 [citation needed]
	 */
	// public static void swapHotbar(int slot, int hotbarSlot) {
	// if (hotbarSlot != 40) {
	// if (hotbarSlot < 0 || hotbarSlot > 8) {
	// throw new IllegalArgumentException("hotbarSlot must be between 0 and 8 or 40
	// for offhand.");
	// }
	// }
	// mc.interactionManager.clickSlot(syncId, slot, hotbarSlot,
	// SlotActionType.SWAP, mc.player);
	// }

	// public static void onTick() {
	// if (DataManager.getBotStatus() == BotStatus.DROPPING) {
	// if (!dropWaitFinished)
	// return;
	// dropStepper.tick(DROP_INTERVAL_TICKS, 0); // Context is unused
	// } else {
	// BTScreen.LOGGER.info("THIS NEVER HAPPENS AM I RIGHT");
	// dropStepper.reset(); // In case the bot changes state
	// dropWaitFinished = false;
	// }
	// }

	public static void dropInventory() {
		for (Integer slot : workingSlots) {
			// if (slot <= 8) {
			// mc.player.getInventory().setSelectedSlot(slot);
			// mc.player.dropSelectedItem(true);
			// continue;
			// }
			BTScreen.LOGGER.info("Dropping inventory: ");
			mc.interactionManager.clickSlot(0, slot, 1, SlotActionType.THROW, mc.player);
		}
	}
}