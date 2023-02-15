package blazingtwist.cannontracer.clientside.gui;

import blazingtwist.cannontracer.clientside.gui.panels.CannonTesterPanel;
import blazingtwist.cannontracer.networking.ClientPacketHandler;
import blazingtwist.cannontracer.shared.datatypes.TestCannonData;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import net.minecraft.util.math.Vec3i;

public class CannonTesterGui extends LightweightGuiDescription implements IOnCloseListener {

	private static CannonTesterGui instance;

	public static CannonTesterGui getInstance() {
		if (instance == null) {
			instance = new CannonTesterGui();
		}
		return instance;
	}

	private final CannonTesterPanel cannonTesterPanel;

	private CannonTesterGui() {
		cannonTesterPanel = CannonTesterPanel.createPanel();
		setRootPanel(cannonTesterPanel);
		cannonTesterPanel.validate(this);
		this.setFullscreen(true);
	}

	public void loadData(Vec3i commandPos, TestCannonData cannon) {
		cannonTesterPanel.loadCannon(commandPos, cannon);
	}

	@Override
	public void onClose() {
		ClientPacketHandler packetHandler = ClientPacketHandler.getInstance();
		Vec3i commandPos = cannonTesterPanel.getCommandPos();
		TestCannonData cannon = cannonTesterPanel.buildCannon();
		packetHandler.clientToServer_sendCannonData(commandPos, cannon);
	}
}
