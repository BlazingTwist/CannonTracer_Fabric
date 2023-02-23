package blazingtwist.cannontracer.clientside.datatype;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.minecraft.util.math.MathHelper;

public class Color {
	@JsonProperty("red")
	private int red;

	@JsonProperty("green")
	private int green;

	@JsonProperty("blue")
	private int blue;

	@JsonProperty("alpha")
	private int alpha;

	public Color() {
	}

	public Color(int red, int green, int blue, int alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}

	public void set(Color other) {
		set(other.red, other.green, other.blue, other.alpha);
	}

	public void set(int red, int green, int blue, int alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}

	public int getRed() {
		return red;
	}

	public Color setRed(int red) {
		this.red = red;
		return this;
	}

	public int getGreen() {
		return green;
	}

	public Color setGreen(int green) {
		this.green = green;
		return this;
	}

	public int getBlue() {
		return blue;
	}

	public Color setBlue(int blue) {
		this.blue = blue;
		return this;
	}

	public int getAlpha() {
		return alpha;
	}

	public Color setAlpha(int alpha) {
		this.alpha = alpha;
		return this;
	}

	/**
	 * Inverts `this` color and returns `this`
	 *
	 * @return this Color Object
	 */
	public Color invert() {
		red = 255 - red;
		green = 255 - green;
		blue = 255 - blue;
		return this;
	}

	/**
	 * Adds 'value' to each component and returns 'this'
	 *
	 * @param value the value to add to each color component
	 * @return this Color Object
	 */
	public Color add(int value) {
		red = Math.max(0, Math.min(255, red + value));
		green = Math.max(0, Math.min(255, green + value));
		blue = Math.max(0, Math.min(255, blue + value));
		return this;
	}

	public Color mul(float value) {
		red = Math.min(255, MathHelper.floor(red * value));
		green = Math.min(255, MathHelper.floor(green * value));
		blue = Math.min(255, MathHelper.floor(blue * value));
		return this;
	}

	/**
	 * Convert this color to grayScale
	 *
	 * @return this Color Object
	 */
	public Color toGray() {
		double rLuma = (red / 255d) * 0.2989;
		double gLuma = (green / 255d) * 0.5870;
		double bLuma = (blue / 255d) * 0.1140;
		int gray = MathHelper.floor((rLuma + gLuma + bLuma) * 255);
		this.red = gray;
		this.green = gray;
		this.blue = gray;
		return this;
	}

	/**
	 * @return a new Color instance with the same Property-Values
	 */
	public Color copy() {
		return new Color(red, green, blue, alpha);
	}
}
