package blazingtwist.cannontracer.serverside.datatype;

import net.minecraft.util.math.Box;

public class Region {
	private Box boundingBox;
	private boolean tntDisabled = true;

	public Region(Box boundingBox) {
		this.boundingBox = boundingBox;
	}

	public Box getBoundingBox() {
		return boundingBox;
	}

	public boolean isTntDisabled() {
		return tntDisabled;
	}

	public void setBoundingBox(Box boundingBox) {
		this.boundingBox = boundingBox;
	}

	public void setTntDisabled(boolean tntDisabled) {
		this.tntDisabled = tntDisabled;
	}

	public long computePriority() {
		double dx = boundingBox.maxX - boundingBox.minX;
		double dy = boundingBox.maxY - boundingBox.minY;
		double dz = boundingBox.maxZ - boundingBox.minZ;
		return Math.round(dx * dy * dz);
	}
}
