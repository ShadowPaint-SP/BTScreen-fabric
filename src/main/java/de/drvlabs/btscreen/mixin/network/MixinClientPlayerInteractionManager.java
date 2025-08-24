package de.drvlabs.btscreen.mixin.network;

import static de.drvlabs.btscreen.config.Configs.Generic.BLOCK_BREAK_COOLDOWN;
import static de.drvlabs.btscreen.config.Configs.Generic.NO_INSTA_BREAK;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.utils.BotStatus;
import net.minecraft.client.network.ClientPlayerInteractionManager;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {
	@Shadow
	private int blockBreakingCooldown;

	@Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcodes.PUTFIELD, ordinal = 2))
	private void survivalBreakDelayChange(ClientPlayerInteractionManager interactionManager, int value) {
		if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING) {
			blockBreakingCooldown = BLOCK_BREAK_COOLDOWN.getIntegerValue();
		} else {
			blockBreakingCooldown = value;
		}
	}

	@ModifyExpressionValue(method = "method_41930", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
	private float modifyBlockBreakingDelta(float original) {
		if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
				&& NO_INSTA_BREAK.getBooleanValue() && original >= 1) {
			blockBreakingCooldown = 5;
			return 0;
		}
		return original;
	}
}