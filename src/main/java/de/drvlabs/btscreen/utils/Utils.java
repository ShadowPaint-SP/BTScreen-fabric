package de.drvlabs.btscreen.utils;

import org.apache.commons.lang3.StringUtils;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.process.IBaritoneProcess;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.btprocess.BTActiveListener;
import de.drvlabs.btscreen.config.DataManager;
import de.drvlabs.btscreen.utils.preset.PresetMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Utils {
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final IBaritone BT = BaritoneAPI.getProvider().getPrimaryBaritone();

    public static boolean isInGame() {
        return MC.player != null && Utils.MC.world != null;
    }

    public static IBaritoneProcess getActiveProcess() {
        return BT.getPathingControlManager().mostRecentInControl().orElse(null);
    }

    public static Identifier getWorldId() {
        if (!isInGame())
            return null;
        return MC.world.getRegistryKey().getValue();
    }

    public static void execute(String command) {
        BTScreen.debugLog("Executing command: " + command);
        BT.getCommandManager().execute(command);
    }

    public static void executeBuild(String command) {
        PresetMode presetMode = DataManager.DIMENSION.getPresetMode();
        if (!presetMode.setSettings())
            return;
        BTScreen.debugLog("Updated settings to: " + presetMode.name());
        RepeatAction.trackCommand(command);
        execute(command);
    }

    public static boolean isActive() {
        return isInGame() && BTActiveListener.isBaritoneActive();
    }

    public static boolean isPaused() {
        return BTActiveListener.isBaritonePaused();
    }

    public static void pause() {
        execute("pause");
    }

    public static void resume() {
        execute("resume");
    }

    public static void cancel() {
        execute("cancel");
    }

    public static void chatMessage(Text... message) {
        if (!isInGame())
            return;
        MutableText msg = Text.literal("");
        for (Text text : message) {
            msg.append(text);
        }
        MC.getMessageHandler().onGameMessage(msg, false);
    }

    public static void overlayMessage(Text message) {
        if (!isInGame())
            return;
        MC.inGameHud.setOverlayMessage(message, false);
    }

    public static void sendCommand(String command) {
        if (!isInGame())
            return;
        String normalizeCommand = StringUtils.normalizeSpace(command.trim());
        if (normalizeCommand.isEmpty())
            return;
        MC.getNetworkHandler().sendChatCommand(normalizeCommand);
    }
}
