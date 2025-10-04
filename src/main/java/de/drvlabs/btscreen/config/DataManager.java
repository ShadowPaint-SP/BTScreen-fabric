package de.drvlabs.btscreen.config;

import java.nio.file.Path;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.implementation.PresetMode;
import de.drvlabs.btscreen.implementation.customcommands.CommandsManager;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;

public abstract class DataManager {
    public static final Server SERVER = new Server();

    public static final class Server extends DataManager {
        private CommandsManager commandsManager = new CommandsManager();

        private Server() {
            super(true);
        }

        public CommandsManager getCommandsManager() {
            return commandsManager;
        }

        @Override
        protected void fromJson(JsonObject root) {
            if (JsonUtils.hasObject(root, "commands")) {
                commandsManager.loadFromJson(root.get("commands").getAsJsonObject());
            }
        }

        @Override
        protected void toJson(JsonObject root) {
            root.add("commands", commandsManager.toJson());
        }
    }

    public static final Dimension DIMENSION = new Dimension();

    public static final class Dimension extends DataManager {
        private static PresetMode presetMode;

        private Dimension() {
            super(false);
        }

        @Override
        protected void fromJson(JsonObject root) {
            String operation_mode = JsonUtils.getString(root, "operation_mode");
            presetMode = PresetMode.fromStringStatic(operation_mode);
        }

        @Override
        protected void toJson(JsonObject root) {
            root.add("operation_mode", new JsonPrimitive(presetMode.name()));
        }

        public PresetMode getPresetMode() {
            return presetMode;
        }

        public void setPresetMode(PresetMode mode) {
            presetMode = mode;
        }
    }

    private final boolean globalData;
    private Path file = null;

    protected DataManager(boolean globalData) {
        this.globalData = globalData;
    }

    public void load() {
        file = getCurrentStorageFile();
        JsonObject root = new JsonObject();
        JsonElement element = JsonUtils.parseJsonFileAsPath(file);
        if (element != null && element.isJsonObject()) {
            root = element.getAsJsonObject();
        }
        fromJson(root);
    }

    protected abstract void fromJson(JsonObject root);

    public void save() {
        if (file == null) {
            return;
        }
        BTScreen.debugLog("Saving data");
        JsonObject root = new JsonObject();
        toJson(root);
        JsonUtils.writeJsonToFileAsPath(root, file);
    }

    protected abstract void toJson(JsonObject root);

    public void unload() {
        save();
        file = null;
    }

    private Path getCurrentStorageFile() {
        Path dir = getCurrentConfigDirectory();
        FileUtils.createDirectoriesIfMissing(dir, BTScreen.LOGGER::warn);
        return dir.resolve(StringUtils.getStorageFileName(globalData, BTScreen.MOD_ID + "_", ".json", "default"));
    }

    public static Path getCurrentConfigDirectory() {
        return FileUtils.getConfigDirectoryAsPath().resolve(BTScreen.MOD_ID);
    }
}
