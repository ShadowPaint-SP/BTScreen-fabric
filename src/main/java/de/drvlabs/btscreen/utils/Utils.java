package de.drvlabs.btscreen.utils;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.process.IBaritoneProcess;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.event.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class Utils {
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final IBaritone BT = BaritoneAPI.getProvider().getPrimaryBaritone();

    public static boolean isInGame() {
        return MC.player != null && Utils.MC.world != null;
    }

    public static IBaritoneProcess getActiveProcess() {
        return BT.getPathingControlManager().mostRecentInControl().orElse(null);
    }

    public static void execute(String command) {
        BT.getCommandManager().execute(command);
    }

    public static void executeBuild(String command) {
        if (Configs.Generic.REPEAT_ACTION.getBooleanValue()) {
            RepeatAction.trackCommand(command);
        }
        execute(command);
        DataManager.setBotStatus(BotStatus.MINING);
    }

    public static boolean isActive() {
        return isInGame() && EventHandler.isBaritoneActive();
    }

    public static void pause(BotStatus newStatus) {
        execute("pause");
        DataManager.setBotStatus(newStatus);
    }

    public static void resume() {
        execute("resume");
        DataManager.setBotStatus(BotStatus.MINING);
    }

    public static void cancel() {
        BT.getPathingBehavior().cancelEverything();
        execute("stop");
        DataManager.getInstance().setActive(false);
        RepeatAction.cancelRepeatAction();
        DataManager.setBotStatus(BotStatus.IDLE);
    }

    public static void tpTo(String homeName) {
        if (MC.player != null) {
            if (Configs.Generic.HOME_COMMAND.getStringValue().equals("tp")
                    && homeName.equals(Configs.Generic.DROP_HOME.getStringValue())) {
                MC.player.networkHandler.sendChatCommand(Configs.Generic.HOME_COMMAND.getStringValue()
                        + " " + MC.player.getNameForScoreboard() + " " + homeName + " 180 0");
                return;
            }
            MC.player.networkHandler
                    .sendChatCommand(Configs.Generic.HOME_COMMAND.getStringValue() + " " + homeName);
        }
    }

    public static void setHome(String homeName) {
        if (MC.player != null) {
            if (Configs.Generic.HOME_COMMAND.getStringValue().equals("tp")) {
                Configs.Generic.MINE_HOME.setValueFromString(MC.player.getBlockPos().getX() + " "
                        + MC.player.getBlockPos().getY() + " " + MC.player.getBlockPos().getZ());
                return;
            }
            MC.player.networkHandler
                    .sendChatCommand(Configs.Generic.SETHOME_COMMAND.getStringValue() + " " + homeName);
        }
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
        MC.getNetworkHandler().sendChatCommand(command);
    }
}
