package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.SAFETY;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LocationCheck extends BTProcessHelper {
    private Vec3d lastLocation = null;
    private World lastWorld = null;

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

    {
        SAFETY.setValueChangeCallback(c -> onLostControl());
    }

    @Override
    public double priority() {
        return super.priority() - 0.05;
    }

    private boolean inRange() {
        Vec3d currentLocation = Utils.MC.player.getPos();
        World currentWorld = Utils.MC.world;
        boolean result = true;
        if (lastWorld != null && lastLocation != null) {
            result = currentWorld == lastWorld && currentLocation.isInRange(lastLocation, 5);
        }
        lastLocation = currentLocation;
        lastWorld = currentWorld;
        return result;
    }
}
