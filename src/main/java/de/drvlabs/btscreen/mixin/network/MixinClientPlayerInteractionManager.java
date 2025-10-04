package de.drvlabs.btscreen.mixin.network;

import static de.drvlabs.btscreen.config.Configs.Generic.NO_BREAK_COOLDOWN;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.client.network.ClientPlayerInteractionManager;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {
    @Shadow
    private int blockBreakingCooldown;

    @Inject(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD, ordinal = 2))
    private void survivalBreakDelayChange(CallbackInfoReturnable<?> ci) {
        if (NO_BREAK_COOLDOWN.getBooleanValue() && Utils.isActive()) {
            blockBreakingCooldown = 0;
        }
    }
}