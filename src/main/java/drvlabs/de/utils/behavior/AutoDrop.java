package drvlabs.de.utils.behavior;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

import baritone.api.BaritoneAPI;
import drvlabs.de.BTScreen;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.CommandUtils;

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
		for (Integer slot : workingSlots) {
			// TODO add wait()
			BTScreen.LOGGER.info("Dropping inventory: ");
			mc.interactionManager.clickSlot(0, slot, 1, SlotActionType.THROW, mc.player);

		}

	}

}
