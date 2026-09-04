package de.drvlabs.btscreen.gui;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import baritone.api.selection.ISelectionManager;
import baritone.api.utils.BetterBlockPos;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.config.DataManager;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.gui.ui.UiButton;
import de.drvlabs.btscreen.gui.ui.UiGlyph;
import de.drvlabs.btscreen.gui.ui.UiScreen;
import de.drvlabs.btscreen.gui.ui.UiTextField;
import de.drvlabs.btscreen.gui.ui.UiTheme;
import de.drvlabs.btscreen.implementation.Caveman;
import de.drvlabs.btscreen.implementation.PresetMode;
import de.drvlabs.btscreen.implementation.customcommands.Commands;
import de.drvlabs.btscreen.utils.Utils;

public final class GuiMainMenu extends UiScreen {
    private static final int SECTION_TOP = 42;
    private static final int SECOND_SECTION_TOP = 104;
    private static final int ADDITIONAL_TOP = 165;

    private UiTextField blocksToReplace;
    private UiTextField blockToPlace;
    private final List<UiButton> commandButtons = new ArrayList<>();
    @Nullable
    private CommandGrid commandGrid;
    private int commandScrollRow;

    public GuiMainMenu() {
        this(null);
    }

    public GuiMainMenu(@Nullable Screen parent) {
        super(Component.translatable(LangKeys.GUI_TITLE + ".btscreen_main_menu",
                BTScreen.MOD_NAME, BTScreen.MOD_VERSION), parent);
    }

    @Override
    protected void init() {
        clearWidgets();
        commandButtons.clear();
        commandGrid = null;
        MainLayout layout = layout();

        addSettingsButton(layout);
        addSelectionControls(layout.firstX, SECTION_TOP, layout.columnWidth);
        addBotControls(layout.firstX, SECOND_SECTION_TOP, layout.columnWidth);
        addResizeControls(layout.secondX, SECTION_TOP, layout.columnWidth);
        addMoveControls(layout.secondX, SECOND_SECTION_TOP, layout.columnWidth);

        int commandsX = layout.wide ? layout.thirdX : layout.secondX;
        int commandsY = layout.wide ? SECTION_TOP : 148;
        addCommandControls(commandsX, commandsY, layout.columnWidth);

        if (hasAdditionalControls()) {
            int additionalWidth = layout.wide
                    ? layout.columnWidth * 2 + layout.columnGap
                    : layout.columnWidth;
            addAdditionalControls(layout.firstX, ADDITIONAL_TOP, additionalWidth);
        }
    }

    private void addSettingsButton(MainLayout layout) {
        Component label = Component.translatable(LangKeys.GUI_BUTTON + ".configuration_menu");
        int buttonWidth = font.width(label) + 14;
        int contentRight = layout.wide
                ? layout.thirdX + layout.columnWidth
                : layout.secondX + layout.columnWidth;
        addRenderableWidget(UiButton.compact(contentRight - buttonWidth, 8, buttonWidth, label,
                (button, input) -> open(new GuiConfigs(this))));
    }

    private void addSelectionControls(int x, int y, int sectionWidth) {
        List<GridAction> actions = List.of(
                action(LangKeys.GUI_BUTTON + ".selPosOne", "sel pos1",
                        LangKeys.GUI_BUTTON + ".hover.selPosOneInfoText"),
                action(LangKeys.GUI_BUTTON + ".selPosTwo", "sel pos2",
                        LangKeys.GUI_BUTTON + ".hover.selPosTwoInfoText"),
                action(LangKeys.GUI_BUTTON + ".selchunk", input -> selectCurrentChunk()),
                action(LangKeys.GUI_BUTTON + ".selDelete", input -> {
                    Utils.execute("sel clear");
                    showNotice(NoticeTone.WARNING, LangKeys.INFO + ".main_menu.selDelete");
                }, LangKeys.GUI_BUTTON + ".hover.selDeleteInfoText"),
                action(LangKeys.GUI_BUTTON + ".selUndo", "sel undo"),
                action(LangKeys.GUI_BUTTON + ".caveman", input -> Caveman.clipSelections(this)));
        addGrid(x, y + 16, sectionWidth, 3, actions);
    }

    private void addBotControls(int x, int y, int sectionWidth) {
        int activeButtons = Utils.isActive() ? 3 : 2;
        int buttonWidth = (sectionWidth - (activeButtons - 1) * 3) / activeButtons;
        int bx = x;

        addRenderableWidget(UiButton.compact(bx, y + 16, buttonWidth,
                Component.translatable(LangKeys.GUI_BUTTON + ".startBot"), (button, input) -> startBot())
                .tooltip(Component.translatable(LangKeys.GUI_BUTTON + ".hover.startBotInfoText")));
        bx += buttonWidth + 3;
        addRenderableWidget(UiButton.compact(bx, y + 16, buttonWidth,
                Component.translatable(LangKeys.GUI_BUTTON + ".stopBot"), (button, input) -> {
                    Utils.cancel();
                    rebuildWidgets();
                    showNotice(NoticeTone.SUCCESS, LangKeys.INFO + ".main_menu.stopBot");
                }).destructive());

        if (Utils.isActive()) {
            bx += buttonWidth + 3;
            Component pauseLabel = Component.translatable(LangKeys.GUI_BUTTON
                    + (Utils.isPaused() ? ".resume" : ".pause"));
            addRenderableWidget(UiButton.compact(bx, y + 16, buttonWidth, pauseLabel,
                    (button, input) -> {
                        if (Utils.isPaused()) {
                            Utils.resume();
                        } else {
                            Utils.pause();
                        }
                        rebuildWidgets();
                    }));
        }

        Component preset = Component.translatable(LangKeys.GUI_BUTTON + ".preset_mode",
                DataManager.DIMENSION.getPresetMode().getDisplayName());
        addRenderableWidget(UiButton.compact(x, y + 35, sectionWidth, preset,
                (button, input) -> cyclePreset(input)));
    }

    private void addResizeControls(int x, int y, int sectionWidth) {
        int buttonWidth = (sectionWidth - 6) / 3;
        Direction[] directions = Direction.values();
        for (int i = 0; i < directions.length; i++) {
            Direction direction = directions[i];
            int bx = x + (i % 3) * (buttonWidth + 3);
            int by = y + 16 + (i / 3) * 19;
            UiButton button = UiButton.compact(bx, by, buttonWidth,
                    Component.translatable(direction.translationKey),
                    (pressed, input) -> resize(direction.command, input)).glyph(direction.glyph);
            button.tooltip(Component.translatable(LangKeys.GUI_BUTTON + ".hover.plus_minus_tip_ctrl_alt_shift"));
            addRenderableWidget(button);
        }
    }

    private void addMoveControls(int x, int y, int sectionWidth) {
        int buttonWidth = (sectionWidth - 6) / 3;
        String[] axes = { "X", "Y", "Z" };
        String[] directions = { "east", "up", "north" };
        for (int i = 0; i < axes.length; i++) {
            int index = i;
            UiButton button = UiButton.compact(x + i * (buttonWidth + 3), y + 16, buttonWidth,
                    Component.literal(axes[i]), (pressed, input) -> resize("shift all " + directions[index], input));
            button.tooltip(Component.translatable(LangKeys.GUI_BUTTON + ".hover.plus_minus_tip_ctrl_alt_shift"));
            addRenderableWidget(button);
        }
    }

    private void addAdditionalControls(int x, int y, int sectionWidth) {
        int fieldGap = 3;
        int replacementWidth = Math.max(42, sectionWidth / 3);
        int blocksWidth = sectionWidth - replacementWidth - fieldGap;

        blocksToReplace = new UiTextField(font, x, y + 16, blocksWidth, 16,
                Component.translatable("btscreen.gui.label.blocksToReplace"));
        blocksToReplace.setMaxLength(512);
        blocksToReplace.setValue(String.join(", ", Configs.Lists.BLOCKS_TO_GET_REPLACED.getStrings()));
        blocksToReplace.hint(Component.translatable(LangKeys.GUI + ".textfieldContent.placeholder.blocksToReplace"));
        addRenderableWidget(blocksToReplace);

        blockToPlace = new UiTextField(font, x + blocksWidth + fieldGap, y + 16, replacementWidth, 16,
                Component.translatable("btscreen.gui.label.blockToPlace"));
        blockToPlace.setMaxLength(256);
        blockToPlace.setValue(Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue());
        blockToPlace.hint(Component.translatable(LangKeys.GUI + ".textfieldContent.placeholder.blockToPlace"));
        addRenderableWidget(blockToPlace);

        List<GridAction> actions = List.of(
                action(LangKeys.GUI_BUTTON + ".sel_copy", "sel copy"),
                action(LangKeys.GUI_BUTTON + ".sel_paste", input -> Utils.executeBuild("sel paste")),
                action(LangKeys.GUI_BUTTON + ".sel_replace", input -> runSelectionAction(SelectionAction.REPLACE)),
                action(LangKeys.GUI_BUTTON + ".sel_set", input -> runSelectionAction(SelectionAction.SET)),
                action(LangKeys.GUI_BUTTON + ".sel_shell", input -> runSelectionAction(SelectionAction.SHELL)),
                action(LangKeys.GUI_BUTTON + ".sel_walls", input -> runSelectionAction(SelectionAction.WALLS)));
        addGrid(x, y + 35, sectionWidth, sectionWidth >= 400 ? 6 : 3, actions);
    }

    private void addCommandControls(int x, int y, int sectionWidth) {
        List<Commands> commands = DataManager.SERVER.getCommandsManager().getAllCommands();
        int totalRows = (commands.size() + 1) / 2;
        int availableRows = Math.max(0, (height - y - 16 - 24) / 19);
        int visibleRows = Math.min(totalRows, availableRows);
        commandGrid = new CommandGrid(x, y + 16, sectionWidth, visibleRows, totalRows);
        commandScrollRow = Math.max(0, Math.min(commandScrollRow, commandGrid.maxScrollRow()));

        boolean scrollable = commandGrid.isScrollable();
        int gridWidth = sectionWidth - (scrollable ? 5 : 0);
        int buttonWidth = (gridWidth - 3) / 2;

        for (int i = 0; i < commands.size(); i++) {
            Commands command = commands.get(i);
            UiButton commandButton = UiButton.compact(0, 0, buttonWidth,
                    Component.literal(command.getName()), (button, input) -> {
                        command.executeCommand();
                        minecraft.setScreenAndShow(null);
                    }).tooltip(Component.literal(command.getCommand()));
            commandButtons.add(commandButton);
            addRenderableWidget(commandButton);
        }
        layoutCommandButtons();

        int managementY = commandGrid.top + visibleRows * 19;
        int half = (sectionWidth - 3) / 2;
        addRenderableWidget(UiButton.compact(x, managementY, half,
                Component.translatable("btscreen.gui.button.manageCommands"),
                (button, input) -> open(new GuiCommandList(this))));
        addRenderableWidget(UiButton.compact(x + half + 3, managementY, half,
                Component.translatable(LangKeys.GUI_BUTTON + ".change_menu.createCommand"),
                (button, input) -> open(new GuiConfigureCommand(null, this))));
    }

    private void layoutCommandButtons() {
        if (commandGrid == null) {
            return;
        }
        int gridWidth = commandGrid.width - (commandGrid.isScrollable() ? 5 : 0);
        int buttonWidth = (gridWidth - 3) / 2;
        for (int i = 0; i < commandButtons.size(); i++) {
            UiButton button = commandButtons.get(i);
            int commandRow = i / 2;
            int visibleRow = commandRow - commandScrollRow;
            button.setX(commandGrid.x + (i % 2) * (buttonWidth + 3));
            button.setY(commandGrid.top + visibleRow * 19);
            button.setWidth(buttonWidth);
            button.visible = visibleRow >= 0 && visibleRow < commandGrid.visibleRows;
        }
    }

    private void addGrid(int x, int y, int sectionWidth, int columns, List<GridAction> actions) {
        int buttonWidth = (sectionWidth - (columns - 1) * 3) / columns;
        for (int i = 0; i < actions.size(); i++) {
            GridAction action = actions.get(i);
            UiButton button = UiButton.compact(x + (i % columns) * (buttonWidth + 3),
                    y + (i / columns) * 19, buttonWidth, Component.translatable(action.translationKey),
                    (pressed, input) -> action.action.run(input));
            if (action.tooltipKey != null) {
                button.tooltip(Component.translatable(action.tooltipKey));
            }
            addRenderableWidget(button);
        }
    }

    private void startBot() {
        String command = DataManager.DIMENSION.getPresetMode().getCommand();
        if (command == null || command.isEmpty()) {
            return;
        }
        Utils.executeBuild(command);
        rebuildWidgets();
        showNotice(NoticeTone.SUCCESS, LangKeys.INFO + ".main_menu.startBot");
    }

    private void cyclePreset(InputWithModifiers input) {
        PresetMode mode = DataManager.DIMENSION.getPresetMode().cycle(input.input() != 1);
        DataManager.DIMENSION.setPresetMode(mode);
        rebuildWidgets();
    }

    private void resize(String command, InputWithModifiers input) {
        int amount = input.input() == 1 ? -1 : 1;
        if (input.hasControlDown()) {
            amount *= 100;
        }
        if (input.hasShiftDown()) {
            amount *= 10;
        }
        if (input.hasAltDown()) {
            amount *= 5;
        }
        Utils.execute("sel " + command + " " + amount);
    }

    private void runSelectionAction(SelectionAction action) {
        storeBlockInputs();
        String blocks = String.join(" ", Configs.Lists.BLOCKS_TO_GET_REPLACED.getStrings());
        String replacement = Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue();
        switch (action) {
            case REPLACE -> Utils.executeBuild("sel replace " + blocks + " " + replacement);
            case SET -> Utils.executeBuild("sel set " + blocks);
            case SHELL -> Utils.executeBuild("sel shell " + replacement);
            case WALLS -> Utils.executeBuild("sel walls " + replacement);
        }
    }

    private void storeBlockInputs() {
        if (blocksToReplace != null) {
            List<String> blocks = Arrays.stream(blocksToReplace.getValue().split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .toList();
            if (!blocks.isEmpty()) {
                Configs.Lists.BLOCKS_TO_GET_REPLACED.setStrings(blocks);
            }
        }
        if (blockToPlace != null && !blockToPlace.getValue().isBlank()) {
            Configs.Lists.BLOCK_TO_REPLACE_WITH.setValueFromString(blockToPlace.getValue().trim());
        }
    }

    private boolean hasAdditionalControls() {
        return DataManager.DIMENSION.getPresetMode().additionalControls;
    }

    private MainLayout layout() {
        int contentWidth = Math.min(width - UiTheme.MARGIN * 2, 900);
        int contentX = (width - contentWidth) / 2;
        boolean wide = contentWidth >= 660;
        int columnGap = wide ? 22 : 16;
        int columnCount = wide ? 3 : 2;
        int columnWidth = (contentWidth - columnGap * (columnCount - 1)) / columnCount;
        int secondX = contentX + columnWidth + columnGap;
        int thirdX = secondX + columnWidth + columnGap;
        return new MainLayout(contentX, secondX, thirdX, columnWidth, columnGap, wide);
    }

    @Override
    protected int headerLeft() {
        return layout().firstX;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0 && commandGrid != null && commandGrid.isScrollable()
                && commandGrid.contains(mouseX, mouseY)) {
            int direction = scrollY > 0 ? -1 : 1;
            int nextRow = Math.max(0, Math.min(commandGrid.maxScrollRow(), commandScrollRow + direction));
            if (nextRow != commandScrollRow) {
                commandScrollRow = nextRow;
                layoutCommandButtons();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        MainLayout layout = layout();
        drawSectionHeading(graphics, layout.firstX, SECTION_TOP, layout.columnWidth,
                Component.translatable(LangKeys.GUI_SECTION + ".label.selManagement"));
        drawSectionHeading(graphics, layout.firstX, SECOND_SECTION_TOP, layout.columnWidth,
                Component.translatable(LangKeys.GUI_SECTION + ".label.botControl"));
        drawSectionHeading(graphics, layout.secondX, SECTION_TOP, layout.columnWidth,
                Component.translatable(LangKeys.GUI_SECTION + ".label.boxResizing"));
        drawSectionHeading(graphics, layout.secondX, SECOND_SECTION_TOP, layout.columnWidth,
                Component.translatable(LangKeys.GUI_SECTION + ".label.boxMoving"));

        int commandsX = layout.wide ? layout.thirdX : layout.secondX;
        int commandsY = layout.wide ? SECTION_TOP : 148;
        drawSectionHeading(graphics, commandsX, commandsY, layout.columnWidth,
                Component.translatable(LangKeys.GUI_BUTTON + ".change_menu.command_list_manager"));
        drawCommandScrollbar(graphics);

        if (hasAdditionalControls()) {
            int additionalWidth = layout.wide
                    ? layout.columnWidth * 2 + layout.columnGap
                    : layout.columnWidth;
            drawSectionHeading(graphics, layout.firstX, ADDITIONAL_TOP, additionalWidth,
                    Component.translatable(LangKeys.GUI_SECTION + ".label.additionalControls"));
        }

        String status = Utils.isActive() ? (Utils.isPaused() ? "PAUSED" : "RUNNING") : "IDLE";
        int statusColor = Utils.isActive()
                ? (Utils.isPaused() ? UiTheme.WARNING : UiTheme.SUCCESS)
                : UiTheme.TEXT_MUTED;
        graphics.text(font, status,
                layout.firstX + layout.columnWidth - font.width(status), SECOND_SECTION_TOP, statusColor);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void drawCommandScrollbar(GuiGraphicsExtractor graphics) {
        if (commandGrid == null || !commandGrid.isScrollable()) {
            return;
        }
        int trackHeight = commandGrid.visibleRows * 19 - 3;
        int trackX = commandGrid.x + commandGrid.width - 2;
        graphics.fill(trackX, commandGrid.top, trackX + 2, commandGrid.top + trackHeight, 0x66555A5E);

        int thumbHeight = Math.max(8, trackHeight * commandGrid.visibleRows / commandGrid.totalRows);
        int thumbTravel = trackHeight - thumbHeight;
        int thumbY = commandGrid.top
                + thumbTravel * commandScrollRow / commandGrid.maxScrollRow();
        graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, UiTheme.ACCENT);
    }

    public static void selectCurrentChunk() {
        ISelectionManager selectionManager = Utils.BT.getSelectionManager();
        ChunkPos chunkPos = Utils.MC.player.chunkPosition();
        BetterBlockPos corner1 = new BetterBlockPos(chunkPos.getWorldPosition().below(59));
        BetterBlockPos corner2 = new BetterBlockPos(chunkPos.getWorldPosition().offset(15,
                Utils.MC.level.getChunk(corner1).getHighestFilledSectionIndex() * 16 - 48, 15));
        selectionManager.addSelection(corner1, corner2);
    }

    private static GridAction action(String translationKey, String command) {
        return action(translationKey, input -> Utils.execute(command));
    }

    private static GridAction action(String translationKey, String command, String tooltipKey) {
        return action(translationKey, input -> Utils.execute(command), tooltipKey);
    }

    private static GridAction action(String translationKey, InputAction action) {
        return new GridAction(translationKey, action, null);
    }

    private static GridAction action(String translationKey, InputAction action, String tooltipKey) {
        return new GridAction(translationKey, action, tooltipKey);
    }

    @FunctionalInterface
    private interface InputAction {
        void run(InputWithModifiers input);
    }

    private record GridAction(String translationKey, InputAction action, @Nullable String tooltipKey) {
    }

    private record MainLayout(int firstX, int secondX, int thirdX, int columnWidth, int columnGap, boolean wide) {
    }

    private record CommandGrid(int x, int top, int width, int visibleRows, int totalRows) {
        private boolean isScrollable() {
            return visibleRows > 0 && totalRows > visibleRows;
        }

        private int maxScrollRow() {
            return Math.max(0, totalRows - visibleRows);
        }

        private boolean contains(double mouseX, double mouseY) {
            int gridHeight = visibleRows * 19 - 3;
            return mouseX >= x && mouseX < x + width
                    && mouseY >= top && mouseY < top + gridHeight;
        }
    }

    private enum SelectionAction {
        REPLACE, SET, SHELL, WALLS
    }

    private enum Direction {
        NORTH(LangKeys.GUI_BUTTON + ".north", "expand all north", UiGlyph.NORTH),
        UP(LangKeys.GUI_BUTTON + ".up", "expand all up", UiGlyph.UP),
        EAST(LangKeys.GUI_BUTTON + ".east", "expand all east", UiGlyph.EAST),
        WEST(LangKeys.GUI_BUTTON + ".west", "expand all west", UiGlyph.WEST),
        DOWN(LangKeys.GUI_BUTTON + ".down", "expand all down", UiGlyph.DOWN),
        SOUTH(LangKeys.GUI_BUTTON + ".south", "expand all south", UiGlyph.SOUTH);

        private final String translationKey;
        private final String command;
        private final UiGlyph glyph;

        Direction(String translationKey, String command, UiGlyph glyph) {
            this.translationKey = translationKey;
            this.command = command;
            this.glyph = glyph;
        }
    }
}
