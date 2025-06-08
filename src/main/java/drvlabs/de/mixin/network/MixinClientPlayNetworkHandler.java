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
import drvlabs.de.utils.Waiter;
import drvlabs.de.utils.behavior.AutoDrop;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
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

	@Inject(method = "onGameMessage", at = @At("RETURN"))
	private void btscreen_onGameMessage(net.minecraft.network.packet.s2c.play.GameMessageS2CPacket packet,
			CallbackInfo ci) {
		// DataStorage.getInstance().onChatMessage(packet.content());
	}

	// Executes every second this could be interesting for better waiter function
	// (TEST THIS)
	// @Inject(method = "onWorldTimeUpdate", at = @At("RETURN"))
	// private void
	// btscreen_onTimeUpdate(net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
	// packetIn,
	// CallbackInfo ci) {
	// // DataStorage.getInstance().onServerTimeUpdate(packetIn.time());
	// BTScreen.LOGGER.info("Time updated");
	// }

	/*
	 * Initialize the selected preset so if u use this for the first time or the
	 * settings were changed previously they are fixed again.
	 */
	@Inject(method = "onGameJoin", at = @At("TAIL"))
	private void btscreen_onPostGameJoin(net.minecraft.network.packet.s2c.play.GameJoinS2CPacket packet,
			CallbackInfo ci) {
		ServerInfo server = mc.getCurrentServerEntry();
		if (server == null) {
			BTScreen.LOGGER.info("Singleplayer");
			return;
		}
		BTScreen.LOGGER.info("Connected to: " + server.address);
		if (server.address.contains("rsdclan.de") && Configs.Generic.RSD_SETTINGS.getBooleanValue()) {
			Waiter.wait("rsd", 100, () -> {
				CommandUtils.sendCommand("cnolock");
				CommandUtils.sendCommand("adblock");
				Waiter.cancel("rsd");
			});
			// TODO evtl make this a list u can configure
		}
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
					CommandUtils.tpTo(Configs.Generic.MINE_HOME.getStringValue());
					DataManager.setBotStatus(BotStatus.MINING);
					CommandUtils.execute("resume");
				}
			}
		}
	}
	// @ModifyArg(method = "onTitle", at = @At(value = "INVOKE", target =
	// "Lnet/minecraft/client/gui/hud/InGameHud;setTitle(Lnet/minecraft/text/Text;)V"))
	// public Text onTitle(Text title) {
	// EventTitle et = new EventTitle("TITLE", title);
	// et.trigger();
	// if (et.message == null || et.isCanceled()) {
	// return null;
	// } else {
	// return et.message.getRaw();
	// }
	// }
}