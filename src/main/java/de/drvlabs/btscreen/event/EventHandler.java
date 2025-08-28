package de.drvlabs.btscreen.event;

import baritone.api.pathing.calc.IPathingControlManager;
import baritone.api.process.IBaritoneProcess;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.btprocess.AutoDrop;
import de.drvlabs.btscreen.btprocess.AutoEat;
import de.drvlabs.btscreen.btprocess.AutoHaste;
import de.drvlabs.btscreen.btprocess.AutoRepair;
import de.drvlabs.btscreen.btprocess.AutoSleep;
import de.drvlabs.btscreen.btprocess.AutoTorch;
import de.drvlabs.btscreen.btprocess.BTActiveListener;
import de.drvlabs.btscreen.btprocess.BTActiveListener.BaritoneStarted;
import de.drvlabs.btscreen.btprocess.BTActiveListener.BaritoneStopped;
import de.drvlabs.btscreen.btprocess.Teleport;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.gui.GuiConfigs;
import de.drvlabs.btscreen.utils.LocationCheck;
import de.drvlabs.btscreen.utils.RepeatAction;
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

public final class EventHandler implements EndWorldTick, AfterClientWorldChange, ClientStarted,
        BaritoneStarted, BaritoneStopped {
    @Override
    public void onClientStarted(MinecraftClient client) {
        ConfigManager.getInstance().registerConfigHandler(BTScreen.MOD_ID, new Configs());
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(BTScreen.MOD_ID, BTScreen.MOD_NAME, GuiConfigs::new));

        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.INSTANCE);

        BTActiveListener.STARTED.register(this);
        BTActiveListener.STOPPED.register(this);

        final IPathingControlManager controlManager = Utils.BT.getPathingControlManager();
        controlManager.registerProcess(new BTActiveListener());
        controlManager.registerProcess(new Teleport());
        controlManager.registerProcess(new AutoDrop());
        controlManager.registerProcess(new AutoEat());
        controlManager.registerProcess(new AutoHaste());
        controlManager.registerProcess(new AutoRepair());
        controlManager.registerProcess(new AutoSleep());
        controlManager.registerProcess(new AutoTorch());
    }

    private IBaritoneProcess lastProcess = null;

    @Override
    public void onEndTick(ClientWorld world) {
        IBaritoneProcess currentProcess = Utils.BT.getPathingControlManager()
                .mostRecentInControl().orElse(null);
        BTActiveListener.setPauseProcess(currentProcess);
        if (currentProcess != null && lastProcess != currentProcess) {
            BTScreen.debugLog(
                    "Current Process: displayName: {}, displayName0: {}, isActive: {}, isTemporary: {}, priority: {}, toString: {}, className: {}",
                    currentProcess.displayName(), currentProcess.displayName0(), currentProcess.isActive(),
                    currentProcess.isTemporary(), currentProcess.priority(), currentProcess.toString(),
                    currentProcess.getClass().getName());
        }
        lastProcess = currentProcess;
        Waiter.tickAll();
        if (Utils.isInGame()) {
            LocationCheck.checkLocation();
        }
        BTActiveListener.updateBaritoneIsActive();
    }

    @Override
    public void afterWorldChange(MinecraftClient client, ClientWorld world) {
        DataManager.save();
        if (world != null) {
            DataManager.load();
            BTScreen.debugLog("Loaded settings");
        } else {
            DataManager.clear();
        }
    }

    @Override
    public void baritoneStarted() {
        BTScreen.debugLog("Baritone is active");
        AutoDrop.updateMaxSlots();
    }

    @Override
    public void baritoneStopped() {
        BTScreen.debugLog("Baritone is inactive");
        RepeatAction.cancelRepeatAction();
    }
}
