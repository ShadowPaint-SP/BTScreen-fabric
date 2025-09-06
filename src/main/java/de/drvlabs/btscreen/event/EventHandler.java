package de.drvlabs.btscreen.event;

import static de.drvlabs.btscreen.config.Configs.Generic.SHOW_PROCESS_CHANGES;
import static de.drvlabs.btscreen.config.Configs.Lists.PROCESS_CHANGES_BLACKLIST;

import java.util.List;

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
import de.drvlabs.btscreen.btprocess.LocationCheck;
import de.drvlabs.btscreen.btprocess.Teleport;
import de.drvlabs.btscreen.config.DataManager;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.gui.GuiConfigs;
import de.drvlabs.btscreen.gui.GuiMainMenu;
import de.drvlabs.btscreen.utils.RepeatAction;
import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndWorldTick;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents.AfterClientWorldChange;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;

public final class EventHandler implements EndWorldTick, AfterClientWorldChange, ClientStarted,
        BaritoneEvents.Started, BaritoneEvents.Stopped, BaritoneEvents.ProcessChanged,
        ClientPlayConnectionEvents.Join, ClientPlayConnectionEvents.Disconnect {
    @Override
    public void onClientStarted(MinecraftClient client) {
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(BTScreen.MOD_ID, BTScreen.MOD_NAME, GuiConfigs::new));

        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.INSTANCE);

        BaritoneEvents.STARTED.register(this);
        BaritoneEvents.STOPPED.register(this);
        BaritoneEvents.PROCESS_CHANGED.register(this);

        final IPathingControlManager controlManager = Utils.BT.getPathingControlManager();
        controlManager.registerProcess(new BTActiveListener());
        controlManager.registerProcess(new Teleport());
        controlManager.registerProcess(new AutoDrop());
        controlManager.registerProcess(new AutoEat());
        controlManager.registerProcess(new AutoHaste());
        controlManager.registerProcess(new AutoRepair());
        controlManager.registerProcess(new AutoSleep());
        controlManager.registerProcess(new AutoTorch());
        controlManager.registerProcess(new LocationCheck());
    }

    @Override
    public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        DataManager.SERVER.load();
        DataManager.DIMENSION.load();
    }

    @Override
    public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        DataManager.SERVER.unload();
        DataManager.DIMENSION.unload();
        AutoTorch.onTeleport();
        RepeatAction.cancel();
        Waiter.cancelAll();
    }

    @Override
    public void afterWorldChange(MinecraftClient client, ClientWorld world) {
        DataManager.DIMENSION.save();
        DataManager.DIMENSION.load();
        AutoTorch.onTeleport();
    }

    private IBaritoneProcess lastProcess = null;

    @Override
    public void onEndTick(ClientWorld world) {
        IBaritoneProcess currentProcess = Utils.getActiveProcess();
        BTActiveListener.setPauseProcess(currentProcess);
        if (lastProcess != currentProcess) {
            BaritoneEvents.PROCESS_CHANGED.invoker().onProcessChanged(lastProcess, currentProcess);
        }
        lastProcess = currentProcess;
        Waiter.tickAll();
        BTActiveListener.updateBaritoneStatus();
    }

    @Override
    public void baritoneStarted() {
        BTScreen.debugLog("Baritone is active");
        // reinit screen if set
        Screen screen = Utils.MC.currentScreen;
        if (screen instanceof GuiMainMenu) {
            screen.init(Utils.MC, screen.width, screen.height);
        }
    }

    @Override
    public void baritoneStopped(boolean canceled) {
        BTScreen.debugLog("Baritone is inactive. canceled: " + canceled);
        RepeatAction.baritoneStopped(canceled);
    }

    private static boolean shouldDisplayProcesses(IBaritoneProcess... processes) {
        if (!SHOW_PROCESS_CHANGES.getBooleanValue()) {
            return false;
        }
        final List<String> strings = PROCESS_CHANGES_BLACKLIST.getStrings();
        for (IBaritoneProcess process : processes) {
            String processName = process == null ? "IDLE" : process.getClass().getSimpleName();
            if (strings.contains(processName)) {
                return false;
            }
        }
        return true;
    }

    private static String toDebugString(IBaritoneProcess process) {
        return process == null ? "IDLE"
                : String.format("%s[%s, isTemporary: %s, priority: %s, toString: %s]",
                        process.getClass().getSimpleName(), process.displayName0(), process.isTemporary(),
                        process.priority(), process.toString());
    }

    private static String toString(IBaritoneProcess process) {
        return process == null ? "IDLE"
                : String.format("%s[%s]", process.getClass().getSimpleName(), process.displayName0());
    }

    public void onProcessChanged(IBaritoneProcess oldProcess, IBaritoneProcess newProcess) {
        if (shouldDisplayProcesses(oldProcess, newProcess)) {
            BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".processChanged",
                    toString(oldProcess), toString(newProcess)));
        }
        BTScreen.debugLog("Baritone Process changed: oldProcess: {}, newProcess: {}",
                toDebugString(oldProcess), toDebugString(newProcess));
    }
}