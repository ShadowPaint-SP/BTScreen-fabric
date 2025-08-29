package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_REPAIR;
import static de.drvlabs.btscreen.config.Configs.Generic.ITEM_DURABILITY_THRESHOLD;
import static de.drvlabs.btscreen.config.Configs.Generic.PERIODIC_ATTACK_INTERVAL;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

public class AutoRepair extends BTProcessWithInitializer {
    private static boolean active = false;
    private static int slot = -1;
    private int attackIntervalCounter = 0;
    private int swordSlot = -1;

    @Override
    public boolean isActive() {
        return isActive(AUTO_REPAIR) && active;
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
            int tmpSlot = inventory.selectedSlot;
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
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
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
        if (newStack.isDamageable() && areEqualIgnoreDamage(newStack, oldStack)) {
            if (!newStack.isDamaged()) {
                // stop
                active = false;
                BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".autoRepair.finished"));
                return;
            }
            if (newStack.getMaxDamage() - newStack.getDamage() <= ITEM_DURABILITY_THRESHOLD.getIntegerValue()) {
                // start
                active = true;
                AutoRepair.slot = slot;
                return;
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
