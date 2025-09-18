package de.drvlabs.btscreen.mixin.baritone;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import baritone.command.defaults.SetCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.utils.preset.PresetMode;
import net.minecraft.text.Text;

/*
 * WARNING: class is obfuscated
 */
@Mixin(value = SetCommand.class, remap = false)
public abstract class MixinSetCommand {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private void blockCommand(CallbackInfo ci) {
        if (PresetMode.overwroteSettings()) {
            BTScreen.chatMessage(Text.literal("set command blocked while bot is active in Preset Mode"));
            ci.cancel();
        }
    }
}
