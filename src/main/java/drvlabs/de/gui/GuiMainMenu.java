package drvlabs.de.gui;

import baritone.api.BaritoneAPI;
import drvlabs.de.BTScreen;
import drvlabs.de.Reference;
import drvlabs.de.baritone.preset.PresetMode;
import drvlabs.de.data.DataManager;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.ITextFieldListener;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.MinecraftClient;

public class GuiMainMenu extends GuiBase {
	private int selStepAmount;

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

		x += this.createButton(x, y, -1, ButtonListener.Type.CONFIGURATION);
		this.createButton(x, y, -1, ButtonListener.Type.SELPOSONE);
		y += 22;
		this.createButton(x, y, -1, ButtonListener.Type.SELPOSTWO);
		y += 22;

		this.createCoordinateInput(x, y, width, CoordinateType.UP);
		y += 20;
		this.createCoordinateInput(x, y, width, CoordinateType.DOWN);
		y += 20;
		this.createCoordinateInput(x, y, width, CoordinateType.NORTH);
		y += 20;
		this.createCoordinateInput(x, y, width, CoordinateType.SOUTH);
		y += 20;
		this.createCoordinateInput(x, y, width, CoordinateType.WEST);
		y += 20;
		this.createCoordinateInput(x, y, width, CoordinateType.EAST);
		y += 22;

		label = StringUtils.translate("btscreen.gui.button.preset_mode", DataManager.getPresetMode().getName());
		int width2 = this.getStringWidth(label) + 10;
		x = 12;
		y = this.getScreenHeight() - 26;
		ButtonGeneric button = new ButtonGeneric(x, y, width2, 20, label);
		this.addButton(button, new ButtonListenerCyclePresetMode(this));
	}

	private int createButton(int x, int y, int width, ButtonListener.Type type) {
		ButtonListener listener = new ButtonListener(type, this);
		String label = StringUtils.translate(type.getTranslationKey());
		ButtonIcons icon = type.getIcon();

		if (width == -1) {
			width = this.getStringWidth(label) + 10;
		}
		if (icon != null) {
			width += icon.getWidth() + 10;
		}

		ButtonGeneric button = new ButtonGeneric(x, y, width, 20, label, icon);

		// if (type == ButtonListener.Type.CONFIGURATION) {
		// button.setHoverStrings(StringUtils.translate("btscreen.gui.button.hover.config_info_text"));
		// }

		this.addButton(button, listener);

		return width;
	}

	private static class ButtonListener implements IButtonActionListener {
		private final Type type;
		private final GuiMainMenu gui;

		public ButtonListener(Type type, GuiMainMenu gui) {
			this.type = type;
			this.gui = gui;
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
					this.gui.addMessage(MessageType.SUCCESS, "btscreen.info.main_menu.CONFIGURATION");
					return;
				case Type.SELPOSONE:
					BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("sel pos1");
					return;
				case Type.SELPOSTWO:
					BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("sel pos2");
					return;
				case Type.SHIFTX:

					return;
				case Type.SHIFTY:

					return;
				case Type.SHIFTZ:

					return;
				case Type.UP:
					BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("sel expand all up " + amount);
					return;
				case Type.DOWN:
					BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("sel expand all down " + amount);
					return;
				case Type.NORTH:
					BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("sel expand all north " + amount);
					return;
				case Type.EAST:
					BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("sel expand all east " + amount);
					return;
				case Type.SOUTH:
					BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("sel expand all south " + amount);
					return;
				case Type.WEST:
					BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("sel expand all west " + amount);
					return;
				default:
					break;
			}
			// TODO: add the different button functionalitys
		}

		public enum Type {
			CONFIGURATION("btscreen.gui.button.configuration_menu", ButtonIcons.CONFIGURATION),
			SELPOSONE("btscreen.gui.button.selection_pos_one", null),
			SELPOSTWO("btscreen.gui.button.selection_pos_two", null),
			SHIFTX("btscreen.gui.button.shift_sel_x", null),
			SHIFTY("btscreen.gui.button.shift_sel_y", null),
			SHIFTZ("btscreen.gui.button.shift_sel_z", null),
			UP("btscreen.gui.button.up", null),
			DOWN("btscreen.gui.button.down", null),
			NORTH("btscreen.gui.button.north", null),
			EAST("btscreen.gui.button.east", null),
			SOUTH("btscreen.gui.button.south", null),
			WEST("btscreen.gui.button.west", null);

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
			BTScreen.LOGGER.info("Preset Mode: " + mode);
		}
	}

	protected void createCoordinateInput(int x, int y, int width, CoordinateType coordType) {

		y += 2;
		ButtonListener.Type type = null;

		switch (coordType) {
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
		}

		this.createCoordinateButton(x, y, type);
		String label = ":" + coordType.name();
		this.addLabel(x + 20, y, 20, 20, 0xFFFFFFFF, label);
	}

	protected void createCoordinateButton(int x, int y,
			ButtonListener.Type type) {
		String hover = StringUtils.translate("btscreen.gui.button.hover.plus_minus_tip_ctrl_alt_shift");
		ButtonGeneric button = new ButtonGeneric(x, y, ButtonIcons.BUTTON_PLUS_MINUS_16, hover);
		ButtonListener listener = new ButtonListener(type, this);
		this.addButton(button, listener);
	}

	protected static class TextFieldListener implements ITextFieldListener<GuiTextFieldGeneric> {
		private final GuiMainMenu parent;

		public TextFieldListener(GuiMainMenu parent) {
			this.parent = parent;
		}

		@Override
		public boolean onTextChange(GuiTextFieldGeneric textField) {
			this.parent.selStepAmount = Integer.parseInt(textField.getText());
			return false;
		}
	}

	public enum TextFieldType {
		BOX_SHIFT,
		BOX_TRANSFORM
	}

	public enum CoordinateType {
		UP,
		DOWN,
		NORTH,
		SOUTH,
		WEST,
		EAST
	}
}
