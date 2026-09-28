package de.drvlabs.btscreen.gui;

import java.util.ArrayList;
import java.util.List;

import de.drvlabs.btscreen.gui.ui.UiButton;
import de.drvlabs.btscreen.gui.ui.UiTextField;
import de.drvlabs.btscreen.gui.ui.UiTheme;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.config.options.table.ConfigTable;
import fi.dy.masa.malilib.config.options.table.TableRow;
import fi.dy.masa.malilib.config.options.table.type.EntryTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** A centered, in-screen editor for malilib string lists and typed tables. */
final class GuiConfigEntries {
    private final GuiConfigs parent;
    private final IConfigBase config;
    private final Font font = Minecraft.getInstance().font;
    private int scrollRow;
    private int x;
    private int y;
    private int width;
    private int height;

    GuiConfigEntries(GuiConfigs parent, IConfigBase config) {
        this.parent = parent;
        this.config = config;
    }

    private int count() {
        return config instanceof ConfigStringList list ? list.getStrings().size()
                : ((ConfigTable) config).getTable().size();
    }

    private int capacity() {
        return Math.max(1, (height - 110) / 24);
    }

    boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    void install() {
        width = Math.min(parent.width - 24, 620);
        height = Math.min(parent.height - 32, 470);
        x = (parent.width - width) / 2;
        y = (parent.height - height) / 2;
        scrollRow = Math.min(scrollRow, Math.max(0, count() - capacity()));

        parent.addModalWidget(UiButton.create(x + width - 76, y + 12, 64,
                Component.translatable("gui.done"), (button, input) -> parent.closeEntries()));
        UiButton add = UiButton.create(x + 12, y + 38, 88,
                Component.translatable("btscreen.gui.settings.addRow"),
                (button, input) -> insertRow(count()));
        add.active = !(config instanceof ConfigTable table) || table.allowNewEntry();
        parent.addModalWidget(add);
        parent.addModalWidget(UiButton.create(x + 104, y + 38, 92,
                Component.translatable("btscreen.gui.settings.resetAll"), (button, input) -> resetAll()));

        int end = Math.min(count(), scrollRow + capacity());
        for (int i = scrollRow; i < end; i++) {
            installRow(i, y + 82 + (i - scrollRow) * 24);
        }
    }

    private void installRow(int index, int rowY) {
        int fieldX = x + 36;
        int toolX = x + width - 144;
        int fieldWidth = Math.max(40, toolX - fieldX - 8);
        if (config instanceof ConfigStringList list) {
            UiTextField field = new UiTextField(font, fieldX, rowY, fieldWidth, 20,
                    Component.literal("Entry " + (index + 1)));
            field.setMaxLength(1024);
            field.setValue(list.getStrings().get(index));
            field.setResponder(text -> {
                List<String> values = new ArrayList<>(list.getStrings());
                values.set(index, text);
                list.setStrings(values);
            });
            parent.addModalWidget(field);
        } else {
            ConfigTable table = (ConfigTable) config;
            int columns = table.getTypes().size();
            int columnWidth = Math.max(24, (fieldWidth - (columns - 1) * 4) / columns);
            for (int column = 0; column < columns; column++) {
                int currentColumn = column;
                UiTextField field = new UiTextField(font,
                        fieldX + column * (columnWidth + 4), rowY, columnWidth, 20,
                        Component.literal(table.getLabels().get(column).label()));
                field.setMaxLength(256);
                field.setValue(table.getTable().get(index).get(column).asString());
                field.setResponder(text -> updateCell(table, index, currentColumn, text));
                parent.addModalWidget(field);
            }
        }

        parent.addModalWidget(UiButton.create(toolX, rowY, 43,
                Component.translatable("btscreen.gui.settings.reset"),
                (button, input) -> resetRow(index)));
        UiButton insert = UiButton.create(toolX + 46, rowY, 20, Component.literal("+"),
                (button, input) -> insertRow(index + 1));
        insert.active = !(config instanceof ConfigTable table) || table.allowNewEntry();
        parent.addModalWidget(insert);
        parent.addModalWidget(UiButton.create(toolX + 69, rowY, 20, Component.literal("−"),
                (button, input) -> removeRow(index)).destructive());
        UiButton up = UiButton.create(toolX + 92, rowY, 18, Component.literal("↑"),
                (button, input) -> moveRow(index, -1));
        up.active = index > 0;
        parent.addModalWidget(up);
        UiButton down = UiButton.create(toolX + 113, rowY, 18, Component.literal("↓"),
                (button, input) -> moveRow(index, 1));
        down.active = index < count() - 1;
        parent.addModalWidget(down);
    }

    void draw(GuiGraphicsExtractor graphics) {
        graphics.fill(x, y, x + width, y + height, 0xF20A0A0A);
        graphics.outline(x, y, width, height, UiTheme.BORDER);
        graphics.text(font, config.getConfigGuiDisplayName(), x + 12, y + 16, UiTheme.TEXT);
        graphics.fill(x + 12, y + 67, x + width - 12, y + 68, UiTheme.BORDER);
        if (config instanceof ConfigTable table) {
            int fieldX = x + 36;
            int fieldWidth = x + width - 152 - fieldX;
            int columnWidth = Math.max(24, (fieldWidth - (table.getTypes().size() - 1) * 4)
                    / table.getTypes().size());
            for (int column = 0; column < table.getTypes().size(); column++) {
                graphics.text(font, font.plainSubstrByWidth(table.getLabels().get(column).label(), columnWidth),
                        fieldX + column * (columnWidth + 4), y + 70, UiTheme.TEXT_MUTED);
            }
        } else {
            graphics.text(font, Component.translatable("btscreen.gui.settings.entryCount", count()),
                    x + 36, y + 70, UiTheme.TEXT_MUTED);
        }
        for (int index = scrollRow; index < Math.min(count(), scrollRow + capacity()); index++) {
            graphics.text(font, Integer.toString(index + 1), x + 12,
                    y + 88 + (index - scrollRow) * 24, UiTheme.TEXT_MUTED);
        }
        if (count() > capacity()) {
            int trackY = y + 82;
            int trackHeight = capacity() * 24 - 4;
            int thumbHeight = Math.max(12, trackHeight * capacity() / count());
            int travel = trackHeight - thumbHeight;
            int thumbY = trackY + travel * scrollRow / (count() - capacity());
            graphics.fill(x + width - 7, trackY, x + width - 5, trackY + trackHeight, UiTheme.BORDER);
            graphics.fill(x + width - 7, thumbY, x + width - 5, thumbY + thumbHeight, UiTheme.ACCENT);
        }
    }

    boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (contains(mouseX, mouseY) && scrollY != 0) {
            int next = Math.max(0, Math.min(Math.max(0, count() - capacity()),
                    scrollRow - (int) Math.signum(scrollY) * 2));
            if (next != scrollRow) {
                scrollRow = next;
                parent.refreshEntries();
            }
        }
        return true;
    }

    private void updateCell(ConfigTable table, int row, int column, String text) {
        List<TableRow> rows = new ArrayList<>(table.getTable());
        List<Object> values = new ArrayList<>();
        TableRow current = rows.get(row);
        for (int i = 0; i < table.getTypes().size(); i++) {
            EntryTypes type = table.getTypes().get(i);
            String source = i == column ? text : current.get(i).asString();
            try {
                values.add(switch (type) {
                    case STRING -> source;
                    case INTEGER -> Integer.parseInt(source);
                    case DOUBLE -> Double.parseDouble(source);
                    case BOOLEAN -> {
                        if (!source.equalsIgnoreCase("true") && !source.equalsIgnoreCase("false")) {
                            throw new IllegalArgumentException("Expected true or false");
                        }
                        yield Boolean.parseBoolean(source);
                    }
                    case LABEL -> table.getLabels().get(i);
                });
            } catch (IllegalArgumentException ignored) {
                return;
            }
        }
        rows.set(row, TableRow.of(values.toArray()));
        table.setTable(rows);
        table.setModified();
    }

    private TableRow emptyRow(ConfigTable table) {
        List<Object> values = new ArrayList<>();
        for (EntryTypes type : table.getTypes()) {
            values.add(switch (type) {
                case STRING -> "";
                case INTEGER -> 0;
                case DOUBLE -> 0D;
                case BOOLEAN -> false;
                case LABEL -> table.getLabels().get(values.size());
            });
        }
        return TableRow.of(values.toArray());
    }

    private void insertRow(int at) {
        if (config instanceof ConfigStringList list) {
            List<String> values = new ArrayList<>(list.getStrings());
            values.add(at, "");
            list.setStrings(values);
        } else {
            ConfigTable table = (ConfigTable) config;
            if (!table.allowNewEntry()) return;
            List<TableRow> rows = new ArrayList<>(table.getTable());
            rows.add(at, emptyRow(table));
            table.setTable(rows);
            table.setModified();
        }
        scrollRow = Math.max(0, Math.min(at, count() - capacity()));
        parent.refreshEntries();
    }

    private void removeRow(int at) {
        if (config instanceof ConfigStringList list) {
            List<String> values = new ArrayList<>(list.getStrings());
            values.remove(at);
            list.setStrings(values);
        } else {
            ConfigTable table = (ConfigTable) config;
            List<TableRow> rows = new ArrayList<>(table.getTable());
            rows.remove(at);
            table.setTable(rows);
            table.setModified();
        }
        parent.refreshEntries();
    }

    private void moveRow(int at, int direction) {
        int target = at + direction;
        if (target < 0 || target >= count()) return;
        if (config instanceof ConfigStringList list) {
            List<String> values = new ArrayList<>(list.getStrings());
            String item = values.remove(at);
            values.add(target, item);
            list.setStrings(values);
        } else {
            ConfigTable table = (ConfigTable) config;
            List<TableRow> rows = new ArrayList<>(table.getTable());
            TableRow item = rows.remove(at);
            rows.add(target, item);
            table.setTable(rows);
            table.setModified();
        }
        parent.refreshEntries();
    }

    private void resetRow(int at) {
        if (config instanceof ConfigStringList list) {
            List<String> values = new ArrayList<>(list.getStrings());
            if (at < list.getDefaultStrings().size()) values.set(at, list.getDefaultStrings().get(at));
            else values.remove(at);
            list.setStrings(values);
        } else {
            ConfigTable table = (ConfigTable) config;
            List<TableRow> rows = new ArrayList<>(table.getTable());
            if (at < table.getDefaultTable().size()) rows.set(at, table.getDefaultTable().get(at));
            else rows.remove(at);
            table.setTable(rows);
            table.setModified();
        }
        parent.refreshEntries();
    }

    private void resetAll() {
        if (config instanceof ConfigStringList list) {
            list.resetToDefault();
        } else {
            ConfigTable table = (ConfigTable) config;
            table.resetToDefault();
            table.setModified();
        }
        scrollRow = 0;
        parent.refreshEntries();
    }
}
