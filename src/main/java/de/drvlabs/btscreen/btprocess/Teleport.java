package de.drvlabs.btscreen.btprocess;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.utils.CommandUtils;
import fi.dy.masa.malilib.config.options.ConfigString;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

public class Teleport extends BTProcessHelper {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    public static Home nextHome = null;
    private boolean teleporting = false;
    private boolean teleportBack = false;
    private int timeoutTicks = 0;
    private Vec3d oldPos = null;
    private ClientWorld oldWorld = null;

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        // Timeout
        if (timeoutTicks > 100) {
            CommandUtils.stop();
            onLostControl();
        }
        // Teleport Finished
        if (teleporting && (!oldPos.isInRange(MC.player.getPos(), 1) || oldWorld != MC.world)) {
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
            oldPos = MC.player.getPos();
            oldWorld = MC.world;
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
        // Reset
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
            return super.priority() + 0.1;
        }
        return super.priority() - 0.1;
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
            CommandUtils.sendCommand(Configs.Generic.HOME_COMMAND.getStringValue() + " " + config.getStringValue());
        }

        private void setHome() {
            CommandUtils.sendCommand(Configs.Generic.SETHOME_COMMAND.getStringValue() + " " + config.getStringValue());
        }
    }
}
