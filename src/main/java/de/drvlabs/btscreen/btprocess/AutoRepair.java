package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_REPAIR;
import static de.drvlabs.btscreen.config.Configs.Generic.ITEM_DURABILITY_THRESHOLD;
import static de.drvlabs.btscreen.config.Configs.Generic.PERIODIC_ATTACK_INTERVAL;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class AutoRepair extends BTProcessWithInitializer {
    public static final AutoRepair INSTANCE = new AutoRepair();
    private static final float SWEEP_ATTACK_THRESHOLD = 0.9F;

    private AutoRepair() {
    }

    private static int slot = -1;
    private int attackIntervalCounter = 0;
    private int swordSlot = -1;

    @Override
    public boolean isActive() {
        return isActive(AUTO_REPAIR) && slot != -1;
    }

    @Override
    protected void onInitialize() {
        swordSlot = getSwordSlotInHotbar();
        Teleport.requestTeleport(Teleport.Home.REPAIR);
    }

    @Override
    protected PathingCommand onTick() {
        // -3 registers the sword switch, -2 waits and attacks, -1 restores the tool.
        if (attackIntervalCounter < 0) {
            if (attackIntervalCounter == -2) {
                if (Utils.MC.player.getAttackStrengthScale(0.5F) <= SWEEP_ATTACK_THRESHOLD) {
                    return REQUEST_PAUSE;
                }
                Utils.MC.startAttack();
            } else if (attackIntervalCounter == -1 && Inventory.isHotbarSlot(slot)) {
                Utils.MC.player.getInventory().setSelectedSlot(slot);
            }
            attackIntervalCounter++;
            return REQUEST_PAUSE;
        }

        if (++attackIntervalCounter >= PERIODIC_ATTACK_INTERVAL.getIntegerValue()) {
            Inventory inventory = Utils.MC.player.getInventory();
            boolean hasSword = Inventory.isHotbarSlot(swordSlot);
            if (hasSword) {
                inventory.setSelectedSlot(swordSlot);
                attackIntervalCounter = -3;
            } else {
                Utils.MC.startAttack();
                attackIntervalCounter = 0;
            }
        }
        return REQUEST_PAUSE;
    }

    @Override
    protected void onReset() {
        slot = -1;
        attackIntervalCounter = 0;
        swordSlot = -1;
    }

    public static void onPlayerInventorySlotUpdatePre(int slot, ItemStack newStack, ItemStack oldStack) {
        if (!isActive(AUTO_REPAIR)) {
            return;
        }
        if (newStack.isDamageableItem() && areEqualIgnoreComponents(newStack, oldStack)) {
            if (!newStack.isDamaged() && AutoRepair.slot == slot) {
                // stop
                AutoRepair.slot = -1;
                return;
            }
            if ((newStack.getMaxDamage() - newStack.getDamageValue()) <= ITEM_DURABILITY_THRESHOLD.getIntegerValue()) {
                // start
                AutoRepair.slot = slot;
                return;
            }
        }
    }

    private static boolean areEqualIgnoreComponents(ItemStack a, ItemStack b) {
        return ItemStack.isSameItem(a, b) && a.getCount() == b.getCount();
    }

    private static int getSwordSlotInHotbar() {
        Inventory inventory = Utils.MC.player.getInventory();
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            if (inventory.getItem(i).is(ItemTags.SWORDS)) {
                return i;
            }
        }
        return -1;
    }
}
