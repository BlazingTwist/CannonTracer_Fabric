package blazingtwist.cannontracer.networking.marshallers;

import blazingtwist.cannontracer.clientside.TraceRenderer;
import blazingtwist.cannontracer.serverside.datatype.EntityDataChain;
import blazingtwist.cannontracer.shared.datatypes.FinalVec3d;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class EntityTraceMarshaller {

	public static PacketByteBuf serverTraceToBuffer(long firstSpawnTick, Map<String, List<EntityDataChain>> entitiesByTypeName) {
		PacketByteBuf buffer = PacketByteBufs.create();

		// packet structure:
		// numEntries:
		//   entityName, numTraces:
		//     startTick, numTicks:
		//       position, velocity
		// =
		// varInt, [string, varInt, [varLong, varInt, [double, double, double, double, double, double]]]

		Set<Map.Entry<String, List<EntityDataChain>>> entityEntries = entitiesByTypeName.entrySet();
		buffer.writeVarInt(entityEntries.size());

		for (Map.Entry<String, List<EntityDataChain>> entityEntry : entityEntries) {
			buffer.writeString(entityEntry.getKey());
			buffer.writeVarInt(entityEntry.getValue().size());
			for (EntityDataChain chain : entityEntry.getValue()) {
				long startTick = chain.creationTick - firstSpawnTick;
				buffer.writeVarLong(startTick);
				buffer.writeVarInt(chain.links.size());
				for (EntityDataChain.ChainLink tickData : chain.links) {
					buffer.writeDouble(tickData.position().x);
					buffer.writeDouble(tickData.position().y);
					buffer.writeDouble(tickData.position().z);
					buffer.writeDouble(tickData.velocity().x);
					buffer.writeDouble(tickData.velocity().y);
					buffer.writeDouble(tickData.velocity().z);
				}
			}
		}

		return buffer;
	}

	public static void pushTraceBufferToClient(PacketByteBuf buffer, TraceRenderer clientRenderer) {
		clientRenderer.clearTraces();

		int numEntityTypes = buffer.readVarInt();
		for (int entityTypeIndex = 0; entityTypeIndex < numEntityTypes; entityTypeIndex++) {
			String entityTypeName = buffer.readString();
			int numEntityTraces = buffer.readVarInt();
			for (int entityTraceIndex = 0; entityTraceIndex < numEntityTraces; entityTraceIndex++) {
				long traceStartTick = buffer.readVarLong();
				int numTraceTicks = buffer.readVarInt();
				FinalVec3d[] tracePositions = new FinalVec3d[numTraceTicks];
				FinalVec3d[] traceVelocities = new FinalVec3d[numTraceTicks];
				for(int traceTick = 0; traceTick < numTraceTicks; traceTick++) {
					tracePositions[traceTick] = new FinalVec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
					traceVelocities[traceTick] = new FinalVec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
				}

				clientRenderer.addTrace(entityTypeName, traceStartTick, tracePositions, traceVelocities);
			}
		}
	}

}
