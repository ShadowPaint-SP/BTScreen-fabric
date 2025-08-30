package de.drvlabs.btscreen.btprocess;

import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.config.IConfigBoolean;

public abstract class BTProcessHelper implements IBaritoneProcess {
    @Override
    public String displayName0() {
        return this.getClass().getSimpleName();
    }

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

    protected static PathingCommand requestPause() {
        Utils.BT.getInputOverrideHandler().clearAllKeys();
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
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
