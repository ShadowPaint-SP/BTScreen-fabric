package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_HASTE;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.BTScreen;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.text.Text;

public class AutoHaste extends BTProcessWithInitializer {
    private static boolean hasHaste = false;

    @Override
    public boolean isActive() {
        return isActive(AUTO_HASTE) && !hasHaste;
    }

    @Override
    protected void onInitialize() {
        Teleport.requestTeleport(Teleport.Home.HASTE);
        BTScreen.chatMessage(Text.literal("Started waiting for Haste"));
    }

    @Override
    protected PathingCommand onTick() {
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    @Override
    protected void onReset() {
        hasHaste = false;
        BTScreen.chatMessage(Text.literal("Finished waiting for Haste"));
    }

    public static void onEffect(EntityStatusEffectS2CPacket packet) {
        if (!hasHaste && packet.getEffectId().matches(StatusEffects.HASTE::matchesKey)) {
            hasHaste = true;
        }
    }

    public static void onRemoveEffect(RemoveEntityStatusEffectS2CPacket packet) {
        if (hasHaste && packet.effect().matches(StatusEffects.HASTE::matchesKey)) {
            hasHaste = false;
        }
    }
}
