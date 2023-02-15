package blazingtwist.cannontracer.shared.utils;

import java.awt.*;

public class ColorUtils {

	public static int multiply(int a, int b) {
		Color colorA = new Color(a, true);
		Color colorB = new Color(b, true);
		Color resultColor = new Color(
				multiplyColorComponent(colorA.getRed(), colorB.getRed()),
				multiplyColorComponent(colorA.getGreen(), colorB.getGreen()),
				multiplyColorComponent(colorA.getBlue(), colorB.getBlue()),
				multiplyColorComponent(colorA.getAlpha(), colorB.getAlpha())
		);
		return resultColor.getRGB();
	}

	private static int multiplyColorComponent(int a, int b) {
		float res = (a / 255f) * (b / 255f);
		return Math.round(res * 255f);
	}

}
