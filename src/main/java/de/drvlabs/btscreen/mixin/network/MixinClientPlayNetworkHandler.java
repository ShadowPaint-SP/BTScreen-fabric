package de.drvlabs.btscreen.mixin.network;

import static de.drvlabs.btscreen.config.Configs.Generic.SAFETY_MIN_HEALTH;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.btprocess.AutoDrop;
import de.drvlabs.btscreen.btprocess.AutoEat;
import de.drvlabs.btscreen.btprocess.AutoHaste;
import de.drvlabs.btscreen.btprocess.AutoRepair;
import de.drvlabs.btscreen.btprocess.AutoTorch;
import de.drvlabs.btscreen.btprocess.Teleport;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow
    @Final
    private ClientLevel level;

    // Runs before the change is applied to the inventory
    @Inject(method = "handleContainerSetSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;setItem(IILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.BEFORE, ordinal = 0))
    private void onPlayerInventorySlotUpdatePre(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        Slot slot = Utils.MC.player.inventoryMenu.getSlot(packet.getSlot());
        ItemStack newStack = packet.getItem();
        AutoRepair.onPlayerInventorySlotUpdatePre(slot.getContainerSlot(), newStack, slot.getItem());
    }

    // Runs after the change is applied to the inventory
    @Inject(method = "handleContainerSetSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;setItem(IILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER, ordinal = 0))
    private void onPlayerInventorySlotUpdatePost(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        AutoDrop.checkInventory();
    }

    @Inject(method = "handleSetHealth", at = @At("TAIL"))
    private void onHealthUpdate(ClientboundSetHealthPacket packet, CallbackInfo ci) {
        AutoEat.onSetFoodLevel(packet.getFood());
        if (packet.getHealth() <= SAFETY_MIN_HEALTH.getIntegerValue() && Utils.isActive()) {
            BTScreen.chatMessage(Component.translatable(LangKeys.INFO + ".safety.lowHealth").withStyle(ChatFormatting.RED));
            Utils.cancel();
            Teleport.Home.SAFETY.tpToHome();
        }
    }

    @Inject(method = "handleUpdateMobEffect", at = @At("TAIL"))
    private void onEntityStatusEffect(ClientboundUpdateMobEffectPacket packet, CallbackInfo ci) {
        if (packet.getEntityId() != Utils.MC.player.getId()) {
            return;
        }
        AutoHaste.onEffect(packet);
    }

    @Inject(method = "handleRemoveMobEffect", at = @At("TAIL"))
    private void onRemoveEntityStatusEffect(ClientboundRemoveMobEffectPacket packet, CallbackInfo ci) {
        if (packet.getEntity(this.level) != Utils.MC.player) {
            return;
        }
        AutoHaste.onRemoveEffect(packet);
    }

    @Inject(method = "applyLightData", at = @At("TAIL"))
    private void readLightData(int x, int z, ClientboundLightUpdatePacketData data, boolean bl, CallbackInfo ci) {
        AutoTorch.onLightData(x, z);
    }
}
