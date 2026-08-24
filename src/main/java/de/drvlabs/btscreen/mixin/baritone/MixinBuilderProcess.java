package de.drvlabs.btscreen.mixin.baritone;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Redirect;

import baritone.pathing.movement.MovementHelper;
import baritone.process.BuilderProcess;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.drvlabs.btscreen.btprocess.SmartWaterClear;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Lets the smart process place its filler into flowing liquid. */
@Mixin(value = BuilderProcess.class, remap = false)
public abstract class MixinBuilderProcess {
    // Baritone only recognizes LiquidBlock here. BubbleColumnBlock also contains
    // source water, but mining it first lets the column immediately reform.
    @WrapOperation(
            method = "a(Lbaritone/process/BuilderProcess$BuilderCalculationContext;Ljava/util/List;Ljava/util/List;Ljava/util/List;Ljava/util/Map;Ljava/util/List;Ljava/util/List;Ljava/util/List;Lbaritone/api/utils/BetterBlockPos;)V",
            constant = @Constant(classValue = LiquidBlock.class))
    private static boolean treatBubbleColumnAsLiquid(Object block, Operation<Boolean> original) {
        return SmartWaterClear.isBaritoneLiquid(original.call(block), block instanceof BubbleColumnBlock);
    }

    @Redirect(
            method = "a(Lbaritone/process/BuilderProcess$BuilderCalculationContext;Ljava/util/List;Ljava/util/List;Ljava/util/List;Ljava/util/Map;Ljava/util/List;Ljava/util/List;Ljava/util/List;Lbaritone/api/utils/BetterBlockPos;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lbaritone/pathing/movement/MovementHelper;g(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean treatFlowingLiquidAsPlaceable(BlockState state) {
        return !SmartWaterClear.isRunning() && MovementHelper.g(state);
    }
}
