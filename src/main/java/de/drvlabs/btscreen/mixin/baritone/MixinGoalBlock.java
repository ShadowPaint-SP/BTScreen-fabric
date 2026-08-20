package de.drvlabs.btscreen.mixin.baritone;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import baritone.api.pathing.goals.GoalBlock;
import de.drvlabs.btscreen.btprocess.SmartWaterClear;

/** Routes the current smart-water-clear G placement through H_inner. */
@Mixin(value = GoalBlock.class, remap = false)
public abstract class MixinGoalBlock {
    @Inject(method = "isInGoal(III)Z", at = @At("HEAD"), cancellable = true)
    private void constrainSmartWaterClearPlacement(int standingX, int standingY, int standingZ,
            CallbackInfoReturnable<Boolean> cir) {
        GoalBlock goal = (GoalBlock) (Object) this;
        // Builder's GoalPlace and source-liquid GoalBlock both point one block
        // above the block that will be placed.
        Boolean allowed = SmartWaterClear.isAllowedGPlacementPosition(goal.x, goal.y - 1, goal.z,
                standingX, standingY, standingZ);
        if (allowed != null) {
            cir.setReturnValue(allowed);
        }
    }
}
