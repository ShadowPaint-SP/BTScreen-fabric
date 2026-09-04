package de.drvlabs.btscreen.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

public final class GuiCommandList extends UiScreen {
    private static final int ROW_HEIGHT = 34;

    private final List<CommandRow> rows = new ArrayList<>();
    private UiTextField searchField;
    private int scrollOffset;

    public GuiCommandList() {
        this(null);
    }

    public GuiCommandList(@Nullable Screen parent) {
        super(Component.translatable(LangKeys.GUI_TITLE + ".manage_command_list"), parent);
    }

    @Override
    protected void init() {
        clearWidgets();
        rows.clear();
        int panelX = UiTheme.MARGIN;
        int panelWidth = width - UiTheme.MARGIN * 2;

        searchField = new UiTextField(font, panelX + 8, 78, panelWidth - 16, 20,
                Component.translatable("btscreen.gui.label.search"));
        searchField.hint(Component.translatable("btscreen.gui.label.search"));
        searchField.setResponder(value -> layoutRows());
        addRenderableWidget(searchField);

        for (Commands command : DataManager.SERVER.getCommandsManager().getAllCommands()) {
            int buttonsWidth = Math.min(204, panelWidth / 2);
            int buttonWidth = (buttonsWidth - 8) / 3;
            UiButton execute = UiButton.create(0, 0, buttonWidth,
                    Component.translatable(LangKeys.GUI_BUTTON + ".customCommand.execute"), (button, input) -> {
                        command.executeCommand();
                        minecraft.setScreenAndShow(null);
                    });
            UiButton edit = UiButton.create(0, 0, buttonWidth,
                    Component.translatable(LangKeys.GUI_BUTTON + ".customCommand.configure"),
                    (button, input) -> open(new GuiConfigureCommand(command, this)));
            UiButton remove = UiButton.create(0, 0, buttonWidth,
                    Component.translatable(LangKeys.GUI_BUTTON + ".customCommand.remove"),
                    (button, input) -> remove(command)).destructive();
            addRenderableWidget(execute);
            addRenderableWidget(edit);
            addRenderableWidget(remove);
            rows.add(new CommandRow(command, execute, edit, remove));
        }

        int footerY = height - 27;
        addRenderableWidget(UiButton.create(panelX, footerY, 100,
                Component.translatable("gui.back"), (button, input) -> onClose()));
        int createWidth = Math.min(140, panelWidth - 105);
        addRenderableWidget(UiButton.create(width - UiTheme.MARGIN - createWidth, footerY, createWidth,
                Component.translatable(LangKeys.GUI_BUTTON + ".change_menu.createCommand"),
                (button, input) -> open(new GuiConfigureCommand(null, this))));
        layoutRows();
    }

    private void remove(Commands command) {
        DataManager.SERVER.getCommandsManager().removeCommand(command);
        DataManager.SERVER.save();
        rebuildWidgets();
        showNotice(NoticeTone.WARNING, Component.translatable("btscreen.gui.notice.commandRemoved", command.getName()));
    }

    private void layoutRows() {
        if (searchField == null) {
            return;
        }
        String query = searchField.getValue().toLowerCase(Locale.ROOT);
        int panelX = UiTheme.MARGIN;
        int panelWidth = width - UiTheme.MARGIN * 2;
        int buttonArea = Math.min(204, panelWidth / 2);
        int buttonWidth = (buttonArea - 8) / 3;
        int contentTop = 104;
        int contentBottom = height - 36;
        int matchingRows = (int) rows.stream()
                .filter(row -> query.isBlank()
                        || row.command.getName().toLowerCase(Locale.ROOT).contains(query)
                        || row.command.getCommand().toLowerCase(Locale.ROOT).contains(query))
                .count();
        int maxScroll = Math.max(0, matchingRows * ROW_HEIGHT - (contentBottom - contentTop));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        int visibleIndex = 0;

        for (CommandRow row : rows) {
            boolean matches = query.isBlank()
                    || row.command.getName().toLowerCase(Locale.ROOT).contains(query)
                    || row.command.getCommand().toLowerCase(Locale.ROOT).contains(query);
            int y = contentTop + visibleIndex * ROW_HEIGHT - scrollOffset;
            boolean visible = matches && y >= contentTop && y + ROW_HEIGHT <= contentBottom;
            if (matches) {
                visibleIndex++;
            }
            row.y = y;
            row.visible = visible;
            UiButton[] buttons = { row.execute, row.edit, row.remove };
            for (int i = 0; i < buttons.length; i++) {
                UiButton button = buttons[i];
                button.setX(panelX + panelWidth - 8 - buttonArea + i * (buttonWidth + 4));
                button.setY(y + 6);
                button.visible = visible;
            }
        }

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseY >= 104 && mouseY < height - 36) {
            scrollOffset = Math.max(0, scrollOffset - (int) Math.round(scrollY * 24));
            layoutRows();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelX = UiTheme.MARGIN;
        int panelWidth = width - UiTheme.MARGIN * 2;
        drawPanel(graphics, panelX, 48, panelWidth, height - 84,
                Component.translatable(LangKeys.GUI_TITLE + ".manage_command_list"));
        for (CommandRow row : rows) {
            if (!row.visible) {
                continue;
            }
            int rowColor = row.command.hashCode() % 2 == 0 ? 0x48182229 : 0x3820272C;
            graphics.fill(panelX + 5, row.y, panelX + panelWidth - 5, row.y + ROW_HEIGHT - 2, rowColor);
            graphics.text(font, row.command.getName(), panelX + 10, row.y + 6, UiTheme.TEXT);
            graphics.text(font, font.plainSubstrByWidth(row.command.getCommand(), Math.max(20, panelWidth / 2 - 20)),
                    panelX + 10, row.y + 19, UiTheme.TEXT_MUTED);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private static final class CommandRow {
        private final Commands command;
        private final UiButton execute;
        private final UiButton edit;
        private final UiButton remove;
        private int y;
        private boolean visible;

        private CommandRow(Commands command, UiButton execute, UiButton edit, UiButton remove) {
            this.command = command;
            this.execute = execute;
            this.edit = edit;
            this.remove = remove;
        }
    }
}
