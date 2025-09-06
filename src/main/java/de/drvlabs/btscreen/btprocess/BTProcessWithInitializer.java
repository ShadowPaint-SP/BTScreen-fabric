package de.drvlabs.btscreen.btprocess;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.utils.Utils;
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
        if (isSafeToCancel) {
            if (RESET_WAITER.isCompleted()) {
                RESET_WAITER.start(1);
                Utils.BT.getInputOverrideHandler().clearAllKeys();
                onInitialize();
            } else {
                return onTick();
            }
        }
        return REQUEST_PAUSE;
    }

    /**
     * Called at the first tick if {@link #isActive} is {@code true} and process is
     * in control of pathing only at the first tick
     */
    abstract protected void onInitialize();

    /**
     * Called when this process is in control of pathing and initialized
     * 
     * @return What the IPathingBehavior should do
     */
    abstract protected PathingCommand onTick();

    /**
     * Called after {@link #isActive} returns {@code false} again, in the same, or
     * next tick
     */
    abstract protected void onReset();

    @Override
    public final void onLostControl() {
        RESET_WAITER.cancel();
        onReset();
    }

    @Override
    public double priority() {
        if (!RESET_WAITER.isCompleted()) {
            // makes sure to be the first process if initialized
            return super.priority() + 0.001;
        }
        return super.priority();
    }
}
