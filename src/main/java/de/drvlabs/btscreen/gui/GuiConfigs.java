package de.drvlabs.btscreen.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.config.Hotkeys;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.gui.ui.UiButton;
import de.drvlabs.btscreen.gui.ui.UiScreen;
import de.drvlabs.btscreen.gui.ui.UiTextField;
import de.drvlabs.btscreen.gui.ui.UiTheme;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigInteger;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.config.IConfigValue;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.config.options.table.ConfigTable;
import fi.dy.masa.malilib.gui.GuiBase;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** BTScreen's config browser; the existing config objects remain the source of truth. */
public final class GuiConfigs extends UiScreen {
    private enum Tab { GENERAL, LISTS, HOTKEYS }

    private final List<Row> rows = new ArrayList<>();
    private Tab tab = Tab.GENERAL;
    private String query = "";
    private int scrollRow;
    @Nullable
    private GuiConfigEntries entries;
    private final List<AbstractWidget> modalWidgets = new ArrayList<>();

    public GuiConfigs() {
        this(null);
    }

    public GuiConfigs(@Nullable Screen parent) {
        super(Component.translatable(LangKeys.GUI_TITLE + ".configs",
                BTScreen.MOD_NAME, BTScreen.MOD_VERSION), parent);
    }

    @Override
    protected void init() {
        clearWidgets();
        rows.clear();
        modalWidgets.clear();
        int left = browserLeft();
        int contentWidth = browserWidth();
        int tabWidth = Math.min(92, (contentWidth - 8) / 3);
        for (int i = 0; i < Tab.values().length; i++) {
            Tab target = Tab.values()[i];
            String name = switch (target) {
                case GENERAL -> Component.translatable("btscreen.gui.button.config_gui.generic").getString();
                case LISTS -> Component.translatable("btscreen.gui.button.config_gui.lists").getString();
                case HOTKEYS -> Component.translatable("btscreen.gui.button.config_gui.hotkeys").getString();
            };
            addRenderableWidget(UiButton.create(left + i * (tabWidth + 4), 38, tabWidth,
                    Component.literal(name), (button, input) -> {
                        tab = target;
                        scrollRow = 0;
                        rebuildWidgets();
                    }).selected(tab == target));
        }

        UiTextField search = new UiTextField(font, left, 66, contentWidth, 20,
                Component.translatable("btscreen.gui.settings.search"));
        search.setValue(query);
        search.hint(Component.translatable("btscreen.gui.settings.search"));
        search.setResponder(value -> {
            query = value;
            scrollRow = 0;
            layoutRows();
        });
        addRenderableWidget(search);

        List<? extends IConfigBase> options = switch (tab) {
            case GENERAL -> Configs.Generic.OPTIONS;
            case LISTS -> Configs.Lists.OPTIONS;
            case HOTKEYS -> Hotkeys.HOTKEY_LIST;
        };
        for (IConfigBase config : options) {
            rows.add(createRow(config));
        }
        layoutRows();
        if (entries != null) {
            addRenderableOnly((Renderable) (graphics, mouseX, mouseY, partialTick) -> {
                graphics.fill(0, 0, width, height, 0x66000000);
                entries.draw(graphics);
            });
            entries.install();
        }
    }

    private int browserWidth() {
        return Math.min(width - UiTheme.MARGIN * 2, 530);
    }

    private int browserLeft() {
        return (width - browserWidth()) / 2;
    }

    @Override
    protected int headerLeft() {
        return browserLeft();
    }

    private Row createRow(IConfigBase config) {
        int left = browserLeft();
        int contentWidth = browserWidth();
        int labelWidth = Math.min(190, contentWidth / 3 + 14);
        int resetWidth = 54;
        int valueX = left + labelWidth + 6;
        int available = contentWidth - labelWidth - resetWidth - 12;
        int valueWidth = config instanceof IConfigBoolean ? Math.min(126, available)
                : config instanceof ConfigStringList || config instanceof ConfigTable
                        ? Math.min(190, available)
                        : Math.min(250, available);
        List<AbstractWidget> widgets = new ArrayList<>();
        UiButton label = UiButton.create(left, 0, labelWidth,
                Component.literal(config.getConfigGuiDisplayName()),
                (button, input) -> {}).textOnly();
        String description = config.getComment();
        if (entries == null && description != null && !description.isBlank()) {
            label.tooltip(Component.literal(description.replaceAll("§.", "")));
        }
        widgets.add(addRenderableWidget(label));

        if (config instanceof IConfigBoolean booleanConfig) {
            widgets.add(addRenderableWidget(UiButton.create(valueX, 0, valueWidth,
                    Component.literal(Boolean.toString(booleanConfig.getBooleanValue())), (button, input) -> {
                        booleanConfig.toggleBooleanValue();
                        button.setMessage(Component.literal(Boolean.toString(booleanConfig.getBooleanValue())));
                        button.selected(booleanConfig.getBooleanValue());
                    }).selected(booleanConfig.getBooleanValue())));
        } else if (config instanceof ConfigStringList list) {
            widgets.add(addRenderableWidget(UiButton.create(valueX, 0, valueWidth,
                    Component.translatable("btscreen.gui.settings.editList", list.getStrings().size()),
                    (button, input) -> openEntries(list))));
        } else if (config instanceof ConfigTable table) {
            widgets.add(addRenderableWidget(UiButton.create(valueX, 0, valueWidth,
                    Component.translatable("btscreen.gui.settings.editTable", table.getTable().size()),
                    (button, input) -> openEntries(table))));
        } else if (config instanceof ConfigHotkey hotkey) {
            widgets.add(addRenderableWidget(UiButton.create(valueX, 0, valueWidth,
                    Component.literal(hotkey.getKeybind().getKeysDisplayString()),
                    (button, input) -> open(new GuiConfigHotkey(this, hotkey)))));
        } else if (config instanceof IConfigValue value) {
            UiTextField field = new UiTextField(font, valueX, 0, valueWidth, 20,
                    Component.literal(config.getConfigGuiDisplayName()));
            field.setMaxLength(1024);
            field.setValue(value.getStringValue());
            field.setResponder(text -> {
                if (config instanceof IConfigInteger integer && !(config instanceof de.drvlabs.btscreen.config.ConfigTicks)) {
                    try {
                        int parsed = Integer.parseInt(text);
                        if (parsed >= integer.getMinIntegerValue() && parsed <= integer.getMaxIntegerValue()) {
                            integer.setIntegerValue(parsed);
                        }
                    } catch (NumberFormatException ignored) {
                        // Keep the last valid value while editing.
                    }
                } else if (!(config instanceof de.drvlabs.btscreen.config.ConfigTicks)
                        || text.matches("(?i)(?:\\d+[tsmhdw])+|\\d+")) {
                    value.setValueFromString(text);
                }
            });
            widgets.add(addRenderableWidget(field));
        }

        widgets.add(addRenderableWidget(UiButton.create(valueX + valueWidth + 6, 0, resetWidth,
                Component.translatable("btscreen.gui.settings.reset"), (button, input) -> {
                    ((IConfigResettable) config).resetToDefault();
                    if (config instanceof ConfigTable table) table.setModified();
                    rebuildWidgets();
                })));
        return new Row(config, widgets);
    }

    private void layoutRows() {
        int top = 98;
        int bottom = height - 16;
        int capacity = Math.max(0, (bottom - top) / 24);
        String needle = query.toLowerCase(Locale.ROOT);
        List<Row> matching = rows.stream().filter(row -> needle.isBlank()
                || row.config.getConfigGuiDisplayName().toLowerCase(Locale.ROOT).contains(needle)
                || row.config.getName().toLowerCase(Locale.ROOT).contains(needle)).toList();
        scrollRow = Math.max(0, Math.min(scrollRow, Math.max(0, matching.size() - capacity)));
        for (Row row : rows) {
            int index = matching.indexOf(row) - scrollRow;
            boolean visible = index >= 0 && index < capacity;
            for (AbstractWidget widget : row.widgets) {
                widget.visible = visible;
                if (visible) widget.setY(top + index * 24);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (entries != null) {
            return entries.mouseScrolled(mouseX, mouseY, scrollY);
        }
        if (mouseY >= 98 && mouseY < height - 16 && scrollY != 0) {
            scrollRow = Math.max(0, scrollRow - (int) Math.signum(scrollY) * 2);
            layoutRows();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    void openEntries(IConfigBase config) {
        setFocused(null);
        entries = new GuiConfigEntries(this, config);
        rebuildWidgets();
    }

    void closeEntries() {
        setFocused(null);
        entries = null;
        Configs.saveToFile();
        rebuildWidgets();
    }

    <T extends AbstractWidget> T addModalWidget(T widget) {
        modalWidgets.add(widget);
        return addRenderableWidget(widget);
    }

    void refreshEntries() {
        setFocused(null);
        rebuildWidgets();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (entries == null) {
            boolean handled = super.mouseClicked(event, doubleClick);
            if (entries != null) setFocused(null);
            return handled;
        }
        for (int i = modalWidgets.size() - 1; i >= 0; i--) {
            AbstractWidget widget = modalWidgets.get(i);
            if (widget.mouseClicked(event, doubleClick)) {
                setFocused(modalWidgets.contains(widget) ? widget : null);
                return true;
            }
        }
        if (!entries.contains(event.x(), event.y())) closeEntries();
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (entries != null && event.isEscape()) {
            closeEntries();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        if (entries != null) {
            closeEntries();
            return;
        }
        Configs.saveToFile();
        super.onClose();
    }

    private record Row(IConfigBase config, List<AbstractWidget> widgets) { }

    /** Adapter for malilib's mod selector, which requires a GuiBase supplier. */
    public static final class Bridge extends GuiBase {
        @Override
        public void initGui() {
            mc.gui.setScreen(new GuiConfigs(getParent()));
        }
    }
}
