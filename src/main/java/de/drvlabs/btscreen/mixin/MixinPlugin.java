package de.drvlabs.btscreen.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import de.drvlabs.btscreen.mixin.network.MixinClientPlayerInteractionManager;
import net.fabricmc.loader.api.FabricLoader;

public class MixinPlugin implements IMixinConfigPlugin {
    private static boolean loaded;
    private static boolean isMeteorPresent;

    @Override
    public void onLoad(String mixinPackage) {
        if (loaded)
            return;
        isMeteorPresent = FabricLoader.getInstance().isModLoaded("meteor-client");
        loaded = true;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equalsIgnoreCase(MixinClientPlayerInteractionManager.class.getName())) {
            return !isMeteorPresent;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}