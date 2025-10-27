package de.drvlabs.btscreen.gui.widgets;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.DataManager;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.gui.GuiConfigureCommand;
import de.drvlabs.btscreen.implementation.customcommands.Commands;
import de.drvlabs.btscreen.implementation.customcommands.CommandsManager;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import fi.dy.masa.malilib.util.StringUtils;

public class WidgetCommand extends WidgetListEntryBase<Commands> {
	public final CommandsManager manager;
	public final WidgetCommandList parent;
	public final Commands command;
	public final boolean isOdd;
	public int buttonsStartX;

	public WidgetCommand(int x, int y, int width, int height, boolean isOdd,
			Commands command, int listIndex, WidgetCommandList parent) {
		super(x, y, width, height, command, listIndex);

		this.parent = parent;
		this.command = command;
		this.isOdd = isOdd;
		this.manager = DataManager.SERVER.getCommandsManager();

		int posX = x + width - 2;
		int posY = y + 1;

		// Note: These are placed from right to left

		posX = this.createButtonGeneric(posX, posY, ButtonListener.ButtonType.REMOVE);
		posX = this.createButtonGeneric(posX, posY, ButtonListener.ButtonType.CONFIGURE);
		posX = this.createButtonGeneric(posX, posY, ButtonListener.ButtonType.EXECUTE);

		this.buttonsStartX = posX;
	}

	public int createButtonGeneric(int xRight, int y, ButtonListener.ButtonType type) {
		return this
				.addButton(new ButtonGeneric(xRight, y, -1, true, type.getDisplayName()),
						new ButtonListener(type, this))
				.getX() - 1;
	}

	public static class ButtonListener implements IButtonActionListener {
		public final ButtonType type;
		public final WidgetCommand widget;

		public ButtonListener(ButtonType type, WidgetCommand widget) {
			this.type = type;
			this.widget = widget;
		}

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			switch (this.type) {
				case ButtonType.EXECUTE:
					this.widget.command.executeCommand();
					GuiBase.openGui(null);
					break;
				case ButtonType.CONFIGURE:
					BTScreen.debugLog("Configure command" + this.widget.command.getName());
					GuiConfigureCommand gui = new GuiConfigureCommand(this.widget.command);
					gui.setParent(this.widget.parent.getParentGui());
					GuiBase.openGui(gui);
					break;
				case ButtonType.REMOVE:
					this.widget.manager.removeCommand(this.widget.command);
					this.widget.parent.refreshEntries();
					break;
			}
		}

		public enum ButtonType {
			EXECUTE(LangKeys.GUI_BUTTON + ".customCommand.execute"),
			CONFIGURE(LangKeys.GUI_BUTTON + ".customCommand.configure"),
			REMOVE(LangKeys.GUI_BUTTON + ".customCommand.remove");

			private final String translationKey;

			ButtonType(String translationKey) {
				this.translationKey = translationKey;
			}

			public String getTranslationKey() {
				return this.translationKey;
			}

			public String getDisplayName() {
				return StringUtils.translate(this.translationKey);
			}

		}
	}
}