package blazingtwist.cannontracer.clientside.gui.panels;

import blazingtwist.cannontracer.clientside.SettingsManager;
import blazingtwist.cannontracer.clientside.datatype.KeyBind;
import blazingtwist.cannontracer.clientside.datatype.TracerConfig;
import blazingtwist.cannontracer.clientside.datatype.TracerKeyBinds;
import blazingtwist.cannontracer.clientside.gui.widgets.CTTextures;
import blazingtwist.cannontracer.clientside.gui.widgets.LabelWithShadow;
import blazingtwist.cannontracer.clientside.gui.widgets.TextureButton;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WScrollPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class KeyBindsPanel extends WBox {

	public static WPanel createKeyBindsPanel() {
		KeyBindsPanel keyBindsPanel = new KeyBindsPanel();
		WScrollPanel scrollPanel = new WScrollPanel(keyBindsPanel);
		scrollPanel.setSize(629, 280);
		keyBindsPanel.setParent(scrollPanel);
		return scrollPanel;
	}

	private WScrollPanel parent;

	private KeyBindsPanel() {
		super(Axis.VERTICAL);
		this.setSpacing(5);
		this.setVerticalAlignment(VerticalAlignment.TOP);
		this.setHorizontalAlignment(HorizontalAlignment.LEFT);
		this.setInsets(new Insets(4));

		TracerConfig tracerConfig = SettingsManager.getInstance().getTracerConfig();
		TracerKeyBinds keyBindsConfig = tracerConfig.getKeyBinds();
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_open_menu", keyBindsConfig.openMenu));
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_x_ray", keyBindsConfig.toggleXRay));
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_position_text", keyBindsConfig.togglePositionText));
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_velocity_text", keyBindsConfig.toggleVelocityText));
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_pull_traces", keyBindsConfig.pullData));
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_clear_traces", keyBindsConfig.clearData));
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_increment_tick", keyBindsConfig.displayTickIncrement));
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_decrement_tick", keyBindsConfig.displayTickDecrement));
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_next_despawn_tick", keyBindsConfig.displayNextDespawnTick));
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_previous_despawn_tick", keyBindsConfig.displayPrevDespawnTick));
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_last_tick", keyBindsConfig.displayLastTick));
		this.add(new KeyBindsEntryPanel(this, "gui.cannontracer.key_binds_tab.bind_first_tick", keyBindsConfig.displayFirstTick));
	}

	public void setParent(WScrollPanel parent) {
		this.parent = parent;
	}

	public void forceLayout() {
		this.width = this.children.stream().mapToInt(WWidget::getWidth).max().orElse(0);
		this.layout();
		if (parent != null) {
			parent.layout();
		}
	}

	public static class KeyBindsEntryPanel extends WPlainPanel implements SingleKeyPanel.KeyPanelContainer {
		private final KeyBindsPanel parent;
		private final KeyBind correspondingKeyBind;

		private final List<SingleKeyPanel> keyPanelsList = new ArrayList<>();
		private final TextureButton addKeyButton;

		private final int baseKeyPanelX;

		public KeyBindsEntryPanel(KeyBindsPanel parent, String translationKey, KeyBind existingKeyBind) {
			this.parent = parent;
			correspondingKeyBind = existingKeyBind;

			this.setInsets(new Insets(0, 0, 0, 4));
			this.setSize(20, 20);
			this.setBackgroundPainter(this::paintBackground);

			TextureButton deleteAllKeysButton = new TextureButton(CTTextures.BUTTON_PACKAGE_RED_CIRCLE);
			deleteAllKeysButton.setLabelTexture(CTTextures.ICON_CROSS);
			deleteAllKeysButton.setOnClick(this::onDeleteAllKeysPressed);
			this.add(deleteAllKeysButton, 4, 4, 11, 11);

			LabelWithShadow keyBindLabel = new LabelWithShadow(Text.translatable(translationKey).formatted(Formatting.WHITE));
			keyBindLabel.setHorizontalAlignment(HorizontalAlignment.LEFT);
			keyBindLabel.setVerticalAlignment(VerticalAlignment.CENTER);
			this.add(keyBindLabel, 22, 2, 110, 16);

			baseKeyPanelX = 22 + 110 + 7 - 12;
			for (Integer triggerKey : existingKeyBind.getTrigger()) {
				SingleKeyPanel keyPanel = new SingleKeyPanel(this, triggerKey, true);
				keyPanelsList.add(keyPanel);
				this.add(keyPanel, 0, 0, 115, 20);
			}

			for (Integer exclude : existingKeyBind.getExclude()) {
				SingleKeyPanel keyPanel = new SingleKeyPanel(this, exclude, false);
				keyPanelsList.add(keyPanel);
				this.add(keyPanel, 0, 0, 115, 20);
			}

			addKeyButton = new TextureButton(CTTextures.BUTTON_PACKAGE_GREEN_CIRCLE);
			addKeyButton.setLabelTexture(CTTextures.ICON_PLUS);
			addKeyButton.setOnClick(this::onAddKeyPanelPressed);
			this.add(addKeyButton, 0, 0, 11, 11);

			recomputePositions();
		}

		private void paintBackground(MatrixStack matrices, int x, int y, WWidget widget) {
			ScreenDrawing.coloredRect(matrices, x, y, this.getWidth(), this.getHeight(), 0x28000000);
		}

		@Override
		public void removeKeyPanel(SingleKeyPanel panel) {
			if (panel.isTriggerKey()) {
				correspondingKeyBind.removeTrigger(panel.getKey());
			} else {
				correspondingKeyBind.removeExclude(panel.getKey());
			}
			this.width -= (panel.getWidth() + 12);
			this.remove(panel);
			keyPanelsList.remove(panel);
			recomputePositions();
		}

		@Override
		public void handleKeyChange(SingleKeyPanel panel, int oldKey, int newKey) {
			if (panel.isTriggerKey()) {
				correspondingKeyBind.removeTrigger(oldKey);
				correspondingKeyBind.addTrigger(newKey);
			} else {
				correspondingKeyBind.removeExclude(oldKey);
				correspondingKeyBind.removeExclude(newKey);
			}
		}

		@Override
		public void handleTriggerToggle(SingleKeyPanel panel, boolean isTriggerKey) {
			int key = panel.getKey();
			if (isTriggerKey) {
				correspondingKeyBind.removeExclude(key);
				correspondingKeyBind.addTrigger(key);
			} else {
				correspondingKeyBind.removeTrigger(key);
				correspondingKeyBind.addExclude(key);
			}
		}

		@Override
		public boolean canResize() {
			return false;
		}

		private void onDeleteAllKeysPressed() {
			correspondingKeyBind.setTrigger();
			correspondingKeyBind.setExclude();
			for (SingleKeyPanel panel : keyPanelsList) {
				this.width -= (panel.getWidth() + 12);
				this.remove(panel);
			}
			keyPanelsList.clear();
			recomputePositions();
		}

		private void onAddKeyPanelPressed() {
			correspondingKeyBind.addTrigger(GLFW.GLFW_KEY_UNKNOWN);
			SingleKeyPanel panel = new SingleKeyPanel(this, GLFW.GLFW_KEY_UNKNOWN, true);
			keyPanelsList.add(panel);
			this.add(panel, 0, 0, 115, 20);
			panel.setHost(this.getHost());
			recomputePositions();
		}

		private void recomputePositions() {
			int keyPanelX = baseKeyPanelX;
			for (SingleKeyPanel panel : keyPanelsList) {
				panel.setLocation(keyPanelX, 0);
				keyPanelX += (panel.getWidth() + 12);
			}
			addKeyButton.setLocation(keyPanelX + 14, 4);

			parent.forceLayout();
		}
	}

}
