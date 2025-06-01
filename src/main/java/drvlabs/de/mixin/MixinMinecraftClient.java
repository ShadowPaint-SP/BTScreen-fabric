package drvlabs.de.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import drvlabs.de.data.DataManager;

@Mixin(value = MinecraftClient.class)
public abstract class MixinMinecraftClient extends ReentrantThreadExecutor<Runnable> {
	public MixinMinecraftClient(String string_1) {
		super(string_1);
	}

	@Inject(method = "tick()V", at = @At("HEAD"))
	private void onRunTickStart(CallbackInfo ci) {
		DataManager.onClientTickStart();
	}
}