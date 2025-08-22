package de.drvlabs.btscreen.btprocess;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.utils.Waiter;

public abstract class BTProcessWithInitializer extends BTProcessHelper {
    private final Waiter RESET_WAITER = Waiter.wait(0, w -> {
        if (isActive()) {
            w.start(1);
        } else {
            onLostControl();
        }
    });

    @Override
    public final PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (RESET_WAITER.isCompleted()) {
            RESET_WAITER.start(1);
            onInitialize();
        } else {
            return onTick();
        }
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    abstract protected void onInitialize();

    abstract protected PathingCommand onTick();

    abstract protected void onReset();

    @Override
    public final void onLostControl() {
        RESET_WAITER.cancel();
        onReset();
    }

    @Override
    public double priority() {
        if (!RESET_WAITER.isCompleted()) {
            return super.priority() + 0.01;
        }
        return super.priority();
    }
}
