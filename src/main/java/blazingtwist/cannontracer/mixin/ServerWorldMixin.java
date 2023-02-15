package blazingtwist.cannontracer.mixin;

import blazingtwist.cannontracer.serverside.EntityTracker;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// env: local and dedicated server
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

	@Inject(at = @At("HEAD"), method = "addPlayer")
	public void onAddPlayer(ServerPlayerEntity player, CallbackInfo info) {
		EntityTracker.getInstance().onEntitySpawn(player);
	}

	@Inject(at = @At("HEAD"), method = "addEntity")
	public void onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
		EntityTracker.getInstance().onEntitySpawn(entity);
	}

	@ModifyVariable(at = @At("HEAD"), method = "addEntities", ordinal = 0, argsOnly = true)
	public Stream<Entity> onAddEntities(Stream<Entity> entities) {
		List<Entity> entityList = entities.collect(Collectors.toList());
		for (Entity entity : entityList) {
			EntityTracker.getInstance().onEntitySpawn(entity);
		}
		return entityList.stream();
	}
}
