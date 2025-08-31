package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.SAFETY;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.event.BaritoneEvents;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class LocationCheck extends BTProcessHelper implements BaritoneEvents.Stopped, BaritoneEvents.Paused {
    private Vec3d lastLocation = null;
    private Identifier lastWorld = null;

    @Override
    public boolean isActive() {
        return isActive(SAFETY);
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (!inRange()) {
            BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".locationCheck.playerMovedTooFar")
                    .formatted(Formatting.RED));
            Utils.cancel();
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
        SAFETY.setValueChangeCallback(c -> onLostControl());
        BaritoneEvents.STOPPED.register(this);
        BaritoneEvents.PAUSED.register(this);
    }

    @Override
    public double priority() {
        return super.priority() - 0.05;
    }

    private boolean inRange() {
        Vec3d currentLocation = Utils.MC.player.getPos();
        Identifier currentWorld = Utils.getWorldId();
        boolean result = true;
        if (lastWorld != null && lastLocation != null) {
            result = currentWorld.equals(lastWorld) && currentLocation.isInRange(lastLocation, 5);
        }
        lastLocation = currentLocation;
        lastWorld = currentWorld;
        return result;
    }
}
