package blazingtwist.cannontracer.mixin;

import blazingtwist.cannontracer.serverside.RegionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(World.class)
public class WorldMixin {

	@ModifyVariable(at = @At("STORE"), method = "createExplosion("
			+ "Lnet/minecraft/entity/Entity;"
			+ "Lnet/minecraft/entity/damage/DamageSource;"
			+ "Lnet/minecraft/world/explosion/ExplosionBehavior;"
			+ "D" + "D" + "D" + "F" + "Z"
			+ "Lnet/minecraft/world/World$ExplosionSourceType;"
			+ "Z"
			+ ")"
			+ "Lnet/minecraft/world/explosion/Explosion;")
	public Explosion.DestructionType onCreateExplosion(Explosion.DestructionType destructionType,
													   Entity entity, DamageSource damageSource, ExplosionBehavior behavior,
													   double x, double y, double z, float power, boolean createFire,
													   World.ExplosionSourceType sourceType,
													   boolean particles) {
		if (RegionManager.getInstance().shouldNullifyExplosionDamage(x, y, z)) {
			return Explosion.DestructionType.KEEP;
		}
		return destructionType;
	}

}
