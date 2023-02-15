package blazingtwist.cannontracer.networking;

import net.minecraft.util.Identifier;

public interface PacketIdentifiers {

	Identifier ServerToClient_requestClientConfig = new Identifier("cannontracer", "stc_request-client-config");
	Identifier ClientToServer_sendClientConfig = new Identifier("cannontracer", "cts_send-client-config");
	Identifier ClientToServer_requestTraceData = new Identifier("cannontracer", "cts_request-trace-data");
	Identifier ServerToClient_sendTraceData = new Identifier("cannontracer", "stc_send-trace-data");
	Identifier ServerToClient_sendCannonData = new Identifier("cannontracer", "stc_send-cannon-data");
	Identifier ClientToServer_sendCannonData = new Identifier("cannontracer", "cts_send-cannon-data");
	Identifier ServerToClient_sendRegionData = new Identifier("cannontracer", "stc_send-region-data");
	Identifier ServerToClient_clearRegionData = new Identifier("cannontracer", "stc_clear-region-data");

}
