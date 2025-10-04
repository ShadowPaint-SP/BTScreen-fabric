package de.drvlabs.btscreen.mixin.baritone;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandException;
import baritone.api.command.exception.CommandInvalidStateException;
import baritone.command.defaults.SetCommand;
import de.drvlabs.btscreen.utils.preset.PresetMode;

/*
 * WARNING: class is obfuscated
 */
@Mixin(value = SetCommand.class, remap = false)
public abstract class MixinSetCommand {
    private static final List<String> FULL_BLOCKLIST = Arrays.asList("s", "save", "load", "ld");
    private static final List<String> FILTER_BLOCKLIST = Arrays.asList("reset", "toggle");

    @Inject(method = "execute", at = @At("HEAD"))
    private void blockExecute(String label, IArgConsumer args, CallbackInfo ci) throws CommandException {
        if (!PresetMode.SETTINGS_MANAGER.isApplied() || !args.hasAny())
            return;
        String arg1 = args.peekString().toLowerCase();
        if (FULL_BLOCKLIST.contains(arg1)) {
            throw new CommandInvalidStateException(
                    "Command '" + label + " " + arg1 + "' blocked while Preset Mode active.");
        }
        if (args.has(2)) {
            final List<String> blockList = PresetMode.SETTINGS_MANAGER.getModifiedSettings();
            if (blockList.contains(arg1)) {
                throw new CommandInvalidStateException(
                        "Setting '" + arg1 + "' blocked from modification while Preset Mode active.");
            }
            if (FILTER_BLOCKLIST.contains(arg1)) {
                String arg2 = args.peekString(1).toLowerCase();
                if (blockList.contains(arg2)) {
                    throw new CommandInvalidStateException(
                            "Setting '" + arg2 + "' blocked from modification while Preset Mode active.");
                }
            }
        }
    }
}
