package de.drvlabs.btscreen.utils.customcommands;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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

			UUID hashCode = JsonUtils.hasString(obj, "hash_code") ? UUID.fromString(JsonUtils.getString(obj, "hash_code"))
					: null;
			String name = obj.get("name").getAsString();
			String command = obj.get("command").getAsString();

			Commands newCommand = new Commands(name, command, hashCode);

			return newCommand;
		}

		return null;
	}
}
