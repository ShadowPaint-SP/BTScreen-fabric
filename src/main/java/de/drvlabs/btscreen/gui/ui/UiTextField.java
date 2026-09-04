package de.drvlabs.btscreen.gui.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** Borderless vanilla text editing with BTScreen's own frame and focus state. */
public final class UiTextField extends EditBox {
    public UiTextField(Font font, int x, int y, int width, int height, Component narration) {
        super(font, x, y, width, height, narration);
        setBordered(false);
        setTextColor(UiTheme.TEXT);
        setTextColorUneditable(UiTheme.TEXT_MUTED);
    }

    public UiTextField hint(Component hint) {
        setHint(hint);
        return this;
    }

    @Override
    protected void updateTextPosition() {
        super.updateTextPosition();
        if (!isBordered()) {
            this.textX = getX() + 4;
            this.textY = getY() + (getHeight() - 8) / 2;
        }
    }

    @Override
    public int getInnerWidth() {
        return Math.max(0, super.getInnerWidth() - 8);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (!isVisible()) {
            return;
        }
        int border = isFocused() || isHovered() ? UiTheme.ACCENT : UiTheme.BORDER;
        graphics.fill(getX(), getY(), getRight(), getBottom(), UiTheme.CONTROL);
        graphics.outline(getX(), getY(), getWidth(), getHeight(), border);
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
    }
}
