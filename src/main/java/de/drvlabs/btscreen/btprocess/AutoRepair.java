package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_REPAIR;
import static de.drvlabs.btscreen.config.Configs.Generic.ITEM_DURABILITY_THRESHOLD;
import static de.drvlabs.btscreen.config.Configs.Generic.PERIODIC_ATTACK_INTERVAL;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

public class AutoRepair extends BTProcessWithInitializer {
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
        BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".autoRepair.started"));
    }

    @Override
    protected PathingCommand onTick() {
        if (++attackIntervalCounter >= PERIODIC_ATTACK_INTERVAL.getIntegerValue()) {
            PlayerInventory inventory = Utils.MC.player.getInventory();
            int tmpSlot = inventory.getSelectedSlot();
            if (PlayerInventory.isValidHotbarIndex(swordSlot)) {
                inventory.setSelectedSlot(swordSlot);
            }
            Utils.MC.doAttack();
            if (PlayerInventory.isValidHotbarIndex(slot)) {
                inventory.setSelectedSlot(slot);
            } else {
                inventory.setSelectedSlot(tmpSlot);
            }
            attackIntervalCounter = 0;
        }
        return requestPause();
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
        if (newStack.isDamageable() && areEqualIgnoreComponents(newStack, oldStack)) {
            if (!newStack.isDamaged() && AutoRepair.slot == slot) {
                // stop
                AutoRepair.slot = -1;
                BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".autoRepair.finished"));
                return;
            }
            if ((newStack.getMaxDamage() - newStack.getDamage()) <= ITEM_DURABILITY_THRESHOLD.getIntegerValue()) {
                // start
                AutoRepair.slot = slot;
                return;
            }
        }
    }

    private static boolean areEqualIgnoreComponents(ItemStack a, ItemStack b) {
        return ItemStack.areItemsEqual(a, b) && a.getCount() == b.getCount();
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
