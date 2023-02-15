package blazingtwist.cannontracer.shared.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MutableVec3d {
	@JsonProperty("x")
	private double x;

	@JsonProperty("y")
	private double y;

	@JsonProperty("z")
	private double z;

	public MutableVec3d() {
	}

	public void set(FinalVec3d vec3d) {
		this.x = vec3d.x();
		this.y = vec3d.y();
		this.z = vec3d.z();
	}

	public void setMax(MutableVec3d other) {
		x = max(x, other.x);
		y = max(y, other.y);
		z = max(z, other.z);
	}

	public void setMax(FinalVec3d other) {
		x = max(x, other.x());
		y = max(y, other.y());
		z = max(z, other.z());
	}

	public FinalVec3d toVec3d() {
		return new FinalVec3d(x, y, z);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	private double max(double a, double b) {
		return Math.abs(a) >= Math.abs(b) ? a : b;
	}
}
