package blazingtwist.cannontracer.mixin;

import blazingtwist.cannontracer.serverside.RegionManager;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class ExplosionMixin {

	@Inject(at = @At("HEAD"), method = "affectWorld")
	public void onAffectWorld(CallbackInfo info) {
		@SuppressWarnings("ConstantConditions")
		Explosion instance = (Explosion) (Object) this;

		instance.getAffectedBlocks().removeIf(pos -> RegionManager.getInstance().isProtected(pos));
	}

}
