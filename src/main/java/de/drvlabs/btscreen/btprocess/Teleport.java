package de.drvlabs.btscreen.btprocess;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.config.options.ConfigString;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

public class Teleport extends BTProcessHelper {
    private static Home nextHome = null;
    private boolean teleporting = false;
    private boolean teleportBack = false;
    private int timeoutTicks = 0;
    private Vec3d oldPos = null;
    private ClientWorld oldWorld = null;

    @Override
    public boolean isActive() {
        return nextHome != null || teleporting || teleportBack;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        // Timeout
        if (timeoutTicks > 100) {
            Utils.cancel();
            onLostControl();
        }
        // Teleport Finished
        if (teleporting && (!oldPos.isInRange(Utils.MC.player.getPos(), 1) || oldWorld != Utils.MC.world)) {
            teleporting = false;
            timeoutTicks = 0;
            oldPos = null;
            oldWorld = null;
        }
        // Teleport Back
        if (!teleporting && teleportBack) {
            teleporting = true;
            teleportBack = false;
            Home.MINE.tpToHome();
        }
        // Teleport to Home
        if (!teleporting) {
            if (nextHome == null) {
                return new PathingCommand(null, PathingCommandType.DEFER);
            }
            teleporting = true;
            oldPos = Utils.MC.player.getPos();
            oldWorld = Utils.MC.world;
            if (!teleportBack && nextHome != Home.MINE) {
                teleportBack = true;
                Home.MINE.setHome();
            }
            nextHome.tpToHome();
            nextHome = null;
        }
        // Pause while teleporting
        timeoutTicks++;
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    @Override
    public void onLostControl() {
        nextHome = null;
        teleporting = false;
        teleportBack = false;
        timeoutTicks = 0;
        oldPos = null;
        oldWorld = null;
    }

    @Override
    public double priority() {
        if (teleporting || nextHome != null) {
            return super.priority() + 0.01;
        }
        return super.priority() - 0.01;
    }

    /**
     * Requests a teleport to the specified home.
     * If a teleport is already pending, the request is ignored.
     * 
     * @param home
     * @return
     */
    public static boolean requestTeleport(Home home) {
        if (nextHome != null) {
            return false;
        }
        nextHome = home;
        return true;
    }

    public static enum Home {
        SLEEP(Configs.Generic.SLEEP_HOME),
        DROP(Configs.Generic.DROP_HOME),
        HASTE(Configs.Generic.HASTE_HOME),
        REPAIR(Configs.Generic.REPAIR_HOME),
        MINE(Configs.Generic.MINE_HOME),
        ;

        Home(ConfigString config) {
            this.config = config;
        }

        private final ConfigString config;

        private void tpToHome() {
            Utils.sendCommand(Configs.Generic.HOME_COMMAND.getStringValue() + " " + config.getStringValue());
        }

        private void setHome() {
            Utils.sendCommand(Configs.Generic.SETHOME_COMMAND.getStringValue() + " " + config.getStringValue());
        }
    }
}
