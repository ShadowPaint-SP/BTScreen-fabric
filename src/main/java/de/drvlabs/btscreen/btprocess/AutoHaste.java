package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_HASTE;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;

public class AutoHaste extends BTProcessHelper {
    @Override
    public boolean isActive() {
        return isActive(AUTO_HASTE);
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
