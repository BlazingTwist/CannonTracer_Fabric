package blazingtwist.cannontracer.shared.utils;

public class StringUtils {

	public static boolean isInteger(String text) {
		if (text == null || text.isEmpty()) {
			return true;
		}
		try {
			Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isFloat(String text) {
		if (text == null || text.isEmpty()) {
			return true;
		}
		try {
			Float.parseFloat(text);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isDouble(String text) {
		if (text == null || text.isEmpty()) {
			return true;
		}
		try {
			Double.parseDouble(text);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static int parseInt(String text, int fallback) {
		if (text == null || text.isEmpty()) {
			return fallback;
		}
		return Integer.parseInt(text);
	}

	public static float parseFloat(String text, float fallback) {
		if (text == null || text.isEmpty()) {
			return fallback;
		}
		return Float.parseFloat(text);
	}

	public static double parseDouble(String text, double fallback) {
		if (text == null || text.isEmpty()) {
			return fallback;
		}
		return Double.parseDouble(text);
	}

}
