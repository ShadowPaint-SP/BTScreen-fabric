package de.drvlabs.btscreen.event;

import org.apache.commons.lang3.Strings;

import java.util.function.Consumer;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.btprocess.AutoTorch;
import de.drvlabs.btscreen.btprocess.BTActiveListener;
import de.drvlabs.btscreen.btprocess.ClearAreaPlus;
import de.drvlabs.btscreen.btprocess.Teleport;
import de.drvlabs.btscreen.btprocess.SmartWaterClear;
import de.drvlabs.btscreen.config.DataManager;
import de.drvlabs.btscreen.gui.GuiMainMenu;
import de.drvlabs.btscreen.implementation.PresetMode;
import de.drvlabs.btscreen.implementation.ProcessChanged;
import de.drvlabs.btscreen.implementation.RepeatAction;
import de.drvlabs.btscreen.implementation.RetryUnreplaceableLiquids;
import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

public final class EventHandler implements ClientTickEvents.EndLevelTick, ClientLevelEvents.AfterClientLevelChange,
        BaritoneEvents.Started, BaritoneEvents.Stopped, ClientPlayConnectionEvents.Join,
        ClientPlayConnectionEvents.Disconnect {
    private static EventHandler INSTANCE = null;

    private EventHandler() {
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register(this);
        ClientTickEvents.END_LEVEL_TICK.register(this);
        ClientPlayConnectionEvents.JOIN.register(this);
        ClientPlayConnectionEvents.DISCONNECT.register(this);
        BaritoneEvents.STARTED.register(this);
        BaritoneEvents.STOPPED.register(this);

        final Settings settings = BaritoneAPI.getSettings();
        Consumer<Component> chatLogger = settings.logger.value;
        settings.logger.value = msg -> {
            boolean suppressChat = SmartWaterClear.shouldSuppressBaritoneChat(msg)
                    || ClearAreaPlus.shouldSuppressBaritoneChat(msg);
            onBaritoneLog(msg);
            if (!suppressChat) {
                chatLogger.accept(msg);
            }
        };
        settings.toaster.value = settings.toaster.value.andThen((prefix, msg) -> onBaritoneLog(msg));
    }

    public static void register() {
        if (INSTANCE == null) {
            INSTANCE = new EventHandler();
        }
    }

    @Override
    public void onPlayReady(ClientPacketListener handler, PacketSender sender, Minecraft client) {
        DataManager.SERVER.load();
        DataManager.DIMENSION.load();
    }

    @Override
    public void onPlayDisconnect(ClientPacketListener handler, Minecraft client) {
        DataManager.SERVER.unload();
        DataManager.DIMENSION.unload();
        AutoTorch.onTeleport();
        RepeatAction.cancel();
        SmartWaterClear.resetForWorldChange();
        ClearAreaPlus.resetForWorldChange();
        Waiter.cancelAll();
    }

    @Override
    public void afterLevelChange(Minecraft client, ClientLevel level) {
        DataManager.DIMENSION.save();
        DataManager.DIMENSION.load();
        AutoTorch.onTeleport();
    }

    @Override
    public void onEndTick(ClientLevel world) {
        SmartWaterClear.onTick();
        BTActiveListener.onTick();
        ProcessChanged.onTick();
        Waiter.tickAll();
        BTActiveListener.updateBaritoneStatus();
    }

    private void onBaritoneLog(Component msg) {
        final String prefix = baritone.api.utils.Helper.getPrefix().getString();
        final String msgString = Strings.CS.removeStart(msg.getString(), prefix + " ");
        if (!ClearAreaPlus.handleBaritoneLog(msgString)) {
            RetryUnreplaceableLiquids.onBaritoneLog(msgString);
        }
    }

    @Override
    public void baritoneStarted() {
        BTScreen.debugLog("Baritone is active");
        // reinit screen if set
        Screen screen = Utils.MC.gui.screen();
        if (screen instanceof GuiMainMenu) {
            screen.init(screen.width, screen.height);
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
}
