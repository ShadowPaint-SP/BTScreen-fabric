package de.drvlabs.btscreen.gui.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Tiny code-drawn pixel glyphs. They stay sharp at every GUI scale. */
public enum UiGlyph {
    NORTH, EAST, SOUTH, WEST, UP, DOWN;

    public void draw(GuiGraphicsExtractor graphics, int x, int y, int color) {
        switch (this) {
            case NORTH -> verticalArrow(graphics, x, y, color, true, false);
            case SOUTH -> verticalArrow(graphics, x, y, color, false, false);
            case UP -> verticalArrow(graphics, x, y, color, true, true);
            case DOWN -> verticalArrow(graphics, x, y, color, false, true);
            case EAST -> horizontalArrow(graphics, x, y, color, true);
            case WEST -> horizontalArrow(graphics, x, y, color, false);
        }
    }

    private static void verticalArrow(GuiGraphicsExtractor graphics, int x, int y, int color,
            boolean upward, boolean doubled) {
        int tipY = upward ? y : y + 6;
        int nextY = upward ? y + 1 : y + 5;
        graphics.fill(x + 3, tipY, x + 4, tipY + 1, color);
        graphics.fill(x + 2, nextY, x + 5, nextY + 1, color);
        graphics.fill(x + 1, upward ? y + 2 : y + 4, x + 6, upward ? y + 3 : y + 5, color);
        graphics.fill(x + 3, y + 2, x + 4, y + 7, color);
        if (doubled) {
            int markerY = upward ? y + 4 : y + 2;
            graphics.fill(x + 1, markerY, x + 3, markerY + 1, color);
            graphics.fill(x + 4, markerY, x + 6, markerY + 1, color);
        }
    }

    private static void horizontalArrow(GuiGraphicsExtractor graphics, int x, int y, int color, boolean right) {
        int tipX = right ? x + 6 : x;
        int nextX = right ? x + 5 : x + 1;
        graphics.fill(tipX, y + 3, tipX + 1, y + 4, color);
        graphics.fill(nextX, y + 2, nextX + 1, y + 5, color);
        graphics.fill(right ? x + 4 : x + 2, y + 1, right ? x + 5 : x + 3, y + 6, color);
        graphics.fill(x, y + 3, x + 7, y + 4, color);
    }
}
