package de.drvlabs.btscreen.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.utils.BotStatus;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.util.Hand;

@Mixin(value = MinecraftClient.class)
public abstract class MixinMinecraftClient {
	@Shadow
	@Nullable
	public Screen currentScreen;

	@Shadow
	@Final
	public GameOptions options;

	@Inject(method = "handleInputEvents", at = @At("HEAD"))
	private void onProcessKeybindsPre(CallbackInfo ci) {
		if (this.currentScreen == null) {
			if (DataManager.getActive() && Configs.Generic.AUTO_EAT.getBooleanValue() && DataManager.getNeedsToEat()
					&& DataManager.getBotStatus() != BotStatus.IDLE) {
				FoodComponent food = Utils.MC.player.getOffHandStack().get(DataComponentTypes.FOOD);
				if (food != null) {
					KeyBinding.setKeyPressed(
							InputUtil.fromTranslationKey(this.options.useKey.getBoundKeyTranslationKey()), true);
					Utils.MC.interactionManager.interactItem(Utils.MC.player, Hand.OFF_HAND);
				}
			}
		}
	}
}