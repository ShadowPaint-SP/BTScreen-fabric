package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_SLEEP;

import java.util.Iterator;
import java.util.List;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoSleep extends BTProcessWithInitializer {
    private ClientWorld oldWorld = null;
    private Iterator<BlockPos> bedPositions = null;

    @Override
    public boolean isActive() {
        return isActive(AUTO_SLEEP) && (isNight() || oldWorld != null);
    }

    @Override
    protected void onInitialize() {
        oldWorld = Utils.MC.world;
        Teleport.requestTeleport(Teleport.Home.SLEEP);
        BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".autoSleep.started"));
    }

    @Override
    protected PathingCommand onTick() {
        if (oldWorld == null) {
            return new PathingCommand(null, PathingCommandType.DEFER);
        }
        if (oldWorld != Utils.MC.world) {
            AUTO_SLEEP.setBooleanValue(false);
            BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".autoSleep.wrongDimension")
                    .formatted(Formatting.RED));
        }
        if (!Utils.MC.player.isSleeping()) {
            if (bedPositions == null) {
                bedPositions = getBedPositions(Utils.MC.player.getBlockPos());
            } else {
                if (bedPositions.hasNext()) {
                    BlockPos pos = bedPositions.next();
                    hitBed(pos);
                } else {
                    oldWorld = null;
                    BTScreen.chatMessage(
                            Text.translatable(LangKeys.INFO + ".autoSleep.noBed").formatted(Formatting.RED));
                }
            }
        } else if (Utils.MC.player.canResetTimeBySleeping()) {
            oldWorld = null;
            Screen screen = Utils.MC.currentScreen;
            if (screen != null) {
                screen.close();
            }
            BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".autoSleep.finished"));
        }
        return requestPause();
    }

    @Override
    protected void onReset() {
        oldWorld = null;
        bedPositions = null;
    }

    private static boolean isNight() {
        long curTime = Utils.MC.world.getTimeOfDay() % SharedConstants.TICKS_PER_IN_GAME_DAY;
        return (curTime >= 12700 && curTime <= 23000);
    }

    private static Iterator<BlockPos> getBedPositions(BlockPos pos) {
        // FIXME: search for beds in range
        return List.of(pos.east(), pos.west(), pos.north(), pos.south()).iterator();
    }

    private static void hitBed(BlockPos pos) {
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.DOWN, pos, false);
        BTScreen.debugLog("Hitting Bed at: " + pos);
        if (Utils.MC.interactionManager.interactBlock(Utils.MC.player, Hand.MAIN_HAND, hit).isAccepted()) {
            Utils.MC.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
