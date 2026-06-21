package de.drvlabs.btscreen.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import baritone.api.selection.ISelectionManager;
import baritone.api.utils.BetterBlockPos;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.config.DataManager;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.implementation.PresetMode;
import de.drvlabs.btscreen.implementation.customcommands.Commands;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;

public class GuiMainMenu extends GuiBase {
    private final int textColor = 0xFEFEFEFF;
    public static GuiTextFieldGeneric textBlocksToReplace;
    public static GuiTextFieldGeneric textBlocksToPlace;

    public GuiMainMenu() {
        this.title = StringUtils.translate(LangKeys.GUI_TITLE + ".btscreen_main_menu",
                BTScreen.MOD_NAME, BTScreen.MOD_VERSION);
    }

    @Override
    public void initGui() {
        super.initGui();
        int x = 12;
        int y = 30;
        int width = 68;
        String label;
        ButtonGeneric button;

        ////////////////////////////////////////////////// Selection Management
        this.addLabel(x, y, width, 20, textColor, LangKeys.GUI_SECTION + ".label.selManagement");
        y += 22;
        x += 5;
        this.createButton(x, y + 22, -1, ButtonListener.Type.SELCHUNK, false);
        x += this.createButton(x, y, -1, ButtonListener.Type.SELPOSONE, false);
        x += this.createButton(x, y, -1, ButtonListener.Type.SELPOSTWO, false);
        x += this.createButton(x, y, -1, ButtonListener.Type.SELDELETE, false);
        x += this.createButton(x, y, -1, ButtonListener.Type.SELUNDO, false);
        y += 22;
        ////////////////////////////////////////////////// Bot Control
        y += 30;
        x = 12;
        this.addLabel(x, y, width, 20, textColor, LangKeys.GUI_SECTION + ".label.botControl");
        y += 22;
        x += 5;
        x += this.createButton(x, y, -1, ButtonListener.Type.START, true);
        x += this.createButton(x, y, x - 17, ButtonListener.Type.STOP, false);
        y += 22;
        x = 17;
        if (Utils.isActive()) {
            if (Utils.isPaused()) {
                label = StringUtils.translate(LangKeys.GUI_BUTTON + ".resume");
            } else {
                label = StringUtils.translate(LangKeys.GUI_BUTTON + ".pause");
            }
            width = this.getStringWidth(label) + 10;
            button = new ButtonGeneric(x, y, width, 20, label);
            this.addButton(button, new ButtonListener(ButtonListener.Type.PAUSE_RESUME, this));
        }

        ////////////////////////////////////////////////// Additional Controls
        if (this.getScreenHeight() >= 290) {
            y += 60;
            x = 12;
            this.addLabel(x, y, width, 20, textColor, LangKeys.GUI_SECTION + ".label.additionalControls");
            y += 22;
            x += 5;
            width = 80;

            textBlocksToReplace = new GuiTextFieldGeneric(x, y, width * 2, 20, this.font);
            textBlocksToReplace.setHint(
                    Component.nullToEmpty(
                            StringUtils.translate(LangKeys.GUI + ".textfieldContent.placeholder.blocksToReplace")));
            textBlocksToReplace.setMaxLengthWrapper(256);
            if (!Configs.Lists.BLOCKS_TO_GET_REPLACED.getStrings().isEmpty()) {
                textBlocksToReplace.setValueWrapper(
                        String.join(", ", Configs.Lists.BLOCKS_TO_GET_REPLACED.getStrings()));
            }

            textBlocksToPlace = new GuiTextFieldGeneric(x + width * 2, y, width, 20, this.font);
            textBlocksToPlace.setHint(
                    Component.nullToEmpty(
                            StringUtils.translate(LangKeys.GUI + ".textfieldContent.placeholder.blockToPlace")));
            textBlocksToPlace.setMaxLengthWrapper(256);
            if (!Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue().isEmpty()) {
                textBlocksToPlace.setValueWrapper(Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue());
            }

            int labelWidth = this.getStringWidth(StringUtils.translate(LangKeys.GUI + ".label.repeatAction"));
            this.addLabel(x + (width * 3) - labelWidth, y - 22, labelWidth, 20,
                    Configs.Generic.REPEAT_ACTION.getBooleanValue() ? 0x00FF00 : 0xFF0000,
                    LangKeys.GUI + ".label.repeatAction");

            if (DataManager.DIMENSION.getPresetMode().additionalControls) {
                this.addTextField(textBlocksToReplace, null);
                this.addTextField(textBlocksToPlace, null);
                y += 22;
                x += this.createButton(x, y, width, ButtonListener.Type.SEL_COPY, false);
                x += this.createButton(x, y, width, ButtonListener.Type.SEL_PASTE, false);
                x += this.createButton(x, y, width, ButtonListener.Type.SEL_REPLACE, false);
                y += 22;
                x = 17;
                x += this.createButton(x, y, width, ButtonListener.Type.SEL_SET, false);
                x += this.createButton(x, y, width, ButtonListener.Type.SEL_SHELL, false);
                x += this.createButton(x, y, width, ButtonListener.Type.SEL_WALLS, false);
            }
        }

        ////////////////////////////////////////////////// Box Resizing
        x = this.getScreenWidth() / 2;
        y = 30;
        this.addLabel(x, y, width, 20, textColor, LangKeys.GUI_SECTION + ".label.boxResizing");
        y += 22;
        x += 5;
        this.createCoordinateInput(x, y, width, CoordinateType.NORTH);
        y += 20;
        this.createCoordinateInput(x, y, width, CoordinateType.WEST);
        y += 20;
        x += this.createCoordinateInput(x, y, width, CoordinateType.SOUTH) + 10;
        y -= 40;
        int maxX = x + this.createCoordinateInput(x, y, width, CoordinateType.UP);
        y += 20;
        maxX = Math.max(maxX, x + this.createCoordinateInput(x, y, width, CoordinateType.EAST));
        y += 20;
        maxX = Math.max(maxX, x + this.createCoordinateInput(x, y, width, CoordinateType.DOWN));

        ////////////////////////////////////////////////// Box Moving
        x = this.getScreenWidth() / 2;
        y += 30;
        this.addLabel(x, y, width, 20, textColor, LangKeys.GUI_SECTION + ".label.boxMoving");
        y += 22;
        x += 5;
        x += this.createCoordinateInput(x, y, width, CoordinateType.SHIFTX) + 3;
        x += this.createCoordinateInput(x, y, width, CoordinateType.SHIFTY) + 3;
        this.createCoordinateInput(x, y, width, CoordinateType.SHIFTZ);

        ////////////////////////////////////////////////// Custom Commands
        y = 30;
        x = this.getScreenWidth() - 217;
        if (x >= maxX) {
            for (Commands command : DataManager.SERVER.getCommandsManager().getAllCommands()) {
                y += this.createCommandButton(x, y, 100, ButtonListener.Type.COMMAND, command);
                if (y >= this.getScreenHeight() - 26) {
                    y = 30;
                    x += 100;
                    if (x >= this.getScreenWidth()) {
                        break;
                    }
                }
            }
        }

        /// ////////////////////////////////////////////// Bottom Menu
        x = 12;
        y = this.getScreenHeight() - 26;
        x += this.createButton(x, y, -1, ButtonListener.Type.CONFIGURATION, true);
        label = StringUtils.translate(LangKeys.GUI_BUTTON + ".preset_mode",
                DataManager.DIMENSION.getPresetMode().getDisplayName());
        width = this.getStringWidth(label) + 10;
        button = new ButtonGeneric(x, y, width, 20, label);
        x += this.addButton(button, new ButtonListenerCyclePresetMode(this)).getWidth();
        this.createChangeMenuButton(x, y, -1, ButtonListenerChangeMenu.ButtonType.COMMAND_LIST_MANAGER);
    }

    private void createChangeMenuButton(int x, int y, int width, ButtonListenerChangeMenu.ButtonType type) {
        ButtonIcons icon = type.getIcon();
        if (width == -1) {
            width = this.getStringWidth(type.getDisplayName()) + 10;
        }
        if (icon != null) {
            width += icon.getWidth() + 5;
        }

        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, type.getDisplayName(), type.getIcon());

        this.addButton(button, new ButtonListenerChangeMenu(type, this));
    }

    private int createCommandButton(int x, int y, int width, ButtonListener.Type type, Commands command) {
        String label = command.getName();
        ButtonListener listener = new ButtonListener(type, this, command);
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, label);
        this.addButton(button, listener);
        return 20;
    }

    private int createButton(int x, int y, int width, ButtonListener.Type type, boolean withIcon) {
        ButtonListener listener = new ButtonListener(type, this);
        String label = StringUtils.translate(type.getTranslationKey());
        ButtonIcons icon = type.getIcon();
        ButtonGeneric button = null;

        if (width == -1) {
            width = this.getStringWidth(label) + 10;
        }
        if (icon != null) {
            width += icon.getWidth() + 5;
        }
        if (withIcon) {
            button = new ButtonGeneric(x, y, width, 20, label, icon);
        } else {
            button = new ButtonGeneric(x, y, width, 20, label);
        }

        if (type == ButtonListener.Type.START) {
            button.setHoverStrings(StringUtils.translate(LangKeys.GUI_BUTTON + ".hover.startBotInfoText"));
        } else if (type == ButtonListener.Type.SELDELETE) {
            button.setHoverStrings(StringUtils.translate(LangKeys.GUI_BUTTON + ".hover.selDeleteInfoText"));
        } else if (type == ButtonListener.Type.SELPOSONE) {
            button.setHoverStrings(StringUtils.translate(LangKeys.GUI_BUTTON + ".hover.selPosOneInfoText"));
        } else if (type == ButtonListener.Type.SELPOSTWO) {
            button.setHoverStrings(StringUtils.translate(LangKeys.GUI_BUTTON + ".hover.selPosTwoInfoText"));
        }

        this.addButton(button, listener);

        return width;
    }

    public static void updateBlocksToReplace() {
        String parts = textBlocksToReplace.getValue();
        if (parts.isEmpty()) {
            return;
        }
        List<String> newValue = Arrays.stream(parts.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        Configs.Lists.BLOCKS_TO_GET_REPLACED.setStrings(newValue);
    }

    public static void updateBlockToPlace() {
        String newValue = textBlocksToPlace.getValue();
        if (newValue.isEmpty()) {
            return;
        }
        Configs.Lists.BLOCK_TO_REPLACE_WITH.setValueFromString(newValue);
    }

    public static class ButtonListener implements IButtonActionListener {
        private final Type type;
        private final GuiMainMenu gui;
        private Commands command;

        public ButtonListener(Type type, GuiMainMenu gui) {
            this(type, gui, null);
        }

        public ButtonListener(Type type, GuiMainMenu gui, @Nullable Commands command) {
            this.type = type;
            this.gui = gui;
            this.command = command;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            int amount = mouseButton == 1 ? -1 : 1;
            if (GuiBase.isCtrlDown()) {
                amount *= 100;
            }
            if (GuiBase.isShiftDown()) {
                amount *= 10;
            }
            if (GuiBase.isAltDown()) {
                amount *= 5;
            }
            switch (this.type) {
                case Type.CONFIGURATION:
                    GuiBase.openGui(new GuiConfigs());
                    return;
                case Type.START:
                    String command = DataManager.DIMENSION.getPresetMode().getCommand(this.gui);
                    if (command == null || command.isEmpty())
                        return;
                    Utils.executeBuild(command);
                    this.gui.initGui();
                    this.gui.addMessage(MessageType.ERROR, 1000, LangKeys.INFO + ".main_menu.startBot");
                    return;
                case Type.STOP:
                    Utils.cancel();
                    this.gui.initGui();
                    this.gui.addMessage(MessageType.SUCCESS, 1000, LangKeys.INFO + ".main_menu.stopBot");
                    return;
                case Type.SELPOSONE:
                    Utils.execute("sel pos1");
                    return;
                case Type.SELPOSTWO:
                    Utils.execute("sel pos2");
                    return;
                case Type.SELDELETE:
                    Utils.execute("sel clear");
                    this.gui.addMessage(MessageType.WARNING, 1000, LangKeys.INFO + ".main_menu.selDelete");
                    return;
                case Type.SELUNDO:
                    Utils.execute("sel undo");
                    return;
                case Type.SELCHUNK:
                    selectCurrentChunk();
                    return;
                case Type.SHIFTX:
                    Utils.execute("sel shift all east " + amount);
                    return;
                case Type.SHIFTY:
                    Utils.execute("sel shift all up " + amount);
                    return;
                case Type.SHIFTZ:
                    Utils.execute("sel shift all north " + amount);
                    return;
                case Type.UP:
                    Utils.execute("sel expand all up " + amount);
                    return;
                case Type.DOWN:
                    Utils.execute("sel expand all down " + amount);
                    return;
                case Type.NORTH:
                    Utils.execute("sel expand all north " + amount);
                    return;
                case Type.EAST:
                    Utils.execute("sel expand all east " + amount);
                    return;
                case Type.SOUTH:
                    Utils.execute("sel expand all south " + amount);
                    return;
                case Type.WEST:
                    Utils.execute("sel expand all west " + amount);
                    return;
                case Type.COMMAND:
                    if (this.command != null) {
                        this.command.executeCommand();
                    }
                    return;
                case Type.PAUSE_RESUME:
                    if (Utils.isPaused()) {
                        Utils.resume();
                    } else {
                        Utils.pause();
                    }
                    this.gui.initGui();
                    return;
                case Type.SEL_SET:
                    updateBlocksToReplace();
                    Utils.executeBuild("sel set "
                            + String.join(" ", Configs.Lists.BLOCKS_TO_GET_REPLACED.getStrings()));
                    return;
                case Type.SEL_WALLS:
                    updateBlocksToReplace();
                    Utils.executeBuild("sel walls "
                            + Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue());
                    return;
                case Type.SEL_SHELL:
                    updateBlocksToReplace();
                    Utils.executeBuild("sel shell " + Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue());
                    return;
                case Type.SEL_REPLACE:
                    updateBlocksToReplace();
                    updateBlockToPlace();
                    Utils.executeBuild("sel replace "
                            + String.join(" ", Configs.Lists.BLOCKS_TO_GET_REPLACED.getStrings()) + " "
                            + Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue());
                    return;
                case Type.SEL_COPY:
                    Utils.execute("sel copy");
                    return;
                case Type.SEL_PASTE:
                    Utils.executeBuild("sel paste");
                    return;
                default:
                    break;
            }

        }

        public enum Type {
            CONFIGURATION(LangKeys.GUI_BUTTON + ".configuration_menu", ButtonIcons.CONFIGURATION),
            START(LangKeys.GUI_BUTTON + ".startBot", ButtonIcons.RUNNER),
            STOP(LangKeys.GUI_BUTTON + ".stopBot", null),
            SELPOSONE(LangKeys.GUI_BUTTON + ".selPosOne", null),
            SELPOSTWO(LangKeys.GUI_BUTTON + ".selPosTwo", null),
            SELDELETE(LangKeys.GUI_BUTTON + ".selDelete", null),
            SELUNDO(LangKeys.GUI_BUTTON + ".selUndo", null),
            SHIFTX(LangKeys.GUI_BUTTON + ".shift_sel_x", null),
            SHIFTY(LangKeys.GUI_BUTTON + ".shift_sel_y", null),
            SHIFTZ(LangKeys.GUI_BUTTON + ".shift_sel_z", null),
            UP(LangKeys.GUI_BUTTON + ".up", null),
            DOWN(LangKeys.GUI_BUTTON + ".down", null),
            NORTH(LangKeys.GUI_BUTTON + ".north", null),
            EAST(LangKeys.GUI_BUTTON + ".east", null),
            SOUTH(LangKeys.GUI_BUTTON + ".south", null),
            WEST(LangKeys.GUI_BUTTON + ".west", null),
            COMMAND(LangKeys.GUI_BUTTON + ".command", null),
            PAUSE_RESUME(LangKeys.GUI_BUTTON + ".pause_resume", null),
            SEL_SET(LangKeys.GUI_BUTTON + ".sel_set", null),
            SEL_WALLS(LangKeys.GUI_BUTTON + ".sel_walls", null),
            SEL_SHELL(LangKeys.GUI_BUTTON + ".sel_shell", null),
            SEL_REPLACE(LangKeys.GUI_BUTTON + ".sel_replace", null),
            SEL_COPY(LangKeys.GUI_BUTTON + ".sel_copy", null),
            SEL_PASTE(LangKeys.GUI_BUTTON + ".sel_paste", null),
            SELCHUNK(LangKeys.GUI_BUTTON + ".selchunk", null);

            private final String translationKey;
            private final ButtonIcons icon;

            Type(String translationKey, ButtonIcons icon) {
                this.translationKey = translationKey;
                this.icon = icon;
            }

            public String getTranslationKey() {
                return this.translationKey;
            }

            public ButtonIcons getIcon() {
                return this.icon;
            }
        }
    }

    public static void selectCurrentChunk() {
        ISelectionManager selectionManager = Utils.BT.getSelectionManager();
        ChunkPos chunkPos = Utils.MC.player.chunkPosition();
        BetterBlockPos corner1 = new BetterBlockPos(chunkPos.getWorldPosition().below(59));
        BetterBlockPos corner2 = new BetterBlockPos(
                chunkPos.getWorldPosition().offset(15,
                        Utils.MC.level.getChunk(corner1).getHighestFilledSectionIndex() * 16 - 48, 15));
        selectionManager.addSelection(corner1, corner2);
    }

    public static class ButtonListenerChangeMenu implements IButtonActionListener {
        private final ButtonType type;
        @Nullable
        private final Screen parent;

        public ButtonListenerChangeMenu(ButtonType type, @Nullable Screen parent) {
            this.type = type;
            this.parent = parent;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            GuiBase gui = null;

            switch (this.type) {
                case CONFIGURATION:
                    GuiBase.openGui(new GuiConfigs());
                    return;
                case MAIN_MENU:
                    gui = new GuiMainMenu();
                    break;
                case CREATE_COMMAND:
                    gui = new GuiConfigureCommand(null);
                    break;
                case COMMAND_LIST_MANAGER:
                    gui = new GuiCommandList();
                    break;
            }

            if (gui != null) {
                gui.setParent(this.parent);
                GuiBase.openGui(gui);
            }
        }

        public enum ButtonType {
            // Command List Interaction GUI
            COMMAND_LIST_MANAGER(LangKeys.GUI_BUTTON + ".change_menu.command_list_manager", ButtonIcons.BROWSER),
            // Create a new command
            CREATE_COMMAND(LangKeys.GUI_BUTTON + ".change_menu.createCommand", null),
            // In-game Configuration GUI
            CONFIGURATION(LangKeys.GUI_BUTTON + ".change_menu.configuration_menu", ButtonIcons.CONFIGURATION),
            // Switch to the BTScreen main menu
            MAIN_MENU(LangKeys.GUI_BUTTON + ".change_menu.to_main_menu", null);

            private final String labelKey;
            private final ButtonIcons icon;

            ButtonType(String labelKey, ButtonIcons icon) {
                this.labelKey = labelKey;
                this.icon = icon;
            }

            public String getDisplayName() {
                return StringUtils.translate(this.labelKey, BTScreen.MOD_NAME, BTScreen.MOD_VERSION);
            }

            public ButtonIcons getIcon() {
                return this.icon;
            }
        }
    }

    private static class ButtonListenerCyclePresetMode implements IButtonActionListener {
        private final GuiMainMenu gui;

        private ButtonListenerCyclePresetMode(GuiMainMenu gui) {
            this.gui = gui;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            PresetMode mode = DataManager.DIMENSION.getPresetMode().cycle(mouseButton == 0);
            DataManager.DIMENSION.setPresetMode(mode);
            this.gui.initGui();
        }
    }

    protected int createCoordinateInput(int x, int y, int width, CoordinateType coordType) {

        y += 2;
        ButtonListener.Type type = null;

        switch (coordType) {
            case SHIFTX:
                type = ButtonListener.Type.SHIFTX;
                break;
            case SHIFTY:
                type = ButtonListener.Type.SHIFTY;
                break;
            case SHIFTZ:
                type = ButtonListener.Type.SHIFTZ;
                break;
            case UP:
                type = ButtonListener.Type.UP;
                break;
            case DOWN:
                type = ButtonListener.Type.DOWN;
                break;
            case NORTH:
                type = ButtonListener.Type.NORTH;
                break;
            case SOUTH:
                type = ButtonListener.Type.SOUTH;
                break;
            case WEST:
                type = ButtonListener.Type.WEST;
                break;
            case EAST:
                type = ButtonListener.Type.EAST;
                break;
            default:
                return 0;
        }

        this.createCoordinateButton(x, y, type);
        String label = StringUtils.translate(type.getTranslationKey());
        this.addLabel(x + 18, y, 20, 20, textColor, label);
        return 20 + this.getStringWidth(label);
    }

    protected void createCoordinateButton(int x, int y,
            ButtonListener.Type type) {
        String hover = StringUtils.translate(LangKeys.GUI_BUTTON + ".hover.plus_minus_tip_ctrl_alt_shift");
        ButtonGeneric button = new ButtonGeneric(x, y, ButtonIcons.BUTTON_PLUS_MINUS_16, hover);
        ButtonListener listener = new ButtonListener(type, this);
        this.addButton(button, listener);
    }

    public enum CoordinateType {
        SHIFTX,
        SHIFTY,
        SHIFTZ,
        UP,
        DOWN,
        NORTH,
        SOUTH,
        WEST,
        EAST
    }
}
