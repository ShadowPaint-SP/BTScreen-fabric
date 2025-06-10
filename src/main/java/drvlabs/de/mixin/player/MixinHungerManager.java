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
			BTScreen.debugLog("FOOD LEVEL: " + foodLevel);
			if (foodLevel < 19) {
				DataManager.setNeedsToEat(true);
				BTScreen.debugLog("FOOD:" + DataManager.getNeedsToEat());
			} else {
				DataManager.setNeedsToEat(false);
				BTScreen.debugLog("FOOD:" + DataManager.getNeedsToEat());
				MinecraftClient.getInstance().options.useKey.setPressed(false);
			}
		}
	}
}
