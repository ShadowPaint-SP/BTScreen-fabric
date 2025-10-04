package de.drvlabs.btscreen.gui.widgets;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.drvlabs.btscreen.config.DataManager;
import de.drvlabs.btscreen.gui.ButtonIcons;
import de.drvlabs.btscreen.gui.GuiCommandList;
import de.drvlabs.btscreen.implementation.customcommands.Commands;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetSearchBar;

public class WidgetCommandList extends WidgetListBase<Commands, WidgetCommand> {
	public final GuiCommandList parent;

	public WidgetCommandList(int x, int y, int width, int height, GuiCommandList parent) {
		super(x, y, width, height, null);

		this.parent = parent;
		this.browserEntryHeight = 22;
		this.widgetSearchBar = new WidgetSearchBar(x + 2, y + 4, width - 14, 14, 0, ButtonIcons.FILE_ICON_SEARCH,
				LeftRight.LEFT);
		this.browserEntriesOffsetY = this.widgetSearchBar.getHeight() + 3;
	}

	public GuiCommandList getParentGui() {
		return this.parent;
	}

	@Override
	protected Collection<Commands> getAllEntries() {
		return DataManager.SERVER.getCommandsManager().getAllCommands();
	}

	@Override
	protected List<String> getEntryStringsForFilter(Commands entry) {
		return ImmutableList.of(entry.getName(), entry.getCommand());
	}

	@Override
	protected WidgetCommand createListEntryWidget(int x, int y, int listIndex, boolean isOdd,
			Commands entry) {
		return new WidgetCommand(x, y, this.browserEntryWidth, this.getBrowserEntryHeightFor(entry),
				isOdd, entry, listIndex, this);
	}
}