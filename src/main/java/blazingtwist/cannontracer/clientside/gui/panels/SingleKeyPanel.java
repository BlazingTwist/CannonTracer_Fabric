package blazingtwist.cannontracer.clientside.gui.panels;

import blazingtwist.cannontracer.clientside.gui.widgets.CTTextures;
import blazingtwist.cannontracer.clientside.gui.widgets.KeyBindField;
import blazingtwist.cannontracer.clientside.gui.widgets.TextureButton;
import blazingtwist.cannontracer.clientside.gui.widgets.ToggleButton;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.util.InputUtil;

/**
 * Panel that contains a single key of a KeyBind
 * [keyDeleteButton, keyPressReleaseToggleButton, keyInput]
 */
public class SingleKeyPanel extends WPlainPanel {

	private final KeyPanelContainer parentKeyBind;
	private final TextureButton deleteKeyButton;
	private final KeyBindField keyBindField;
	private final ToggleButton keyPressReleaseToggle;

	public SingleKeyPanel(KeyPanelContainer parentKeyBind, int key, boolean isTriggerKey) {
		this.parentKeyBind = parentKeyBind;

		deleteKeyButton = new TextureButton(CTTextures.BUTTON_PACKAGE_RED_CIRCLE);
		deleteKeyButton.setLabelTexture(CTTextures.ICON_CROSS);
		deleteKeyButton.setOnClick(this::onDeleteClicked);
		this.add(deleteKeyButton, 2, 4, 11, 11);

		keyBindField = new KeyBindField();
		keyBindField.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(key));
		keyBindField.setOnKeyChanged(this::onBoundKeyChanged);
		this.add(keyBindField, 16, 2, 75, 16);

		keyPressReleaseToggle = new ToggleButton(CTTextures.BUTTON_PACKAGE_VANILLA_50, isTriggerKey);
		keyPressReleaseToggle.setEnabledLabels(null, CTTextures.ICON_KEY_PRESS, 0xaaffaa);
		keyPressReleaseToggle.setDisabledLabels(null, CTTextures.ICON_KEY_RELEASE, 0xffaaaa);
		keyPressReleaseToggle.setOnToggleListener(this::onKeyPressReleaseToggle);
		this.add(keyPressReleaseToggle, 94, 2, 15, 15);

		this.setSize(115, 20);
		this.setInsets(new Insets(0, 12, 0, 0));
	}

	@Override
	public boolean canResize() {
		return false;
	}

	public int getKey() {
		return keyBindField.getBoundKey().getCode();
	}

	public boolean isTriggerKey() {
		return keyPressReleaseToggle.getState();
	}

	private void onDeleteClicked() {
		parentKeyBind.removeKeyPanel(this);
	}

	private void onKeyPressReleaseToggle(boolean newState) {
		parentKeyBind.handleTriggerToggle(this, newState);
	}

	private void onBoundKeyChanged(InputUtil.Key oldKey, InputUtil.Key newKey) {
		parentKeyBind.handleKeyChange(this, oldKey.getCode(), newKey.getCode());
	}

	public interface KeyPanelContainer {
		void removeKeyPanel(SingleKeyPanel panel);

		void handleTriggerToggle(SingleKeyPanel panel, boolean isTriggerKey);

		void handleKeyChange(SingleKeyPanel panel, int oldKey, int newKey);
	}
}
