package blazingtwist.cannontracer.networking.marshallers;

import blazingtwist.cannontracer.shared.datatypes.FinalVec3d;
import blazingtwist.cannontracer.shared.datatypes.TestCannonData;
import blazingtwist.cannontracer.shared.datatypes.TntAlignment;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3i;

public class TestCannonMarshaller {

	public static PacketByteBuf testCannonToBuffer(Vec3i cmdPos, TestCannonData cannon) {
		PacketByteBuf buffer = PacketByteBufs.create();

		writeVec3i(buffer, cmdPos);
		writeVec3d(buffer, cannon.getBlockOffset());
		writeVec3d(buffer, cannon.getPixelOffset());
		writeVec3d(buffer, cannon.getVelocity());
		buffer.writeEnumConstant(cannon.getXAlign());
		buffer.writeEnumConstant(cannon.getYAlign());
		buffer.writeEnumConstant(cannon.getZAlign());

		List<TestCannonData.CannonCharge> charges = cannon.getCharges();
		buffer.writeVarInt(charges.size());
		for (TestCannonData.CannonCharge charge : charges) {
			buffer.writeBoolean(charge.isEnabled());
			buffer.writeVarInt(charge.getDelay());
			buffer.writeVarInt(charge.getAmount());
			buffer.writeBoolean(charge.getRandom());
			buffer.writeString(charge.getNote());
		}

		return buffer;
	}

	public static void bufferToCannon(PacketByteBuf buffer, TestCannonConsumer callback) {
		Vec3i commandPos = readVec3i(buffer);
		FinalVec3d blockOffset = readVec3d(buffer);
		FinalVec3d pixelOffset = readVec3d(buffer);
		FinalVec3d velocity = readVec3d(buffer);

		TntAlignment xAlign = buffer.readEnumConstant(TntAlignment.class);
		TntAlignment yAlign = buffer.readEnumConstant(TntAlignment.class);
		TntAlignment zAlign = buffer.readEnumConstant(TntAlignment.class);

		TestCannonData cannon = new TestCannonData(blockOffset, pixelOffset, velocity, xAlign, yAlign, zAlign);
		List<TestCannonData.CannonCharge> charges = cannon.getCharges();

		int numCharges = buffer.readVarInt();
		for (int i = 0; i < numCharges; i++) {
			boolean enabled = buffer.readBoolean();
			int delay = buffer.readVarInt();
			int amount = buffer.readVarInt();
			boolean random = buffer.readBoolean();
			String note = buffer.readString();
			charges.add(new TestCannonData.CannonCharge(enabled, delay, amount, random, note));
		}

		callback.acceptData(commandPos, cannon);
	}

	private static void writeVec3i(PacketByteBuf buffer, Vec3i data) {
		buffer.writeInt(data.getX());
		buffer.writeInt(data.getY());
		buffer.writeInt(data.getZ());
	}

	private static void writeVec3d(PacketByteBuf buffer, FinalVec3d data) {
		buffer.writeDouble(data.x());
		buffer.writeDouble(data.y());
		buffer.writeDouble(data.z());
	}

	private static Vec3i readVec3i(PacketByteBuf buffer) {
		int x = buffer.readInt();
		int y = buffer.readInt();
		int z = buffer.readInt();
		return new Vec3i(x, y, z);
	}

	private static FinalVec3d readVec3d(PacketByteBuf buffer) {
		double x = buffer.readDouble();
		double y = buffer.readDouble();
		double z = buffer.readDouble();
		return new FinalVec3d(x, y, z);
	}

	@FunctionalInterface
	public interface TestCannonConsumer {
		void acceptData(Vec3i commandPos, TestCannonData cannon);
	}

}
