package drvlabs.de.mixin.network;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

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

	/*
	 * Handles checking the health of the tool when starting to break a block.
	 */
	@Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
	private void handleBreakingRestriction1(BlockPos pos, Direction side, CallbackInfoReturnable<Boolean> cir) {
		if (this.client.player != null && this.client.world != null) {
			if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
					&& Configs.Generic.AUTO_REPAIR.getBooleanValue()) {
				AutoRepair.tryStartingRepairIfNearlyBroken();
			}
		}
	}

	@Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcodes.PUTFIELD, ordinal = 2))
	private void survivalBreakDelayChange(ClientPlayerInteractionManager interactionManager, int value) {
		if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING) {
			blockBreakingCooldown = Configs.Generic.BLOCK_BREAK_COOLDOWN.getIntegerValue();
		} else {
			blockBreakingCooldown = value;
		}
	}

	@ModifyExpressionValue(method = "method_41930", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
	private float modifyBlockBreakingDelta(float original) {
		if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
				&& Configs.Generic.NO_INSTA_BREAK.getBooleanValue() && original >= 1) {
			blockBreakingCooldown = 5;
			return 0;
		}
		return original;
	}
}