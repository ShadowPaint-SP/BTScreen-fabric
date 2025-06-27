package de.drvlabs.btscreen.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.utils.BotStatus;
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
			} else {
				DataManager.setNeedsToEat(false);
				MinecraftClient.getInstance().options.useKey.setPressed(false);
			}
		}
	}
}
