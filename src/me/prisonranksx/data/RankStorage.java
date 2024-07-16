package me.prisonranksx.data;

import java.util.*;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.components.*;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.utils.ToString;

public class RankStorage extends StorageFields {

	/**
	 * Key represents the path name, and the value represents a Map with a key
	 * representing the rank name and the value representing the Rank object that
	 * holds information of the rank, such as display name, cost, etc..
	 */
	public static final Map<String, Map<String, Rank>> PATHS = new HashMap<>();
	/**
	 * Key represents the path name, and the value represents the last rank name.
	 */
	private static final Map<String, String> LAST_RANKS = new HashMap<>();
	/**
	 * Key represents the path name, and the value represents the first rank name.
	 */
	private static final Map<String, String> FIRST_RANKS = new HashMap<>();
	/**
	 * All available ranks
	 */
	private static final Set<String> AVAILABLE_RANKS = new LinkedHashSet<>();
	/**
	 * The first path section in the ranks config file is the default path
	 */
	private static final String DEFAULT_PATH = ConfigManager.getRanksConfig()
			.getConfigurationSection("Ranks")
			.getKeys(false)
			.iterator()
			.next();

	/**
	 * Clears loaded ranks if there's any then loads ranks.yml ranks.
	 */
	@SuppressWarnings("unchecked")
	public static void loadRanks() {
		PATHS.clear();
		LAST_RANKS.clear();
		FIRST_RANKS.clear();
		FileConfiguration ranksConfig = ConfigManager.getRanksConfig();
		ConfigurationSection pathSection = ranksConfig.getConfigurationSection("Ranks");
		pathSection.getKeys(false).forEach(pathName -> {
			ConfigurationSection rankSection = pathSection.getConfigurationSection(pathName);
			Map<String, Rank> ranksMap = new LinkedHashMap<>();
			AVAILABLE_RANKS.addAll(rankSection.getKeys(false));
			rankSection.getKeys(false).forEach(rankName -> {
				ConfigurationSection current = rankSection.getConfigurationSection(rankName);
				Rank rank = new Rank(rankName,
						StringManager
								.parseColorsAndSymbols(ConfigManager.getOrElse(current, String.class, DISPLAY_FIELDS)),
						ConfigManager.getOrElse(current, NEXT_FIELDS),
						ConfigManager.getDoubleOrElse(current, COST_FIELDS),
						StringManager.parseColorsAndSymbols(current.getStringList("broadcast")),
						StringManager
								.parseColorsAndSymbols(ConfigManager.getOrElse(current, List.class, MESSAGE_FIELDS)),
						CommandsComponent.parseCommands(ConfigManager.getOrElse(current, COMMANDS_FIELDS)),
						RequirementsComponent.parseRequirements(ConfigManager.getOrElse(current, REQUIREMENTS_FIELDS)),
						ActionBarComponent.parseActionBar(ConfigManager.getOrElse(current, ACTION_BAR_FIELDS)),
						PermissionsComponent.parsePermissions(ConfigManager.getOrElse(current, ADD_PERMISSIONS_FIELDS),
								ConfigManager.getOrElse(current, DEL_PERMISSIONS_FIELDS)),
						FireworkComponent.parseFirework(ConfigManager.getOrElse(current, FIREWORK_FIELDS)),
						RandomCommandsComponent
								.parseRandomCommands(ConfigManager.getOrElse(current, RANDOM_COMMANDS_FIELDS)),
						StringManager.parseColorsAndSymbols(
								ConfigManager.getOrElse(current, List.class, REQUIREMENTS_FAIL_MESSAGE_FIELDS)),
						ConfigManager.getBooleanOrElse(current, "allow-prestige", "allowprestige", "prestige"));
				rank.setIndex(ranksMap.size());
				ranksMap.put(rankName, rank);
				if (!FIRST_RANKS.containsKey(pathName)) FIRST_RANKS.put(pathName, rankName);
				if (rank.getNextName() == null) LAST_RANKS.put(pathName, rankName);
			});
			PATHS.put(pathName.toLowerCase(), ranksMap);
		});
		check();
	}

	private static void check() {
		PATHS.entrySet().forEach(entry -> {
			Map<String, Rank> ranksMap = entry.getValue();
			for (Map.Entry<String, Rank> rankEntry : ranksMap.entrySet()) {
				Rank rank = rankEntry.getValue();
				if (rank.getNextName() != null && !AVAILABLE_RANKS.contains(rank.getNextName())) {
					PrisonRanksX.logSevere("Rank '" + rank.getName() + "' next rank named '" + rank.getNextName()
							+ "' is non-existent. Please fix that in your config files!");
				}
			}
		});
	}

	/**
	 * @param rankName rank name to be looked up in the specified path
	 * @param pathName name of the path to search the rank in
	 * @return whether rank is found in the given path, path with said name actually
	 *         exists, or not (CaSe-SeNsItIvE).
	 */
	public static boolean isInPath(String rankName, String pathName) {
		Map<String, Rank> ranks = PATHS.get(pathName);
		return ranks != null && ranks.containsKey(rankName);
	}

	/**
	 * @param pathName name of the path to check its existence
	 * @return whether the path is found within the map that retrieved the path
	 *         names from the config file (CASE-INSENSITIVE).
	 */
	public static boolean pathExists(String pathName) {
		return PATHS.containsKey(pathName.toLowerCase());
	}

	/**
	 * @param pathName name of the path to get ranks that come underneath it
	 * @return A map consisting of rank names and rank objects
	 */
	@Nullable
	public static Map<String, Rank> getPathRanksMap(String pathName) {
		return PATHS.get(pathName);
	}

	/**
	 * name and pathName checks are CASE-SENSITIVE
	 * 
	 * @param name     name of rank
	 * @param pathName path name of rank
	 * @return Rank from name and pathName or null if not found.
	 */
	@Nullable
	public static Rank getRank(@Nullable String name, @Nullable String pathName) {
		if (pathName == null || name == null) return null;
		Map<String, Rank> ranks = PATHS.get(pathName);
		return ranks == null ? null : ranks.get(name);
	}

	/**
	 * Gets rank names within a path
	 * 
	 * @param pathName to get ranks of
	 * @return names of ranks in a path
	 */
	public static Set<String> getPathRankNames(String pathName) {
		return PATHS.get(pathName).keySet();
	}

	public static Collection<Rank> getPathRanks(String pathName) {
		return PATHS.get(pathName).values();
	}

	@Nullable
	public static String getLastRankName(String pathName) {
		return LAST_RANKS.get(pathName);
	}

	@Nullable
	public static Rank getLastRank(String pathName) {
		return RankStorage.getRank(LAST_RANKS.get(pathName), pathName);
	}

	@Nullable
	public static String getFirstRankName(String pathName) {
		return FIRST_RANKS.get(pathName);
	}

	/**
	 * Gets first rank within specified path
	 * 
	 * @param pathName to get first rank of
	 * @return first rank in specified path
	 */
	@Nullable
	public static Rank getFirstRank(String pathName) {
		return RankStorage.getRank(FIRST_RANKS.get(pathName), pathName);
	}

	/**
	 * Quickly checks if rank exists
	 * 
	 * @param rankName to check for
	 * @return whether rank exists or not
	 */
	public static boolean rankExists(String rankName) {
		return AVAILABLE_RANKS.contains(rankName);
	}

	/**
	 *
	 * @return first rank in default path
	 */
	public static String getFirstRankName() {
		return getFirstRankName(getDefaultPath());
	}

	/**
	 *
	 * DEFAULT_PATH The first path section in the ranks config file is the default
	 * path
	 * 
	 * @return loaded default path name
	 */
	public static String getDefaultPath() {
		return DEFAULT_PATH;
	}

	/**
	 * First check if rankName and pathName exist with matching case. If not, check
	 * ignoring case. Otherwise, return rankName even if it doesn't exist.
	 * 
	 * @param rankName name of rank to check
	 * @param pathName name of path to check
	 * @return rankName with correct case
	 */
	@NotNull
	public static String matchRankName(String rankName, String pathName) {
		if (isInPath(rankName, pathName)) return rankName;
		for (String name : getPathRankNames(pathName)) if (rankName.equalsIgnoreCase(name)) return name;
		return rankName;
	}

	/**
	 * First check if rankName and pathName exist with matching case. If not, check
	 * ignoring case. Otherwise, return null.
	 *
	 * @param rankName name of rank to check
	 * @param pathName name of path to check
	 * @return rankName with correct case or null
	 */
	@Nullable
	public static String findRankName(String rankName, String pathName) {
		if (isInPath(rankName, pathName)) return rankName;
		for (String name : getPathRankNames(pathName)) if (rankName.equalsIgnoreCase(name)) return name;
		return null;
	}

	/**
	 *
	 * @return all paths that are loaded from config file.
	 */
	public static Set<String> getPaths() {
		return PATHS.keySet();
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}

}
