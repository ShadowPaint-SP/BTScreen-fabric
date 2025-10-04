package de.drvlabs.btscreen.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class BaritoneEvents {
    public static final Event<Started> STARTED = EventFactory
            .createArrayBacked(Started.class, callbacks -> () -> {
                for (Started callback : callbacks) {
                    callback.baritoneStarted();
                }
            });

    public static final Event<Paused> PAUSED = EventFactory
            .createArrayBacked(Paused.class, callbacks -> () -> {
                for (Paused callback : callbacks) {
                    callback.baritonePaused();
                }
            });

    public static final Event<Resumed> RESUMED = EventFactory
            .createArrayBacked(Resumed.class, callbacks -> () -> {
                for (Resumed callback : callbacks) {
                    callback.baritoneResumed();
                }
            });

    public static final Event<Stopped> STOPPED = EventFactory
            .createArrayBacked(Stopped.class, callbacks -> canceled -> {
                for (Stopped callback : callbacks) {
                    callback.baritoneStopped(canceled);
                }
            });

    private BaritoneEvents() {
    }

    @FunctionalInterface
    public interface Started {
        void baritoneStarted();
    }

    @FunctionalInterface
    public interface Paused {
        void baritonePaused();
    }

    @FunctionalInterface
    public interface Resumed {
        void baritoneResumed();
    }

    @FunctionalInterface
    public interface Stopped {
        void baritoneStopped(boolean canceled);
    }
}
