package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_TORCH;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;

public class AutoTorch extends BTProcessHelper {
    @Override
    public boolean isActive() {
        return isActive(AUTO_TORCH);
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        // TODO Auto-generated method stub
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    @Override
    public void onLostControl() {
        // TODO Auto-generated method stub
    }
}
