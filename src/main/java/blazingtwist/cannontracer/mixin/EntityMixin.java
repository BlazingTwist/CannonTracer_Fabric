package blazingtwist.cannontracer.mixin;

import blazingtwist.cannontracer.serverside.EntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// env: local and dedicated server
@Mixin(Entity.class)
public abstract class EntityMixin {

	@Inject(at = @At("HEAD"), method = "move")
	public void onMove(MovementType moveType, Vec3d velocity, CallbackInfo info) {
		Entity instance = (Entity) (Object) this;
		if (instance != null && moveType == MovementType.SELF && instance.getServer() != null) {
			EntityTracker.getInstance().onEntityMove(instance, velocity);
		}
	}

}
