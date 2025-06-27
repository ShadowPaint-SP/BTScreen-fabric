package de.drvlabs.btscreen.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Waiter {
	private static class WaitTask {
		final Runnable callback;
		final int interval;
		final String taskId;
		int counter = 0;
		boolean completed = false;

		WaitTask(int interval, Runnable callback) {
			this.interval = interval;
			this.callback = callback;
			this.taskId = UUID.randomUUID().toString();
		}

		public void tick() {
			if (completed) {
				return;
			}

			counter++;
			if (counter >= interval) {
				callback.run();
				completed = true;
			}
		}

		public boolean isCompleted() {
			return completed;
		}
	}

	private static final Map<String, WaitTask> waiters = new HashMap<>();
	private static final List<WaitTask> newTasksPendingAddition = new ArrayList<>();

	/**
	 * Schedules a one-shot task to run after a specified number of ticks.
	 * This method adds the task to a temporary list if called during a tickAll
	 * cycle,
	 * ensuring it's added to the main map safely afterwards.
	 *
	 * @param ticks    The number of ticks to wait before executing the callback.
	 * @param callback The Runnable to execute once the ticks have elapsed.
	 */
	public static void wait(int ticks, Runnable callback) {
		WaitTask newTask = new WaitTask(ticks, callback);
		newTasksPendingAddition.add(newTask);
	}

	/**
	 * Ticks all active waiter tasks. Tasks that complete are automatically removed.
	 * This method is designed to be called once per game tick.
	 */
	public static void tickAll() {
		List<String> tasksToRemove = new ArrayList<>();
		for (WaitTask task : waiters.values()) {
			task.tick();
			if (task.isCompleted()) {
				tasksToRemove.add(task.taskId);
			}
		}

		for (String taskId : tasksToRemove) {
			waiters.remove(taskId);
		}

		for (WaitTask newTask : newTasksPendingAddition) {
			waiters.put(newTask.taskId, newTask);
		}
		newTasksPendingAddition.clear();
	}
}