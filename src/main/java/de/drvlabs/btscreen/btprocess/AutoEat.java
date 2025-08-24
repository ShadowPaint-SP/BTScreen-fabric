package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_EAT;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;

public class AutoEat extends BTProcessHelper {
    @Override
    public boolean isActive() {
        return isActive(AUTO_EAT);
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
