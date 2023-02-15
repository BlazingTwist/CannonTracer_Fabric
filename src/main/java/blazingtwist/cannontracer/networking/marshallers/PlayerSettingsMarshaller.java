package blazingtwist.cannontracer.networking.marshallers;

import blazingtwist.cannontracer.clientside.datatype.EntityTrackingSettings;
import blazingtwist.cannontracer.clientside.datatype.TracerConfig;
import blazingtwist.cannontracer.serverside.datatype.PlayerSettings;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class PlayerSettingsMarshaller {
	public static PacketByteBuf clientSettingsToBuffer(TracerConfig clientSettings) {
		PacketByteBuf buffer = PacketByteBufs.create();

		buffer.writeBoolean(clientSettings.isLogEntities());
		buffer.writeInt(clientSettings.getMaxRange());

		List<Map.Entry<String, EntityTrackingSettings>> activeEntities = clientSettings.getTrackedEntities().entrySet().stream()
				.filter(entry -> entry.getValue().isRender())
				.collect(Collectors.toList());

		buffer.writeInt(activeEntities.size());
		for (Map.Entry<String, EntityTrackingSettings> activeEntity : activeEntities) {
			buffer.writeString(activeEntity.getKey());
			buffer.writeFloat(activeEntity.getValue().getTime());
		}

		return buffer;
	}

	public static PlayerSettings serverSettingsFromBuffer(PacketByteBuf buffer) {
		PlayerSettings playerSettings = new PlayerSettings();

		playerSettings.listenToEntitySpawns = buffer.readBoolean();
		playerSettings.maxRange = buffer.readInt();

		int numActiveEntities = buffer.readInt();
		for (int i = 0; i < numActiveEntities; i++) {
			String entityName = buffer.readString();
			float time = buffer.readFloat();
			playerSettings.observedEntities.put(entityName, time);
		}

		return playerSettings;
	}
}
