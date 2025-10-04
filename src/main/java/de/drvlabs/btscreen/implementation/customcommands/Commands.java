package de.drvlabs.btscreen.implementation.customcommands;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;
import fi.dy.masa.malilib.util.JsonUtils;

public class Commands {
	private final UUID hashId;
	private String displayName;
	private String command;

	public Commands(String name, String command, @Nullable UUID hash) {
		this.hashId = hash != null ? hash : UUID.randomUUID();
		this.displayName = name;
		this.command = command;
	}

	public String getName() {
		return this.displayName;
	}

	public void setName(String name) {
		this.displayName = name;
	}

	public String getCommand() {
		return this.command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public boolean executeCommand() {
		final Iterator<String> iterator = List.of(getCommand().split(";")).iterator();
		Waiter.wait(1, w -> {
			if (iterator.hasNext()) {
				Utils.sendCommand(iterator.next());
				w.start(1);
			}
		});
		return true;
	}

	@Nullable
	public JsonObject toJson() {
		JsonObject obj = new JsonObject();

		obj.add("name", new JsonPrimitive(this.displayName));
		obj.add("command", new JsonPrimitive(this.command));
		obj.add("hash_code", new JsonPrimitive(this.hashId.toString()));

		return obj;

	}

	@Nullable
	public static Commands fromJson(JsonObject obj) {
		if (JsonUtils.hasString(obj, "name") &&
				JsonUtils.hasString(obj, "command")) {

			UUID hashCode = JsonUtils.hasString(obj, "hash_code")
					? UUID.fromString(JsonUtils.getString(obj, "hash_code"))
					: null;
			String name = obj.get("name").getAsString();
			String command = obj.get("command").getAsString();

			Commands newCommand = new Commands(name, command, hashCode);

			return newCommand;
		}

		return null;
	}
}
