package de.drvlabs.btscreen.gui.ui;

import java.time.Duration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

/** The one button implementation used throughout BTScreen. */
public final class UiButton extends AbstractButton {
    @FunctionalInterface
    public interface PressAction {
        void onPress(UiButton button, InputWithModifiers input);
    }

    private final PressAction action;
    private boolean selected;
    private boolean destructive;
    private UiGlyph glyph;

    public UiButton(int x, int y, int width, int height, Component label, PressAction action) {
        super(x, y, width, height, label);
        this.action = action;
    }

    public static UiButton create(int x, int y, int width, Component label, PressAction action) {
        return new UiButton(x, y, width, UiTheme.CONTROL_HEIGHT, label, action);
    }

    public static UiButton compact(int x, int y, int width, Component label, PressAction action) {
        return new UiButton(x, y, width, UiTheme.COMPACT_CONTROL_HEIGHT, label, action);
    }

    public UiButton tooltip(Component tooltip) {
        setTooltip(Tooltip.create(tooltip));
        setTooltipDelay(Duration.ofMillis(350));
        return this;
    }

    public UiButton selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public UiButton destructive() {
        this.destructive = true;
        return this;
    }

    public UiButton glyph(UiGlyph glyph) {
        this.glyph = glyph;
        return this;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (active) {
            action.onPress(this, input);
        }
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        boolean highlighted = isHoveredOrFocused();
        int background = active ? (highlighted ? UiTheme.PANEL_HOVER : UiTheme.CONTROL)
                : UiTheme.CONTROL_DISABLED;
        int border = !active ? 0xFF3B4449
                : destructive && highlighted ? UiTheme.ERROR
                : selected || highlighted ? UiTheme.ACCENT
                : UiTheme.BORDER;
        int text = !active ? UiTheme.TEXT_MUTED
                : destructive ? (highlighted ? UiTheme.ERROR : 0xFFFFA1A1)
                : selected || highlighted ? UiTheme.ACCENT
                : UiTheme.TEXT;

        graphics.fill(getX(), getY(), getRight(), getBottom(), background);
        graphics.outline(getX(), getY(), getWidth(), getHeight(), border);
        if (selected) {
            graphics.fill(getX() + 1, getY() + 1, getX() + 3, getBottom() - 1, UiTheme.ACCENT);
        }
        if (glyph != null) {
            glyph.draw(graphics, getX() + 6, getY() + (getHeight() - 7) / 2, text);
        }
        extractScrollingStringOverContents(
                graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR),
                getMessage().copy().withColor(text), glyph == null ? 3 : 13);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
