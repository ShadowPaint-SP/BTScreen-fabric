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

	// @Inject(method = "attackBlock", slice = @Slice(from = @At(value = "FIELD",
	// ordinal = 0, target =
	// "Lnet/minecraft/client/network/ClientPlayerInteractionManager;breakingBlock:Z")),
	// at = @At(value = "INVOKE", target =
	// "Lnet/minecraft/client/world/ClientWorld;getBlockState("
	// +
	// "Lnet/minecraft/util/math/BlockPos;" +
	// ")Lnet/minecraft/block/BlockState;", ordinal = 0), cancellable = true)
	// private void onClickBlockPre(BlockPos pos, Direction face,
	// CallbackInfoReturnable<Boolean> cir) {
	// if (this.client.player != null && this.client.world != null) {
	// BTScreen.LOGGER.info("onClickBlockPre");
	// if (Configs.Generic.AUTO_REPAIR.getBooleanValue()) {
	// // InventoryUtils.trySwitchToEffectiveTool(pos);
	// cir.setReturnValue(false);
	// }
	// }
	// }

	@Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
	private void handleBreakingRestriction1(BlockPos pos, Direction side, CallbackInfoReturnable<Boolean> cir) {
		if (this.client.player != null && this.client.world != null) {
			if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
					&& Configs.Generic.AUTO_REPAIR.getBooleanValue()) {
				AutoRepair.tryStartingRepairIfNearlyBroken();
			}
		}
	}

	// @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable
	// = true) // MCP: onPlayerDamageBlock
	// private void handleBreakingRestriction2(BlockPos pos, Direction side,
	// CallbackInfoReturnable<Boolean> cir) {
	// BTScreen.LOGGER.info("handleBreakingRestriction2");
	// if (Configs.Disable.DISABLE_BLOCK_BREAK_COOLDOWN.getBooleanValue())
	//// && this.client.player.isCreative() == false)
	// {
	// this.blockBreakingCooldown = 0;
	// }

	// if (CameraUtils.shouldPreventPlayerInputs() ||
	// PlacementTweaks.isPositionAllowedByBreakingRestriction(pos, side) == false) {
	// cir.setReturnValue(true);
	// } else {
	// InventoryUtils.trySwapCurrentToolIfNearlyBroken();
	// }
	// }

	// @Inject(method = "hasLimitedAttackSpeed", at = @At("HEAD"), cancellable =
	// true)
	// private void overrideLimitedAttackSpeed(CallbackInfoReturnable<Boolean> cir)
	// {
	// if (FeatureToggle.TWEAK_FAST_LEFT_CLICK.getBooleanValue())
	// {
	// cir.setReturnValue(false);
	// }
	// }
}