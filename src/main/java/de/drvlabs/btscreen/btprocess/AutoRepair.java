package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_REPAIR;
import static de.drvlabs.btscreen.config.Configs.Generic.ITEM_DURABILITY_THRESHOLD;
import static de.drvlabs.btscreen.config.Configs.Generic.PERIODIC_ATTACK_INTERVAL;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

public class AutoRepair extends BTProcessHelper {
    private static boolean active = false;
    private static boolean initialized = false;
    private static int slot = -1;
    private int attackIntervalCounter = 0;
    private int swordSlot = -1;

    @Override
    public boolean isActive() {
        return isActive(AUTO_REPAIR) && active;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (!active) {
            return new PathingCommand(null, PathingCommandType.DEFER);
        }
        if (!initialized) {
            initialized = true;
            attackIntervalCounter = 0;
            swordSlot = getSwordSlotInHotbar();
            Teleport.requestTeleport(Teleport.Home.REPAIR);
        } else if (++attackIntervalCounter >= PERIODIC_ATTACK_INTERVAL.getIntegerValue()) {
            if (PlayerInventory.isValidHotbarIndex(slot)) {
                PlayerInventory inventory = Utils.MC.player.getInventory();
                int tmpSlot = inventory.getSelectedSlot();
                inventory.setSelectedSlot(swordSlot);
                Utils.MC.doAttack();
                inventory.setSelectedSlot(tmpSlot);
            } else {
                Utils.MC.doAttack();
            }
            attackIntervalCounter = 0;
        }
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    @Override
    public void onLostControl() {
        reset();
    }

    private static void reset() {
        active = false;
        initialized = false;
        slot = -1;
    }

    public static void checkRepairNeeded(int slot, ItemStack newStack, ItemStack oldStack) {
        if (!isActive(AUTO_REPAIR)) {
            return;
        }
        if (DataManager.getActive() && newStack.isDamageable() && areEqualIgnoreDamage(newStack, oldStack)) {
            if (!newStack.isDamaged()) {
                // stop
                reset();
                BTScreen.debugLog("Finished Repairing");
                return;
                // Utils.tpTo(Configs.Generic.MINE_HOME.getStringValue());
                // Utils.resume();
                // AutoDrop.checkInventory();
            }
            if (newStack.getMaxDamage() - newStack.getDamage() <= ITEM_DURABILITY_THRESHOLD.getIntegerValue()) {
                // start
                active = true;
                AutoRepair.slot = slot;
                return;
                // BTScreen.debugLog("Start Repairing");
                // Utils.pause(BotStatus.REPAIRING);
                // swordSlot = getSwordSlotInHotbar();
                // Utils.setHome(Configs.Generic.MINE_HOME.getStringValue());
                // Utils.tpTo(Configs.Generic.REPAIR_HOME.getStringValue());
            }
        }
    }

    private static boolean areEqualIgnoreDamage(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        } else if (!a.isEmpty() && !b.isEmpty() && ItemStack.areItemsEqual(a, b) && a.getCount() == b.getCount()) {
            ComponentMap bc = b.getComponents();
            return a.getComponents().stream()
                    .allMatch(e -> e.type() == DataComponentTypes.DAMAGE || e.equals(bc.get(e.type())));
        } else {
            return false;
        }
    }

    private static int getSwordSlotInHotbar() {
        PlayerInventory inventory = Utils.MC.player.getInventory();
        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
            if (inventory.getStack(i).isIn(ItemTags.SWORDS)) {
                return i;
            }
        }
        return -1;
    }
}
