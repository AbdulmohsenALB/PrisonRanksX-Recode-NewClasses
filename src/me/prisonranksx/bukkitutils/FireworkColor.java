package me.prisonranksx.bukkitutils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Color;

public class FireworkColor {

	private static final Map<String, Color> colors = new HashMap<>();

	static {
		for (Field field : Color.class.getFields()) {
			if (field.getType().equals(Color.class)) {
				String colorName = field.getName();
				try {
					registerColor(colorName, (Color) field.get(Color.class));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void registerColor(String name, Color color) {
		colors.put(name, color);
	}

	public static void registerColor(String name, int r, int g, int b) {
		colors.put(name, Color.fromRGB(r, g, b));
	}

	@Nullable
	public static Color getColorExact(@Nullable String name) {
		return colors.get(name);
	}

	@Nullable
	public static Color getColor(@Nonnull String name) {
		return colors.get(name.toUpperCase());
	}

	@Nonnull
	public static Color parseColor(@Nonnull String colorString) {
		Color color = colors.get(colorString.toUpperCase());
		if (color != null) return color;
		if (colorString.indexOf(",") == -1) return colors.get("WHITE");
		String rgbColorString = colorString.replace(" ,", ",");
		String[] rgbSplit = rgbColorString.split(",");
		if (rgbSplit.length != 3) return colors.get("WHITE");
		return Color.fromRGB(Integer.parseInt(rgbSplit[0]), Integer.parseInt(rgbSplit[1]),
				Integer.parseInt(rgbSplit[2]));
	}

	public static Set<String> getColorNames() {
		return colors.keySet();
	}

	public static Collection<Color> getColors() {
		return colors.values();
	}

}
