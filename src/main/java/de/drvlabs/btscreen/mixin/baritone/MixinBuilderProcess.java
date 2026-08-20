package de.drvlabs.btscreen.mixin.baritone;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import baritone.pathing.movement.MovementHelper;
import baritone.process.BuilderProcess;
import de.drvlabs.btscreen.btprocess.SmartWaterClear;
import net.minecraft.world.level.block.state.BlockState;

/** Lets the smart process place its filler into flowing liquid. */
@Mixin(value = BuilderProcess.class, remap = false)
public abstract class MixinBuilderProcess {
    @Redirect(
            method = "a(Lbaritone/process/BuilderProcess$BuilderCalculationContext;Ljava/util/List;Ljava/util/List;Ljava/util/List;Ljava/util/Map;Ljava/util/List;Ljava/util/List;Ljava/util/List;Lbaritone/api/utils/BetterBlockPos;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lbaritone/pathing/movement/MovementHelper;g(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean treatFlowingLiquidAsPlaceable(BlockState state) {
        return !SmartWaterClear.isRunning() && MovementHelper.g(state);
    }
}
