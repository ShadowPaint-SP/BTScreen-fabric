package de.drvlabs.btscreen.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.gui.ui.UiButton;
import de.drvlabs.btscreen.gui.ui.UiScreen;
import de.drvlabs.btscreen.gui.ui.UiTextField;
import de.drvlabs.btscreen.gui.ui.UiTheme;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.hotkeys.KeybindSettings.Context;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Edits the complete malilib keybind, including activation behavior. */
public final class GuiConfigHotkey extends UiScreen {
    private final ConfigHotkey hotkey;
    private boolean recording;
    private UiTextField keys;

    public GuiConfigHotkey(GuiConfigs parent, ConfigHotkey hotkey) {
        super(Component.literal(hotkey.getConfigGuiDisplayName()), parent);
        this.hotkey = hotkey;
    }

    private int contentWidth() {
        return Math.min(width - UiTheme.MARGIN * 2, 520);
    }

    private int contentLeft() {
        return (width - contentWidth()) / 2;
    }

    @Override
    protected int headerLeft() {
        return contentLeft();
    }

    @Override
    protected void init() {
        clearWidgets();
        int left = contentLeft();
        int contentWidth = contentWidth();
        int buttonWidth = (contentWidth - 4) / 2;
        addRenderableWidget(UiButton.create(left, 38, 74, Component.translatable("gui.done"),
                (button, input) -> onClose()));
        addRenderableWidget(UiButton.create(left + 78, 38, 74,
                Component.translatable("btscreen.gui.settings.reset"),
                (button, input) -> {
                    hotkey.resetToDefault();
                    hotkey.getKeybind().resetSettingsToDefaults();
                    hotkey.checkIfClean();
                    rebuildWidgets();
                }));
        keys = new UiTextField(font, left, 91, Math.max(100, contentWidth - 108), 20,
                Component.translatable("btscreen.gui.settings.keys"));
        keys.setValue(hotkey.getStringValue());
        keys.setResponder(hotkey::setValueFromString);
        addRenderableWidget(keys);
        addRenderableWidget(UiButton.create(left + contentWidth - 104, 91, 104,
                Component.translatable(recording
                        ? "btscreen.gui.settings.pressKey" : "btscreen.gui.settings.recordKeys"),
                (button, input) -> {
                    recording = !recording;
                    button.setMessage(Component.translatable(recording
                            ? "btscreen.gui.settings.pressKey" : "btscreen.gui.settings.recordKeys"));
                }));

        KeybindSettings settings = hotkey.getKeybind().getSettings();
        settingButton(left, 128, buttonWidth,
                Component.translatable("btscreen.gui.settings.context").getString()
                        + ": " + settings.getContext().getDisplayName(),
                current -> KeybindSettings.create(
                        Context.values()[(current.getContext().ordinal() + 1) % Context.values().length],
                        current.getActivateOn(), current.getAllowExtraKeys(), current.isOrderSensitive(),
                        current.isExclusive(), current.shouldCancel(), current.getAllowEmpty()));
        settingButton(left + buttonWidth + 4, 128, buttonWidth,
                Component.translatable("btscreen.gui.settings.action").getString()
                        + ": " + settings.getActivateOn().getDisplayName(),
                current -> KeybindSettings.create(current.getContext(),
                        KeyAction.values()[(current.getActivateOn().ordinal() + 1) % KeyAction.values().length],
                        current.getAllowExtraKeys(), current.isOrderSensitive(), current.isExclusive(),
                        current.shouldCancel(), current.getAllowEmpty()));
        booleanSetting(left, 154, buttonWidth, "allowExtra", settings.getAllowExtraKeys(),
                current -> KeybindSettings.create(current.getContext(), current.getActivateOn(),
                        !current.getAllowExtraKeys(), current.isOrderSensitive(), current.isExclusive(),
                        current.shouldCancel(), current.getAllowEmpty()));
        booleanSetting(left + buttonWidth + 4, 154, buttonWidth, "orderSensitive", settings.isOrderSensitive(),
                current -> KeybindSettings.create(current.getContext(), current.getActivateOn(),
                        current.getAllowExtraKeys(), !current.isOrderSensitive(), current.isExclusive(),
                        current.shouldCancel(), current.getAllowEmpty()));
        booleanSetting(left, 180, buttonWidth, "exclusive", settings.isExclusive(),
                current -> KeybindSettings.create(current.getContext(), current.getActivateOn(),
                        current.getAllowExtraKeys(), current.isOrderSensitive(), !current.isExclusive(),
                        current.shouldCancel(), current.getAllowEmpty()));
        booleanSetting(left + buttonWidth + 4, 180, buttonWidth, "cancelInput", settings.shouldCancel(),
                current -> KeybindSettings.create(current.getContext(), current.getActivateOn(),
                        current.getAllowExtraKeys(), current.isOrderSensitive(), current.isExclusive(),
                        !current.shouldCancel(), current.getAllowEmpty()));
        booleanSetting(left, 206, buttonWidth, "allowEmpty", settings.getAllowEmpty(),
                current -> KeybindSettings.create(current.getContext(), current.getActivateOn(),
                        current.getAllowExtraKeys(), current.isOrderSensitive(), current.isExclusive(),
                        current.shouldCancel(), !current.getAllowEmpty()));
    }

    private void settingButton(int x, int y, int width, String text,
            Function<KeybindSettings, KeybindSettings> change) {
        addRenderableWidget(UiButton.create(x, y, width, Component.literal(text), (button, input) -> {
            hotkey.getKeybind().setSettings(change.apply(hotkey.getKeybind().getSettings()));
            rebuildWidgets();
        }));
    }

    private void booleanSetting(int x, int y, int width, String label, boolean value,
            Function<KeybindSettings, KeybindSettings> change) {
        String key = "btscreen.gui.settings." + label;
        settingButton(x, y, width, Component.translatable(key).getString() + ": "
                + Component.translatable(value ? "options.on" : "options.off").getString(), change);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!recording) return super.keyPressed(event);
        int key = event.key();
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            recording = false;
            rebuildWidgets();
            return true;
        }
        if (key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE) {
            hotkey.setValueFromString("");
        } else {
            List<String> names = new ArrayList<>();
            if ((event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0
                    && key != GLFW.GLFW_KEY_LEFT_CONTROL && key != GLFW.GLFW_KEY_RIGHT_CONTROL) {
                names.add(KeybindMulti.getStorageStringForKeyCode(GLFW.GLFW_KEY_LEFT_CONTROL));
            }
            if ((event.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0
                    && key != GLFW.GLFW_KEY_LEFT_SHIFT && key != GLFW.GLFW_KEY_RIGHT_SHIFT) {
                names.add(KeybindMulti.getStorageStringForKeyCode(GLFW.GLFW_KEY_LEFT_SHIFT));
            }
            if ((event.modifiers() & GLFW.GLFW_MOD_ALT) != 0
                    && key != GLFW.GLFW_KEY_LEFT_ALT && key != GLFW.GLFW_KEY_RIGHT_ALT) {
                names.add(KeybindMulti.getStorageStringForKeyCode(GLFW.GLFW_KEY_LEFT_ALT));
            }
            String keyName = KeybindMulti.getStorageStringForKeyCode(key);
            if (keyName != null) names.add(keyName);
            hotkey.setValueFromString(String.join(",", names.stream().filter(name -> name != null).toList()));
        }
        recording = false;
        rebuildWidgets();
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (recording) {
            String name = KeybindMulti.getStorageStringForKeyCode(event.button() - 100);
            if (name != null) hotkey.setValueFromString(name);
            recording = false;
            rebuildWidgets();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.text(font, Component.translatable("btscreen.gui.settings.keysHint"),
                contentLeft(), 76, UiTheme.TEXT_MUTED);
        graphics.text(font, Component.translatable("btscreen.gui.settings.clearHint"),
                contentLeft(), 116, UiTheme.TEXT_MUTED);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Configs.saveToFile();
        super.onClose();
    }
}
