package blazingtwist.cannontracer.clientside.gui.panels;

import blazingtwist.cannontracer.clientside.SettingsManager;
import blazingtwist.cannontracer.clientside.datatype.HudConfig;
import blazingtwist.cannontracer.clientside.datatype.SessionSettings;
import blazingtwist.cannontracer.clientside.datatype.TracerConfig;
import blazingtwist.cannontracer.clientside.gui.widgets.BetterTextField;
import blazingtwist.cannontracer.clientside.gui.widgets.CTTextures;
import blazingtwist.cannontracer.clientside.gui.widgets.ColoredBoxWidget;
import blazingtwist.cannontracer.clientside.gui.widgets.IntInputFieldWithIncrementer;
import blazingtwist.cannontracer.clientside.gui.widgets.LabelWithShadow;
import blazingtwist.cannontracer.clientside.gui.widgets.TextureButton;
import blazingtwist.cannontracer.clientside.gui.widgets.ToggleButton;
import blazingtwist.cannontracer.shared.utils.StringUtils;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WSlider;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.minecraft.text.Text;

public class TracerSettingsPanel extends WPlainPanel {

	public static TracerSettingsPanel createPanel() {
		SettingsManager settingsManager = SettingsManager.getInstance();
		return new TracerSettingsPanel(settingsManager.getTracerConfig(), settingsManager.getSessionSettings());
	}

	private static final int sliderFactor = 1024;

	private final TracerConfig tracerConfig;
	private final SessionSettings sessionSettings;
	private final HudConfig hudConfig;

	private TracerSettingsPanel(TracerConfig tracerConfig, SessionSettings sessionSettings) {
		this.tracerConfig = tracerConfig;
		this.sessionSettings = sessionSettings;
		this.hudConfig = tracerConfig.getHudConfig();
		this.setInsets(new Insets(10));
		this.setSize(630, 280);

		addSectionLabel("gui.cannontracer.settings_tab.section_label_tracking", 0, 0);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_range", 20, 30);
		BetterTextField rangeField = new BetterTextField();
		rangeField.setText(Integer.toString(tracerConfig.getMaxRange()));
		rangeField.setTextPredicate(StringUtils::isInteger);
		rangeField.setOnSubmitListener(this::onSubmitRange);
		this.add(rangeField, 95, 30, 48, 16);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_logging", 20, 50);
		ToggleButton loggingButton = new ToggleButton(CTTextures.BUTTON_PACKAGE_VANILLA_50, tracerConfig.isLogEntities());
		loggingButton.setOnToggleListener(this::onToggleLogging);
		this.add(loggingButton, 95, 50, 48, 16);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_tick", 20, 70);
		BetterTextField tickField = new BetterTextField();
		tickField.setText(Long.toString(sessionSettings.getRenderTick()));
		tickField.setTextPredicate(StringUtils::isInteger);
		tickField.setOnSubmitListener(this::onSubmitTick);
		this.add(tickField, 95, 70, 48, 16);

		addSectionLabel("gui.cannontracer.settings_tab.section_label_traces", 200, 0);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_x_ray", 220, 30);
		ToggleButton xRayButton = new ToggleButton(CTTextures.BUTTON_PACKAGE_VANILLA_50, tracerConfig.isXRayTraces());
		xRayButton.setOnToggleListener(this::onToggleXRay);
		this.add(xRayButton, 295, 30, 64, 16);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_position_text", 220, 50);
		ToggleButton positionTextButton = new ToggleButton(CTTextures.BUTTON_PACKAGE_VANILLA_50, tracerConfig.isDrawPositionText());
		positionTextButton.setOnToggleListener(this::onTogglePositionText);
		this.add(positionTextButton, 295, 50, 64, 16);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_velocity_text", 220, 70);
		ToggleButton velocityTextButton = new ToggleButton(CTTextures.BUTTON_PACKAGE_VANILLA_50, tracerConfig.isDrawVelocityText());
		velocityTextButton.setOnToggleListener(this::onToggleVelocityText);
		this.add(velocityTextButton, 295, 70, 64, 16);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_digit_precision", 220, 90);
		IntInputFieldWithIncrementer intField = new IntInputFieldWithIncrementer(this, 295, 90, 32, 0xFF_FFFFFF,
				0, 15, tracerConfig::getRenderDigitPrecision, tracerConfig::setRenderDigitPrecision, null);
		intField.field.setText(Integer.toString(tracerConfig.getRenderDigitPrecision()));

		addOptionLabel("gui.cannontracer.settings_tab.option_label_draw_text_scale", 220, 110);
		BetterTextField drawTextScaleField = new BetterTextField();
		drawTextScaleField.setText(Float.toString(tracerConfig.getDrawTextScale()));
		drawTextScaleField.setTextPredicate(StringUtils::isFloat);
		drawTextScaleField.setOnSubmitListener(this::onSubmitDrawScale);
		this.add(drawTextScaleField, 295, 110, 64, 16);

		addSectionLabel("gui.cannontracer.settings_tab.section_label_hud", 416, 0);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_enable", 436, 30);
		ToggleButton hudEnableButton = new ToggleButton(CTTextures.BUTTON_PACKAGE_VANILLA_50, hudConfig.isEnabled());
		hudEnableButton.setOnToggleListener(this::onToggleHudEnabled);
		this.add(hudEnableButton, 511, 30, 48, 16);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_position_x", 436, 50);
		WSlider xPositionSlider = new WSlider(0, sliderFactor, Axis.HORIZONTAL);
		xPositionSlider.setValue((int) (hudConfig.getXOffset() * sliderFactor));
		xPositionSlider.setValueChangeListener(this::onXPositionChanged);
		this.add(xPositionSlider, 511, 50, 48, 16);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_position_y", 436, 70);
		WSlider yPositionSlider = new WSlider(0, sliderFactor, Axis.HORIZONTAL);
		yPositionSlider.setValue((int) (hudConfig.getYOffset() * sliderFactor));
		yPositionSlider.setValueChangeListener(this::onYPositionChanged);
		this.add(yPositionSlider, 511, 70, 48, 16);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_alignment", 436, 90);
		TextureButton alignLeftButton = new TextureButton(CTTextures.BUTTON_PACKAGE_VANILLA_50);
		alignLeftButton.setLabelTexture(CTTextures.ICON_ALIGN_LEFT);
		alignLeftButton.setOnClick(() -> this.onAlignChanged(HudConfig.Alignment.LEFT));
		this.add(alignLeftButton, 511, 90, 16, 16);

		TextureButton alignCenterButton = new TextureButton(CTTextures.BUTTON_PACKAGE_VANILLA_50);
		alignCenterButton.setLabelTexture(CTTextures.ICON_ALIGN_CENTER);
		alignCenterButton.setOnClick(() -> this.onAlignChanged(HudConfig.Alignment.CENTER));
		this.add(alignCenterButton, 511 + 16, 90, 16, 16);

		TextureButton alignRightButton = new TextureButton(CTTextures.BUTTON_PACKAGE_VANILLA_50);
		alignRightButton.setLabelTexture(CTTextures.ICON_ALIGN_RIGHT);
		alignRightButton.setOnClick(() -> this.onAlignChanged(HudConfig.Alignment.RIGHT));
		this.add(alignRightButton, 511 + 16 + 16, 90, 16, 16);

		addOptionLabel("gui.cannontracer.settings_tab.option_label_scale", 436, 110);
		BetterTextField scaleField = new BetterTextField();
		scaleField.setText(Float.toString(hudConfig.getScale()));
		scaleField.setTextPredicate(StringUtils::isFloat);
		scaleField.setOnSubmitListener(this::onSubmitHudScale);
		this.add(scaleField, 511, 110, 48, 16);
	}

	private void addSectionLabel(String translationKey, int x, int y) {
		ColoredBoxWidget background = new ColoredBoxWidget();
		background.setColor(0xff_43723f);
		background.setBorderWidth(1);
		background.setBorderColor(0xff_669b5f);
		background.setSize(60, 20);
		this.add(background, x, y, 60, 20);

		LabelWithShadow label = new LabelWithShadow(Text.translatable(translationKey));
		label.setTextShadow(false);
		label.setHorizontalAlignment(HorizontalAlignment.CENTER);
		label.setVerticalAlignment(VerticalAlignment.CENTER);
		label.setColor(0xffffffff);
		this.add(label, x, y, 60, 20);
	}

	private void addOptionLabel(String translationKey, int x, int y) {
		LabelWithShadow label = new LabelWithShadow(Text.translatable(translationKey));
		label.setTextShadow(true);
		label.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		label.setVerticalAlignment(VerticalAlignment.CENTER);
		label.setColor(0xFFFFFFFF);
		this.add(label, x, y, 70, 16);
	}

	private void onSubmitRange(String newValue) {
		tracerConfig.setMaxRange(StringUtils.parseInt(newValue, tracerConfig.getMaxRange()));
	}

	private void onToggleLogging(boolean newValue) {
		tracerConfig.setLogEntities(newValue);
	}

	private void onSubmitTick(String newValue) {
		sessionSettings.setRenderTick(StringUtils.parseInt(newValue, (int) sessionSettings.getRenderTick()));
	}

	private void onToggleXRay(boolean newValue) {
		tracerConfig.setXRayTraces(newValue);
	}

	private void onTogglePositionText(boolean newValue) {
		tracerConfig.setDrawPositionText(newValue);
	}

	private void onToggleVelocityText(boolean newValue) {
		tracerConfig.setDrawVelocityText(newValue);
	}

	private void onSubmitDrawScale(String newValue) {
		tracerConfig.setDrawTextScale(StringUtils.parseFloat(newValue, tracerConfig.getDrawTextScale()));
	}

	private void onToggleHudEnabled(boolean newValue) {
		hudConfig.setEnabled(newValue);
	}

	private void onXPositionChanged(int newValue) {
		float xOffset = newValue / ((float) sliderFactor);
		hudConfig.setXOffset(xOffset);
	}

	private void onYPositionChanged(int newValue) {
		float yOffset = newValue / ((float) sliderFactor);
		hudConfig.setYOffset(yOffset);
	}

	private void onAlignChanged(HudConfig.Alignment alignment) {
		hudConfig.setAlignment(alignment);
	}

	private void onSubmitHudScale(String newValue) {
		hudConfig.setScale(StringUtils.parseFloat(newValue, hudConfig.getScale()));
	}
}
