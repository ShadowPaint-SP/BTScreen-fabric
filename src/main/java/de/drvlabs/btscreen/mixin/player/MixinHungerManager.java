package de.drvlabs.btscreen.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.utils.BotStatus;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.HungerManager;

@Mixin(HungerManager.class)
public class MixinHungerManager {
	/** {@link ClientPlayerInteractionManager#interactItem} */
	@Inject(method = "setFoodLevel", at = @At("HEAD"))
	private void onSetFoodLevel(int foodLevel, CallbackInfo ci) {
		if (DataManager.getActive() && Configs.Generic.AUTO_EAT.getBooleanValue()
				&& DataManager.getBotStatus() != BotStatus.IDLE) {

			FoodComponent food = Utils.MC.player.getOffHandStack().get(DataComponentTypes.FOOD);
			if (food == null) {
				return;
			}
			float test = foodLevel + food.nutrition();
			BTScreen.debugLog("Food level: " + foodLevel + ", saturation: " + food.nutrition() + ", new foodlevel: " + test);
			if (foodLevel < Configs.Generic.FOOD_LEVEL.getIntegerValue()) {
				DataManager.setNeedsToEat(true);
			} else if (foodLevel >= Configs.Generic.FOOD_LEVEL.getIntegerValue()
					&& test == 20) {
				DataManager.setNeedsToEat(true);
			} else {
				DataManager.setNeedsToEat(false);
				Utils.MC.options.useKey.setPressed(false);
			}
		}
	}
}
