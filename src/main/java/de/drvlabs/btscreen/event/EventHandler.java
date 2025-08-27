package de.drvlabs.btscreen.event;

import java.util.List;

import baritone.api.pathing.calc.IPathingControlManager;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.btprocess.BTProcessHelper;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.gui.GuiConfigs;
import de.drvlabs.btscreen.utils.LocationCheck;
import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndWorldTick;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents.AfterClientWorldChange;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public final class EventHandler implements EndWorldTick, AfterClientWorldChange, ClientStarted {
    private final class BaritoneCancelListener extends BTProcessHelper {
        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
            throw new UnsupportedOperationException("Should never tick! Always inactive");
        }

        @Override
        public void onLostControl() {
            baritoneIsActive = false;
        }
    }

    private static boolean baritoneIsActive = false;
    private static final List<IBaritoneProcess> IS_ACTIVE_LIST = List.of(
            Utils.BT.getFarmProcess(),
            Utils.BT.getMineProcess(),
            Utils.BT.getBuilderProcess(),
            Utils.BT.getExploreProcess(),
            Utils.BT.getCustomGoalProcess(),
            Utils.BT.getGetToBlockProcess());

    private static void setBaritoneActive() {
        baritoneIsActive = IS_ACTIVE_LIST.stream().anyMatch(IBaritoneProcess::isActive);
    }

    public static boolean isBaritoneActive() {
        return baritoneIsActive;
    }

    private IBaritoneProcess lastProcess = null;

    @Override
    public void onClientStarted(MinecraftClient client) {
        ConfigManager.getInstance().registerConfigHandler(BTScreen.MOD_ID, new Configs());
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(BTScreen.MOD_ID, BTScreen.MOD_NAME, GuiConfigs::new));

        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.INSTANCE);

        final IPathingControlManager controlManager = Utils.BT.getPathingControlManager();
        controlManager.registerProcess(new BaritoneCancelListener());
        controlManager.registerProcess(new de.drvlabs.btscreen.btprocess.Teleport());
        controlManager.registerProcess(new de.drvlabs.btscreen.btprocess.AutoDrop());
        controlManager.registerProcess(new de.drvlabs.btscreen.btprocess.AutoEat());
        controlManager.registerProcess(new de.drvlabs.btscreen.btprocess.AutoHaste());
        controlManager.registerProcess(new de.drvlabs.btscreen.btprocess.AutoRepair());
        controlManager.registerProcess(new de.drvlabs.btscreen.btprocess.AutoSleep());
        controlManager.registerProcess(new de.drvlabs.btscreen.btprocess.AutoTorch());
    }

    @Override
    public void onEndTick(ClientWorld world) {
        IBaritoneProcess currentProcess = Utils.BT.getPathingControlManager()
                .mostRecentInControl().orElse(null);
        if (currentProcess != null && lastProcess != currentProcess) {
            BTScreen.debugLog("Current Process: {}, {}, {}, {}, {}, {}", currentProcess.displayName(),
                    currentProcess.displayName0(), currentProcess.isActive(), currentProcess.isTemporary(),
                    currentProcess.priority(), currentProcess.toString());
            lastProcess = currentProcess;
        }
        Waiter.tickAll();
        if (Utils.isInGame()) {
            LocationCheck.checkLocation();
        }
        setBaritoneActive();
    }

    @Override
    public void afterWorldChange(MinecraftClient client, ClientWorld world) {
        DataManager.save();
        if (world != null) {
            DataManager.load();
            BTScreen.LOGGER.error("Loaded settings");
        } else {
            DataManager.clear();
        }
    }
}
