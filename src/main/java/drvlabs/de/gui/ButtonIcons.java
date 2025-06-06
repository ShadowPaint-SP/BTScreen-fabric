package drvlabs.de.gui;

import drvlabs.de.Reference;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public enum ButtonIcons implements IGuiIcon {
	CONFIGURATION(102, 84, 14, 14),
	RUNNER(102, 126, 14, 14),
	BUTTON_PLUS_MINUS_8(0, 0, 8, 8),
	BUTTON_PLUS_MINUS_12(24, 0, 12, 12),
	BUTTON_PLUS_MINUS_16(0, 128, 16, 16),;

	public static final Identifier TEXTURE = Identifier.of(Reference.MOD_ID, "textures/gui/gui_widgets.png");

	private final int u;
	private final int v;
	private final int w;
	private final int h;

	ButtonIcons(int u, int v, int w, int h) {
		this.u = u;
		this.v = v;
		this.w = w;
		this.h = h;
	}

	@Override
	public int getWidth() {
		return this.w;
	}

	@Override
	public int getHeight() {
		return this.h;
	}

	@Override
	public int getU() {
		return this.u;
	}

	@Override
	public int getV() {
		return this.v;
	}

	@Override
	public void renderAt(int x, int y, float zLevel, boolean enabled, boolean selected, DrawContext drawContext) {
		int u = this.u;
		if (enabled) {
			u += this.w;
		}
		if (selected) {
			u += this.w;
		}
		RenderUtils.drawTexturedRect(this.getTexture(), x, y, u, this.v, this.w, this.h, zLevel, drawContext);
		RenderUtils.forceDraw(drawContext);
	}

	@Override
	public Identifier getTexture() {
		return TEXTURE;
	}

}
