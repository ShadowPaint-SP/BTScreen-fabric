package de.drvlabs.btscreen.mixin.baritone;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import baritone.api.pathing.goals.GoalGetToBlock;
import de.drvlabs.btscreen.btprocess.SmartWaterClear;

/** Keeps current H_outer obstruction clearing on its assigned H_inner cell. */
@Mixin(targets = "baritone.process.BuilderProcess$GoalBreak", remap = false)
public abstract class MixinBuilderGoalBreak {
    @Inject(method = "isInGoal(III)Z", at = @At("HEAD"), cancellable = true)
    private void constrainSmartWaterClearGoal(int standingX, int standingY, int standingZ,
            CallbackInfoReturnable<Boolean> cir) {
        GoalGetToBlock goal = (GoalGetToBlock) (Object) this;
        Boolean allowed = SmartWaterClear.isAllowedHClearStandingPosition(goal.x, goal.y, goal.z,
                standingX, standingY, standingZ);
        if (allowed != null) {
            cir.setReturnValue(allowed);
        }
    }
}
