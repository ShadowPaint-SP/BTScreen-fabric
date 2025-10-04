package de.drvlabs.btscreen.event;

import static de.drvlabs.btscreen.config.Configs.Generic.SHOW_PROCESS_CHANGES;
import static de.drvlabs.btscreen.config.Configs.Lists.PROCESS_CHANGES_BLACKLIST;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.process.IBaritoneProcess;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.btprocess.AutoTorch;
import de.drvlabs.btscreen.btprocess.BTActiveListener;
import de.drvlabs.btscreen.btprocess.Teleport;
import de.drvlabs.btscreen.config.DataManager;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.gui.GuiMainMenu;
import de.drvlabs.btscreen.utils.RepeatAction;
import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;
import de.drvlabs.btscreen.utils.preset.PresetMode;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;

public final class EventHandler implements ClientTickEvents.EndWorldTick, ClientWorldEvents.AfterClientWorldChange,
        BaritoneEvents.Started, BaritoneEvents.Stopped, ClientPlayConnectionEvents.Join,
        ClientPlayConnectionEvents.Disconnect {
    private static EventHandler INSTANCE = null;

    private EventHandler() {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(this);
        ClientTickEvents.END_WORLD_TICK.register(this);
        ClientPlayConnectionEvents.JOIN.register(this);
        ClientPlayConnectionEvents.DISCONNECT.register(this);
        BaritoneEvents.STARTED.register(this);
        BaritoneEvents.STOPPED.register(this);

        final Settings settings = BaritoneAPI.getSettings();
        settings.logger.value = settings.logger.value.andThen(this::onBaritoneLog);
        settings.toaster.value = settings.toaster.value.andThen((prefix, msg) -> onBaritoneLog(msg));
    }

    public static void register() {
        if (INSTANCE == null) {
            INSTANCE = new EventHandler();
        }
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
            baritoneProcessChanged(lastProcess, currentProcess);
            lastProcess = currentProcess;
        }
        Waiter.tickAll();
        BTActiveListener.updateBaritoneStatus();
    }

    private int retryLiquidCount = -1;

    private void onBaritoneLog(Text msg) {
        final String prefix = baritone.api.utils.Helper.getPrefix().getString();
        final String msgString = StringUtils.removeStart(msg.getString(), prefix + " ");
        if (retryLiquidCount < 0 && msgString.equals("Unreplaceable liquids at at least:")) {
            retryLiquidCount = 2;
            Waiter.wait(100, w -> {
                if (retryLiquidCount > 0 && Utils.isPaused()) {
                    Utils.resume();
                    w.start(100);
                }
                retryLiquidCount--;
            });
        }
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
        PresetMode.SETTINGS_MANAGER.reset();
        if (!canceled) {
            Teleport.Home.FINISHED.tpToHome();
        }
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

    private void baritoneProcessChanged(IBaritoneProcess oldProcess, IBaritoneProcess newProcess) {
        if (shouldDisplayProcesses(oldProcess, newProcess)) {
            BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".processChanged",
                    toString(oldProcess), toString(newProcess)));
        }
        BTScreen.debugLog("Baritone Process changed: oldProcess: {}, newProcess: {}",
                toDebugString(oldProcess), toDebugString(newProcess));
    }
}