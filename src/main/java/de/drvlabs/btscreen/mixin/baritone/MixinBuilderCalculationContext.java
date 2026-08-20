package de.drvlabs.btscreen.mixin.baritone;

import static baritone.api.pathing.movement.ActionCosts.COST_INF;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.drvlabs.btscreen.btprocess.SmartWaterClear;
import net.minecraft.world.level.block.state.BlockState;

/** Prevents BuilderProcess paths from mining completed smart-water-clear wall cells. */
@Mixin(targets = "baritone.process.BuilderProcess$BuilderCalculationContext", remap = false)
public abstract class MixinBuilderCalculationContext {
    @Inject(method = "b(IIILnet/minecraft/world/level/block/state/BlockState;)D", at = @At("HEAD"), cancellable = true)
    private void protectSmartWaterClearWall(int x, int y, int z, BlockState current,
            CallbackInfoReturnable<Double> cir) {
        if (SmartWaterClear.isProtected(x, y, z)) {
            cir.setReturnValue(COST_INF);
        }
    }
}
