package blazingtwist.cannontracer.clientside.datatype;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EntityTrackingSettings {
	@JsonProperty("render")
	private boolean render = true;

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

	public EntityTrackingSettings setRender(boolean render) {
		this.render = render;
		return this;
	}

	public float getTime() {
		return time;
	}

	public EntityTrackingSettings setTime(float time) {
		this.time = time;
		return this;
	}

	public float getThickness() {
		return thickness;
	}

	public EntityTrackingSettings setThickness(float thickness) {
		this.thickness = thickness;
		return this;
	}

	public Color getColor() {
		return color;
	}

	public EntityTrackingSettings setColor(Color color) {
		this.color.set(color);
		return this;
	}

	public double getHitBoxRadius() {
		return hitBoxRadius;
	}

	public EntityTrackingSettings setHitBoxRadius(double hitBoxRadius) {
		this.hitBoxRadius = hitBoxRadius;
		return this;
	}
}
