package de.drvlabs.btscreen.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.DataManager;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.gui.ui.UiButton;
import de.drvlabs.btscreen.gui.ui.UiScreen;
import de.drvlabs.btscreen.gui.ui.UiTextField;
import de.drvlabs.btscreen.gui.ui.UiTheme;
import de.drvlabs.btscreen.implementation.customcommands.Commands;

public final class GuiConfigureCommand extends UiScreen {
    private final Commands command;
    private final boolean newCommand;
    private UiTextField nameField;
    private UiTextField commandField;

    public GuiConfigureCommand(@Nullable Commands command) {
        this(command, null);
    }

    public GuiConfigureCommand(@Nullable Commands command, @Nullable Screen parent) {
        super(Component.translatable(LangKeys.GUI_TITLE + ".configure_command"), parent);
        this.newCommand = command == null;
        this.command = command == null ? new Commands("", "", null) : command;
    }

    @Override
    protected void init() {
        clearWidgets();
        int panelWidth = Math.min(440, width - 24);
        int x = (width - panelWidth) / 2;
        int fieldWidth = panelWidth - 16;

        nameField = new UiTextField(font, x + 8, 82, fieldWidth, 20,
                Component.translatable(LangKeys.GUI + ".configure_command.label.name"));
        nameField.setMaxLength(256);
        nameField.setValue(command.getName());
        nameField.hint(Component.translatable(LangKeys.GUI + ".configure_command.label.name"));
        addRenderableWidget(nameField);

        commandField = new UiTextField(font, x + 8, 133, fieldWidth, 20,
                Component.translatable(LangKeys.GUI + ".configure_command.label.command"));
        commandField.setMaxLength(2048);
        commandField.setValue(command.getCommand());
        commandField.hint(Component.translatable(LangKeys.GUI + ".configure_command.label.command"));
        addRenderableWidget(commandField);

        int half = (fieldWidth - 4) / 2;
        addRenderableWidget(UiButton.create(x + 8, 168, half,
                Component.translatable("gui.cancel"), (button, input) -> onClose()));
        addRenderableWidget(UiButton.create(x + 12 + half, 168, half,
                Component.translatable(LangKeys.GUI_BUTTON + ".save"), (button, input) -> save()));
    }

    private void save() {
        String name = nameField.getValue().trim();
        String commandText = commandField.getValue().trim();
        if (name.isEmpty() || commandText.isEmpty()) {
            showNotice(NoticeTone.ERROR, LangKeys.INFO + ".guiConfigureCommand.saveError");
            return;
        }

        command.setName(name);
        command.setCommand(commandText);
        if (newCommand) {
            DataManager.SERVER.getCommandsManager().addCommand(command);
        }
        DataManager.SERVER.save();
        BTScreen.debugLog("Saved custom command '{}'", name);
        if (parent instanceof GuiCommandList commandList) {
            commandList.init(commandList.width, commandList.height);
            open(commandList);
        } else {
            open(new GuiCommandList(parent));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = Math.min(440, width - 24);
        int x = (width - panelWidth) / 2;
        drawPanel(graphics, x, 48, panelWidth, 148,
                Component.translatable(newCommand ? "btscreen.gui.heading.newCommand" : "btscreen.gui.heading.editCommand"));
        graphics.text(font, Component.translatable(LangKeys.GUI + ".configure_command.label.name"),
                x + 8, 68, UiTheme.TEXT_MUTED);
        graphics.text(font, Component.translatable(LangKeys.GUI + ".configure_command.label.command"),
                x + 8, 119, UiTheme.TEXT_MUTED);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
}
