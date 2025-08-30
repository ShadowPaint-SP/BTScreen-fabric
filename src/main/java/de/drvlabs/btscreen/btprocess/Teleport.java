package de.drvlabs.btscreen.btprocess;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.config.options.ConfigString;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class Teleport extends BTProcessHelper {
    private static Home nextHome = null;
    private Home lastHome = null;
    private boolean teleporting = false;
    private int timeoutTicks = 0;
    private Vec3d oldPos = null;
    private ClientWorld oldWorld = null;

    @Override
    public boolean isActive() {
        return nextHome != null || teleporting || lastHome != null;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (isSafeToCancel) {
            if (timeoutTicks > 100) {
                // Timeout
                BTScreen.chatMessage(
                        Text.translatable(LangKeys.INFO + ".teleport.timeout").formatted(Formatting.RED));
                Utils.cancel();
                onLostControl();
            }
            if (!teleporting) {
                if (nextHome != null) {
                    if (nextHome.isSame(lastHome)) {
                        nextHome = null;
                        return new PathingCommand(null, PathingCommandType.DEFER);
                    }
                    BTScreen.debugLog("Teleporting to " + nextHome + " Home");
                    // Teleport to Home prepare
                    teleporting = true;
                    oldPos = Utils.MC.player.getPos();
                    oldWorld = Utils.MC.world;
                    // Set Mine home before teleporting
                    if (nextHome != Home.MINE) {
                        if (lastHome == null) {
                            Home.MINE.setHome();
                            if (nextHome.isSame(Home.DROP)) {
                                AutoDrop.teleportIntegration();
                            }
                        }
                        lastHome = nextHome;
                    }
                } else if (lastHome != null) {
                    // Teleport Back
                    lastHome = null;
                    nextHome = Home.MINE;
                }
            } else if (nextHome != null) {
                // Teleport to Home
                nextHome.tpToHome();
                nextHome = null;
            } else if (oldPos != null
                    && (!oldPos.isInRange(Utils.MC.player.getPos(), 1) || oldWorld != Utils.MC.world)) {
                BTScreen.debugLog("Teleport Finished");
                // Teleport Finished
                teleporting = false;
                timeoutTicks = 0;
                oldPos = null;
                oldWorld = null;
            }
            timeoutTicks++;
        }
        return requestPause();
    }

    @Override
    public void onLostControl() {
        nextHome = null;
        teleporting = false;
        lastHome = null;
        timeoutTicks = 0;
        oldPos = null;
        oldWorld = null;
    }

    @Override
    public double priority() {
        if (teleporting || nextHome != null) {
            return super.priority() + 0.05;
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

        private boolean isSame(Home home) {
            return home != null && (this == home || config.getStringValue().equals(home.config.getStringValue()));
        }
    }
}
