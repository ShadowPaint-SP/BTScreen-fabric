package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.SAFETY_LOCATION;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.event.BaritoneEvents;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class LocationCheck extends BTProcessHelper implements BaritoneEvents.Stopped, BaritoneEvents.Paused {
    public static final LocationCheck INSTANCE = new LocationCheck();

    private LocationCheck() {
    }

    private Vec3 lastLocation = null;
    private Identifier lastWorld = null;

    @Override
    public boolean isActive() {
        return isActive(SAFETY_LOCATION);
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (!inRange()) {
            BTScreen.chatMessage(Component.translatable(LangKeys.INFO + ".safety.movedTooFar")
                    .withStyle(ChatFormatting.RED));
            Utils.cancel();
            Teleport.Home.SAFETY.tpToHome();
            return REQUEST_PAUSE;
        }
        return DEFER;
    }

    @Override
    public void onLostControl() {
        lastLocation = null;
        lastWorld = null;
    }

    @Override
    public void baritonePaused() {
        onLostControl();
    }

    @Override
    public void baritoneStopped(boolean canceled) {
        onLostControl();
    }

    {
        SAFETY_LOCATION.setValueChangeCallback(c -> onLostControl());
        BaritoneEvents.STOPPED.register(this);
        BaritoneEvents.PAUSED.register(this);
    }

    @Override
    public double priority() {
        return super.priority() - 0.05;
    }

    private boolean inRange() {
        Vec3 currentLocation = Utils.MC.player.position();
        Identifier currentWorld = Utils.getWorldId();
        boolean result = true;
        if (lastWorld != null && lastLocation != null) {
            result = currentWorld.equals(lastWorld) && currentLocation.closerThan(lastLocation, 5);
        }
        lastLocation = currentLocation;
        lastWorld = currentWorld;
        return result;
    }
}
