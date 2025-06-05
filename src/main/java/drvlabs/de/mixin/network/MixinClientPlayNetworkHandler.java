//package drvlabs.de.mixin.client;

//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.network.ClientCommonNetworkHandler;
//import net.minecraft.client.network.ClientConnectionState;
//import net.minecraft.client.network.ClientPlayNetworkHandler;
//import net.minecraft.client.network.PlayerListEntry;
//import net.minecraft.client.world.ClientWorld;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.ItemEntity;
//import net.minecraft.entity.LivingEntity;
//import net.minecraft.network.ClientConnection;
//import net.minecraft.network.packet.s2c.play.*;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//import drvlabs.de.BTScreen;
//import drvlabs.de.utils.behavior.AutoDrop;

//import java.util.*;

//@Mixin(ClientPlayNetworkHandler.class)
//public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler {

//	@Shadow
//	private ClientWorld world;

//	@Shadow
//	@Final
//	private Map<UUID, PlayerListEntry> playerListEntries;

//	@Unique
//	private final Set<UUID> newPlayerEntries = new HashSet<>();

//	// @ModifyArg(method = "onTitle", at = @At(value = "INVOKE", target =
//	// "Lnet/minecraft/client/gui/hud/InGameHud;setTitle(Lnet/minecraft/text/Text;)V"))
//	// public Text onTitle(Text title) {
//	// EventTitle et = new EventTitle("TITLE", title);
//	// et.trigger();
//	// if (et.message == null || et.isCanceled()) {
//	// return null;
//	// } else {
//	// return et.message.getRaw();
//	// }
//	// }

//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"), method = "onItemPickupAnimation")
//	public void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet, CallbackInfo info) {
//		assert client.world != null;
//		final Entity e = client.world.getEntityById(packet.getEntityId());
//		LivingEntity c = (LivingEntity) client.world.getEntityById(packet.getCollectorEntityId());
//		if (c == null) {
//			c = client.player;
//		}
//		assert c != null;
//		if (c.equals(client.player) && e instanceof ItemEntity) {
//			BTScreen.LOGGER.info("Item picked up!!!!!!!!!!!!!!!!!!!!!!!!!!");
//			// AutoDrop.checkInventory(client.player, client);
//		}
//	}

//	// @Inject(at = @At("TAIL"), method = "onGameJoin")
//	// public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
//	// // TODO anti lock system
//	// }

//	// @Inject(method = "onEntityStatusEffect", at = @At(value = "INVOKE", target =
//	// "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
//	// shift = At.Shift.AFTER))
//	// public void onEntityStatusEffect(EntityStatusEffectS2CPacket packet,
//	// CallbackInfo info) {
//	// assert client.player != null;
//	// if (packet.getEntityId() == client.player.getId()) {
//	// StatusEffectInstance newEffect = new
//	// StatusEffectInstance(packet.getEffectId(), packet.getDuration(),
//	// packet.getAmplifier(), packet.isAmbient(), packet.shouldShowParticles(),
//	// packet.shouldShowIcon(), null);
//	// StatusEffectInstance oldEffect =
//	// client.player.getStatusEffect(packet.getEffectId());
//	// new EventStatusEffectUpdate(oldEffect == null ? null : new
//	// StatusEffectHelper(oldEffect),
//	// new StatusEffectHelper(newEffect), true).trigger();
//	// }
//	// }

//	// @Inject(method = "onRemoveEntityStatusEffect", at = @At(value = "INVOKE",
//	// target =
//	// "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
//	// shift = At.Shift.AFTER))
//	// public void onEntityStatusEffect(RemoveEntityStatusEffectS2CPacket packet,
//	// CallbackInfo info) {
//	// if (packet.getEntity(client.world) == client.player) {
//	// assert client.player != null;
//	// new EventStatusEffectUpdate(new
//	// StatusEffectHelper(client.player.getStatusEffect(packet.effect())), null,
//	// false)
//	// .trigger();
//	// }
//	// }

//	// @Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE",
//	// target =
//	// "Lnet/minecraft/screen/ScreenHandler;setCursorStack(Lnet/minecraft/item/ItemStack;)V"))
//	// public void onHeldSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet,
//	// CallbackInfo ci) {
//	// HandledScreen<?> screen;
//	// if (this.client.currentScreen instanceof HandledScreen<?>) {
//	// screen = (HandledScreen<?>) this.client.currentScreen;
//	// } else {
//	// screen = new InventoryScreen(this.client.player);
//	// }
//	// new EventSlotUpdate(screen, "HELD", -999,
//	// this.client.player.currentScreenHandler.getCursorStack(),
//	// packet.getStack()).trigger();
//	// }

//	// @Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE",
//	// target =
//	// "Lnet/minecraft/entity/player/PlayerInventory;setStack(ILnet/minecraft/item/ItemStack;)V"))
//	// public void onInventorySlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet,
//	// CallbackInfo ci) {
//	// assert client.player != null;
//	// new EventSlotUpdate(new InventoryScreen(client.player), "INVENTORY",
//	// packet.getSlot(),
//	// this.client.player.playerScreenHandler.getSlot(packet.getSlot()).getStack(),
//	// packet.getStack()).trigger();
//	// }

//	// @Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE",
//	// target =
//	// "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(IILnet/minecraft/item/ItemStack;)V"))
//	// public void onScreenSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet,
//	// CallbackInfo ci) {
//	// assert client.player != null;
//	// new EventSlotUpdate(new InventoryScreen(client.player), "INVENTORY",
//	// packet.getSlot(),
//	// this.client.player.playerScreenHandler.getSlot(packet.getSlot()).getStack(),
//	// packet.getStack()).trigger();
//	// }

//	// @Inject(method = "onInventory", at = @At("TAIL"))
//	// public void onInventoryUpdate(InventoryS2CPacket packet, CallbackInfo ci) {
//	// if (packet.getSyncId() == 0) {
//	// assert client.player != null;
//	// new EventContainerUpdate(new InventoryScreen(client.player)).trigger();
//	// } else {
//	// if (this.client.currentScreen instanceof HandledScreen<?>) {
//	// new EventContainerUpdate((HandledScreen<?>)
//	// this.client.currentScreen).trigger();
//	// }
//	// }

//}

package drvlabs.de.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import drvlabs.de.BTScreen;
import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.behavior.AutoDrop;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

	@Inject(method = "onItemPickupAnimation", at = @At("RETURN"))
	public void btscreen_onItemPickupAnimation(net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket packet,
			CallbackInfo ci) {
		if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
				&& Configs.Generic.AUTO_DROP.getBooleanValue()) {
			AutoDrop.checkInventory();
			BTScreen.LOGGER.info("Picked up item!!!!!!!!!!");
		}

	}

	@Inject(method = "onGameMessage", at = @At("RETURN"))
	private void btscreen_onGameMessage(net.minecraft.network.packet.s2c.play.GameMessageS2CPacket packet,
			CallbackInfo ci) {
		// DataStorage.getInstance().onChatMessage(packet.content());
	}

	@Inject(method = "onWorldTimeUpdate", at = @At("RETURN"))
	private void btscreen_onTimeUpdate(net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket packetIn,
			CallbackInfo ci) {
		// DataStorage.getInstance().onServerTimeUpdate(packetIn.time());
	}

	@Inject(method = "onGameJoin", at = @At("RETURN"))
	private void btscreen_onPostGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		// DataStorage.getInstance().setSimulationDistance(packet.simulationDistance());
	}

}