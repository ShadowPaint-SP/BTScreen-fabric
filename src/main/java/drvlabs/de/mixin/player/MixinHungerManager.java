package drvlabs.de.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import drvlabs.de.BTScreen;
import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.HungerManager;

@Mixin(HungerManager.class)
public class MixinHungerManager {
	@Shadow
	private int foodLevel;

	@Inject(method = "setFoodLevel", at = @At("HEAD"))
	private void onSetFoodLevel(int foodLevel, CallbackInfo ci) {
		if (DataManager.getActive() && Configs.Generic.AUTO_EAT.getBooleanValue()
				&& DataManager.getBotStatus() != BotStatus.IDLE) {
			if (foodLevel < Configs.Generic.FOOD_LEVEL.getIntegerValue()) {
				DataManager.setNeedsToEat(true);
				BTScreen.debugLog("Eating");
			} else {
				DataManager.setNeedsToEat(false);
				BTScreen.debugLog("Stopped Eating");
				MinecraftClient.getInstance().options.useKey.setPressed(false);
			}
		}
	}
}
