package de.drvlabs.btscreen.mixin.network;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.drvlabs.btscreen.btprocess.AutoDrop;
import de.drvlabs.btscreen.btprocess.AutoHaste;
import de.drvlabs.btscreen.btprocess.AutoRepair;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow
    @Final
    private ClientWorld world;

    // Runs before the change is applied to the inventory
    @Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(IILnet/minecraft/item/ItemStack;)V", shift = At.Shift.BEFORE, ordinal = 0))
    private void onPlayerInventorySlotUpdatePre(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        int slot = packet.getSlot();
        ItemStack newStack = packet.getStack();
        ItemStack oldStack = Utils.MC.player.getInventory().getStack(slot);
        AutoRepair.onPlayerInventorySlotUpdatePre(slot, newStack, oldStack);
    }

    // Runs after the change is applied to the inventory
    @Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(IILnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER, ordinal = 0))
    private void onPlayerInventorySlotUpdatePost(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        AutoDrop.checkInventory();
    }

    @Inject(method = "onEntityStatusEffect", at = @At("TAIL"))
    private void onEntityStatusEffect(EntityStatusEffectS2CPacket packet, CallbackInfo ci) {
        if (packet.getEntityId() != Utils.MC.player.getId()) {
            return;
        }
        AutoHaste.onEffect(packet);
    }

    @Inject(method = "onRemoveEntityStatusEffect", at = @At("TAIL"))
    private void onRemoveEntityStatusEffect(RemoveEntityStatusEffectS2CPacket packet, CallbackInfo ci) {
        if (packet.getEntity(this.world) != Utils.MC.player) {
            return;
        }
        AutoHaste.onRemoveEffect(packet);
    }
}