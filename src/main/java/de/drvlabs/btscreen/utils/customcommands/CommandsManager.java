package de.drvlabs.btscreen.utils.customcommands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;
import fi.dy.masa.malilib.util.JsonUtils;

public class CommandsManager {
	protected final List<Commands> commands = new ArrayList<>();

	public List<Commands> getAllCommands() {
		return commands;
	}

	public void addCommand(Commands command) {
		commands.add(command);
	}

	public void removeCommand(Commands command) {
		commands.remove(command);
	}

	public boolean executeCommand(Commands command) {
		if (command != null) {
			final Iterator<String> iterator = List.of(command.getCommand().split(";")).iterator();
			Waiter.wait(1, w -> {
				if (iterator.hasNext()) {
					Utils.sendCommand(iterator.next());
					w.start(1);
				}
			});
			return true;
		}
		return false;
	}

	public void clear() {
		commands.clear();
	}

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();

		if (commands.size() > 0) {
			JsonArray arr = new JsonArray();

			for (Commands command : commands) {
				JsonObject objCommand = command.toJson();
				if (objCommand != null) {
					arr.add(objCommand);
				}
			}
			obj.add("commands", arr);
		}

		return obj;
	}

	public void loadFromJson(JsonObject obj) {
		this.clear();

		if (JsonUtils.hasArray(obj, "commands")) {
			JsonArray arr = obj.get("commands").getAsJsonArray();
			final int size = arr.size();

			for (int i = 0; i < size; ++i) {
				JsonElement el = arr.get(i);

				if (el.isJsonObject()) {
					Commands command = Commands.fromJson(el.getAsJsonObject());

					if (command != null) {
						this.addCommand(command);
					}
				}
			}
		}
	}

}
