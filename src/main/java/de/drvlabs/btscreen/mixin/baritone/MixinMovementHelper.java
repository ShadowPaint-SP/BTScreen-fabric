package de.drvlabs.btscreen.mixin.baritone;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import baritone.pathing.movement.CalculationContext;
import baritone.pathing.movement.MovementHelper;
import de.drvlabs.btscreen.btprocess.SmartWaterClear;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;

/** Makes Smart Water Clear bridge over water instead of planning swimming paths. */
@Mixin(value = MovementHelper.class, remap = false)
public interface MixinMovementHelper {
    @Inject(
            method = "a(Lbaritone/pathing/movement/CalculationContext;IIILnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void disallowSmartWaterClearSwimming(CalculationContext context, int x, int y, int z,
            BlockState state, CallbackInfoReturnable<Boolean> cir) {
        disallowSmartWaterClearSwimming(state, cir);
    }

    private static void disallowSmartWaterClearSwimming(BlockState state,
            CallbackInfoReturnable<Boolean> cir) {
        if (SmartWaterClear.isRunning() && state.getFluidState().is(FluidTags.WATER)) {
            cir.setReturnValue(false);
        }
    }
}
