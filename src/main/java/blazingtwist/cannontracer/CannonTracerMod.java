package blazingtwist.cannontracer;

import blazingtwist.cannontracer.clientside.RegionRenderer;
import blazingtwist.cannontracer.clientside.SettingsManager;
import blazingtwist.cannontracer.clientside.TraceRenderer;
import blazingtwist.cannontracer.clientside.command.TracerClientCMD;
import blazingtwist.cannontracer.clientside.gui.HudRenderer;
import blazingtwist.cannontracer.networking.ClientPacketHandler;
import blazingtwist.cannontracer.networking.ServerPacketHandler;
import blazingtwist.cannontracer.serverside.CannonTesterCommandHandler;
import blazingtwist.cannontracer.serverside.EntityTracker;
import blazingtwist.cannontracer.serverside.RegionManager;
import blazingtwist.cannontracer.serverside.command.CommandRegistrar;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CannonTracerMod implements ModInitializer, ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("cannontracer");

	@Override
	public void onInitialize() {
		LOGGER.info("initializing cannon tracer mod.");
		RegionManager.getInstance();
		EntityTracker.getInstance();
		ServerPacketHandler.getInstance();
		CannonTesterCommandHandler.getInstance();
		CommandRegistrar.getInstance();
	}

	@Override
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void onInitializeClient() {
		LOGGER.info("initializing cannon tracer mod, client-side.");
		HudRenderer.getInstance();
		SettingsManager.getInstance();
		TracerClientCMD.getInstance();
		RegionRenderer.getInstance();
		TraceRenderer.getInstance();
		ClientPacketHandler.getInstance();
	}
}
