package drvlabs.de.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.util.thread.ReentrantThreadExecutor;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.IMinecraftClientInvoker;

@Mixin(value = MinecraftClient.class)
public abstract class MixinMinecraftClient extends ReentrantThreadExecutor<Runnable>
		implements IMinecraftClientInvoker {
	@Shadow
	@Nullable
	public Screen currentScreen;

	@Shadow
	@Nullable
	public ClientPlayerInteractionManager interactionManager;

	@Shadow
	private boolean doAttack() {
		return false;
	}

	@Shadow
	@Final
	public GameOptions options;

	@Shadow
	private int itemUseCooldown;

	@Override
	public void btscreen_setItemUseCooldown(int value) {
		this.itemUseCooldown = value;
	}

	@Override
	public boolean btscreen_invokeDoAttack() {
		return this.doAttack();
	}

	public MixinMinecraftClient(String string_1) {
		super(string_1);
	}

	@Inject(method = "tick()V", at = @At("HEAD"))
	private void onRunTickStart(CallbackInfo ci) {
		DataManager.onClientTickStart(); // TODO Check if needed

	}

	@Inject(method = "handleInputEvents", at = @At("HEAD"))
	private void onProcessKeybindsPre(CallbackInfo ci) {
		if (this.currentScreen == null) {
			if (DataManager.getActive() && DataManager.getBotStatus() == BotStatus.MINING
					&& Configs.Generic.AUTO_EAT.getBooleanValue() && DataManager.getNeedsToEat()) {
				FoodComponent food = MinecraftClient.getInstance().player.getOffHandStack().get(DataComponentTypes.FOOD);
				if (food != null) {
					KeyBinding.setKeyPressed(InputUtil.fromTranslationKey(this.options.useKey.getBoundKeyTranslationKey()), true);
				}
			}
		}
	}
}