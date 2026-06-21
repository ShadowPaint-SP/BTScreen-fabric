package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_SLEEP;
import static de.drvlabs.btscreen.config.Configs.Generic.MAX_SLEEP_TICKS;

import java.util.Iterator;
import java.util.List;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class AutoSleep extends BTProcessWithInitializer {
    public static final AutoSleep INSTANCE = new AutoSleep();

    private AutoSleep() {
    }

    private int sleepTimer = 0;
    private Iterator<BlockPos> bedPositions = null;

    @Override
    public boolean isActive() {
        return isActive(AUTO_SLEEP) && (sleepTimer > 0 || isNight());
    }

    @Override
    protected void onInitialize() {
        Teleport.requestTeleport(Teleport.Home.SLEEP);
    }

    @Override
    protected PathingCommand onTick() {
        Identifier mineWorld = Teleport.Home.MINE.getWorld();
        if (sleepTimer >= MAX_SLEEP_TICKS.getIntegerValue()) {
            if (mineWorld.equals(Utils.getWorldId()) && !isNight()) {
                onLostControl();
            }
            return DEFER;
        }
        if (!mineWorld.equals(Utils.getWorldId())) {
            AUTO_SLEEP.setBooleanValue(false);
            BTScreen.chatMessage(Component.translatable(LangKeys.INFO + ".autoSleep.wrongDimension")
                    .withStyle(ChatFormatting.RED));
            return DEFER;
        }
        if (!Utils.MC.player.isSleeping()) {
            if (bedPositions == null) {
                bedPositions = getBedPositions(Utils.MC.player.blockPosition());
            } else if (bedPositions.hasNext()) {
                BlockPos pos = bedPositions.next();
                hitBed(pos);
            } else {
                sleepTimer = MAX_SLEEP_TICKS.getIntegerValue();
                BTScreen.chatMessage(
                        Component.translatable(LangKeys.INFO + ".autoSleep.noBed").withStyle(ChatFormatting.RED));
            }
        } else if (Utils.MC.player.isSleepingLongEnough()
                && sleepTimer++ >= MAX_SLEEP_TICKS.getIntegerValue()) {
            Screen screen = Utils.MC.screen;
            if (screen != null) {
                screen.onClose();
            }
        }
        return REQUEST_PAUSE;
    }

    @Override
    protected void onReset() {
        sleepTimer = 0;
        bedPositions = null;
    }

    private static boolean isNight() {
        DimensionType dimension = Utils.MC.level.dimensionType();
        if (dimension.hasFixedTime() || !dimension.hasSkyLight() || dimension.defaultClock().isEmpty()) {
            return false;
        }
        long curTime = Utils.MC.level.clockManager().getTotalTicks(dimension.defaultClock().get())
                % SharedConstants.TICKS_PER_GAME_DAY;
        return (curTime >= 12700 && curTime <= 23000);
    }

    private static Iterator<BlockPos> getBedPositions(BlockPos pos) {
        // FIXME: search for beds in range
        return List.of(pos.east(), pos.west(), pos.north(), pos.south()).iterator();
    }

    private static void hitBed(BlockPos pos) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, false);
        BTScreen.debugLog("Hitting Bed at: " + pos);
        if (Utils.MC.gameMode.useItemOn(Utils.MC.player, InteractionHand.MAIN_HAND, hit).consumesAction()) {
            Utils.MC.player.swing(InteractionHand.MAIN_HAND);
        }
    }
}
