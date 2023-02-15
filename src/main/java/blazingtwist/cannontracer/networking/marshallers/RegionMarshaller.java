package blazingtwist.cannontracer.networking.marshallers;

import blazingtwist.cannontracer.serverside.datatype.Region;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Box;

public class RegionMarshaller {

	public static PacketByteBuf regionBoundaryToBuffer(Region region) {
		Box boundingBox = region.getBoundingBox();
		PacketByteBuf buffer = PacketByteBufs.create();

		buffer.writeDouble(boundingBox.minX);
		buffer.writeDouble(boundingBox.minY);
		buffer.writeDouble(boundingBox.minZ);

		buffer.writeDouble(boundingBox.maxX);
		buffer.writeDouble(boundingBox.maxY);
		buffer.writeDouble(boundingBox.maxZ);

		return buffer;
	}

	public static Box bufferToRegionBoundary(PacketByteBuf buffer) {
		double minX = buffer.readDouble();
		double minY = buffer.readDouble();
		double minZ = buffer.readDouble();

		double maxX = buffer.readDouble();
		double maxY = buffer.readDouble();
		double maxZ = buffer.readDouble();

		return new Box(minX, minY, minZ, maxX, maxY, maxZ);
	}

}
