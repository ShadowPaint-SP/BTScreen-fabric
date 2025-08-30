package de.drvlabs.btscreen.btprocess;

import java.util.List;

import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.gui.GuiMainMenu;
import de.drvlabs.btscreen.utils.Utils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screen.Screen;

public class BTActiveListener extends BTProcessHelper {
    public static final Event<BaritoneStarted> STARTED = EventFactory
            .createArrayBacked(BaritoneStarted.class, callbacks -> () -> {
                for (BaritoneStarted callback : callbacks) {
                    callback.baritoneStarted();
                }
            });
    public static final Event<BaritoneStopped> STOPPED = EventFactory
            .createArrayBacked(BaritoneStopped.class, callbacks -> canceled -> {
                for (BaritoneStopped callback : callbacks) {
                    callback.baritoneStopped(canceled);
                }
            });

    @FunctionalInterface
    public interface BaritoneStarted {
        void baritoneStarted();
    }

    @FunctionalInterface
    public interface BaritoneStopped {
        void baritoneStopped(boolean canceled);
    }

    private static final List<IBaritoneProcess> IS_ACTIVE_LIST = List.of(
            Utils.BT.getFarmProcess(),
            Utils.BT.getMineProcess(),
            Utils.BT.getBuilderProcess(),
            Utils.BT.getExploreProcess(),
            Utils.BT.getCustomGoalProcess(),
            Utils.BT.getGetToBlockProcess());

    private static boolean baritoneIsActive = false;

    public static void updateBaritoneIsActive() {
        setBaritoneActive(IS_ACTIVE_LIST.stream().anyMatch(IBaritoneProcess::isActive), false);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        return defer();
    }

    @Override
    public void onLostControl() {
        setBaritoneActive(false, true);
    }

    private static void setBaritoneActive(boolean newBaritoneIsActive, boolean canceled) {
        if (newBaritoneIsActive && !baritoneIsActive) {
            STARTED.invoker().baritoneStarted();
        }
        if (!newBaritoneIsActive && baritoneIsActive) {
            STOPPED.invoker().baritoneStopped(canceled);
        }
        baritoneIsActive = newBaritoneIsActive;
    }

    private static IBaritoneProcess pauseProcess;

    public static void setPauseProcess(IBaritoneProcess process) {
        if (pauseProcess != null || process == null
                || !process.getClass().getName().startsWith("baritone.command.defaults.ExecutionControlCommands")) {
            return;
        }
        pauseProcess = process;
        // reinit screen if set
        Screen screen = Utils.MC.currentScreen;
        if (screen instanceof GuiMainMenu) {
            screen.init(Utils.MC, screen.width, screen.height);
        }
    }

    public static boolean isBaritonePaused() {
        return pauseProcess != null && pauseProcess.isActive();
    }

    public static boolean isBaritoneActive() {
        return baritoneIsActive;
    }
}
