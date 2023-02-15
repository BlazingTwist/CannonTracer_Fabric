package blazingtwist.cannontracer.clientside.gui;

import blazingtwist.cannontracer.clientside.SettingsManager;
import blazingtwist.cannontracer.clientside.gui.panels.EntitySettingsPanel;
import blazingtwist.cannontracer.clientside.gui.panels.HotKeysPanel;
import blazingtwist.cannontracer.clientside.gui.panels.KeyBindsPanel;
import blazingtwist.cannontracer.clientside.gui.panels.TracerSettingsPanel;
import blazingtwist.cannontracer.clientside.gui.panels.WTabPanel;
import blazingtwist.cannontracer.clientside.gui.widgets.CTTextures;
import blazingtwist.cannontracer.networking.ClientPacketHandler;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.minecraft.text.Text;

public class TracerConfigGui extends LightweightGuiDescription implements IOnCloseListener {

	private static TracerConfigGui instance;

	public static TracerConfigGui getInstance() {
		if (instance == null) {
			instance = new TracerConfigGui();
		}
		return instance;
	}

	private static final int sizeX = 630;
	private static final int sizeY = 310;

	private TracerConfigGui() {
		WTabPanel mainTabPanel = new WTabPanel();
		mainTabPanel.setSize(sizeX, sizeY);

		mainTabPanel.add(
				TracerSettingsPanel.createPanel(),
				tab -> tab.icon(new TextureIcon(CTTextures.ICON_GEAR).setColor(0xff_53c653)).tooltip(Text.translatable("gui.cannontracer.settings_tab.title"))
		);
		mainTabPanel.add(
				new EntitySettingsPanel(),
				tab -> tab.icon(new TextureIcon(CTTextures.ICON_RADAR).setColor(0xff_53c653)).tooltip(Text.translatable("gui.cannontracer.entity_tab.title"))
		);
		mainTabPanel.add(
				KeyBindsPanel.createKeyBindsPanel(),
				tab -> tab.icon(new TextureIcon(CTTextures.ICON_KEY_BINDS).setColor(0xff_53c653)).tooltip(Text.translatable("gui.cannontracer.key_binds_tab.title"))
		);
		mainTabPanel.add(
				HotKeysPanel.createHotKeysPanel(),
				tab -> tab.icon(new TextureIcon(CTTextures.ICON_HOT_KEYS).setColor(0xff_53c653)).tooltip(Text.translatable("gui.cannontracer.hot_keys_tab.title"))
		);

		setRootPanel(mainTabPanel);
		mainTabPanel.validate(this);
		this.setFullscreen(true);
	}


	@Override
	public void onClose() {
		SettingsManager.getInstance().saveSettingsToFile();
		ClientPacketHandler.getInstance().clientToServer_sendClientConfig();
	}
}
