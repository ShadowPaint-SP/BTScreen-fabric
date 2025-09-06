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
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow
    @Final
    private ClientWorld world;

    // Runs before the change is applied to the inventory
    @Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(IILnet/minecraft/item/ItemStack;)V", shift = At.Shift.BEFORE, ordinal = 0))
    private void onPlayerInventorySlotUpdatePre(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        Slot slot = Utils.MC.player.playerScreenHandler.getSlot(packet.getSlot());
        ItemStack newStack = packet.getStack();
        AutoRepair.onPlayerInventorySlotUpdatePre(slot.getIndex(), newStack, slot.getStack());
    }

    // Runs after the change is applied to the inventory
    @Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(IILnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER, ordinal = 0))
    private void onPlayerInventorySlotUpdatePost(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        AutoDrop.checkInventory();
    }

    @Inject(method = "onHealthUpdate", at = @At("TAIL"))
    private void onHealthUpdate(HealthUpdateS2CPacket packet, CallbackInfo ci) {
        AutoEat.onSetFoodLevel(packet.getFood());
        if (packet.getHealth() <= SAFETY_MIN_HEALTH.getIntegerValue()) {
            BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".safety.lowHealth").formatted(Formatting.RED));
            Utils.cancel();
            Teleport.Home.SAFETY.tpToHome();
        }
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

    @Inject(method = "readLightData", at = @At("TAIL"))
    private void readLightData(int x, int z, LightData data, boolean bl, CallbackInfo ci) {
        AutoTorch.onLightData(x, z);
    }
}