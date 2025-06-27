package de.drvlabs.btscreen.gui;

import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

import de.drvlabs.btscreen.Reference;
import fi.dy.masa.malilib.gui.interfaces.IFileBrowserIconProvider;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public enum ButtonIcons implements IGuiIcon, IFileBrowserIconProvider {
	CONFIGURATION(102, 84, 14, 14),
	RUNNER(102, 126, 14, 14),
	BROWSER(102, 28, 14, 14),
	FILE_ICON_LITEMATIC(144, 0, 12, 12),
	FILE_ICON_SCHEMATIC(144, 12, 12, 12),
	FILE_ICON_SPONGE_SCH(144, 24, 12, 12),
	FILE_ICON_VANILLA(144, 36, 12, 12),
	FILE_ICON_JSON(144, 44, 12, 12),
	FILE_ICON_DIR(156, 0, 12, 12),
	FILE_ICON_DIR_UP(156, 12, 12, 12),
	FILE_ICON_DIR_ROOT(156, 24, 12, 12),
	FILE_ICON_SEARCH(156, 36, 12, 12),
	FILE_ICON_CREATE_DIR(156, 48, 12, 12),
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

	@Override
	public IGuiIcon getIconRoot() {
		return FILE_ICON_DIR_ROOT;
	}

	@Override
	public IGuiIcon getIconUp() {
		return FILE_ICON_DIR_UP;
	}

	@Override
	public IGuiIcon getIconCreateDirectory() {
		return FILE_ICON_CREATE_DIR;
	}

	@Override
	public IGuiIcon getIconSearch() {
		return FILE_ICON_SEARCH;
	}

	@Override
	public IGuiIcon getIconDirectory() {
		return FILE_ICON_DIR;
	}

	@Override
	@Nullable
	public IGuiIcon getIconForFile(Path file) {
		return null;
	}

}
