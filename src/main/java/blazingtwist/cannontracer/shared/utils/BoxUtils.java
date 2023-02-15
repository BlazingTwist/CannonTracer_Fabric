package blazingtwist.cannontracer.shared.utils;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BoxUtils {

	public static double getDistanceManhattan(Box box, Vec3d pos) {
		// https://stackoverflow.com/questions/5254838/calculating-distance-between-a-point-and-a-rectangular-box-nearest-point
		double dx = Math.max(0, Math.max(box.minX - pos.x, pos.x - box.maxX));
		double dy = Math.max(0, Math.max(box.minY - pos.y, pos.y - box.maxY));
		double dz = Math.max(0, Math.max(box.minZ - pos.z, pos.z - box.maxZ));
		return dx + dy + dz;
	}

}
