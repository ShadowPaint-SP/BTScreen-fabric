package drvlabs.de.utils.behavior;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

import baritone.api.BaritoneAPI;
import drvlabs.de.BTScreen;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.CommandUtils;

import java.util.LinkedList;
import java.util.Queue;

public class AutoDrop {
	private static List<Integer> workingSlots;
	private static MinecraftClient mc = MinecraftClient.getInstance();

	public static void checkInventory() {

		PlayerInventory inventory = mc.player.getInventory();
		boolean hasFreeSlot = false;
		BTScreen.LOGGER.info("Checking Inventory");
		if (DataManager.getBotStatus() != BotStatus.MINING) {
			return;
		}
		if (workingSlots.isEmpty()) {
			return;
		}
		// Check if there's at least one empty slot in the inventory
		for (int slot : workingSlots) {
			ItemStack stack = inventory.getStack(slot);
			if (stack.isEmpty()) {
				hasFreeSlot = true;
				break;
			}

		}
		BTScreen.LOGGER.info("Has Free Slot: " + hasFreeSlot);

		if (!hasFreeSlot) {
			DataManager.setBotStatus(BotStatus.DROPPING);
			BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
			BTScreen.LOGGER.info("Inventory full, pausing bot");
			BTScreen.LOGGER.info("Teleport to drop off location");
			CommandUtils.sendCommand("gamemode creative");
			// TODO: needs a wait function

			dropInventory();

			BTScreen.LOGGER.info("Teleporting back to mine");
			BTScreen.LOGGER.info("Resuming bot");
			DataManager.setBotStatus(BotStatus.MINING);
			BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
		}
	}

	public static void updateMaxSlots() {

		List<Integer> emptySlots = new ArrayList<>();

		for (int i = 0; i <= 35; i++) {
			ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
			if (stack.isEmpty()) {
				emptySlots.add(i);
			}
		}
		BTScreen.LOGGER.info("Free Slots: " + emptySlots.size());
		BTScreen.LOGGER.info("Empty Slots: " + emptySlots.toString());
		DataManager.setBotStatus(BotStatus.MINING);

		AutoDrop.workingSlots = emptySlots;
	}

	public static void dropInventory() {
		AutoDropScheduler.startDropping(workingSlots);
		BTScreen.LOGGER.info("Dropping inventory: ");
		for (Integer slot : workingSlots) {
			// TODO add wait()
			// mc.interactionManager.clickSlot(0, slot, 1, SlotActionType.THROW, mc.player);

		}
	}

	public static class AutoDropScheduler {
		private static Queue<Integer> dropQueue = new LinkedList<>();
		private static int tickDelay = 4; // Delay between drops
		private static int tickCounter = 0;
		private static boolean active = false;

		public static void startDropping(List<Integer> slotsToDrop) {
			if (active)
				return;

			dropQueue.clear();
			dropQueue.addAll(slotsToDrop);
			tickCounter = 0;
			active = true;
		}

		public static void registerTickHandler() {
			ClientTickEvents.END_CLIENT_TICK.register(client -> {
				if (!active || dropQueue.isEmpty())
					return;

				if (tickCounter > 0) {
					tickCounter--;
					return;
				}

				int slot = dropQueue.poll();
				client.interactionManager.clickSlot(0, slot, 1, SlotActionType.THROW, client.player);
				BTScreen.LOGGER.info("Dropped slot: " + slot);

				tickCounter = tickDelay;

				if (dropQueue.isEmpty()) {
					active = false;
					BTScreen.LOGGER.info("Finished dropping inventory.");
					// Resume bot if needed here
					DataManager.setBotStatus(BotStatus.MINING);
					BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
				}
			});
		}
	}

}
