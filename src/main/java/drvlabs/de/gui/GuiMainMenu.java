package drvlabs.de.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import baritone.api.BaritoneAPI;
import baritone.api.process.IBuilderProcess;
import drvlabs.de.Reference;
import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.CommandUtils;
import drvlabs.de.utils.behavior.AutoDrop;
import drvlabs.de.utils.customcommands.Commands;
import drvlabs.de.utils.customcommands.CommandsManager;
import drvlabs.de.utils.preset.PresetMode;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;

public class GuiMainMenu extends GuiBase {
	private final int textColor = 0xFEFEFEFE;
	private static MinecraftClient mc = MinecraftClient.getInstance();
	private static IBuilderProcess baritoneBuildProcess = BaritoneAPI.getProvider().getPrimaryBaritone()
			.getBuilderProcess();
	public static GuiTextFieldGeneric textBlocksToReplace;
	public static GuiTextFieldGeneric textBlocksToPlace;

	public GuiMainMenu() {
		String version = String.format("v%s", Reference.MOD_VERSION);
		this.title = StringUtils.translate("btscreen.gui.title.btscreen_main_menu", version);
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
		this.addLabel(x, y, width, 20, textColor, "btscreen.gui.section.label.selManagement");
		y += 22;
		x += 5;
		x += this.createButton(x, y, -1, ButtonListener.Type.SELPOSONE, false);
		x += this.createButton(x, y, -1, ButtonListener.Type.SELPOSTWO, false);
		x += this.createButton(x, y, -1, ButtonListener.Type.SELDELETE, false);
		x += this.createButton(x, y, -1, ButtonListener.Type.SELUNDO, false);

		////////////////////////////////////////////////// Bot Control
		y += 30;
		x = 12;
		this.addLabel(x, y, width, 20, textColor, "btscreen.gui.section.label.botControl");
		y += 22;
		x += 5;
		x += this.createButton(x, y, -1, ButtonListener.Type.START, true);
		x += this.createButton(x, y, x - 17, ButtonListener.Type.STOP, false);
		y += 22;
		x = 17;
		if (DataManager.getActive()) {
			if (baritoneBuildProcess.isPaused()) {
				label = StringUtils.translate("btscreen.gui.button.resume");
			} else {
				label = StringUtils.translate("btscreen.gui.button.pause");
			}
			width = this.getStringWidth(label) + 10;
			button = new ButtonGeneric(x, y, width, 20, label);
			this.addButton(button, new ButtonListener(ButtonListener.Type.PAUSE_RESUME, this));
		}

		////////////////////////////////////////////////// Additional Controls
		if (this.getScreenHeight() >= 290) {
			y += 60;
			x = 12;
			this.addLabel(x, y, width, 20, textColor, "btscreen.gui.section.label.additionalControls");
			y += 22;
			x += 5;
			width = 80;

			textBlocksToReplace = new GuiTextFieldGeneric(x, y + 2, width * 2, 20, this.textRenderer);
			textBlocksToReplace
					.setPlaceholder(Text.of(StringUtils.translate("btscreen.gui.textfieldContent.placeholder.blocksToReplace")));
			textBlocksToReplace.setMaxLengthWrapper(256);
			if (!Configs.Lists.BLOCKS_TO_GET_REPLACED.getStrings().isEmpty()) {
				textBlocksToReplace.setTextWrapper(
						String.join(", ", Configs.Lists.BLOCKS_TO_GET_REPLACED.getStrings()));
			}
			this.addTextField(textBlocksToReplace, null);

			textBlocksToPlace = new GuiTextFieldGeneric(x + width * 2, y + 2, width, 20, this.textRenderer);
			textBlocksToPlace
					.setPlaceholder(Text.of(StringUtils.translate("btscreen.gui.textfieldContent.placeholder.blockToPlace")));
			textBlocksToPlace.setMaxLengthWrapper(256);
			if (!Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue().isEmpty()) {
				textBlocksToPlace.setTextWrapper(Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue());
			}
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

		////////////////////////////////////////////////// Box Resizing
		x = this.getScreenWidth() / 2;
		y = 30;
		this.addLabel(x, y, width, 20, textColor, "btscreen.gui.section.label.boxResizing");
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
		this.addLabel(x, y, width, 20, textColor, "btscreen.gui.section.label.boxMoving");
		y += 22;
		x += 5;
		x += this.createCoordinateInput(x, y, width, CoordinateType.SHIFTX) + 3;
		x += this.createCoordinateInput(x, y, width, CoordinateType.SHIFTY) + 3;
		this.createCoordinateInput(x, y, width, CoordinateType.SHIFTZ);

		////////////////////////////////////////////////// Custom Commands
		y = 30;
		x = this.getScreenWidth() - 217;
		if (x >= maxX) {
			for (Commands command : CommandsManager.getAllCommands()) {
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
		label = StringUtils.translate("btscreen.gui.button.preset_mode", DataManager.getPresetMode().getName());
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
			button.setHoverStrings(StringUtils.translate("btscreen.gui.button.hover.startBotInfoText"));
		} else if (type == ButtonListener.Type.SELDELETE) {
			button.setHoverStrings(StringUtils.translate("btscreen.gui.button.hover.selDeleteInfoText"));
		} else if (type == ButtonListener.Type.SELPOSONE) {
			button.setHoverStrings(StringUtils.translate("btscreen.gui.button.hover.selPosOneInfoText"));
		} else if (type == ButtonListener.Type.SELPOSTWO) {
			button.setHoverStrings(StringUtils.translate("btscreen.gui.button.hover.selPosTwoInfoText"));
		}

		this.addButton(button, listener);

		return width;
	}

	public static void updateBlocksToReplace() {
		String parts = textBlocksToReplace.getText();
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
		String newValue = textBlocksToPlace.getText();
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
					CommandUtils.execute("sel cleararea");
					DataManager.getInstance().setActive(true);
					AutoDrop.updateMaxSlots();
					DataManager.getPresetMode().setSettings();
					DataManager.setBotStatus(BotStatus.MINING);
					// check for haste
					if (Configs.Generic.AUTO_HASTE.getBooleanValue()) {
						if (!mc.player.hasStatusEffect(StatusEffects.HASTE)) {
							CommandUtils.execute("pause");
							DataManager.setBotStatus(BotStatus.HASTING);
							CommandUtils.setHome(Configs.Generic.MINE_HOME.getStringValue());
							CommandUtils.tpTo(Configs.Generic.HASTE_HOME.getStringValue());
						}
					}
					this.gui.initGui();
					this.gui.addMessage(MessageType.ERROR, 1000, "btscreen.info.main_menu.startBot");
					return;
				case Type.STOP:
					CommandUtils.execute("stop");
					DataManager.getInstance().setActive(false);
					DataManager.setBotStatus(BotStatus.IDLE);
					this.gui.initGui();
					this.gui.addMessage(MessageType.SUCCESS, 1000, "btscreen.info.main_menu.stopBot");
					return;
				case Type.SELPOSONE:
					CommandUtils.execute("sel pos1");
					return;
				case Type.SELPOSTWO:
					CommandUtils.execute("sel pos2");
					return;
				case Type.SELDELETE:
					CommandUtils.execute("sel clear");
					this.gui.addMessage(MessageType.WARNING, 1000, "btscreen.info.main_menu.selDelete");
					return;
				case Type.SELUNDO:
					CommandUtils.execute("sel undo");
					return;
				case Type.SHIFTX:
					CommandUtils.execute("sel shift all east " + amount);
					return;
				case Type.SHIFTY:
					CommandUtils.execute("sel shift all up " + amount);
					return;
				case Type.SHIFTZ:
					CommandUtils.execute("sel shift all north " + amount);
					return;
				case Type.UP:
					CommandUtils.execute("sel expand all up " + amount);
					return;
				case Type.DOWN:
					CommandUtils.execute("sel expand all down " + amount);
					return;
				case Type.NORTH:
					CommandUtils.execute("sel expand all north " + amount);
					return;
				case Type.EAST:
					CommandUtils.execute("sel expand all east " + amount);
					return;
				case Type.SOUTH:
					CommandUtils.execute("sel expand all south " + amount);
					return;
				case Type.WEST:
					CommandUtils.execute("sel expand all west " + amount);
					return;
				case Type.COMMAND:
					if (this.command != null) {
						CommandUtils.sendCommand(this.command.getCommand());
					}
					return;
				case Type.PAUSE_RESUME:
					if (BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().isPaused()) {
						BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().resume();
						DataManager.setBotStatus(BotStatus.MINING);
					} else {
						BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().pause();
						DataManager.setBotStatus(BotStatus.IDLE);
					}
					this.gui.initGui();
					return;
				case Type.SEL_SET:
					PresetMode.setBuildingAbility();
					updateBlocksToReplace();
					CommandUtils.execute("sel set " + Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue());
					return;
				case Type.SEL_WALLS:
					PresetMode.setBuildingAbility();
					updateBlocksToReplace();
					CommandUtils.execute("sel walls " + Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue());
					return;
				case Type.SEL_SHELL:
					PresetMode.setBuildingAbility();
					updateBlocksToReplace();
					CommandUtils.execute("sel shell " + Configs.Lists.BLOCK_TO_REPLACE_WITH.getStringValue());
					return;
				case Type.SEL_REPLACE:
					PresetMode.setBuildingAbility();
					updateBlocksToReplace();
					updateBlockToPlace();
					CommandUtils.btBlockAction("sel replace");
					return;
				case Type.SEL_COPY:
					CommandUtils.execute("sel copy");
					return;
				case Type.SEL_PASTE:
					PresetMode.setBuildingAbility();
					CommandUtils.execute("sel paste");
					return;
				default:
					break;
			}

		}

		public enum Type {
			CONFIGURATION("btscreen.gui.button.configuration_menu", ButtonIcons.CONFIGURATION),
			START("btscreen.gui.button.startBot", ButtonIcons.RUNNER),
			STOP("btscreen.gui.button.stopBot", null),
			SELPOSONE("btscreen.gui.button.selPosOne", null),
			SELPOSTWO("btscreen.gui.button.selPosTwo", null),
			SELDELETE("btscreen.gui.button.selDelete", null),
			SELUNDO("btscreen.gui.button.selUndo", null),
			SHIFTX("btscreen.gui.button.shift_sel_x", null),
			SHIFTY("btscreen.gui.button.shift_sel_y", null),
			SHIFTZ("btscreen.gui.button.shift_sel_z", null),
			UP("btscreen.gui.button.up", null),
			DOWN("btscreen.gui.button.down", null),
			NORTH("btscreen.gui.button.north", null),
			EAST("btscreen.gui.button.east", null),
			SOUTH("btscreen.gui.button.south", null),
			WEST("btscreen.gui.button.west", null),
			COMMAND("btscreen.gui.button.command", null),
			PAUSE_RESUME("btscreen.gui.button.pause_resume", null),
			SEL_SET("btscreen.gui.button.sel_set", null),
			SEL_WALLS("btscreen.gui.button.sel_walls", null),
			SEL_SHELL("btscreen.gui.button.sel_shell", null),
			SEL_REPLACE("btscreen.gui.button.sel_replace", null),
			SEL_COPY("btscreen.gui.button.sel_copy", null),
			SEL_PASTE("btscreen.gui.button.sel_paste", null);

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
			COMMAND_LIST_MANAGER("btscreen.gui.button.change_menu.command_list_manager", ButtonIcons.BROWSER),
			// Create a new command
			CREATE_COMMAND("btscreen.gui.button.change_menu.createCommand", null),
			// In-game Configuration GUI
			CONFIGURATION("btscreen.gui.button.change_menu.configuration_menu", ButtonIcons.CONFIGURATION),
			// Switch to the BTScreen main menu
			MAIN_MENU("btscreen.gui.button.change_menu.to_main_menu", null);

			private final String labelKey;
			private final ButtonIcons icon;

			ButtonType(String labelKey, ButtonIcons icon) {
				this.labelKey = labelKey;
				this.icon = icon;
			}

			public String getLabelKey() {
				return this.labelKey;
			}

			public String getDisplayName() {
				return StringUtils.translate(this.getLabelKey());
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
			PresetMode mode = DataManager.getPresetMode().cycle(MinecraftClient.getInstance().player, mouseButton == 0);
			DataManager.setPresetMode(mode);
			mode.setSettings();
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
		String hover = StringUtils.translate("btscreen.gui.button.hover.plus_minus_tip_ctrl_alt_shift");
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
