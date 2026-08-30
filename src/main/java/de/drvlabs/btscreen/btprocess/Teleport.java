package de.drvlabs.btscreen.btprocess;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.config.options.ConfigString;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public final class Teleport extends BTProcessHelper {
    public static final Teleport INSTANCE = new Teleport();
    private static final int DESTINATION_CHUNK_STABLE_TICKS = 5;

    private Teleport() {
    }

    private static Home nextHome = null;
    private static Home lastHome = null;
    private boolean teleporting = false;
    private int timeoutTicks = 0;
    private Vec3 oldPos = null;
    private Identifier oldWorld = null;
    private int destinationChunkStableTicks = 0;

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
                        Component.translatable(LangKeys.INFO + ".teleport.timeout").withStyle(ChatFormatting.RED));
                Utils.cancel();
                onLostControl();
            }
            if (!teleporting) {
                if (nextHome != null) {
                    if (nextHome.isSame(lastHome)) {
                        nextHome = null;
                        return DEFER;
                    }
                    BTScreen.debugLog("Teleporting to " + nextHome + " Home");
                    // Teleport to Home prepare
                    teleporting = true;
                    destinationChunkStableTicks = 0;
                    oldPos = Utils.MC.player.position();
                    oldWorld = Utils.getWorldId();
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
                    && (!oldPos.closerThan(Utils.MC.player.position(), 1) || !oldWorld.equals(Utils.getWorldId()))) {
                ChunkPos playerChunk = Utils.MC.player.chunkPosition();
                if (!Utils.MC.level.hasChunk(playerChunk.x(), playerChunk.z())) {
                    destinationChunkStableTicks = 0;
                } else if (++destinationChunkStableTicks >= DESTINATION_CHUNK_STABLE_TICKS) {
                    BTScreen.debugLog("Teleport Finished");
                    // Teleport Finished
                    teleporting = false;
                    timeoutTicks = 0;
                    destinationChunkStableTicks = 0;
                    oldPos = null;
                    oldWorld = null;
                    if (lastHome != null) {
                        lastHome.lastWorld = Utils.getWorldId();
                    }
                }
            }
            timeoutTicks++;
        }
        return REQUEST_PAUSE;
    }

    @Override
    public void onLostControl() {
        nextHome = null;
        teleporting = false;
        lastHome = null;
        timeoutTicks = 0;
        oldPos = null;
        oldWorld = null;
        destinationChunkStableTicks = 0;
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

    public static Home getLastHome() {
        return lastHome;
    }

    public static enum Home {
        MINE(Configs.Generic.MINE_HOME),
        SLEEP(Configs.Generic.SLEEP_HOME),
        DROP(Configs.Generic.DROP_HOME),
        HASTE(Configs.Generic.HASTE_HOME),
        REPAIR(Configs.Generic.REPAIR_HOME),
        SAFETY(Configs.Generic.SAFETY_HOME) {
            @Override
            public void tpToHome() {
                if (isConfigured()) {
                    super.tpToHome();
                } else if (FINISHED.isConfigured()) {
                    FINISHED.tpToHome();
                } else if (SLEEP.isConfigured()) {
                    SLEEP.tpToHome();
                } else {
                    Utils.MC.disconnectFromWorld(Component.translatable(LangKeys.INFO + ".savetyDisconnect", BTScreen.MOD_NAME));
                }
            }
        },
        FINISHED(Configs.Generic.FINISHED_HOME),
        ;

        Home(ConfigString config) {
            this.config = config;
        }

        private final ConfigString config;
        private Identifier lastWorld = null;

        public boolean isConfigured() {
            return !this.config.getStringValue().isEmpty();
        }

        public Identifier getWorld() {
            return this.lastWorld;
        }

        public void tpToHome() {
            if (!isConfigured())
                return;
            Utils.BT.getInputOverrideHandler().clearAllKeys();
            String home = this.config.getStringValue();
            if (home.startsWith("/")) {
                Utils.sendCommand(home.substring(1));
            } else {
                Utils.sendCommand(Configs.Generic.HOME_COMMAND.getStringValue() + " " + home);
            }
            AutoTorch.onTeleport();
        }

        public void setHome() {
            this.lastWorld = Utils.getWorldId();
            String home = this.config.getStringValue();
            if (home.isEmpty() || home.startsWith("/"))
                return;
            Utils.sendCommand(Configs.Generic.SETHOME_COMMAND.getStringValue() + " " + home);
        }

        public boolean isSame(Home home) {
            return home != null && (this == home || this.config.getStringValue().equals(home.config.getStringValue()));
        }
    }
}
