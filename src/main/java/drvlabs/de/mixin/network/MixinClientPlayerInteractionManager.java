package drvlabs.de.mixin.network;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.behavior.AutoRepair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {
	@Shadow
	@Final
	private MinecraftClient client;
	@Shadow
	private int blockBreakingCooldown;

	@Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
	private void handleBreakingRestriction1(BlockPos pos, Direction side, CallbackInfoReturnable<Boolean> cir) {
		if (this.client.player != null && this.client.world != null) {
			if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
					&& Configs.Generic.AUTO_REPAIR.getBooleanValue()) {
				AutoRepair.tryStartingRepairIfNearlyBroken();
			}
		}
	}
}