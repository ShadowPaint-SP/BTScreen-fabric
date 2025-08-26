package de.drvlabs.btscreen.event;

import java.util.List;

import baritone.api.pathing.calc.IPathingControlManager;
import baritone.api.process.IBaritoneProcess;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.data.DataManager;
import de.drvlabs.btscreen.gui.GuiConfigs;
import de.drvlabs.btscreen.utils.BotStatus;
import de.drvlabs.btscreen.utils.LocationCheck;
import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;
import de.drvlabs.btscreen.utils.behavior.AutoTorch;
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
        if (Utils.isInGame()) {
            Waiter.tickAll();
            if (DataManager.getActive() && Utils.BT.getPathingControlManager()
                    .mostRecentInControl().isPresent()) {
                if (DataManager.getBotStatus() == BotStatus.IDLE) {
                    return;
                }
                if (DataManager.getBotStatus() == BotStatus.MINING) {
                    LocationCheck.checkLocation();
                }
                if (Configs.Generic.AUTO_TORCH.getBooleanValue()) {
                    if (DataManager.getBotStatus() == BotStatus.MINING && AutoTorch.blockNeedsTorch(Utils.MC)) {
                        AutoTorch.prepare(Utils.MC);
                    }
                    if (DataManager.getBotStatus() == BotStatus.LIGHTING) {
                        AutoTorch.onTick(Utils.MC);
                    }
                }
            } else {
                if (DataManager.getBotStatus() != BotStatus.IDLE) {
                    DataManager.getInstance().setActive(false);
                    DataManager.setBotStatus(BotStatus.IDLE);
                }
            }
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
