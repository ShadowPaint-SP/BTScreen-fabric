package drvlabs.de;

import net.minecraft.MinecraftVersion;
import fi.dy.masa.malilib.util.StringUtils;

public class Reference {

	public static final String MOD_ID = "btscreen";
	public static final String MOD_NAME = "BTScreen";
	public static final String MOD_VERSION = StringUtils.getModVersionString(MOD_ID);
	public static final String MC_VERSION = MinecraftVersion.CURRENT.getName();
	public static final String MOD_TYPE = "fabric";
	public static final String MOD_STRING = MOD_ID + "-" + MOD_TYPE + "-" + MC_VERSION + "-" + MOD_VERSION;
}
