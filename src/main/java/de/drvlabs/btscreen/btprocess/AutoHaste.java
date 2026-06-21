package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_HASTE;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffects;

public final class AutoHaste extends BTProcessWithInitializer {
    public static final AutoHaste INSTANCE = new AutoHaste();

    private AutoHaste() {
    }

    private static boolean hasHaste = false;
    private int timeoutTicks = 0;

    @Override
    public boolean isActive() {
        return isActive(AUTO_HASTE) && !hasHaste;
    }

    @Override
    protected void onInitialize() {
        Teleport.requestTeleport(Teleport.Home.HASTE);
    }

    @Override
    protected PathingCommand onTick() {
        if (++timeoutTicks > 120) {
            AUTO_HASTE.setBooleanValue(false);
            BTScreen.chatMessage(
                    Component.translatable(LangKeys.INFO + ".autoHaste.timeout").withStyle(ChatFormatting.RED));
        }
        return REQUEST_PAUSE;
    }

    @Override
    protected void onReset() {
        timeoutTicks = 0;
    }

    public static void onEffect(ClientboundUpdateMobEffectPacket packet) {
        if (!hasHaste && packet.getEffect().is(MobEffects.HASTE::is)) {
            hasHaste = true;
        }
    }

    public static void onRemoveEffect(ClientboundRemoveMobEffectPacket packet) {
        if (hasHaste && packet.effect().is(MobEffects.HASTE::is)) {
            hasHaste = false;
        }
    }
}
