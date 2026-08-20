package de.drvlabs.btscreen.mixin.baritone;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import baritone.api.pathing.goals.GoalGetToBlock;
import de.drvlabs.btscreen.btprocess.SmartWaterClear;

/** Keeps adjacent G placement goals on their assigned H_inner cell. */
@Mixin(targets = "baritone.process.BuilderProcess$GoalAdjacent", remap = false)
public abstract class MixinBuilderGoalAdjacent {
    @Inject(method = "isInGoal(III)Z", at = @At("HEAD"), cancellable = true)
    private void constrainSmartWaterClearPlacement(int standingX, int standingY, int standingZ,
            CallbackInfoReturnable<Boolean> cir) {
        GoalGetToBlock goal = (GoalGetToBlock) (Object) this;
        Boolean allowed = SmartWaterClear.isAllowedGPlacementPosition(goal.x, goal.y, goal.z,
                standingX, standingY, standingZ);
        if (allowed != null) {
            cir.setReturnValue(allowed);
        }
    }
}
