package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_DROP;
import static de.drvlabs.btscreen.config.Configs.Generic.MIN_DROP_SLOTS;
import static de.drvlabs.btscreen.config.Configs.Lists.INV_PRESERVE_ITEM_BLACKLIST;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.event.BaritoneEvents;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class AutoDrop extends BTProcessWithInitializer implements BaritoneEvents.Started {
    public static final AutoDrop INSTANCE = new AutoDrop();

    private AutoDrop() {
    }

    private static final Set<Identifier> blacklist = new HashSet<>();
    private static final Set<Integer> workingSlots = new HashSet<>();
    private static boolean hasFreeSlot = false;
    private static boolean active = false;

    @Override
    public boolean isActive() {
        return isActive(AUTO_DROP) && !workingSlots.isEmpty() && (!hasFreeSlot || active);
    }

    @Override
    protected void onInitialize() {
        active = true;
        Teleport.requestTeleport(Teleport.Home.DROP);
    }

    @Override
    protected PathingCommand onTick() {
        Inventory inventory = Utils.MC.player.getInventory();
        active = false;
        for (Integer slot : workingSlots) {
            if (shouldDrop(inventory.getItem(slot))) {
                Utils.MC.gameMode.handleContainerInput(0, slot, 1, ContainerInput.THROW, Utils.MC.player);
                active = true;
                break;
            }
        }
        if (!active) {
            checkInventory();
        }
        return REQUEST_PAUSE;
    }

    @Override
    protected void onReset() {
        active = false;
    }

    @Override
    public double priority() {
        if (Teleport.Home.DROP.isSame(Teleport.getLastHome())) {
            return super.priority() + 0.01;
        }
        return super.priority();
    }

    {
        BaritoneEvents.STARTED.register(this);
    }

    @Override
    public void baritoneStarted() {
        updateMaxSlots();
    }

    static void teleportIntegration() {
        if (isActive(AUTO_DROP) && !workingSlots.isEmpty()) {
            active = true;
        }
    }

    public static void checkInventory() {
        hasFreeSlot = Utils.MC.player.getInventory().getFreeSlot() != Inventory.NOT_FOUND_INDEX;
    }

    private static void updateMaxSlots() {
        Inventory inventory = Utils.MC.player.getInventory();
        workingSlots.clear();
        for (int i = Inventory.SELECTION_SIZE; i < Inventory.INVENTORY_SIZE; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                workingSlots.add(i);
            }
        }
        BTScreen.debugLog("Empty Slots: " + workingSlots);
        if (AUTO_DROP.getBooleanValue() && workingSlots.size() < MIN_DROP_SLOTS.getIntegerValue()) {
            AUTO_DROP.setBooleanValue(false);
            workingSlots.clear();
            BTScreen.chatMessage(Component.translatable(LangKeys.INFO + ".autoDrop.tooFewSlots")
                    .withStyle(ChatFormatting.RED));
        }
        AutoDrop.checkInventory();
    }

    static {
        INV_PRESERVE_ITEM_BLACKLIST.setValueChangeCallback(config -> updateBlacklist());
        updateBlacklist();
    }

    private static void updateBlacklist() {
        blacklist.clear();
        for (String string : INV_PRESERVE_ITEM_BLACKLIST.getStrings()) {
            blacklist.add(Identifier.tryParse(string));
        }
    }

    private static boolean shouldDrop(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        Identifier itemIdentifier = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        return itemIdentifier != null && !blacklist.contains(itemIdentifier);
    }
}
