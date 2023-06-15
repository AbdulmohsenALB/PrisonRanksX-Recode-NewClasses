package me.prisonranksx.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.prisonranksx.components.ActionBarComponent;
import me.prisonranksx.components.CommandsComponent;
import me.prisonranksx.components.FireworkComponent;
import me.prisonranksx.components.PermissionsComponent;
import me.prisonranksx.components.RandomCommandsComponent;
import me.prisonranksx.components.RequirementsComponent;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.StringManager;

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
	 * The first path section in the ranks config file is the default path
	 */
	private static final String DEFAULT_PATH = ConfigManager.getRanksConfig()
			.getConfigurationSection("Ranks")
			.getKeys(false)
			.iterator()
			.next();

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
				if (rank.getNextRankName() == null) LAST_RANKS.put(pathName, rankName);
			});
			PATHS.put(pathName.toLowerCase(), ranksMap);
		});
	}

	/**
	 * 
	 * @param rankName rank name to be looked up in the specified path
	 * @param pathName name of the path to search the rank in
	 * @return whether rank is found in the given path, path with such name actually
	 *         exists, or not.
	 */
	public static boolean isInPath(String rankName, String pathName) {
		Map<String, Rank> ranks = PATHS.get(pathName);
		return ranks == null ? false : ranks.containsKey(rankName);
	}

	/**
	 * 
	 * @param pathName name of the path to check its existence
	 * @return whether the path is found within the map that retrieved the path
	 *         names from the config file.
	 */
	public static boolean pathExists(String pathName) {
		return PATHS.containsKey(pathName.toLowerCase());
	}

	/**
	 * 
	 * @param pathName name of the path to get ranks that come underneath it
	 * @return A map consisting of rank names and rank objects
	 */
	@Nullable
	public static Map<String, Rank> getPathRanksMap(String pathName) {
		return PATHS.get(pathName);
	}

	@Nullable
	public static Rank getRank(String name, @Nullable String pathName) {
		Map<String, Rank> ranks = PATHS.get(pathName);
		return ranks == null ? null : ranks.get(name);
	}

	// Linked Hash Set
	public static Set<String> getPathRankNames(String pathName) {
		return PATHS.get(pathName).keySet();
	}

	// Linked Hash Set
	public static Collection<Rank> getPathRanks(String pathName) {
		return PATHS.get(pathName).values();
	}

	@Nullable
	public static String getLastRank(String pathName) {
		return LAST_RANKS.get(pathName);
	}

	@Nullable
	public static String getFirstRank(String pathName) {
		return FIRST_RANKS.get(pathName);
	}

	public static String getFirstRank() {
		return getFirstRank(getDefaultPath());
	}

	public static String getDefaultPath() {
		return DEFAULT_PATH;
	}

	@Nonnull
	public static String matchRankName(String rankName, String pathName) {
		if (isInPath(rankName, pathName)) return rankName;
		for (String name : getPathRankNames(pathName)) if (rankName.equalsIgnoreCase(name)) return name;
		return rankName;
	}

	@Nullable
	public static String findRankName(String rankName, String pathName) {
		if (isInPath(rankName, pathName)) return rankName;
		for (String name : getPathRankNames(pathName)) if (rankName.equalsIgnoreCase(name)) return name;
		return null;
	}

}
