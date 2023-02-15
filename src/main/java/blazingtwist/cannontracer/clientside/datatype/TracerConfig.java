package blazingtwist.cannontracer.clientside.datatype;

import blazingtwist.cannontracer.clientside.DefaultConfigStore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class TracerConfig {

	@JsonProperty("trackedEntities")
	private final ConcurrentHashMap<String, EntityTrackingSettings> trackedEntities = new ConcurrentHashMap<>();

	@JsonProperty("maxRange")
	private int maxRange = 500;

	@JsonProperty("hudConfig")
	private final HudConfig hudConfig = new HudConfig();

	@JsonProperty("xRayTraces")
	private boolean xRayTraces = false;

	@JsonProperty("drawPositionText")
	private boolean drawPositionText = false;

	@JsonProperty("drawVelocityText")
	private boolean drawVelocityText = false;

	@JsonProperty("renderDigitPrecision")
	private int renderDigitPrecision = 3;

	@JsonProperty("drawTextScale")
	private float drawTextScale = 1f;

	@JsonProperty("logEntities")
	private boolean logEntities = false;

	@JsonProperty("keyBinds")
	private final TracerKeyBinds keyBinds = new TracerKeyBinds();

	@JsonProperty("hotKeys")
	private final HotKeyConfig hotKeys = DefaultConfigStore.buildDefaultHotKeyConfig();

	public TracerConfig() {
	}

	public ConcurrentHashMap<String, EntityTrackingSettings> getTrackedEntities() {
		return trackedEntities;
	}

	public TracerConfig setTrackedEntities(HashMap<String, EntityTrackingSettings> trackedEntities) {
		this.trackedEntities.clear();
		this.trackedEntities.putAll(trackedEntities);
		return this;
	}

	public int getMaxRange() {
		return maxRange;
	}

	public TracerConfig setMaxRange(int maxRange) {
		this.maxRange = maxRange;
		return this;
	}

	public HudConfig getHudConfig() {
		return hudConfig;
	}

	public boolean isXRayTraces() {
		return xRayTraces;
	}

	public void setXRayTraces(boolean xRayTraces) {
		this.xRayTraces = xRayTraces;
	}

	public boolean isDrawPositionText() {
		return drawPositionText;
	}

	public void setDrawPositionText(boolean drawPositionText) {
		this.drawPositionText = drawPositionText;
	}

	public boolean isDrawVelocityText() {
		return drawVelocityText;
	}

	public void setDrawVelocityText(boolean drawVelocityText) {
		this.drawVelocityText = drawVelocityText;
	}

	public int getRenderDigitPrecision() {
		return renderDigitPrecision;
	}

	public DecimalFormat buildRenderDecimalFormat() {
		if (renderDigitPrecision <= 0) {
			return new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ROOT));
		}
		return new DecimalFormat("0.0" + ("#".repeat(renderDigitPrecision - 1)), DecimalFormatSymbols.getInstance(Locale.ROOT));
	}

	public TracerConfig setRenderDigitPrecision(int renderDigitPrecision) {
		this.renderDigitPrecision = renderDigitPrecision;
		return this;
	}

	public float getDrawTextScale() {
		return drawTextScale;
	}

	public void setDrawTextScale(float drawTextScale) {
		this.drawTextScale = drawTextScale;
	}

	public boolean isLogEntities() {
		return logEntities;
	}

	public void setLogEntities(boolean logEntities) {
		this.logEntities = logEntities;
	}

	public TracerKeyBinds getKeyBinds() {
		return keyBinds;
	}

	public HotKeyConfig getHotKeys() {
		return hotKeys;
	}
}
