package de.drvlabs.btscreen.gui.ui;

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

/** Common screen chrome, navigation, panels, and transient feedback. */
public abstract class UiScreen extends Screen {
    public enum NoticeTone {
        INFO, SUCCESS, WARNING, ERROR
    }

    @Nullable
    protected final Screen parent;
    private Component notice;
    private NoticeTone noticeTone = NoticeTone.INFO;
    private long noticeUntil;

    protected UiScreen(Component title, @Nullable Screen parent) {
        super(title);
        this.parent = parent;
    }

    public final void open(Screen screen) {
        minecraft.setScreenAndShow(screen);
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft.level != null) {
            extractBlurredBackground(graphics);
        }
        graphics.fillGradient(0, 0, width, height, UiTheme.BACKGROUND_TOP, UiTheme.BACKGROUND_BOTTOM);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int headerLeft = headerLeft();
        graphics.text(font, title, headerLeft, 12, UiTheme.TEXT);
        graphics.fill(headerLeft, 27, headerLeft + 28, 29, UiTheme.ACCENT);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        extractNotice(graphics);
    }

    protected int headerLeft() {
        return UiTheme.MARGIN;
    }

    protected final void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int panelWidth, int panelHeight,
            Component heading) {
        UiTheme.panel(graphics, x, y, panelWidth, panelHeight);
        graphics.text(font, heading, x + 8, y + 7, UiTheme.TEXT);
        UiTheme.separator(graphics, x + 8, y + 22, panelWidth - 16);
    }

    protected final void drawSectionHeading(GuiGraphicsExtractor graphics, int x, int y, int sectionWidth,
            Component heading) {
        graphics.text(font, heading, x, y, UiTheme.TEXT);
        graphics.fill(x, y + 12, x + Math.min(18, sectionWidth), y + 13, UiTheme.ACCENT);
    }

    public final void showNotice(NoticeTone tone, Component message) {
        this.notice = message;
        this.noticeTone = tone;
        this.noticeUntil = Util.getMillis() + 2200L;
    }

    public final void showNotice(NoticeTone tone, String translationKey, Object... args) {
        showNotice(tone, Component.translatable(translationKey, args));
    }

    private void extractNotice(GuiGraphicsExtractor graphics) {
        if (notice == null || Util.getMillis() >= noticeUntil) {
            return;
        }
        int color = switch (noticeTone) {
            case INFO -> UiTheme.ACCENT;
            case SUCCESS -> UiTheme.SUCCESS;
            case WARNING -> UiTheme.WARNING;
            case ERROR -> UiTheme.ERROR;
        };
        int noticeWidth = Math.min(width - 24, font.width(notice) + 20);
        int x = (width - noticeWidth) / 2;
        int y = height - 58;
        graphics.fill(x, y, x + noticeWidth, y + 22, 0xEE090D11);
        graphics.outline(x, y, noticeWidth, 22, color);
        graphics.centeredText(font, notice, width / 2, y + 7, color);
    }
}
