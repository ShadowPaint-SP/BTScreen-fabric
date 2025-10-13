package de.drvlabs.btscreen.btprocess;

import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.config.IConfigBoolean;

public abstract class BTProcessHelper implements IBaritoneProcess {
    /**
     * Has no effect on the current goal or path, just requests a pause
     * 
     * @see {@link PathingCommand} with
     *      {@link PathingCommandType#REQUEST_PAUSE REQUEST_PAUSE}
     */
    protected static final PathingCommand REQUEST_PAUSE = new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    /**
     * Go and ask the next process what to do
     * 
     * @see {@link PathingCommand} with
     *      {@link PathingCommandType#DEFER DEFER}
     */
    protected static final PathingCommand DEFER = new PathingCommand(null, PathingCommandType.DEFER);
    /**
     * Request a cancel of the current path (when safe)
     * 
     * @see {@link PathingCommand} with
     *      {@link PathingCommandType#CANCEL_AND_SET_GOAL CANCEL_AND_SET_GOAL}
     */
    protected static final PathingCommand CANCEL = new PathingCommand(null, PathingCommandType.CANCEL_AND_SET_GOAL);
    /**
     * Continue along current path and current goal
     * 
     * @see {@link PathingCommand} with
     *      {@link PathingCommandType#SET_GOAL_AND_PATH SET_GOAL_AND_PATH}
     */
    protected static final PathingCommand CONTINUE = new PathingCommand(null, PathingCommandType.SET_GOAL_AND_PATH);

    /**
     * Helper function for configuration options.
     * 
     * @param config The configuration option whose value should be checked.
     * @return {@code true} if the configuration option is enabled and the
     *         player and world are not null; otherwise {@code false}.
     */
    protected static boolean isActive(IConfigBoolean config) {
        return config.getBooleanValue() && Utils.isActive();
    }

    @Override
    public String displayName0() {
        return this.getClass().getSimpleName();
    }

    /**
     * Should fully reset the {@link IBaritoneProcess}.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public abstract void onLostControl();

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public double priority() {
        return DEFAULT_PRIORITY + 0.5;
    }
}
