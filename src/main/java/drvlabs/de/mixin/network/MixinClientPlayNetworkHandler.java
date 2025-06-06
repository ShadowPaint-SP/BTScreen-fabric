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

//	// @Inject(at = @At("TAIL"), method = "onGameJoin")
//	// public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
//	// // TODO anti lock system
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
import drvlabs.de.utils.CommandUtils;
import drvlabs.de.utils.behavior.AutoDrop;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.effect.StatusEffects;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
	MinecraftClient mc = MinecraftClient.getInstance();

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
	private void btscreen_onPostGameJoin(net.minecraft.network.packet.s2c.play.GameJoinS2CPacket packet,
			CallbackInfo ci) {
		// DataStorage.getInstance().setSimulationDistance(packet.simulationDistance());
	}

	@Inject(method = "onRemoveEntityStatusEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
	public void onEntityStatusEffect(net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket packet,
			CallbackInfo info) {
		assert mc.player != null;

		if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
				&& Configs.Generic.AUTO_HASTE.getBooleanValue()) {
			if (packet.getEntity(mc.world) == mc.player) {

				BTScreen.LOGGER.info("Removing status effect: " + mc.player.getStatusEffect(packet.effect()));
				if (packet.effect().matches(StatusEffects.HASTE::matchesKey)) {
					CommandUtils.execute("pause");
					DataManager.setBotStatus(BotStatus.HASTING);
					CommandUtils.debugHome(mc.player.getBlockPos().getX() + " " + mc.player.getBlockPos().getY() + " "
							+ mc.player.getBlockPos().getZ());
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
					BTScreen.LOGGER.info("HASTE given");
					CommandUtils.tpTo(Configs.Generic.MINE_HOME.getStringValue()); // TODO home system
					DataManager.setBotStatus(BotStatus.MINING);
					CommandUtils.execute("resume");
				}
			}
		}
	}
}