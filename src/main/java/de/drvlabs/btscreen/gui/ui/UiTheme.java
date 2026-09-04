package de.drvlabs.btscreen.gui.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Shared visual tokens and drawing rules for every BTScreen screen. */
public final class UiTheme {
    public static final int ACCENT = 0xFF4DE3E6;
    public static final int ACCENT_SOFT = 0x554DE3E6;
    public static final int BACKGROUND_TOP = 0xE6121315;
    public static final int BACKGROUND_BOTTOM = 0xF008090A;
    public static final int PANEL = 0xB8121315;
    public static final int PANEL_HOVER = 0xD0222426;
    public static final int CONTROL = 0xB8090A0B;
    public static final int CONTROL_DISABLED = 0x88202428;
    public static final int BORDER = 0xFF555A5E;
    public static final int TEXT = 0xFFF3F7F8;
    public static final int TEXT_MUTED = 0xFF9AA9B0;
    public static final int SUCCESS = 0xFF67E8A5;
    public static final int WARNING = 0xFFFFC857;
    public static final int ERROR = 0xFFFF6B6B;

    public static final int MARGIN = 12;
    public static final int GAP = 8;
    public static final int CONTROL_HEIGHT = 20;
    public static final int COMPACT_CONTROL_HEIGHT = 16;

    private UiTheme() {
    }

    public static void panel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PANEL);
        graphics.outline(x, y, width, height, BORDER);
    }

    public static void separator(GuiGraphicsExtractor graphics, int x, int y, int width) {
        graphics.fill(x, y, x + width, y + 1, 0x6652616A);
    }
}
