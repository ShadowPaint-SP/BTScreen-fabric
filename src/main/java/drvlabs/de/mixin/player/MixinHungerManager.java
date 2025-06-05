package drvlabs.de.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.player.HungerManager;

@Mixin(HungerManager.class)
public class MixinHungerManager {
	@Shadow
	private int foodLevel;

	@Inject(method = "setFoodLevel", at = @At("HEAD"))
	private void onSetFoodLevel(int foodLevel, CallbackInfo ci) {
		if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
				&& Configs.Generic.AUTO_EAT.getBooleanValue()) {
			if (foodLevel != this.foodLevel && this.foodLevel < 21) {
				DataManager.setNeedsToEat(true);
			} else {
				DataManager.setNeedsToEat(false);
			}
		}
	}
}
