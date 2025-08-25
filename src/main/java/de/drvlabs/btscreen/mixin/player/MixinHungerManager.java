package de.drvlabs.btscreen.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.drvlabs.btscreen.btprocess.AutoEat;
import net.minecraft.entity.player.HungerManager;

@Mixin(HungerManager.class)
public class MixinHungerManager {
    @Inject(method = "setFoodLevel", at = @At("HEAD"))
    private void onSetFoodLevel(int foodLevel, CallbackInfo ci) {
        AutoEat.onSetFoodLevel(foodLevel);
    }
}
