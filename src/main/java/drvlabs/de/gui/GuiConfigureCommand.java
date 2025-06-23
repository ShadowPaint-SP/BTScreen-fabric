package drvlabs.de.gui;

import org.jetbrains.annotations.Nullable;

import drvlabs.de.BTScreen;
import drvlabs.de.data.DataManager;
import drvlabs.de.utils.customcommands.Commands;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.StringUtils;

public class GuiConfigureCommand extends GuiBase {
	public final Commands command;
	public GuiTextFieldGeneric textFieldName;
	public GuiTextFieldGeneric textFieldCommand;
	public static boolean newCommand;

	public GuiConfigureCommand(Commands command) {
		if (command == null) {
			BTScreen.debugLog("Creating new command");
			this.command = new Commands("", "", null);
			newCommand = true;
		} else {
			BTScreen.debugLog("Editing command");
			this.command = command;
			newCommand = false;
		}
		this.title = StringUtils.translate("btscreen.gui.title.configure_command");
	}

	@Override
	public void initGui() {
		super.initGui();
		int scaledWidth = GuiUtils.getScaledWindowWidth();
		int width = Math.min(300, scaledWidth - 200);
		int x = 12;
		int y = 22;

		this.textFieldName = new GuiTextFieldGeneric(x, y + 2, width, 16, this.textRenderer);
		this.textFieldName.setMaxLengthWrapper(256);
		this.textFieldName.setTextWrapper(this.command.getName());
		this.addTextField(this.textFieldName, null);

		y += 20;

		this.textFieldCommand = new GuiTextFieldGeneric(x, y + 2, width, 16, this.textRenderer);
		this.textFieldCommand.setMaxLengthWrapper(256);
		this.textFieldCommand.setTextWrapper(this.command.getCommand());
		this.addTextField(this.textFieldCommand, null);

		this.createButton(x + width + 4, y, -1, ButtonListener.Type.SAVE);
	}

	public int createButton(int x, int y, int width, ButtonListener.Type type) {
		ButtonListener listener = new ButtonListener(type, this.command, this);
		String label = type.getDisplayName();
		if (width == -1) {
			width = this.getStringWidth(label) + 10;
		}
		ButtonGeneric button = new ButtonGeneric(x, y, width, 20, label);
		this.addButton(button, listener);
		return width;
	}

	public static class ButtonListener implements IButtonActionListener {
		public final GuiConfigureCommand parent;
		public final Commands command;
		public final Type type;

		public ButtonListener(Type type, Commands command, GuiConfigureCommand parent) {
			this.parent = parent;
			this.command = command;
			this.type = type;
		}

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			this.parent.setNextMessageType(MessageType.ERROR);

			switch (this.type) {
				case SAVE:
					BTScreen.debugLog("Saving command");
					if (this.parent.textFieldCommand.getTextWrapper().equals("")
							|| this.parent.textFieldName.getTextWrapper().equals("")) {
						BTScreen.debugLog("Command name or command is empty");
						this.parent.addMessage(MessageType.ERROR, 1000, "btscreen.info.guiConfigureCommand.saveError");
						return;
					}
					BTScreen.debugLog("Command name: " + this.parent.textFieldName.getTextWrapper());
					this.command.setName(this.parent.textFieldName.getTextWrapper());
					BTScreen.debugLog("Command: " + this.parent.textFieldCommand.getTextWrapper());
					this.command.setCommand(this.parent.textFieldCommand.getTextWrapper());
					if (newCommand) {
						DataManager.getCommandsManager().addCommand(this.command);
						DataManager.save(true);
					}
					break;
			}
			GuiBase.openGui(new GuiCommandList());
		}

		public enum Type {
			SAVE("btscreen.gui.button.save");

			private final String translationKey;
			@Nullable
			private final String hoverText;

			private Type(String translationKey) {
				this(translationKey, null);
			}

			private Type(String translationKey, @Nullable String hoverText) {
				this.translationKey = translationKey;
				this.hoverText = hoverText;
			}

			public String getTranslationKey() {
				return this.translationKey;
			}

			public String getDisplayName(Object... args) {
				return StringUtils.translate(this.translationKey, args);
			}

			@Nullable
			public String getHoverText() {
				return this.hoverText != null ? StringUtils.translate(this.hoverText) : null;
			}
		}
	}

}
