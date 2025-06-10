package drvlabs.de.utils;

import java.util.function.Consumer;

public class IntervalStepper<T> {
	private final Consumer<T> callback;
	private int counter = 0;

	public IntervalStepper(Consumer<T> callback) {
		this.callback = callback;
	}

	public void tick(int interval, T data) {
		counter++;
		if (counter >= interval) {
			counter = 0;
			callback.accept(data);
		}
	}

	public void reset() {
		counter = 0;
	}
}
