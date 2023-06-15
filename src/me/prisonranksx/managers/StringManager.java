package me.prisonranksx.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.prisonranksx.bukkitutils.Colorizer;
import me.prisonranksx.common.StaticCache;

/**
 * Controls string replacements (color codes, PlaceholderAPI placeholders, and
 * symbols)
 */
public class StringManager extends StaticCache {

	private static final Map<String, Object> SYMBOLS_MAP = new HashMap<String, Object>(17) {
		private static final long serialVersionUID = 1L;
		{
			put(">>", "»");
			put("<<", "«");
			put("coolarrow", "➤");
			put("<3", "❤");
			put("shadowarrow", "➢");
			put("shadowarrow_2", "➣");
			put("shadowarrow_down", "⧨");
			put("shadowsquare", "�?�");
			put("nuke", "☢");
			put("+", "✚");
			put("correct", "✔");
			put("incorrect", "✖");
			put("bowarrow", "➸");
			put("squaredot", "◼");
			put("square", "■");
			put("happyface", "☺");
			put("|", "⎟");
		}
	};
	private static final StrSubstitutor SYMBOLS_STRING_SUBSTITUTOR = new StrSubstitutor(SYMBOLS_MAP, "[", "]");
	private static final Map<String, StrSubstitutor> DEFINED_REPLACEMENTS = new HashMap<>();
	private static final IStringManager STRING_MANAGER;
	private static final boolean PLACEHOLDERAPI;

	static {
		STRING_MANAGER = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? new PlaceholderAPIStringManager()
				: new RegularStringManager();
		PLACEHOLDERAPI = STRING_MANAGER instanceof RegularStringManager ? false : true;
	}

	public static boolean isPlaceholderAPI() {
		return PLACEHOLDERAPI;
	}

	/**
	 * Define replacements, so they can be used constantly
	 * 
	 * @param name         to be used when retrieving the replacements
	 * @param replacements map of the placeholders as keys and the replacements as
	 *                     values
	 */
	public static void defineReplacements(String name, Map<String, String> replacements) {
		DEFINED_REPLACEMENTS.put(name, new StrSubstitutor(replacements, "%", "%"));
	}

	public static void deleteReplacements(String name) {
		DEFINED_REPLACEMENTS.remove(name);
	}

	/**
	 * 
	 * @param string parses a string's color codes
	 * @return string with translated color codes including hex color codes if they
	 *         are supported
	 */
	public static String parseColors(String string) {
		return string == null ? null : STRING_MANAGER.parseColors(string);
	}

	/**
	 * Goes through the given string list and applies {@link #parseColors(String)}
	 * on each line
	 * 
	 * @param stringList list of strings to parse
	 * @return string list with translated color codes
	 */
	public static List<String> parseColors(List<String> stringList) {
		return stringList == null || stringList.isEmpty() ? null
				: stringList.stream().map(stringLine -> parseColors(stringLine)).collect(Collectors.toList());
	}

	/**
	 * 
	 * @param string parses a string's colors with plugin provided symbols
	 * @return string with translated color codes and plugin symbols
	 */
	public static String parseColorsAndSymbols(String string) {
		return string == null ? null : STRING_MANAGER.parseColorsAndSymbols(string);
	}

	/**
	 * Goes through the given string list and applies
	 * {@link #parseColorsAndSymbols(String)} on each line
	 * 
	 * @param stringList list of strings to parse
	 * @return string list with translated color codes and plugin provided symbols
	 */
	public static List<String> parseColorsAndSymbols(List<String> stringList) {
		return stringList == null || stringList.isEmpty() ? null
				: stringList.stream().map(stringLine -> parseColorsAndSymbols(stringLine)).collect(Collectors.toList());
	}

	/**
	 * 
	 * @param string parses a string's PlaceholderAPI placeholders
	 * @param player to parse PlaceholderAPI placeholders for
	 * @return string with parsed PlaceholderAPI placeholders and colors
	 */
	public static String parsePlaceholders(String string, Player player) {
		return string == null || player == null ? null : STRING_MANAGER.parsePlaceholders(string, player);
	}

	/**
	 * 
	 * @param string     string to replace defined replacements in
	 * @param definition name of said replacements
	 * @return string with parsed replacements
	 */
	public static String parseReplacements(String string, String definition) {
		return string == null || definition == null ? null : STRING_MANAGER.parseReplacements(string, definition);
	}

	/**
	 * 
	 * @param string parses a string's PlaceholderAPI placeholders, color codes, and
	 *               symbols.
	 * @param player to parse PlaceholderAPI placeholders for
	 * @return parsed string
	 */
	public static String parseAll(String string, Player player) {
		return string == null || player == null ? null : STRING_MANAGER.parseAll(string, player);
	}

	/**
	 * 
	 * @param string     parses a string's PlaceholderAPI placeholders, color codes,
	 *                   and symbols.
	 * @param player     to parse PlaceholderAPI placeholders for
	 * @param definition name of replacements
	 * @return parsed string
	 */
	public static String parseAll(String string, Player player, String definition) {
		return string == null ? null : STRING_MANAGER.parseAll(string, player, definition);
	}

	/**
	 * Combines command arguments into one string starting from {@code num}
	 * 
	 * @param args arguments to combine from
	 * @param num  number to start combining from
	 * @return string with the combined arguments
	 */
	public static String getArgs(String[] args, int num) {
		StringBuilder sb = new StringBuilder();
		for (int i = num; i < args.length; i++) {
			sb.append(args[i]).append(" ");
		}
		return sb.toString().trim();
	}

	private interface IStringManager {

		/**
		 * 
		 * @param string parses a string's color codes
		 * @return string with translated color codes including hex color codes if they
		 *         are supported
		 */
		String parseColors(String string);

		/**
		 * 
		 * @param string parses a string's colors with plugin provided symbols
		 * @return string with translated color codes and plugin symbols
		 */
		String parseColorsAndSymbols(String string);

		/**
		 * 
		 * @param string parses a string's PlaceholderAPI placeholders
		 * @param player to parse PlaceholderAPI placeholders for
		 * @return string with parsed PlaceholderAPI placeholders
		 */
		String parsePlaceholders(String string, Player player);

		/**
		 * 
		 * @param string parses a string's PlaceholderAPI placeholders, color codes, and
		 *               symbols.
		 * @param player to parse PlaceholderAPI placeholders for
		 * @return parsed string
		 */
		String parseAll(String string, Player player);

		/**
		 * 
		 * @param string     parses a string's PlaceholderAPI placeholders, color codes,
		 *                   and symbols.
		 * @param player     to parse PlaceholderAPI placeholders for
		 * @param definition parses replacements
		 * @return parsed string
		 */
		String parseAll(String string, Player player, String definition);

		/**
		 * 
		 * @param string     string to replace defined replacements in
		 * @param definition name of said replacements
		 * @return string with parsed replacements
		 */
		String parseReplacements(String string, String definition);

	}

	private static class PlaceholderAPIStringManager implements IStringManager {

		@Override
		public String parseColors(String string) {
			return Colorizer.colorize(string);
		}

		@Override
		public String parseColorsAndSymbols(String string) {
			return parseColors(SYMBOLS_STRING_SUBSTITUTOR.replace(string));
		}

		@Override
		public String parsePlaceholders(String string, Player player) {
			return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, string);
		}

		@Override
		public String parseReplacements(String string, String definition) {
			return DEFINED_REPLACEMENTS.get(definition).replace(string);
		}

		@Override
		public String parseAll(String string, Player player) {
			return parseColorsAndSymbols(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, string));
		}

		@Override
		public String parseAll(String string, Player player, String definition) {
			return parseAll(parseReplacements(string, definition), player);
		}

	}

	private static class RegularStringManager implements IStringManager {

		@Override
		public String parseColors(String string) {
			return Colorizer.colorize(string);
		}

		@Override
		public String parseColorsAndSymbols(String string) {
			return parseColors(SYMBOLS_STRING_SUBSTITUTOR.replace(string));
		}

		@Override
		public String parsePlaceholders(String string, Player player) {
			return string;
		}

		@Override
		public String parseReplacements(String string, String definition) {
			return DEFINED_REPLACEMENTS.get(definition).replace(string);
		}

		@Override
		public String parseAll(String string, Player player) {
			return parseColorsAndSymbols(string);
		}

		@Override
		public String parseAll(String string, Player player, String definition) {
			return parseAll(parseReplacements(string, definition), player);
		}

	}

}
