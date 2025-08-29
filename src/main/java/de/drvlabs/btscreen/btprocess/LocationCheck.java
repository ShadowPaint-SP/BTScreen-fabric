package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.SAFETY;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.BTScreen;
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
            BTScreen.chatMessage(Text.literal("Error: Player has moved too far from the last recorded location.")
                    .formatted(Formatting.RED));
            Utils.cancel();
        }
        return new PathingCommand(null, PathingCommandType.DEFER);
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
