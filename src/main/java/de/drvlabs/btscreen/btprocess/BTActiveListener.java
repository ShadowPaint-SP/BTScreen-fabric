package de.drvlabs.btscreen.btprocess;

import java.util.List;

import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.event.BaritoneEvents;
import de.drvlabs.btscreen.gui.GuiMainMenu;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.client.gui.screen.Screen;

public class BTActiveListener extends BTProcessHelper {
    private static final List<IBaritoneProcess> IS_ACTIVE_LIST = List.of(
            Utils.BT.getFarmProcess(),
            Utils.BT.getMineProcess(),
            Utils.BT.getBuilderProcess(),
            Utils.BT.getFollowProcess(),
            Utils.BT.getExploreProcess(),
            Utils.BT.getCustomGoalProcess(),
            Utils.BT.getGetToBlockProcess());

    private static boolean baritoneIsActive = false;
    private static boolean baritoneIsPaused = false;

    private static IBaritoneProcess pauseProcess;

    public static void updateBaritoneStatus() {
        setBaritoneActive(IS_ACTIVE_LIST.stream().anyMatch(IBaritoneProcess::isActive), false);
        setBaritonePaused(isBaritonePaused());
    }

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

    public static boolean isBaritoneActive() {
        return baritoneIsActive;
    }

    public static boolean isBaritonePaused() {
        return pauseProcess != null && pauseProcess.isActive();
    }

    private static void setBaritoneActive(boolean newBaritoneIsActive, boolean canceled) {
        boolean oldBaritoneIsActive = baritoneIsActive;
        baritoneIsActive = newBaritoneIsActive;
        if (newBaritoneIsActive && !oldBaritoneIsActive) {
            BaritoneEvents.STARTED.invoker().baritoneStarted();
        }
        if (!newBaritoneIsActive && oldBaritoneIsActive) {
            BaritoneEvents.STOPPED.invoker().baritoneStopped(canceled);
        }
    }

    private static void setBaritonePaused(boolean newBaritoneIsPaused) {
        boolean oldBaritoneIsPaused = baritoneIsPaused;
        baritoneIsPaused = newBaritoneIsPaused;
        if (baritoneIsActive && newBaritoneIsPaused && !oldBaritoneIsPaused) {
            BaritoneEvents.PAUSED.invoker().baritonePaused();
        }
        if (baritoneIsActive && !newBaritoneIsPaused && oldBaritoneIsPaused) {
            BaritoneEvents.RESUMED.invoker().baritoneResumed();
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        return DEFER;
    }

    @Override
    public void onLostControl() {
        setBaritoneActive(false, true);
    }
}
