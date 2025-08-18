package de.drvlabs.btscreen.btprocess;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.process.IBaritoneProcess;
import baritone.api.utils.IPlayerContext;
import fi.dy.masa.malilib.config.IConfigBoolean;

public abstract class BTProcessHelper implements IBaritoneProcess {
    protected static final IBaritone BARITONE = BaritoneAPI.getProvider().getPrimaryBaritone();
    protected final IPlayerContext ctx;

    public BTProcessHelper() {
        this.ctx = BARITONE.getPlayerContext();
    }

    @Override
    public String displayName0() {
        return this.getClass().getSimpleName();
    }

    /**
     * Hilfsfunktion für Konfigurationsoptionen.
     * 
     * @param config Die Konfigurationsoption, deren Wert überprüft werden soll.
     * @return {@code true}, wenn die Konfigurationsoption aktiviert ist und der
     *         Spieler sowie die Welt nicht null sind; ansonsten {@code false}.
     */
    protected boolean isActive(IConfigBoolean config) {
        return config.getBooleanValue() && ctx.player() != null && ctx.world() != null;
    }

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public double priority() {
        return 3;
    }
}
