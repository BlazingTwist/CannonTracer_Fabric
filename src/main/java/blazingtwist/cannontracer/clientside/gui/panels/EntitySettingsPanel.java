package blazingtwist.cannontracer.clientside.gui.panels;

import blazingtwist.cannontracer.CannonTracerMod;
import blazingtwist.cannontracer.clientside.SettingsManager;
import blazingtwist.cannontracer.clientside.datatype.Color;
import blazingtwist.cannontracer.clientside.datatype.EntityTrackingSettings;
import blazingtwist.cannontracer.clientside.gui.widgets.BetterTextField;
import blazingtwist.cannontracer.clientside.gui.widgets.CTTextures;
import blazingtwist.cannontracer.clientside.gui.widgets.LabelWithShadow;
import blazingtwist.cannontracer.clientside.gui.widgets.TextureButton;
import blazingtwist.cannontracer.clientside.gui.widgets.ToggleButton;
import blazingtwist.cannontracer.shared.utils.StringUtils;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@SuppressWarnings("FieldCanBeLocal")
public class EntitySettingsPanel extends WPlainPanel {

	private final ArrayList<Map.Entry<String, EntityTrackingSettings>> dataList;

	@SuppressWarnings("MismatchedReadAndWriteOfArray")
	private final LabelWithShadow[] columnLabels;
	private final WListPanel<Map.Entry<String, EntityTrackingSettings>, EntitySettingsPanel.EntitySettingsEntryPanel> entitySettingsListPanel;
	private final TextureButton addEntryButton;

	public EntitySettingsPanel() {
		Columns[] columns = Columns.values();
		columnLabels = new LabelWithShadow[columns.length];
		for (int i = 0; i < columns.length; i++) {
			Columns column = columns[i];
			LabelWithShadow label = new LabelWithShadow(Text.translatable(column.labelTranslatableID).formatted(Formatting.WHITE, Formatting.BOLD));
			label.setHorizontalAlignment(HorizontalAlignment.CENTER);
			label.setVerticalAlignment(VerticalAlignment.CENTER);
			column.placeOffset(this, label, 4, 6, 15);
			columnLabels[i] = label;
		}

		addEntryButton = new TextureButton(CTTextures.BUTTON_PACKAGE_GREEN_CIRCLE);
		addEntryButton.setLabelTexture(CTTextures.ICON_PLUS);
		addEntryButton.setOnClick(this::onAddEntry);
		this.add(addEntryButton, 6, 8, 11, 11);

		dataList = new ArrayList<>(SettingsManager.getInstance().getTracerConfig().getTrackedEntities().entrySet());
		entitySettingsListPanel = new WListPanel<>(
				dataList,
				() -> new EntitySettingsEntryPanel(dataList),
				(listEntry, panel) -> panel.configure(listEntry)
		);
		entitySettingsListPanel.setListItemHeight(20);
		this.add(entitySettingsListPanel, 0, 21, 629, 280);
	}

	private void onAddEntry() {
		String id = "entry_" + (dataList.size() + 1);
		EntityTrackingSettings settings = new EntityTrackingSettings();

		dataList.add(Map.entry(id, settings));
		SettingsManager.getInstance().getTracerConfig().getTrackedEntities().put(id, settings);

		this.layout();
	}

	@SuppressWarnings("FieldCanBeLocal")
	public static class EntitySettingsEntryPanel extends WPlainPanel {
		private final List<Map.Entry<String, EntityTrackingSettings>> dataList;
		private final BetterTextField entityIDField;
		private final BetterTextField timeField;
		private final BetterTextField thicknessField;
		private final BetterTextField hitBoxRadiusField;
		private final BetterTextField redField;
		private final BetterTextField greenField;
		private final BetterTextField blueField;
		private final BetterTextField alphaField;
		private final ToggleButton renderToggleButton;
		private final ToggleButton exposureBoxToggleButton;
		private final TextureButton deleteButton;

		private String currentlyRegisteredEntityID = null;
		private EntityTrackingSettings entitySettings = null;

		public EntitySettingsEntryPanel(List<Map.Entry<String, EntityTrackingSettings>> dataList) {
			this.dataList = dataList;

			entityIDField = new BetterTextField();
			entityIDField.setOnSubmitListener(this::onEntityIDChanged);
			Columns.EntityID.place(this, entityIDField);

			timeField = new BetterTextField();
			timeField.setTextPredicate(StringUtils::isFloat);
			timeField.setOnSubmitListener(this::onTimeChanged);
			Columns.Time.place(this, timeField);

			thicknessField = new BetterTextField();
			thicknessField.setTextPredicate(StringUtils::isFloat);
			thicknessField.setOnSubmitListener(this::onThicknessChanged);
			Columns.Thickness.place(this, thicknessField);

			hitBoxRadiusField = new BetterTextField();
			hitBoxRadiusField.setTextPredicate(StringUtils::isDouble);
			hitBoxRadiusField.setOnSubmitListener(this::onHitBoxRadiusChanged);
			Columns.HitBoxRadius.place(this, hitBoxRadiusField);

			redField = new BetterTextField();
			redField.setTextPredicate(StringUtils::isInteger);
			redField.setOnSubmitListener(this::onRedChanged);
			Columns.Red.place(this, redField);

			greenField = new BetterTextField();
			greenField.setTextPredicate(StringUtils::isInteger);
			greenField.setOnSubmitListener(this::onGreenChanged);
			Columns.Green.place(this, greenField);

			blueField = new BetterTextField();
			blueField.setTextPredicate(StringUtils::isInteger);
			blueField.setOnSubmitListener(this::onBlueChanged);
			Columns.Blue.place(this, blueField);

			alphaField = new BetterTextField();
			alphaField.setTextPredicate(StringUtils::isInteger);
			alphaField.setOnSubmitListener(this::onAlphaChanged);
			Columns.Alpha.place(this, alphaField);

			renderToggleButton = new ToggleButton(CTTextures.BUTTON_PACKAGE_VANILLA_50, true);
			renderToggleButton.setOnToggleListener(this::onRenderChanged);
			Columns.Render.placeOffset(this, renderToggleButton, 0, 0);

			exposureBoxToggleButton = new ToggleButton(CTTextures.BUTTON_PACKAGE_VANILLA_50, true);
			exposureBoxToggleButton.setOnToggleListener(this::onExposureBoxChanged);
			Columns.ExposureBox.placeOffset(this, exposureBoxToggleButton, 0, 0);

			deleteButton = new TextureButton(CTTextures.BUTTON_PACKAGE_RED_CIRCLE);
			deleteButton.setLabelTexture(CTTextures.ICON_CROSS);
			deleteButton.setOnClick(this::onDeleteClicked);
			this.add(deleteButton, 2, 4, 11, 11);

			this.setSize(610, 20);
		}

		public void configure(Map.Entry<String, EntityTrackingSettings> data) {
			String entityID = data.getKey();
			entitySettings = data.getValue();
			currentlyRegisteredEntityID = entityID;
			entityIDField.setText(entityID);
			timeField.setText(Float.toString(entitySettings.getTime()));
			thicknessField.setText(Float.toString(entitySettings.getThickness()));
			hitBoxRadiusField.setText(Double.toString(entitySettings.getHitBoxRadius()));

			Color color = entitySettings.getColor();
			redField.setText(Integer.toString(color.getRed()));
			greenField.setText(Integer.toString(color.getGreen()));
			blueField.setText(Integer.toString(color.getBlue()));
			alphaField.setText(Integer.toString(color.getAlpha()));

			renderToggleButton.setState(entitySettings.isRender());
			exposureBoxToggleButton.setState(entitySettings.isExposureBox());
		}

		private void onEntityIDChanged(String newValue) {
			CannonTracerMod.LOGGER.warn("new entity id value is: " + newValue);

			if (currentlyRegisteredEntityID != null && currentlyRegisteredEntityID.equals(newValue)) {
				return;
			}

			renameEntity(currentlyRegisteredEntityID, newValue);
		}

		private void onTimeChanged(String newValue) {
			float newTime = StringUtils.parseFloat(newValue, 10);
			entitySettings.setTime(newTime);
			if (!timeField.isFocused()) {
				timeField.setText(Float.toString(newTime));
			}
		}

		private void onThicknessChanged(String newValue) {
			float newThickness = StringUtils.parseFloat(newValue, 3);
			entitySettings.setThickness(newThickness);
			if (!thicknessField.isFocused()) {
				thicknessField.setText(Float.toString(newThickness));
			}
		}

		private void onHitBoxRadiusChanged(String newValue) {
			double newHitBoxRadius = StringUtils.parseDouble(newValue, 0.5);
			entitySettings.setHitBoxRadius(newHitBoxRadius);
			if (!hitBoxRadiusField.isFocused()) {
				hitBoxRadiusField.setText(Double.toString(newHitBoxRadius));
			}
		}

		private void onRedChanged(String newValue) {
			int red = Math.max(0, Math.min(255, StringUtils.parseInt(newValue, 0)));
			entitySettings.getColor().setRed(red);
			if (!redField.isFocused()) {
				redField.setText(Integer.toString(red));
			}
		}

		private void onGreenChanged(String newValue) {
			int green = Math.max(0, Math.min(255, StringUtils.parseInt(newValue, 0)));
			entitySettings.getColor().setGreen(green);
			if (!greenField.isFocused()) {
				greenField.setText(Integer.toString(green));
			}
		}

		private void onBlueChanged(String newValue) {
			int blue = Math.max(0, Math.min(255, StringUtils.parseInt(newValue, 0)));
			entitySettings.getColor().setBlue(blue);
			if (!blueField.isFocused()) {
				blueField.setText(Integer.toString(blue));
			}
		}

		private void onAlphaChanged(String newValue) {
			int alpha = Math.max(0, Math.min(255, StringUtils.parseInt(newValue, 0)));
			entitySettings.getColor().setAlpha(alpha);
			if (!alphaField.isFocused()) {
				alphaField.setText(Integer.toString(alpha));
			}
		}

		private void onRenderChanged(boolean newValue) {
			entitySettings.setRender(newValue);
		}

		private void onExposureBoxChanged(boolean newValue) {
			entitySettings.setExposureBox(newValue);
		}

		private void onDeleteClicked() {
			removeEntityID(currentlyRegisteredEntityID);
			recomputeListWidget();
		}

		private void renameEntity(String oldName, String newName) {
			ConcurrentHashMap<String, EntityTrackingSettings> trackedEntities = SettingsManager.getInstance().getTracerConfig().getTrackedEntities();

			int previousIndex = removeEntityID(oldName);
			if (trackedEntities.containsKey(newName)) {
				int removedDuplicateIndex = removeEntityID(newName);
				if (removedDuplicateIndex < previousIndex) {
					previousIndex--;
				}
			}

			trackedEntities.put(newName, entitySettings);
			this.dataList.add(previousIndex, Map.entry(newName, entitySettings));
			currentlyRegisteredEntityID = newName;

			recomputeListWidget();
		}

		private int removeEntityID(String entityID) {
			SettingsManager.getInstance().getTracerConfig().getTrackedEntities().remove(entityID);

			int listIndex = 0;
			for (Map.Entry<String, EntityTrackingSettings> entry : this.dataList) {
				if (entry.getKey().equals(entityID)) {
					this.dataList.remove(listIndex);
					break;
				}
				listIndex++;
			}
			return listIndex;
		}

		private void recomputeListWidget() {
			if (this.parent != null) {
				this.parent.layout();
			}
		}
	}

	private enum Columns {
		EntityID("gui.cannontracer.entity_tab.label_id", 17, 80),
		Time("gui.cannontracer.entity_tab.label_time", 101, 45),
		Thickness("gui.cannontracer.entity_tab.label_thickness", 150, 80),
		HitBoxRadius("gui.cannontracer.entity_tab.label_hitBoxRadius", 234, 80),
		Red("gui.cannontracer.entity_tab.label_red", 318, 45),
		Green("gui.cannontracer.entity_tab.label_green", 367, 45),
		Blue("gui.cannontracer.entity_tab.label_blue", 416, 45),
		Alpha("gui.cannontracer.entity_tab.label_alpha", 465, 45),
		Render("gui.cannontracer.entity_tab.label_render", 514, 45),
		ExposureBox("gui.cannontracer.entity_tab.label_exposureBox", 563, 45);

		public final String labelTranslatableID;
		private final int startX;
		private final int width;

		Columns(String labelTranslatableID, int startX, int width) {
			this.labelTranslatableID = labelTranslatableID;
			this.startX = startX;
			this.width = width;
		}

		public void place(WPlainPanel container, WWidget widget) {
			container.add(widget, startX, 0, width, 20);
		}

		public void place(WPlainPanel container, WWidget widget, int y, int height) {
			container.add(widget, startX, y, width, height);
		}

		public void placeOffset(WPlainPanel container, WWidget widget, int xOffset, int yOffset) {
			container.add(widget, startX + xOffset, yOffset, width, 20);
		}

		public void placeOffset(WPlainPanel container, WWidget widget, int xOffset, int y, int height) {
			container.add(widget, startX + xOffset, y, width, height);
		}
	}
}
