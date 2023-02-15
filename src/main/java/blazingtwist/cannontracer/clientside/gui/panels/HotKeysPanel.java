package blazingtwist.cannontracer.clientside.gui.panels;

import blazingtwist.cannontracer.clientside.SettingsManager;
import blazingtwist.cannontracer.clientside.datatype.HotKeyConfig;
import blazingtwist.cannontracer.clientside.datatype.KeyBind;
import blazingtwist.cannontracer.clientside.gui.widgets.BetterTextField;
import blazingtwist.cannontracer.clientside.gui.widgets.CTTextures;
import blazingtwist.cannontracer.clientside.gui.widgets.LabelWithShadow;
import blazingtwist.cannontracer.clientside.gui.widgets.TextureButton;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WScrollPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class HotKeysPanel extends WBox {

	public static WPanel createHotKeysPanel() {
		HotKeysPanel hotKeysPanel = new HotKeysPanel();
		WScrollPanel scrollPanel = new WScrollPanel(hotKeysPanel);
		scrollPanel.setSize(629, 280);
		hotKeysPanel.setParent(scrollPanel);
		return scrollPanel;
	}

	private WScrollPanel parent;

	private HotKeysPanel() {
		super(Axis.VERTICAL);
		this.setInsets(new Insets(4));
		this.setSpacing(5);
		this.setVerticalAlignment(VerticalAlignment.TOP);
		this.setHorizontalAlignment(HorizontalAlignment.LEFT);

		this.add(new HotKeysPanelHeaderRow(this));

		HotKeyConfig hotKeysConfig = SettingsManager.getInstance().getTracerConfig().getHotKeys();
		for (HotKeyConfig.HotKey hotKey : hotKeysConfig.getHotKeys()) {
			this.add(new HotKeyPanel(this, hotKey));
		}
	}

	public void setParent(WScrollPanel parent) {
		this.parent = parent;
	}

	public void createNewHotKey() {
		HotKeyConfig.HotKey hotKey = new HotKeyConfig.HotKey("", new KeyBind());
		HotKeyPanel panel = new HotKeyPanel(this, hotKey);
		this.add(panel);
		panel.setHost(this.getHost());
		forceLayout();

		HotKeyConfig hotKeysConfig = SettingsManager.getInstance().getTracerConfig().getHotKeys();
		hotKeysConfig.getHotKeys().add(hotKey);
	}

	public void deleteHotKey(HotKeyPanel panel) {
		this.height -= (panel.getHeight() + this.spacing);
		this.remove(panel);
		forceLayout();

		HotKeyConfig hotKeysConfig = SettingsManager.getInstance().getTracerConfig().getHotKeys();
		hotKeysConfig.getHotKeys().remove(panel.correspondingHotKey);
	}

	public void forceLayout() {
		this.width = this.children.stream().mapToInt(WWidget::getWidth).max().orElse(0);
		this.layout();
		if (parent != null) {
			parent.layout();
		}
	}

	private static class HotKeysPanelHeaderRow extends WBox {

		private final HotKeysPanel parent;

		public HotKeysPanelHeaderRow(HotKeysPanel parent) {
			super(Axis.HORIZONTAL);
			this.setInsets(new Insets(0, 4));
			this.setSpacing(5);
			this.setVerticalAlignment(VerticalAlignment.BOTTOM);
			this.setHorizontalAlignment(HorizontalAlignment.LEFT);

			this.parent = parent;

			LabelWithShadow commandsLabel = new LabelWithShadow(Text.translatable("gui.cannontracer.hot_keys_tab.label_commands").formatted(Formatting.WHITE));
			this.add(commandsLabel, 50, 9);

			TextureButton addCommandButton = new TextureButton(CTTextures.BUTTON_PACKAGE_GREEN_CIRCLE);
			addCommandButton.setLabelTexture(CTTextures.ICON_PLUS);
			addCommandButton.setOnClick(this::onAddCommandPressed);
			this.add(addCommandButton, 11, 11);
		}

		private void onAddCommandPressed() {
			parent.createNewHotKey();
		}
	}

	private static class HotKeyPanel extends WBox implements SingleKeyPanel.KeyPanelContainer {

		private final HotKeysPanel parent;
		private final HotKeyConfig.HotKey correspondingHotKey;

		public HotKeyPanel(HotKeysPanel parent, HotKeyConfig.HotKey hotKey) {
			super(Axis.HORIZONTAL);
			this.setInsets(new Insets(0, 4));
			this.setSpacing(5);
			this.setVerticalAlignment(VerticalAlignment.CENTER);
			this.setHorizontalAlignment(HorizontalAlignment.LEFT);
			this.setBackgroundPainter(this::paintBackground);

			this.parent = parent;
			this.correspondingHotKey = hotKey;

			TextureButton deleteCommandButton = new TextureButton(CTTextures.BUTTON_PACKAGE_RED_CIRCLE);
			deleteCommandButton.setLabelTexture(CTTextures.ICON_CROSS);
			deleteCommandButton.setOnClick(this::onDeleteCommandPressed);
			this.add(deleteCommandButton, 11, 11);

			BetterTextField commandTextField = new BetterTextField();
			commandTextField.setMaxLength(255);
			commandTextField.setText(hotKey.getCommand());
			commandTextField.setOnSubmitListener(this::onCommandSubmit);
			this.add(commandTextField, 200, 16);

			for (Integer trigger : hotKey.getBind().getTrigger()) {
				SingleKeyPanel keyPanel = new SingleKeyPanel(this, trigger, true);
				this.add(keyPanel);
			}
			for (Integer exclude : hotKey.getBind().getExclude()) {
				SingleKeyPanel keyPanel = new SingleKeyPanel(this, exclude, false);
				this.add(keyPanel);
			}

			WWidget spacer = new WWidget();
			spacer.setSize(12 - 5, 20);
			this.add(spacer, 12 - 5, 20);

			TextureButton addKeyButton = new TextureButton(CTTextures.BUTTON_PACKAGE_GREEN_CIRCLE);
			addKeyButton.setLabelTexture(CTTextures.ICON_PLUS);
			addKeyButton.setOnClick(this::onAddKeyButtonPressed);
			this.add(addKeyButton, 11, 11);
		}

		private void paintBackground(MatrixStack matrices, int x, int y, WWidget widget) {
			ScreenDrawing.coloredRect(matrices, x, y, this.getWidth(), this.getHeight(), 0x28000000);
		}

		private void onDeleteCommandPressed() {
			parent.deleteHotKey(this);
		}

		private void onCommandSubmit(String newCommand) {
			correspondingHotKey.setCommand(newCommand);
		}

		@Override
		public void removeKeyPanel(SingleKeyPanel panel) {
			if (panel.isTriggerKey()) {
				correspondingHotKey.getBind().removeTrigger(panel.getKey());
			} else {
				correspondingHotKey.getBind().removeExclude(panel.getKey());
			}
			this.width -= (panel.getWidth() + this.spacing);
			this.remove(panel);
			this.layout();
			parent.forceLayout();
		}

		@Override
		public void handleTriggerToggle(SingleKeyPanel panel, boolean isTriggerKey) {
			int key = panel.getKey();
			KeyBind bind = correspondingHotKey.getBind();
			if (isTriggerKey) {
				bind.removeExclude(key);
				bind.addTrigger(key);
			} else {
				bind.removeTrigger(key);
				bind.addExclude(key);
			}
		}

		@Override
		public void handleKeyChange(SingleKeyPanel panel, int oldKey, int newKey) {
			KeyBind bind = correspondingHotKey.getBind();
			if (panel.isTriggerKey()) {
				bind.removeTrigger(oldKey);
				bind.addTrigger(newKey);
			} else {
				bind.removeExclude(oldKey);
				bind.removeExclude(newKey);
			}
		}

		private void onAddKeyButtonPressed() {
			SingleKeyPanel keyPanel = new SingleKeyPanel(this, GLFW.GLFW_KEY_UNKNOWN, true);
			keyPanel.setParent(this);
			children.add(children.size() - 2, keyPanel);
			keyPanel.setHost(this.getHost());
			this.layout();
			parent.forceLayout();
		}
	}
}
