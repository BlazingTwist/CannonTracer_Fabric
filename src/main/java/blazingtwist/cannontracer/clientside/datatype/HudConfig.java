package blazingtwist.cannontracer.clientside.datatype;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HudConfig {

	@JsonProperty("enabled")
	private boolean enabled = true;

	@JsonProperty("xOffset")
	private float xOffset = 0;

	@JsonProperty("yOffset")
	private float yOffset = 0;

	@JsonProperty("scale")
	private float scale = 1;

	@JsonProperty("alignment")
	private Alignment alignment = Alignment.LEFT;

	public HudConfig() {
	}

	public boolean isEnabled() {
		return enabled;
	}

	public HudConfig setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public float getXOffset() {
		return xOffset;
	}

	public HudConfig setXOffset(float xOffset) {
		this.xOffset = xOffset;
		return this;
	}

	public float getYOffset() {
		return yOffset;
	}

	public HudConfig setYOffset(float yOffset) {
		this.yOffset = yOffset;
		return this;
	}

	public float getScale() {
		return scale;
	}

	public HudConfig setScale(float scale) {
		this.scale = scale;
		return this;
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public HudConfig setAlignment(Alignment alignment) {
		this.alignment = alignment;
		return this;
	}

	public enum Alignment {
		LEFT,
		CENTER,
		RIGHT
	}
}
