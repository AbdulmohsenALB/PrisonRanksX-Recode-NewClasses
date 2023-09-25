package me.prisonranksx.bukkitutils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.entity.Player;

import me.prisonranksx.common.StaticCache;

public class Colorizer extends StaticCache {

	private static final String EMPTY = "";
	private static final IColorizer COLORIZER;
	
	static {
        boolean spigotExists = false;
        try {
            Player.class.getDeclaredMethod("spigot");
            spigotExists = true;
        } catch (NoClassDefFoundError | NoSuchMethodException ex) {}
        COLORIZER = spigotExists ? new SpigotColorizer() : new BukkitColorizer();
	}
	
	/**
	 * 
	 * @param textToColorize string to translate color codes in
	 * @return text with colors
	 * <p> "" if textToColorize is null
	 */
	@NotNull
	public static String colorize(@Nullable String textToColorize) {
		return COLORIZER.colorize(textToColorize);
	}
	
	@Nullable
	public static List<String> colorize(@Nullable List<String> textToColorize) {
		if(textToColorize == null) return null;
		return textToColorize.stream().map(Colorizer::colorize).collect(Collectors.toList());
	}
	
	@Nullable
	public static List<String> colorize(@Nullable String... textToColorize) {
		if(textToColorize == null) return null;
		return colorize(Arrays.asList(textToColorize));
	}
	
	private interface IColorizer {
		
		String colorize(String textToColorize);
		
	}
	
	private static class SpigotColorizer implements IColorizer {

		private static final char COLOR_CHAR = '\u00A7';
		private static final String startTag = "&#";
		private static final String endTag = "";
		private static final Pattern HEX_PATTERN = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
		
		@Override
		public String colorize(String textToColorize) {
			if(textToColorize == null) return EMPTY;
			return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', colorizeHex(textToColorize));
		}
		
		private static String colorizeHex(String message)
		{
			if(message == null || message.isEmpty() || !message.contains("&#"))
				return message;
			Matcher matcher = HEX_PATTERN.matcher(message);
			StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
			while (matcher.find())
			{
				String group = matcher.group(1);
				matcher.appendReplacement(buffer, COLOR_CHAR + "x"
						+ COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
						+ COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
						+ COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
						);
			}
			return matcher.appendTail(buffer).toString();
		}
		
	}
	
	private static class BukkitColorizer implements IColorizer {

		@Override
		public String colorize(String textToColorize) {
			if(textToColorize == null) return EMPTY;
			return org.bukkit.ChatColor.translateAlternateColorCodes('&', textToColorize);
		}
		
	}
	
}
