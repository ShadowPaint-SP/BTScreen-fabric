package drvlabs.de.utils;

import java.util.HashMap;
import java.util.Map;

public class Waiter {
	private static class WaitTask {
		final IntervalStepper<String> stepper;
		final int interval;
		final String context;

		WaitTask(int interval, String context, Runnable callback) {
			this.interval = interval;
			this.context = context;
			this.stepper = new IntervalStepper<>(state -> {
				if (state.equals(context)) {
					callback.run();
					waiters.remove(context); // cleanup
				}
			});
		}
	}

	private static final Map<String, WaitTask> waiters = new HashMap<>();

	public static void wait(String key, int ticks, Runnable callback) {
		waiters.put(key, new WaitTask(ticks, "done", callback));
	}

	public static void tickAll() {
		for (WaitTask task : waiters.values()) {
			task.stepper.tick(task.interval, task.context);
		}
	}

	public static void cancel(String key) {
		waiters.remove(key);
	}

	public static void reset(String key) {
		WaitTask task = waiters.get(key);
		if (task != null)
			task.stepper.reset();
	}
}
