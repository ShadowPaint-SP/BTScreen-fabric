package drvlabs.de.gui;

import drvlabs.de.data.DataManager;
import drvlabs.de.gui.GuiMainMenu.ButtonListenerChangeMenu;
import drvlabs.de.gui.widgets.WidgetCommandList;
import drvlabs.de.utils.customcommands.Commands;
import drvlabs.de.utils.customcommands.CommandsManager;
import drvlabs.de.gui.widgets.WidgetCommand;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;

public class GuiCommandList
		extends GuiListBase<Commands, WidgetCommand, WidgetCommandList> {
	public final CommandsManager manager;

	public GuiCommandList() {
		super(12, 30);

		this.title = StringUtils.translate("btscreen.gui.title.manage_command_list");
		this.manager = DataManager.getCommandsManager();
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
		label = StringUtils.translate(type.getLabelKey());
		buttonWidth = this.getStringWidth(label) + 30;
		button = new ButtonGeneric(x, y, buttonWidth, 20, label, type.getIcon());
		this.addButton(button, new ButtonListenerChangeMenu(type, this.getParent()));

		type = ButtonListenerChangeMenu.ButtonType.CREATE_COMMAND;
		label = StringUtils.translate(type.getLabelKey());
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