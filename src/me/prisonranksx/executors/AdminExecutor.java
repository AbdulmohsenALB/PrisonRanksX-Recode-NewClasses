package me.prisonranksx.executors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.ConfigCreator;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.RebirthStorage;
import me.prisonranksx.data.StorageFields;
import me.prisonranksx.data.UserController;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;

public class AdminExecutor {

	private PrisonRanksX plugin;

	public AdminExecutor(PrisonRanksX plugin) {
		this.plugin = plugin;
	}

	private String getField(ConfigurationSection section, String... fields) {
		return section == null ? fields[0] : ConfigManager.getFieldOrElse(section, fields);
	}

	private ConfigurationSection getMainSection() {
		return ConfigManager.getRanksConfig().getConfigurationSection("Ranks");
	}

	public UserController userControl() {
		return plugin.getUserController();
	}

	public User getUser(UUID uniqueId) {
		return userControl().getUser(uniqueId);
	}

	public boolean setPlayerRank(UUID uniqueId, String rankName, String pathName) {
		User user = getUser(uniqueId);
		if (user == null) {
			PrisonRanksX.logWarning("Failed to change '" + uniqueId.toString() + "' rank to '" + rankName + "'.");
			PrisonRanksX.logWarning("No user data found for: " + uniqueId);
			return false;
		}
		if (RankStorage.getRank(rankName, pathName) == null) {
			PrisonRanksX.logWarning("Unable to find a rank named '" + rankName + "'.");
			return false;
		}
		user.setRankName(rankName);
		user.setPathName(pathName);
		plugin.getRankupExecutor().updateGroup(UniqueId.getPlayer(uniqueId));
		return true;
	}

	public boolean setPlayerRank(UUID uniqueId, String rankName) {
		String pathName = getUser(uniqueId).getPathName();
		return setPlayerRank(uniqueId, rankName,
				pathName == null || !RankStorage.pathExists(pathName) ? RankStorage.getDefaultPath() : pathName);
	}

	public boolean setPlayerPrestige(UUID uniqueId, String prestigeName) {
		User user = getUser(uniqueId);
		if (user == null) {
			PrisonRanksX
					.logWarning("Failed to change '" + uniqueId.toString() + "' prestige to '" + prestigeName + "'.");
			PrisonRanksX.logWarning("No user data found for: " + uniqueId);
			return false;
		}
		if (!PrestigeStorage.prestigeExists(prestigeName)) {
			PrisonRanksX.logWarning("Unable to find a prestige named '" + prestigeName + "'.");
			return false;
		}
		user.setPrestigeName(prestigeName);
		return true;
	}

	public void createRank(String name, double cost, String pathName, String displayName) {
		pathName = pathName == null ? RankStorage.getDefaultPath() : pathName;
		ConfigurationSection pathSection = getMainSection().getConfigurationSection(pathName);
		String lastRankName = RankStorage.getLastRank(pathName);
		// If it's a new path, then create it
		if (pathSection == null) pathSection = getMainSection().createSection(pathName);
		ConfigurationSection lastRankSection = lastRankName == null ? null
				: pathSection.getConfigurationSection(lastRankName);
		// If the path has at least one rank, then we should change the next rank to the
		// rank we're going to create
		if (lastRankName != null) ConfigManager.setOrElse(lastRankSection, StorageFields.NEXT_FIELDS, name);
		ConfigurationSection newRankSection = pathSection.createSection(name);
		newRankSection.set(getField(lastRankSection, StorageFields.COST_FIELDS), cost);
		newRankSection.set(getField(lastRankSection, StorageFields.NEXT_FIELDS), "LASTRANK");
		newRankSection.set(getField(lastRankSection, StorageFields.DISPLAY_FIELDS), displayName);
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	public void setRankDisplayName(String name, String pathName, String displayName) {
		ConfigurationSection rankSection = getMainSection().getConfigurationSection(pathName)
				.getConfigurationSection(name);
		if (rankSection == null) return;
		ConfigManager.setOrElse(rankSection, StorageFields.DISPLAY_FIELDS, displayName);
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	public void setRankCost(String name, String pathName, double cost) {
		ConfigurationSection rankSection = getMainSection().getConfigurationSection(pathName)
				.getConfigurationSection(name);
		if (rankSection == null) return;
		ConfigManager.setOrElse(rankSection, StorageFields.COST_FIELDS, cost);
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	private Map<String, Object> deleteTemporarily(String name, String oldPathName) {
		ConfigurationSection oldPathSection = getMainSection().getConfigurationSection(oldPathName);
		if (oldPathSection == null) return null;
		ConfigurationSection rankSection = oldPathSection.getConfigurationSection(name);
		if (rankSection == null) return null;
		Map<String, Object> savedRankSection = new LinkedHashMap<>(rankSection.getValues(true));
		List<String> rankNames = Lists.newArrayList(oldPathSection.getValues(false).keySet());
		int specifiedRankIndex = rankNames.indexOf(name);
		String toMoveRankName = rankNames.get(specifiedRankIndex);
		// Account for different scenarios
		if (specifiedRankIndex > 0 && specifiedRankIndex != rankNames.size() - 1) {
			int previousRankIndex = specifiedRankIndex - 1;
			String previousRankName = rankNames.get(previousRankIndex);
			ConfigurationSection previousRankSection = oldPathSection.getConfigurationSection(previousRankName);
			if (rankNames.size() > 2) {
				int nextRankIndex = specifiedRankIndex + 1;
				String nextRankName = rankNames.get(nextRankIndex);
				ConfigurationSection nextRankSection = oldPathSection.getConfigurationSection(nextRankName);
				ConfigManager.setOrElse(previousRankSection, StorageFields.NEXT_FIELDS, nextRankName);
				if (rankNames.size() == 3)
					ConfigManager.setOrElse(nextRankSection, StorageFields.NEXT_FIELDS, "LASTRANK");
			} else if (rankNames.size() == 2) {
				ConfigManager.setOrElse(previousRankSection, StorageFields.NEXT_FIELDS, "LASTRANK");
			}
		} else if (specifiedRankIndex == rankNames.size() - 1) {
			if (rankNames.size() > 1) {
				int previousRankIndex = specifiedRankIndex - 1;
				String previousRankName = rankNames.get(previousRankIndex);
				ConfigurationSection previousRankSection = oldPathSection.getConfigurationSection(previousRankName);
				ConfigManager.setOrElse(previousRankSection, StorageFields.NEXT_FIELDS, "LASTRANK");
			}
		}
		oldPathSection.set(toMoveRankName, null);
		if (oldPathSection.getValues(false).size() == 0) getMainSection().set(oldPathName, null);
		return savedRankSection;
	}

	public void deleteRank(String name, String pathName) {
		deleteTemporarily(name, pathName);
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	public void moveRankPath(String name, String oldPathName, String newPathName) {
		Map<String, Object> oldRankValues = deleteTemporarily(name, oldPathName);
		ConfigurationSection newPathSection = getMainSection().getConfigurationSection(newPathName);
		String lastRankName = RankStorage.getLastRank(newPathName);
		// If it's a new path, then create it
		if (newPathSection == null) newPathSection = getMainSection().createSection(newPathName);
		ConfigurationSection lastRankSection = lastRankName == null ? null
				: newPathSection.getConfigurationSection(lastRankName);
		// If the path has at least one rank, then we should change the next rank to the
		// rank we're going to create
		if (lastRankName != null) ConfigManager.setOrElse(lastRankSection, StorageFields.NEXT_FIELDS, name);
		ConfigurationSection newRankSection = newPathSection.createSection(name);
		oldRankValues.entrySet().forEach(entry -> newRankSection.set(entry.getKey(), entry.getValue()));
		newRankSection.set(getField(lastRankSection, StorageFields.NEXT_FIELDS), "LASTRANK");
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	public void copyRank(String name, String pathName, String name2) {
		ConfigurationSection rankSection = getMainSection().getConfigurationSection(pathName)
				.getConfigurationSection(name);
		if (rankSection == null) return;
		ConfigurationSection rank2Section = getMainSection().getConfigurationSection(pathName)
				.getConfigurationSection(name2);
		ConfigManager.setOrElse(rank2Section, StorageFields.COMMANDS_FIELDS,
				ConfigManager.getListOrElse(rankSection, String.class, StorageFields.COMMANDS_FIELDS));
		ConfigManager.setOrElse(rank2Section, StorageFields.ADD_PERMISSIONS_FIELDS,
				ConfigManager.getListOrElse(rankSection, String.class, StorageFields.ADD_PERMISSIONS_FIELDS));
		ConfigManager.setOrElse(rank2Section, StorageFields.DEL_PERMISSIONS_FIELDS,
				ConfigManager.getListOrElse(rankSection, String.class, StorageFields.DEL_PERMISSIONS_FIELDS));
		ConfigManager.setOrElse(rank2Section, StorageFields.MESSAGE_FIELDS,
				ConfigManager.getListOrElse(rankSection, String.class, StorageFields.MESSAGE_FIELDS));
		ConfigManager.setOrElse(rank2Section, StorageFields.FIREWORK_FIELDS,
				ConfigManager.getOrElse(rankSection, StorageFields.FIREWORK_FIELDS));
		ConfigManager.setOrElse(rank2Section, StorageFields.RANDOM_COMMANDS_FIELDS,
				ConfigManager.getOrElse(rankSection, StorageFields.RANDOM_COMMANDS_FIELDS));
		ConfigManager.setOrElse(rank2Section, StorageFields.ACTION_BAR_FIELDS,
				ConfigManager.getOrElse(rankSection, StorageFields.ACTION_BAR_FIELDS));
		ConfigManager.setOrElse(rank2Section, StorageFields.REQUIREMENTS_FIELDS,
				ConfigManager.getListOrElse(rankSection, String.class, StorageFields.REQUIREMENTS_FIELDS));
		ConfigManager.setOrElse(rank2Section, StorageFields.REQUIREMENTS_FAIL_MESSAGE_FIELDS,
				ConfigManager.getListOrElse(rankSection, String.class, StorageFields.REQUIREMENTS_FAIL_MESSAGE_FIELDS));
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	public void reload() {
		ConfigCreator.reloadConfigs("config.yml", "guis.yml", "infinite_prestige.yml", "messages.yml", "prestiges.yml",
				"rebirths.yml", "ranks.yml");
		plugin.initGlobalSettings();

		if (plugin.getGlobalSettings().isRankEnabled()) {
			plugin.getRankSettings().setup();
			RankStorage.loadRanks();
		}
		if (plugin.getGlobalSettings().isPrestigeEnabled()) {
			plugin.getPrestigeSettings().setup();
			PrestigeStorage.loadPrestiges();
		}
		if (plugin.getGlobalSettings().isRebirthEnabled()) {
			plugin.getRebirthSettings().setup();
			RebirthStorage.loadRebirths();
		}
		if (plugin.getGlobalSettings().isHologramsPlugin() && (plugin.getHologramSettings().isHologramsEnabled()))
			plugin.getHologramSettings().setup();
		if (plugin.getGlobalSettings().isPlaceholderAPILoaded()) plugin.getPlaceholderAPISettings().setup();
		if (plugin.getGlobalSettings().isGuiRankList()) plugin.initRanksGUIList();
		Messages.reload();
	}

}
