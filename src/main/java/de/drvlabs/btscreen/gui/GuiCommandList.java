package de.drvlabs.btscreen.gui;

import de.drvlabs.btscreen.config.DataManager;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.gui.GuiMainMenu.ButtonListenerChangeMenu;
import de.drvlabs.btscreen.gui.widgets.WidgetCommand;
import de.drvlabs.btscreen.gui.widgets.WidgetCommandList;
import de.drvlabs.btscreen.implementation.customcommands.Commands;
import de.drvlabs.btscreen.implementation.customcommands.CommandsManager;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;

public class GuiCommandList extends GuiListBase<Commands, WidgetCommand, WidgetCommandList> {
	public final CommandsManager manager;

	public GuiCommandList() {
		super(12, 30);

		this.title = StringUtils.translate(LangKeys.GUI_TITLE + ".manage_command_list");
		this.manager = DataManager.SERVER.getCommandsManager();
	}

	@Override
	protected int getBrowserWidth() {
		return this.getScreenWidth() - 20;
	}

	@Override
	protected int getBrowserHeight() {
		return this.getScreenHeight() - 64;
	}

	@Override
	public void initGui() {
		super.initGui();

		int x = 12;
		int y = this.getScreenHeight() - 26;
		int buttonWidth;
		String label;
		ButtonGeneric button;

		ButtonListenerChangeMenu.ButtonType type = ButtonListenerChangeMenu.ButtonType.MAIN_MENU;
		label = type.getDisplayName();
		buttonWidth = this.getStringWidth(label) + 30;
		button = new ButtonGeneric(x, y, buttonWidth, 20, label, type.getIcon());
		this.addButton(button, new ButtonListenerChangeMenu(type, this.getParent()));

		type = ButtonListenerChangeMenu.ButtonType.CREATE_COMMAND;
		label = type.getDisplayName();
		buttonWidth = this.getStringWidth(label) + 20;
		x = this.getScreenWidth() - buttonWidth - 10;
		button = new ButtonGeneric(x, y, buttonWidth, 20, label);
		this.addButton(button, new ButtonListenerChangeMenu(type, this.getParent()));
	}

	@Override
	protected WidgetCommandList createListWidget(int listX, int listY) {
		return new WidgetCommandList(listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), this);
	}
}