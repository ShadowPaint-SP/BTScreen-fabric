package drvlabs.de.utils.behavior;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

import baritone.api.BaritoneAPI;
import drvlabs.de.BTScreen;
import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.CommandUtils;
import drvlabs.de.utils.IntervalStepper;
import drvlabs.de.utils.Waiter;

public class AutoDrop {
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static final int DROP_INTERVAL_TICKS = 5;

	private static List<Integer> workingSlots = new ArrayList<>();
	private static int currentDropIndex = 0;
	private static boolean dropWaitFinished = false;

	private static final IntervalStepper<Integer> dropStepper = new IntervalStepper<>((i) -> dropNextSlot());

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
				break;
			}
		}

		BTScreen.LOGGER.info("Has Free Slot: " + hasFreeSlot);

		if (!hasFreeSlot) {
			DataManager.setBotStatus(BotStatus.DROPPING);
			BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
			BTScreen.LOGGER.info("Inventory full, pausing bot");
			BTScreen.LOGGER.info("Teleport to drop off location");
			CommandUtils.debugHome(mc.player.getBlockPos().getX() + " " + mc.player.getBlockPos().getY() + " "
					+ mc.player.getBlockPos().getZ());
			CommandUtils.tpTo(Configs.Generic.DROP_HOME.getStringValue());

			currentDropIndex = 0; // start from the first slot
			Waiter.wait("drop_wait", 200, () -> {
				BTScreen.LOGGER.info("Wait over, starting to drop inventory");
				dropWaitFinished = true;
			});
		}
	}

	public static void updateMaxSlots() {
		List<Integer> emptySlots = new ArrayList<>();

		for (int i = 0; i <= 35; i++) {
			ItemStack stack = mc.player.getInventory().getStack(i);
			if (stack.isEmpty()) {
				emptySlots.add(i);
			}
		}

		BTScreen.LOGGER.info("Free Slots: " + emptySlots.size());
		BTScreen.LOGGER.info("Empty Slots: " + emptySlots.toString());

		AutoDrop.workingSlots = emptySlots;
	}

	private static void dropNextSlot() {
		if (currentDropIndex >= workingSlots.size()) {
			BTScreen.LOGGER.info("Finished dropping inventory");

			// Reset state
			currentDropIndex = 0;
			workingSlots.clear();

			// Resume mining
			CommandUtils.tpTo(Configs.Generic.MINE_HOME.getStringValue());
			DataManager.setBotStatus(BotStatus.IDLE);
			// Waiter.wait("resume_drop_wait", 100, () -> {
			DataManager.setBotStatus(BotStatus.MINING);
			BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
			// });

			return;
		}

		int slot = workingSlots.get(currentDropIndex);
		BTScreen.LOGGER.info("Dropping slot: " + slot);
		mc.interactionManager.clickSlot(0, slot, 1, SlotActionType.THROW, mc.player);

		currentDropIndex++;
	}

	// Call this from your tick handler
	public static void onTick() {
		if (DataManager.getBotStatus() == BotStatus.DROPPING) {
			if (!dropWaitFinished)
				return;
			dropStepper.tick(DROP_INTERVAL_TICKS, 0); // Context is unused
		} else {
			dropStepper.reset(); // In case the bot changes state
			dropWaitFinished = false;
		}
	}
}
