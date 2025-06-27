package de.drvlabs.btscreen.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.utils.BotStatus;
import de.drvlabs.btscreen.utils.CommandUtils;
import de.drvlabs.btscreen.utils.behavior.AutoDrop;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.effect.StatusEffects;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

	MinecraftClient mc = MinecraftClient.getInstance();

	@Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "TAIL", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(IILnet/minecraft/item/ItemStack;)V"))
	public void onScreenSlotUpdate(net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket packet,
			CallbackInfo ci) {
		if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
				&& Configs.Generic.AUTO_DROP.getBooleanValue()) {
			AutoDrop.checkInventory();
		}
	}

	// Executes every second this could be interesting for better waiter function
	// (TEST THIS)
	// @Inject(method = "onWorldTimeUpdate", at = @At("RETURN"))
	// private void
	// btscreen_onTimeUpdate(net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
	// packetIn,
	// CallbackInfo ci) {
	// // DataStorage.getInstance().onServerTimeUpdate(packetIn.time());
	// BTScreen.debugLog("Time updated");
	// }

	@Inject(method = "onRemoveEntityStatusEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
	public void onEntityStatusEffect(net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket packet,
			CallbackInfo info) {
		assert mc.player != null;

		if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
				&& Configs.Generic.AUTO_HASTE.getBooleanValue()) {
			if (packet.getEntity(mc.world) == mc.player) {

				if (packet.effect().matches(StatusEffects.HASTE::matchesKey)) {
					BTScreen.debugLog("Lost Haste");
					CommandUtils.pause(BotStatus.HASTING);
					CommandUtils.setHome(Configs.Generic.MINE_HOME.getStringValue());
					CommandUtils.tpTo(Configs.Generic.HASTE_HOME.getStringValue());
				}
			}
		}
	}

	@Inject(method = "onEntityStatusEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
	public void onEntityStatusEffect(net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket packet,
			CallbackInfo info) {
		assert mc.player != null;

		if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.HASTING
				&& Configs.Generic.AUTO_HASTE.getBooleanValue()) {
			if (packet.getEntityId() == mc.player.getId()) {

				if (packet.getEffectId().matches(StatusEffects.HASTE::matchesKey)) {
					BTScreen.debugLog("gained Haste");
					CommandUtils.tpTo(Configs.Generic.MINE_HOME.getStringValue());
					CommandUtils.resume();
				}
			}
		}
	}
}