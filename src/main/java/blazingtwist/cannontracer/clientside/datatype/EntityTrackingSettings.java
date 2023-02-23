package blazingtwist.cannontracer.clientside.datatype;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EntityTrackingSettings {
	@JsonProperty("render")
	private boolean render = true;

	@JsonProperty("exposureBox")
	private boolean exposureBox = false;

	@JsonProperty("time")
	private float time = 10;

	@JsonProperty("thickness")
	private float thickness = 3;

	@JsonProperty("color")
	private final Color color = new Color(0, 0, 0, 255);

	@JsonProperty("hitBoxRadius")
	private double hitBoxRadius = 0.49;

	public EntityTrackingSettings() {
	}

	public EntityTrackingSettings(boolean render, float time, float thickness, Color color, double hitBoxRadius) {
		this.render = render;
		this.time = time;
		this.thickness = thickness;
		this.color.set(color);
		this.hitBoxRadius = hitBoxRadius;
	}

	public boolean isRender() {
		return render;
	}

	public void setRender(boolean render) {
		this.render = render;
	}

	public boolean isExposureBox() {
		return exposureBox;
	}

	public void setExposureBox(boolean exposureBox) {
		this.exposureBox = exposureBox;
	}

	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		this.time = time;
	}

	public float getThickness() {
		return thickness;
	}

	public void setThickness(float thickness) {
		this.thickness = thickness;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color.set(color);
	}

	public double getHitBoxRadius() {
		return hitBoxRadius;
	}

	public void setHitBoxRadius(double hitBoxRadius) {
		this.hitBoxRadius = hitBoxRadius;
	}
}
