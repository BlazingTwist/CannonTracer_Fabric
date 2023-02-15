package blazingtwist.cannontracer.shared.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.minecraft.util.math.Vec3d;

public record FinalVec3d(
		@JsonProperty("x") double x,
		@JsonProperty("y") double y,
		@JsonProperty("z") double z) {

	public static FinalVec3d fromVec3d(Vec3d other) {
		return new FinalVec3d(other.x, other.y, other.z);
	}

	public Vec3d toVec3d() {
		return new Vec3d(this.x, this.y, this.z);
	}
}
