package blazingtwist.cannontracer.shared.utils;

import blazingtwist.cannontracer.clientside.datatype.Color;
import blazingtwist.cannontracer.shared.datatypes.FinalVec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.Box;

public class RenderUtils {

	public static void drawHitBox(BufferBuilder buffer, FinalVec3d position, Color color, double boxRadius) {
		drawBox(buffer, position, color, boxRadius, boxRadius * 2);
	}

	public static void drawBox(BufferBuilder buffer, FinalVec3d position, Color color, double boxRadius, double boxHeight) {
		pushXLine(buffer, position.x() - boxRadius, position.x() + boxRadius, position.y(), position.z() - boxRadius, color);
		pushXLine(buffer, position.x() - boxRadius, position.x() + boxRadius, position.y(), position.z() + boxRadius, color);
		pushXLine(buffer, position.x() - boxRadius, position.x() + boxRadius, position.y() + boxHeight, position.z() - boxRadius, color);
		pushXLine(buffer, position.x() - boxRadius, position.x() + boxRadius, position.y() + boxHeight, position.z() + boxRadius, color);

		pushYLine(buffer, position.x() - boxRadius, position.y(), position.y() + boxHeight, position.z() - boxRadius, color);
		pushYLine(buffer, position.x() - boxRadius, position.y(), position.y() + boxHeight, position.z() + boxRadius, color);
		pushYLine(buffer, position.x() + boxRadius, position.y(), position.y() + boxHeight, position.z() - boxRadius, color);
		pushYLine(buffer, position.x() + boxRadius, position.y(), position.y() + boxHeight, position.z() + boxRadius, color);

		pushZLine(buffer, position.x() - boxRadius, position.y(), position.z() - boxRadius, position.z() + boxRadius, color);
		pushZLine(buffer, position.x() + boxRadius, position.y(), position.z() - boxRadius, position.z() + boxRadius, color);
		pushZLine(buffer, position.x() - boxRadius, position.y() + boxHeight, position.z() - boxRadius, position.z() + boxRadius, color);
		pushZLine(buffer, position.x() + boxRadius, position.y() + boxHeight, position.z() - boxRadius, position.z() + boxRadius, color);
	}

	public static void pushXLine(BufferBuilder buffer, double x0, double x1, double y, double z, Color color) {
		buffer.vertex(x0, y, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(1, 0, 0).next();
		buffer.vertex(x1, y, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(1, 0, 0).next();
	}

	public static void pushYLine(BufferBuilder buffer, double x, double y0, double y1, double z, Color color) {
		buffer.vertex(x, y0, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(0, 1, 0).next();
		buffer.vertex(x, y1, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(0, 1, 0).next();
	}

	public static void pushZLine(BufferBuilder buffer, double x, double y, double z0, double z1, Color color) {
		buffer.vertex(x, y, z0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(0, 0, 1).next();
		buffer.vertex(x, y, z1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(0, 0, 1).next();
	}

	public static void pushBox(BufferBuilder buffer, Box box, Color color) {
		pushXLine(buffer, box.minX, box.maxX, box.minY, box.minZ, color);
		pushXLine(buffer, box.minX, box.maxX, box.minY, box.maxZ, color);
		pushXLine(buffer, box.minX, box.maxX, box.maxY, box.minZ, color);
		pushXLine(buffer, box.minX, box.maxX, box.maxY, box.maxZ, color);

		pushYLine(buffer, box.minX, box.minY, box.maxY, box.minZ, color);
		pushYLine(buffer, box.minX, box.minY, box.maxY, box.maxZ, color);
		pushYLine(buffer, box.maxX, box.minY, box.maxY, box.minZ, color);
		pushYLine(buffer, box.maxX, box.minY, box.maxY, box.maxZ, color);

		pushZLine(buffer, box.minX, box.minY, box.minZ, box.maxZ, color);
		pushZLine(buffer, box.minX, box.maxY, box.minZ, box.maxZ, color);
		pushZLine(buffer, box.maxX, box.minY, box.minZ, box.maxZ, color);
		pushZLine(buffer, box.maxX, box.maxY, box.minZ, box.maxZ, color);
	}

}
