package blazingtwist.cannontracer.networking;

import blazingtwist.cannontracer.networking.marshallers.EntityTraceMarshaller;
import blazingtwist.cannontracer.networking.marshallers.PlayerSettingsMarshaller;
import blazingtwist.cannontracer.networking.marshallers.RegionMarshaller;
import blazingtwist.cannontracer.networking.marshallers.TestCannonMarshaller;
import blazingtwist.cannontracer.serverside.CannonTesterCommandHandler;
import blazingtwist.cannontracer.serverside.EntityTracker;
import blazingtwist.cannontracer.serverside.TracerPlayerManager;
import blazingtwist.cannontracer.serverside.datatype.EntityDataChain;
import blazingtwist.cannontracer.serverside.datatype.PlayerSettings;
import blazingtwist.cannontracer.serverside.datatype.Region;
import blazingtwist.cannontracer.shared.datatypes.TestCannonData;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class ServerPacketHandler {

	private static ServerPacketHandler instance;

	public static ServerPacketHandler getInstance() {
		if (instance == null) {
			instance = new ServerPacketHandler();
		}
		return instance;
	}

	private ServerPacketHandler() {
		ServerPlayNetworking.registerGlobalReceiver(PacketIdentifiers.ClientToServer_sendClientConfig, this::onServerReceives_sendClientConfig);
		ServerPlayNetworking.registerGlobalReceiver(PacketIdentifiers.ClientToServer_requestTraceData, this::onServerReceives_requestTraceData);
		ServerPlayNetworking.registerGlobalReceiver(PacketIdentifiers.ClientToServer_sendCannonData, this::onServerReceives_sendCannonData);
	}

	public void serverToClient_requestClientConfig(ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, PacketIdentifiers.ServerToClient_requestClientConfig, PacketByteBufs.empty());
	}

	public void serverToClient_sendTraceData(ServerPlayerEntity player, long firstSpawnTick, Map<String, List<EntityDataChain>> entitiesByTypeName) {
		PacketByteBuf buffer = EntityTraceMarshaller.serverTraceToBuffer(firstSpawnTick, entitiesByTypeName);
		ServerPlayNetworking.send(player, PacketIdentifiers.ServerToClient_sendTraceData, buffer);
	}

	public void serverToClient_sendCannonData(ServerPlayerEntity player, BlockPos commandPos, TestCannonData data) {
		PacketByteBuf buffer = TestCannonMarshaller.testCannonToBuffer(commandPos, data);
		ServerPlayNetworking.send(player, PacketIdentifiers.ServerToClient_sendCannonData, buffer);
	}

	public void serverToClient_sendRegionData(ServerPlayerEntity player, Region region) {
		PacketByteBuf buffer = RegionMarshaller.regionBoundaryToBuffer(region);
		ServerPlayNetworking.send(player, PacketIdentifiers.ServerToClient_sendRegionData, buffer);
	}

	public void serverToClient_clearRegionData(ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, PacketIdentifiers.ServerToClient_clearRegionData, PacketByteBufs.empty());
	}

	private void onServerReceives_sendClientConfig(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		PlayerSettings playerSettings = PlayerSettingsMarshaller.serverSettingsFromBuffer(buf);
		TracerPlayerManager.getInstance().updatePlayerSettings(player, playerSettings);
	}

	private void onServerReceives_requestTraceData(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		EntityTracker.getInstance().onPlayerRequestTraceData(player);
	}

	private void onServerReceives_sendCannonData(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		TestCannonMarshaller.bufferToCannon(buf, (pos, cannon) -> {
			server.execute(() -> CannonTesterCommandHandler.getInstance().writeCannonToBlock(player, pos, cannon));
		});
	}
}
