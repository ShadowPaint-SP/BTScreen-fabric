package drvlabs.de.gui;

import drvlabs.de.BTScreen;
import drvlabs.de.Reference;
import drvlabs.de.config.Configs;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.BotStatus;
import drvlabs.de.utils.CommandUtils;
import drvlabs.de.utils.behavior.AutoDrop;
import drvlabs.de.utils.preset.PresetMode;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;

public class GuiMainMenu extends GuiBase {
	private final int textColor = 0xFEFEFEFE;
	private static MinecraftClient mc = MinecraftClient.getInstance();

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

		this.addLabel(x, y, width, 20, textColor, "btscreen.gui.section.label.selManagement");
		y += 22;
		x += 5;
		x += this.createButton(x, y, -1, ButtonListener.Type.SELPOSONE);
		x += this.createButton(x, y, -1, ButtonListener.Type.SELPOSTWO);
		x += this.createButton(x, y, -1, ButtonListener.Type.SELDELETE);

		//////////////////////////////////////////////////
		y += 30;
		x = 12;
		this.addLabel(x, y, width, 20, textColor, "btscreen.gui.section.label.botControl");
		y += 22;
		x += 5;
		x += this.createButton(x, y, -1, ButtonListener.Type.START);
		this.createButton(x, y, -1, ButtonListener.Type.STOP);

		//////////////////////////////////////////////////
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
		this.createCoordinateInput(x, y, width, CoordinateType.UP);
		y += 20;
		this.createCoordinateInput(x, y, width, CoordinateType.EAST);
		y += 20;
		this.createCoordinateInput(x, y, width, CoordinateType.DOWN);

		//////////////////////////////////////////////////
		x = this.getScreenWidth() / 2;
		y += 30;
		this.addLabel(x, y, width, 20, textColor, "btscreen.gui.section.label.boxMoving");
		y += 22;
		x += 5;
		x += this.createCoordinateInput(x, y, width, CoordinateType.SHIFTX) + 3;
		x += this.createCoordinateInput(x, y, width, CoordinateType.SHIFTY) + 3;
		this.createCoordinateInput(x, y, width, CoordinateType.SHIFTZ);

		//////////////////////////////////////////////////
		x = 12;
		y = this.getScreenHeight() - 26;
		x += this.createButton(x, y, -1, ButtonListener.Type.CONFIGURATION);
		label = StringUtils.translate("btscreen.gui.button.preset_mode", DataManager.getPresetMode().getName());
		int width2 = this.getStringWidth(label) + 10;
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
			width += icon.getWidth() + 5;
		}

		ButtonGeneric button = new ButtonGeneric(x, y, width, 20, label, icon);

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
					return;
				case Type.START:
					CommandUtils.execute("sel cleararea");
					DataManager.getInstance().setActive(true);
					AutoDrop.updateMaxSlots();
					DataManager.setBotStatus(BotStatus.MINING);
					// check for haste
					if (Configs.Generic.AUTO_HASTE.getBooleanValue()) {
						if (!mc.player.hasStatusEffect(StatusEffects.HASTE)) {
							CommandUtils.execute("pause");
							DataManager.setBotStatus(BotStatus.HASTING);
							CommandUtils.debugHome(mc.player.getBlockPos().getX() + " " + mc.player.getBlockPos().getY() + " "
									+ mc.player.getBlockPos().getZ());
							CommandUtils.tpTo(Configs.Generic.HASTE_HOME.getStringValue());
						}
					}
					this.gui.addMessage(MessageType.ERROR, 1000, "btscreen.info.main_menu.startBot");
					return;
				case Type.STOP:
					CommandUtils.execute("stop");
					DataManager.getInstance().setActive(false);
					DataManager.setBotStatus(BotStatus.IDLE);
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
