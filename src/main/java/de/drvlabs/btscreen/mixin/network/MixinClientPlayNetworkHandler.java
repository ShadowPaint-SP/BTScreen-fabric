package de.drvlabs.btscreen.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.btprocess.AutoRepair;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.utils.BotStatus;
import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.behavior.AutoDrop;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    // Runs before the change is applied to the inventory
    @Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(IILnet/minecraft/item/ItemStack;)V", shift = At.Shift.BEFORE, ordinal = 0))
    public void onPlayerInventorySlotUpdatePre(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        int slot = packet.getSlot();
        ItemStack newStack = packet.getStack();
        ItemStack oldStack = Utils.MC.player.getInventory().getStack(slot);
        AutoRepair.checkRepairNeeded(slot, newStack, oldStack);
    }

    // Runs after the change is applied to the inventory
    @Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(IILnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER, ordinal = 0))
    public void onPlayerInventorySlotUpdatePost(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
                && Configs.Generic.AUTO_DROP.getBooleanValue()) {
            AutoDrop.checkInventory();
        }
    }

    @Inject(method = "onRemoveEntityStatusEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    public void onEntityStatusEffect(RemoveEntityStatusEffectS2CPacket packet, CallbackInfo ci) {
        if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
                && Configs.Generic.AUTO_HASTE.getBooleanValue()) {
            if (packet.getEntity(Utils.MC.world) == Utils.MC.player) {

                if (packet.effect().matches(StatusEffects.HASTE::matchesKey)) {
                    BTScreen.debugLog("Lost Haste");
                    Utils.pause(BotStatus.HASTING);
                    Utils.setHome(Configs.Generic.MINE_HOME.getStringValue());
                    Utils.tpTo(Configs.Generic.HASTE_HOME.getStringValue());
                }
            }
        }
    }

    @Inject(method = "onEntityStatusEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    public void onEntityStatusEffect(EntityStatusEffectS2CPacket packet, CallbackInfo ci) {
        if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.HASTING
                && Configs.Generic.AUTO_HASTE.getBooleanValue()) {
            if (packet.getEntityId() == Utils.MC.player.getId()) {

                if (packet.getEffectId().matches(StatusEffects.HASTE::matchesKey)) {
                    BTScreen.debugLog("gained Haste");
                    Utils.tpTo(Configs.Generic.MINE_HOME.getStringValue());
                    Utils.resume();
                }
            }
        }
    }
}