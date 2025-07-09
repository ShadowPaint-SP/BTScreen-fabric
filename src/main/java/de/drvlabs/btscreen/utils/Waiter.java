package de.drvlabs.btscreen.utils;

import java.util.HashSet;
import java.util.Iterator;

public final class Waiter {
	private final Runnable callback;
	private int waitTicks = 0;

	Waiter(Runnable callback) {
		this.callback = callback;
	}

	public void tick() {
		if (isCompleted()) {
			return;
		}
		waitTicks--;
		if (isCompleted()) {
			callback.run();
		}
	}

	public boolean isCompleted() {
		return waitTicks <= 0;
	}

	public void cancel() {
		waitTicks = 0;
	}

	/**
	 * Starts the waiter with a specified number of ticks. The waiter will execute
	 * its callback after the given ticks have elapsed. Can also be used to extend
	 * the wait time or to restart after finished execution.
	 * 
	 * @param waitTicks The number of ticks to wait before the callback is executed.
	 */
	public void start(int waitTicks) {
		this.waitTicks = waitTicks;
		pendingWaiter.add(this);
	}

	private static final HashSet<Waiter> activeWaiter = new HashSet<>();
	private static final HashSet<Waiter> pendingWaiter = new HashSet<>();

	/**
	 * Schedules a task to run after a specified number of ticks.
	 * This method adds the task to a temporary list if called during a tickAll
	 * cycle, ensuring it's added to the main set safely afterwards.
	 * 
	 * @param ticks    The number of ticks to wait before executing the callback.
	 * @param callback The Runnable to execute once the ticks have elapsed.
	 */
	public static Waiter wait(int ticks, Runnable callback) {
		Waiter newTask = new Waiter(callback);
		newTask.start(ticks);
		return newTask;
	}

	/**
	 * Ticks all active waiter tasks. Tasks that complete are automatically removed.
	 * This method is designed to be called once per game tick.
	 */
	public static void tickAll() {
		for (Iterator<Waiter> i = activeWaiter.iterator(); i.hasNext();) {
			Waiter waiter = i.next();
			waiter.tick();
			if (waiter.isCompleted()) {
				i.remove();
			}
		}
		activeWaiter.addAll(pendingWaiter);
		pendingWaiter.clear();
	}
}