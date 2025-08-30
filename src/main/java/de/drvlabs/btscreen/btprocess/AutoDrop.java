package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_DROP;
import static de.drvlabs.btscreen.config.Configs.Lists.INV_PRESERVE_ITEM_BLACKLIST;

import java.util.HashSet;
import java.util.Set;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AutoDrop extends BTProcessWithInitializer {
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
        BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".autoDrop.started"));
    }

    @Override
    protected PathingCommand onTick() {
        PlayerInventory inventory = Utils.MC.player.getInventory();
        active = false;
        for (Integer slot : workingSlots) {
            if (shouldDrop(inventory.getStack(slot))) {
                Utils.MC.interactionManager.clickSlot(0, slot, 1, SlotActionType.THROW, Utils.MC.player);
                active = true;
                break;
            }
        }
        if (!active) {
            checkInventory();
        }
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    @Override
    protected void onReset() {
        active = false;
    }

    @Override
    public double priority() {
        return super.priority() + 0.01;
    }

    static void teleportIntegration() {
        if (isActive(AUTO_DROP) && !workingSlots.isEmpty()) {
            active = true;
        }
    }

    public static void checkInventory() {
        hasFreeSlot = Utils.MC.player.getInventory().getEmptySlot() != PlayerInventory.NOT_FOUND;
    }

    public static void updateMaxSlots() {
        PlayerInventory inventory = Utils.MC.player.getInventory();
        workingSlots.clear();
        for (int i = PlayerInventory.HOTBAR_SIZE; i < PlayerInventory.MAIN_SIZE; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                workingSlots.add(i);
            }
        }
        BTScreen.debugLog("Empty Slots: " + workingSlots);
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
        Identifier itemIdentifier = Registries.ITEM.getId(itemStack.getItem());
        return itemIdentifier != null && !blacklist.contains(itemIdentifier);
    }
}