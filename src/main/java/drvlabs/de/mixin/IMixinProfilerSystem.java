package drvlabs.de.mixin;

import net.minecraft.util.profiler.ProfilerSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ProfilerSystem.class)
public interface IMixinProfilerSystem {
	@Accessor("tickStarted")
	boolean btscreen_isStarted();
}
