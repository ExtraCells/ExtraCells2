package extracells.util;

import java.awt.*;

public class ColorUtil {

	public static Color getInvertedColor(Color color) {
		return new Color(0xFFFFFF - color.getRGB());
	}

	public static Color getInvertedColor(int colorCode) {
		return getInvertedColor(new Color(colorCode));
	}

	public static int getInvertedInt(Color color) {
		return getInvertedColor(color).getRGB();
	}

	public static int getInvertedInt(int colorCode) {
		return getInvertedColor(colorCode).getRGB();
	}
}
