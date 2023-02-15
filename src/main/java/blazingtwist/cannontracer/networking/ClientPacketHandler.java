package blazingtwist.cannontracer.networking;

import blazingtwist.cannontracer.clientside.RegionRenderer;
import blazingtwist.cannontracer.clientside.SettingsManager;
import blazingtwist.cannontracer.clientside.TraceRenderer;
import blazingtwist.cannontracer.clientside.gui.CannonTracerScreen;
import blazingtwist.cannontracer.networking.marshallers.EntityTraceMarshaller;
import blazingtwist.cannontracer.networking.marshallers.PlayerSettingsMarshaller;
import blazingtwist.cannontracer.networking.marshallers.RegionMarshaller;
import blazingtwist.cannontracer.networking.marshallers.TestCannonMarshaller;
import blazingtwist.cannontracer.shared.datatypes.TestCannonData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;

public class ClientPacketHandler {

	private static ClientPacketHandler instance;

	public static ClientPacketHandler getInstance() {
		if(instance == null) {
			instance = new ClientPacketHandler();
		}
		return instance;
	}

	private ClientPacketHandler() {
		ClientPlayNetworking.registerGlobalReceiver(PacketIdentifiers.ServerToClient_requestClientConfig, this::onClientReceives_requestClientConfig);
		ClientPlayNetworking.registerGlobalReceiver(PacketIdentifiers.ServerToClient_sendTraceData, this::onClientReceives_sendTraceData);
		ClientPlayNetworking.registerGlobalReceiver(PacketIdentifiers.ServerToClient_sendCannonData, this::onClientReceives_sendCannonData);
		ClientPlayNetworking.registerGlobalReceiver(PacketIdentifiers.ServerToClient_sendRegionData, this::onClientReceives_sendRegionData);
		ClientPlayNetworking.registerGlobalReceiver(PacketIdentifiers.ServerToClient_clearRegionData, this::onClientReceives_clearRegionData);
	}

	private boolean isClientServerConnectionNotEstablished() {
		return !SettingsManager.getInstance().isRegisteredWithServer();
	}

	private void alertClient_ServerMessageCancelled() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) {
			return;
		}

		MutableText message = Text.literal("Looks like you're trying to send messages to the server, but are not registered yet. Try ").formatted(Formatting.RED);
		message.append(Text.literal("'/tracer register'").formatted(Formatting.AQUA));
		player.sendMessage(message);
	}

	public void clientToServer_sendClientConfig() {
		if (isClientServerConnectionNotEstablished()) {
			return;
		}

		PacketByteBuf packetBuffer = PlayerSettingsMarshaller.clientSettingsToBuffer(SettingsManager.getInstance().getTracerConfig());
		ClientPlayNetworking.send(PacketIdentifiers.ClientToServer_sendClientConfig, packetBuffer);
	}

	public void clientToServer_requestTraceData() {
		if (isClientServerConnectionNotEstablished()) {
			alertClient_ServerMessageCancelled();
			return;
		}

		ClientPlayNetworking.send(PacketIdentifiers.ClientToServer_requestTraceData, PacketByteBufs.empty());
	}

	public void clientToServer_sendCannonData(Vec3i commandPos, TestCannonData cannon) {
		PacketByteBuf buffer = TestCannonMarshaller.testCannonToBuffer(commandPos, cannon);
		ClientPlayNetworking.send(PacketIdentifiers.ClientToServer_sendCannonData, buffer);
	}

	private void onClientReceives_requestClientConfig(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		SettingsManager settingsManager = SettingsManager.getInstance();
		settingsManager.onServerRegistered();
		clientToServer_sendClientConfig();
	}

		private void onClientReceives_sendTraceData(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		EntityTraceMarshaller.pushTraceBufferToClient(buf, TraceRenderer.getInstance());
	}

	private void onClientReceives_sendCannonData(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		TestCannonMarshaller.bufferToCannon(buf, CannonTracerScreen::openScreen);
	}

	private void onClientReceives_sendRegionData(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		Box region = RegionMarshaller.bufferToRegionBoundary(buf);
		RegionRenderer.getInstance().addRegion(region);
	}

	private void onClientReceives_clearRegionData(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		RegionRenderer.getInstance().clearRegions();
	}
}
