package me.prisonranksx.bukkitutils;

import java.lang.reflect.Field;
import java.util.*;

import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	public static Color getColor(@NotNull String name) {
		return colors.get(name.toUpperCase());
	}

	@NotNull
	public static Color parseColor(@NotNull String colorString) {
		Color color = colors.get(colorString.toUpperCase());
		if (color != null) return color;
		if (colorString.indexOf(",") == -1) return colors.get("WHITE");
		String rgbColorString = colorString.replace(" ,", ",");
		String[] rgbSplit = rgbColorString.split(",");
		if (rgbSplit.length != 3) return colors.get("WHITE");
		return Color.fromRGB(Integer.parseInt(rgbSplit[0]), Integer.parseInt(rgbSplit[1]),
				Integer.parseInt(rgbSplit[2]));
	}

	public static String stringify(Color color) {
		for (Map.Entry<String, Color> entry : colors.entrySet()) {
			if (entry.getValue().equals(color)) return entry.getKey();
		}
		return "R: " + color.getBlue() + ", G: " + color.getGreen() + ", B:" + color.getRed();
	}

	public static String stringify(Collection<Color> colors) {
		List<String> colorString = new ArrayList<>();
		for (Color color : colors) colorString.add(stringify(color));
		return String.join(", ", colorString);
	}

	public static Set<String> getColorNames() {
		return colors.keySet();
	}

	public static Collection<Color> getColors() {
		return colors.values();
	}

}
