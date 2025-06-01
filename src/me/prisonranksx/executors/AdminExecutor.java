package me.prisonranksx.executors;

import com.google.common.collect.Lists;
import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.bukkitutils.ConfigCreator;
import me.prisonranksx.bukkitutils.FireworkColor;
import me.prisonranksx.components.*;
import me.prisonranksx.data.*;
import me.prisonranksx.holders.Level;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.PermissionsManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.ProbabilityCollection;
import me.prisonranksx.utils.Scrif;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AdminExecutor {

	private PrisonRanksX plugin;

	public AdminExecutor(PrisonRanksX plugin) {
		this.plugin = plugin;
	}

	private String getField(ConfigurationSection section, String... fields) {
		return section == null ? fields[0] : ConfigManager.getPossibleField(section, fields);
	}


	private ConfigurationSection getMainRankSection() {
		return ConfigManager.getRanksConfig().getConfigurationSection("Ranks");
	}

	private ConfigurationSection getMainPrestigeSection() {
		return ConfigManager.getPrestigesConfig().getConfigurationSection("Prestiges");
	}

	private ConfigurationSection getMainRebirthSection() {
		return ConfigManager.getRebirthsConfig().getConfigurationSection("Rebirths");
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

	public CompletableFuture<Void> removeRankOnResetPermissions(UUID uniqueId) {
		User user = getUser(uniqueId);
		Player target = user.getPlayer();
		if (plugin.getRankSettings().isRemoveRankPermissionsOnRankReset()) {
			return CompletableFuture.runAsync(() -> RankStorage.getPathRanks(PRXAPI.getPlayerPathOrDefault(target)).forEach(rank -> {
				if (!rank.getName().equals(RankStorage.getFirstRankName())) removeLevelPerms(target, rank);
			}));
		}
		return CompletableFuture.completedFuture(null);
	}

	public CompletableFuture<Void> removeRankOnDeletionPermissions(UUID uniqueId) {
		User user = getUser(uniqueId);
		Player target = user.getPlayer();
		if (plugin.getRankSettings().isRemoveRankPermissionsOnRankDeletion()) {
			return CompletableFuture.runAsync(() -> RankStorage.getPathRanks(PRXAPI.getPlayerPathOrDefault(target))
					.forEach(rank -> removeLevelPerms(target, rank)));
		}
		return CompletableFuture.completedFuture(null);
	}

	public CompletableFuture<Void> removePrestigeOnResetPermissions(UUID uniqueId) {
		User user = getUser(uniqueId);
		Player target = user.getPlayer();
		if (plugin.getPrestigeSettings().isRemovePrestigePermissionsOnPrestigeReset()) {
			return CompletableFuture.runAsync(() -> PrestigeStorage.getPrestiges().forEach(prestige -> {
				if (!prestige.getName().equals(PrestigeStorage.getFirstPrestigeName()))
					removeLevelPerms(target, prestige);
			}));
		}
		return CompletableFuture.completedFuture(null);
	}

	public CompletableFuture<Void> removePrestigeOnDeletionPermissions(UUID uniqueId) {
		User user = getUser(uniqueId);
		Player target = user.getPlayer();
		if (plugin.getPrestigeSettings().isRemovePrestigePermissionsOnPrestigeDeletion()) {
			return CompletableFuture.runAsync(() ->
					PrestigeStorage.getPrestiges().forEach(prestige -> removeLevelPerms(target, prestige))
			);
		}
		return CompletableFuture.completedFuture(null);
	}

	public CompletableFuture<Void> removeRebirthOnResetPermissions(UUID uniqueId) {
		User user = getUser(uniqueId);
		Player target = user.getPlayer();
		if (plugin.getRebirthSettings().isRemoveRebirthPermissionsOnRebirthReset()) {
			return CompletableFuture.runAsync(() -> {
				RebirthStorage.getRebirths().forEach(rebirth -> {
					if (!rebirth.getName().equals(RebirthStorage.getFirstRebirthName())) removeLevelPerms(target, rebirth);
				});
			});
		}
		return CompletableFuture.completedFuture(null);
	}

	public CompletableFuture<Void> removeRebirthOnDeletionPermissions(UUID uniqueId) {
		User user = getUser(uniqueId);
		Player target = user.getPlayer();
		if (plugin.getRebirthSettings().isRemoveRebirthPermissionsOnRebirthDeletion()) {
			return CompletableFuture.runAsync(() -> RebirthStorage.getRebirths().forEach(rebirth -> removeLevelPerms(target, rebirth)));
		}
		return CompletableFuture.completedFuture(null);
	}

	private void removeLevelPerms(Player target, Level level) {
		PermissionsComponent permissionsComponent = level.getPermissionsComponent();
		if (permissionsComponent != null) {
			if (permissionsComponent.hasGlobalAddPerms())
				PermissionsManager.removePermissions(target, permissionsComponent.getAddPermissionCollection());
			if (permissionsComponent.hasAddWorldPerms())
				PermissionsManager.removePermissions(target, permissionsComponent.getAddWorldPermissionMap());
			if (plugin.getGlobalSettings().isLuckPermsLoaded()) {
				PermissionsComponent.LuckPermsPermissionsComponent permComp =
						(PermissionsComponent.LuckPermsPermissionsComponent) permissionsComponent;
				if (permComp.hasAddServerPerms())
					permComp.removeServerPermissions(target, permComp.getAddServerPermissionMap());
			}
		}
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

	public boolean setPlayerRebirth(UUID uniqueId, String rebirthName) {
		User user = getUser(uniqueId);
		if (user == null) {
			PrisonRanksX
					.logWarning("Failed to change '" + uniqueId.toString() + "' rebirth to '" + rebirthName + "'.");
			PrisonRanksX.logWarning("No user data found for: " + uniqueId);
			return false;
		}
		if (!RebirthStorage.rebirthExists(rebirthName)) {
			PrisonRanksX.logWarning("Unable to find a rebirth named '" + rebirthName + "'.");
			return false;
		}
		user.setRebirthName(rebirthName);
		return true;
	}

	public void createRank(String name, double cost, String pathName, String displayName) {
		pathName = pathName == null ? RankStorage.getDefaultPath() : pathName;
		ConfigurationSection pathSection = getMainRankSection().getConfigurationSection(pathName);
		String lastRankName = RankStorage.getLastRankName(pathName);
		// If it's a new path, then create it
		if (pathSection == null) pathSection = getMainRankSection().createSection(pathName);
		ConfigurationSection lastRankSection = lastRankName == null ? null
				: pathSection.getConfigurationSection(lastRankName);
		// If the path has at least one rank, then we should change the next rank to the
		// rank we're going to create
		if (lastRankName != null) ConfigManager.setPossible(lastRankSection, StorageFields.NEXT_FIELDS, name);
		ConfigurationSection newRankSection = pathSection.createSection(name);
		newRankSection.set(getField(lastRankSection, StorageFields.COST_FIELDS), cost);
		newRankSection.set(getField(lastRankSection, StorageFields.NEXT_FIELDS), "LASTRANK");
		newRankSection.set(getField(lastRankSection, StorageFields.DISPLAY_FIELDS), displayName);
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	public void createPrestige(String name, double cost, String displayName) {
		ConfigurationSection prestigeSection = getMainPrestigeSection();
		String lastPrestigeName = PrestigeStorage.getLastPrestigeName();
		ConfigurationSection lastPrestigeSection = lastPrestigeName == null ? null
				: prestigeSection.getConfigurationSection(lastPrestigeName);

		if (lastPrestigeName != null) ConfigManager.setPossible(lastPrestigeSection, StorageFields.NEXT_FIELDS, name);
		ConfigurationSection newPrestigeSection = prestigeSection.createSection(name);
		// getField(..) Tries to match the field names of the previous prestige,
		// if there wasn't a previous prestige (section is null), then pick the first field name from the array.
		newPrestigeSection.set(getField(lastPrestigeSection, StorageFields.COST_FIELDS), cost);
		newPrestigeSection.set(getField(lastPrestigeSection, StorageFields.NEXT_FIELDS), "LASTPRESTIGE");
		newPrestigeSection.set(getField(lastPrestigeSection, StorageFields.DISPLAY_FIELDS), displayName);
		ConfigManager.saveConfig("prestiges.yml");
		PrestigeStorage.loadPrestiges();
	}

	public void createRebirth(String name, double cost, String displayName) {
		ConfigurationSection rebirthSection = getMainRebirthSection();
		String lastRebirthName = RebirthStorage.getLastRebirthName();
		ConfigurationSection lastRebirthSection = lastRebirthName == null ? null
				: rebirthSection.getConfigurationSection(lastRebirthName);

		if (lastRebirthName != null) ConfigManager.setPossible(lastRebirthSection, StorageFields.NEXT_FIELDS, name);
		ConfigurationSection newRebirthSection = rebirthSection.createSection(name);
		newRebirthSection.set(getField(lastRebirthSection, StorageFields.COST_FIELDS), cost);
		newRebirthSection.set(getField(lastRebirthSection, StorageFields.NEXT_FIELDS), "LASTREBIRTH");
		newRebirthSection.set(getField(lastRebirthSection, StorageFields.DISPLAY_FIELDS), displayName);
		ConfigManager.saveConfig("rebirths.yml");
		RebirthStorage.loadRebirths();
	}

	public void setRankDisplayName(String name, String pathName, String displayName) {
		ConfigurationSection rankSection = getMainRankSection().getConfigurationSection(pathName)
				.getConfigurationSection(name);
		if (rankSection == null) return;
		ConfigManager.setPossible(rankSection, StorageFields.DISPLAY_FIELDS, displayName);
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	public void setPrestigeDisplayName(String name, String displayName) {
		ConfigurationSection prestigeSection = getMainPrestigeSection().getConfigurationSection(name);
		if (prestigeSection == null) return;
		ConfigManager.setPossible(prestigeSection, StorageFields.DISPLAY_FIELDS, displayName);
		ConfigManager.saveConfig("prestiges.yml");
		PrestigeStorage.loadPrestiges();
	}

	public void setRebirthDisplayName(String name, String displayName) {
		ConfigurationSection rebirthSection = getMainRebirthSection().getConfigurationSection(name);
		if (rebirthSection == null) return;
		ConfigManager.setPossible(rebirthSection, StorageFields.DISPLAY_FIELDS, displayName);
		ConfigManager.saveConfig("rebirths.yml");
		RebirthStorage.loadRebirths();
	}

	public void setRankCost(String name, String pathName, double cost) {
		ConfigurationSection rankSection = getMainRankSection().getConfigurationSection(pathName)
				.getConfigurationSection(name);
		if (rankSection == null) return;
		ConfigManager.setPossible(rankSection, StorageFields.COST_FIELDS, cost);
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	public void setPrestigeCost(String name, double cost) {
		ConfigurationSection prestigeSection = getMainPrestigeSection().getConfigurationSection(name);
		if (prestigeSection == null) return;
		ConfigManager.setPossible(prestigeSection, StorageFields.COST_FIELDS, cost);
		ConfigManager.saveConfig("prestiges.yml");
		PrestigeStorage.loadPrestiges();
	}

	public void setRebirthCost(String name, double cost) {
		ConfigurationSection rebirthSection = getMainRebirthSection().getConfigurationSection(name);
		if (rebirthSection == null) return;
		ConfigManager.setPossible(rebirthSection, StorageFields.COST_FIELDS, cost);
		ConfigManager.saveConfig("rebirths.yml");
		RebirthStorage.loadRebirths();
	}

	private Map<String, Object> deleteRankTemporarily(String name, String oldPathName) {
		ConfigurationSection oldPathSection = getMainRankSection().getConfigurationSection(oldPathName);
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
				ConfigManager.setPossible(previousRankSection, StorageFields.NEXT_FIELDS, nextRankName);
				if (rankNames.size() == 3)
					ConfigManager.setPossible(nextRankSection, StorageFields.NEXT_FIELDS, "LASTRANK");
			} else if (rankNames.size() == 2) {
				ConfigManager.setPossible(previousRankSection, StorageFields.NEXT_FIELDS, "LASTRANK");
			}
		} else if (specifiedRankIndex == rankNames.size() - 1) {
			if (rankNames.size() > 1) {
				int previousRankIndex = specifiedRankIndex - 1;
				String previousRankName = rankNames.get(previousRankIndex);
				ConfigurationSection previousRankSection = oldPathSection.getConfigurationSection(previousRankName);
				ConfigManager.setPossible(previousRankSection, StorageFields.NEXT_FIELDS, "LASTRANK");
			}
		}
		oldPathSection.set(toMoveRankName, null);
		if (oldPathSection.getValues(false).isEmpty()) getMainRankSection().set(oldPathName, null);
		return savedRankSection;
	}

	private Map<String, Object> deletePrestigeTemporarily(String name) {
		ConfigurationSection prestigeSection = getMainPrestigeSection().getConfigurationSection(name);
		if (prestigeSection == null) return null;
		Map<String, Object> savedPrestigeSection = new LinkedHashMap<>(prestigeSection.getValues(true));
		List<String> prestigeNames = Lists.newArrayList(getMainPrestigeSection().getValues(false).keySet());
		int specifiedPrestigeIndex = prestigeNames.indexOf(name);
		String toMovePrestigeName = prestigeNames.get(specifiedPrestigeIndex);
		// Account for different scenarios
		if (specifiedPrestigeIndex > 0 && specifiedPrestigeIndex != prestigeNames.size() - 1) {
			int previousPrestigeIndex = specifiedPrestigeIndex - 1;
			String previousPrestigeName = prestigeNames.get(previousPrestigeIndex);
			ConfigurationSection previousPrestigeSection = getMainPrestigeSection().getConfigurationSection(previousPrestigeName);
			if (prestigeNames.size() > 2) {
				int nextPrestigeIndex = specifiedPrestigeIndex + 1;
				String nextPrestigeName = prestigeNames.get(nextPrestigeIndex);
				ConfigurationSection nextPrestigeSection = getMainPrestigeSection().getConfigurationSection(nextPrestigeName);
				ConfigManager.setPossible(previousPrestigeSection, StorageFields.NEXT_FIELDS, nextPrestigeName);
				if (prestigeNames.size() == 3)
					ConfigManager.setPossible(nextPrestigeSection, StorageFields.NEXT_FIELDS, "LASTPRESTIGE");
			} else if (prestigeNames.size() == 2) {
				ConfigManager.setPossible(previousPrestigeSection, StorageFields.NEXT_FIELDS, "LASTPRESTIGE");
			}
		} else if (specifiedPrestigeIndex == prestigeNames.size() - 1) {
			if (prestigeNames.size() > 1) {
				int previousPrestigeIndex = specifiedPrestigeIndex - 1;
				String previousPrestigeName = prestigeNames.get(previousPrestigeIndex);
				ConfigurationSection previousPrestigeSection = getMainPrestigeSection().getConfigurationSection(previousPrestigeName);
				ConfigManager.setPossible(previousPrestigeSection, StorageFields.NEXT_FIELDS, "LASTPRESTIGE");
			}
		}
		getMainPrestigeSection().set(toMovePrestigeName, null);
		if (getMainPrestigeSection().getValues(false).isEmpty())
			getMainPrestigeSection().set(name, null);
		return savedPrestigeSection;
	}

	private Map<String, Object> deleteRebirthTemporarily(String name) {
		ConfigurationSection rebirthSection = getMainRebirthSection().getConfigurationSection(name);
		if (rebirthSection == null) return null;
		Map<String, Object> savedRebirthSection = new LinkedHashMap<>(rebirthSection.getValues(true));
		List<String> rebirthNames = Lists.newArrayList(getMainRebirthSection().getValues(false).keySet());
		int specifiedRebirthIndex = rebirthNames.indexOf(name);
		String toMoveRebirthName = rebirthNames.get(specifiedRebirthIndex);
		// Account for different scenarios
		if (specifiedRebirthIndex > 0 && specifiedRebirthIndex != rebirthNames.size() - 1) {
			int previousRebirthIndex = specifiedRebirthIndex - 1;
			String previousRebirthName = rebirthNames.get(previousRebirthIndex);
			ConfigurationSection previousRebirthSection = getMainRebirthSection().getConfigurationSection(previousRebirthName);
			if (rebirthNames.size() > 2) {
				int nextRebirthIndex = specifiedRebirthIndex + 1;
				String nextRebirthName = rebirthNames.get(nextRebirthIndex);
				ConfigurationSection nextRebirthSection = getMainRebirthSection().getConfigurationSection(nextRebirthName);
				ConfigManager.setPossible(previousRebirthSection, StorageFields.NEXT_FIELDS, nextRebirthName);
				if (rebirthNames.size() == 3)
					ConfigManager.setPossible(nextRebirthSection, StorageFields.NEXT_FIELDS, "LASTREBIRTH");
			} else if (rebirthNames.size() == 2) {
				ConfigManager.setPossible(previousRebirthSection, StorageFields.NEXT_FIELDS, "LASTREBIRTH");
			}
		} else if (specifiedRebirthIndex == rebirthNames.size() - 1) {
			if (rebirthNames.size() > 1) {
				int previousRebirthIndex = specifiedRebirthIndex - 1;
				String previousRebirthName = rebirthNames.get(previousRebirthIndex);
				ConfigurationSection previousRebirthSection = getMainRebirthSection().getConfigurationSection(previousRebirthName);
				ConfigManager.setPossible(previousRebirthSection, StorageFields.NEXT_FIELDS, "LASTREBIRTH");
			}
		}
		getMainRebirthSection().set(toMoveRebirthName, null);
		if (getMainRebirthSection().getValues(false).isEmpty())
			getMainRebirthSection().set(name, null);
		return savedRebirthSection;
	}

	public void deleteRank(String name, String pathName) {
		deleteRankTemporarily(name, pathName);
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	public void deletePrestige(String name) {
		deletePrestigeTemporarily(name);
		ConfigManager.saveConfig("prestiges.yml");
		PrestigeStorage.loadPrestiges();
	}

	public void deleteRebirth(String name) {
		deleteRebirthTemporarily(name);
		ConfigManager.saveConfig("rebirths.yml");
		RebirthStorage.loadRebirths();
	}

	public void moveRankPath(String name, String oldPathName, String newPathName) {
		Map<String, Object> oldRankValues = deleteRankTemporarily(name, oldPathName);
		ConfigurationSection newPathSection = getMainRankSection().getConfigurationSection(newPathName);
		String lastRankName = RankStorage.getLastRankName(newPathName);
		// If it's a new path, then create it
		if (newPathSection == null) newPathSection = getMainRankSection().createSection(newPathName);
		ConfigurationSection lastRankSection = lastRankName == null ? null
				: newPathSection.getConfigurationSection(lastRankName);
		// If the path has at least one rank, then we should change the next rank to the
		// rank we're going to create
		if (lastRankName != null) ConfigManager.setPossible(lastRankSection, StorageFields.NEXT_FIELDS, name);
		ConfigurationSection newRankSection = newPathSection.createSection(name);
		oldRankValues.entrySet().forEach(entry -> newRankSection.set(entry.getKey(), entry.getValue()));
		newRankSection.set(getField(lastRankSection, StorageFields.NEXT_FIELDS), "LASTRANK");
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	public void copyRank(String name, String pathName, String name2) {
		ConfigurationSection rankSection = getMainRankSection().getConfigurationSection(pathName)
				.getConfigurationSection(name);
		if (rankSection == null) return;
		ConfigurationSection rank2Section = getMainRankSection().getConfigurationSection(pathName)
				.getConfigurationSection(name2);
		ConfigManager.setPossible(rank2Section, StorageFields.COMMANDS_FIELDS,
				ConfigManager.getPossibleList(rankSection, String.class, StorageFields.COMMANDS_FIELDS));
		ConfigManager.setPossible(rank2Section, StorageFields.ADD_PERMISSIONS_FIELDS,
				ConfigManager.getPossibleList(rankSection, String.class, StorageFields.ADD_PERMISSIONS_FIELDS));
		ConfigManager.setPossible(rank2Section, StorageFields.DEL_PERMISSIONS_FIELDS,
				ConfigManager.getPossibleList(rankSection, String.class, StorageFields.DEL_PERMISSIONS_FIELDS));
		ConfigManager.setPossible(rank2Section, StorageFields.MESSAGE_FIELDS,
				ConfigManager.getPossibleList(rankSection, String.class, StorageFields.MESSAGE_FIELDS));
		ConfigManager.setPossible(rank2Section, StorageFields.FIREWORK_FIELDS,
				ConfigManager.getPossible(rankSection, StorageFields.FIREWORK_FIELDS));
		ConfigManager.setPossible(rank2Section, StorageFields.RANDOM_COMMANDS_FIELDS,
				ConfigManager.getPossible(rankSection, StorageFields.RANDOM_COMMANDS_FIELDS));
		ConfigManager.setPossible(rank2Section, StorageFields.ACTION_BAR_FIELDS,
				ConfigManager.getPossible(rankSection, StorageFields.ACTION_BAR_FIELDS));
		ConfigManager.setPossible(rank2Section, StorageFields.REQUIREMENTS_FIELDS,
				ConfigManager.getPossibleList(rankSection, String.class, StorageFields.REQUIREMENTS_FIELDS));
		ConfigManager.setPossible(rank2Section, StorageFields.REQUIREMENTS_FAIL_MESSAGE_FIELDS,
				ConfigManager.getPossibleList(rankSection, String.class, StorageFields.REQUIREMENTS_FAIL_MESSAGE_FIELDS));
		ConfigManager.saveConfig("ranks.yml");
		RankStorage.loadRanks();
	}

	public void displayRankInfo(CommandSender sender, Rank rank) {
		sendMsg(sender, "-- RANK INFO --");
		long playersCount = Bukkit.getOnlinePlayers().stream().filter(player -> {
			User user = plugin.getUserController().getUser(UniqueId.getUUID(player));
			return Objects.equals(user.getRankName(), rank.getName());
		}).count();
		sendMsg(sender, "&7Amount of Online Players With This Rank: &f" + playersCount);
		sendMsg(sender, "&7Index: &f" + rank.getIndex());
		sendMsg(sender, "&7Name: &f" + rank.getName());
		sendMsg(sender, "&7Path: &f" + RankStorage.findFirstPath(rank.getName()));
		sendMsg(sender, "&7Cost: &f" + rank.getCost() +
				(sender instanceof Player ? " | Your Increased Cost: " + PRXAPI.getRankFinalCost(rank, (Player) sender) : ""));
		sendMsg(sender, "&7Display: &f" + rank.getDisplayName());
		sendMsg(sender, "&7Next Rank: &f" + rank.getNextName());
		sendMsg(sender, "&7Is Allow Prestige: &f" + (rank.isAllowPrestige() || rank.getNextName() == null));

		List<String> broadcastMsgs = rank.getBroadcastMessages();
		sendMsg(sender,
				broadcastMsgs == null ? "&7&mBroadcast Messages:&f none" : "&7Broadcast Messages:");
		if (broadcastMsgs != null) broadcastMsgs.forEach(msg -> sendMsg(sender, "&r" + msg));

		List<String> messages = rank.getMessages();
		sendMsg(sender, messages == null ? "&7&mMessages:&f none" : "&7Messages:");
		if (messages != null) messages.forEach(msg -> sendMsg(sender, "&r" + msg));

		CommandsComponent commandsComponent = rank.getCommandsComponent();
		sendMsg(sender, commandsComponent == null ? "&7&mCommands:&f none" : "&7Commands:");
		if (commandsComponent != null) {
			List<String> console = commandsComponent.getConsoleCommands();
			if (console != null)
				console.forEach(command -> sendMsg(sender, "&f[console] &a" + command));
			List<String> player = commandsComponent.getPlayerCommands();
			if (player != null) player.forEach(command -> sendMsg(sender, "&f[player] &e" + command));
		}

		ActionBarComponent actionBarComponent = rank.getActionBarComponent();
		sendMsg(sender, actionBarComponent == null ? "&7&mAction Bar:&f none" : "&7Action Bar:");
		if (actionBarComponent != null) {
			sendMsg(sender, "&r Interval: &r" + actionBarComponent.getActionBarSender().getInterval());
			actionBarComponent.getActionBarSender().forEachMessage(m -> sendMsg(sender, "&r" + m));
		}

		PermissionsComponent permissionsComponent = rank.getPermissionsComponent();
		sendMsg(sender, permissionsComponent == null ? "&7&mPermissions:&f none" : "&7Permissions:");
		if (permissionsComponent != null) {
			Set<String> add = permissionsComponent.getAddPermissionCollection();
			Set<String> del = permissionsComponent.getDelPermissionCollection();
			if (add != null) add.forEach(permission -> sendMsg(sender, "&7+ &a" + permission));
			if (del != null) del.forEach(permission -> sendMsg(sender, "&7- &c" + permission));
		}
		RandomCommandsComponent randomCommandsComponent = rank.getRandomCommandsComponent();
		sendMsg(sender,
				randomCommandsComponent == null ? "&7&mRandom Commands:&f none" : "&7Random Commands:");
		if (randomCommandsComponent != null) {
			NavigableSet<ProbabilityCollection.ProbabilitySetElement<List<String>>> collection = randomCommandsComponent
					.getCollection();
			if (collection != null) {
				collection.forEach(probabilitySetElement -> {
					sendMsg(sender, " &rChance: " + probabilitySetElement.getProbability() + " %");
					sendMsg(sender, " &rCommands: " + probabilitySetElement.getObject());
				});
			}
		}
		RequirementsComponent requirementsComponent = rank.getRequirementsComponent();
		sendMsg(sender, requirementsComponent == null ? "&7&mRequirements:&f none" : "&7Requirements:");
		if (requirementsComponent != null) {
			Map<String, String> equalRequirements = requirementsComponent.getEqualRequirements();
			if (equalRequirements != null)
				equalRequirements.forEach((k, v) -> sendMsg(sender, "&fEqual: " + k + "&a->&f" + v));
			Map<String, String> notEqualRequirements = requirementsComponent.getNotEqualRequirements();
			if (notEqualRequirements != null) notEqualRequirements
					.forEach((k, v) -> sendMsg(sender, "&fNot Equal: " + k + "&c<-&f" + v));
			Map<String, Double> greaterThanRequirements = requirementsComponent
					.getGreaterThanRequirements();
			if (greaterThanRequirements != null) greaterThanRequirements
					.forEach((k, v) -> sendMsg(sender, "&fGreater Than: " + k + "&a>>&f" + v));
			Map<String, Double> lessThanRequirements = requirementsComponent.getLessThanRequirements();
			if (lessThanRequirements != null) lessThanRequirements
					.forEach((k, v) -> sendMsg(sender, "&fLess Than: " + k + "&c<<&f" + v));
			Map<Scrif, List<String>> scriptRequirements = requirementsComponent.getScriptRequirements();
			if (scriptRequirements != null) scriptRequirements.forEach(
					(k, v) -> sendMsg(sender, "&fScript: " + k.getScript() + " Placeholders: " + v));

		}
		List<String> requirementsMessages = rank.getRequirementsMessages();
		sendMsg(sender, requirementsMessages == null ? "&7&mRequirements Messages:&f none"
				: "&7Requirements Messages:");
		if (requirementsMessages != null) {
			requirementsMessages.forEach(msg -> sendMsg(sender, "&r" + msg));
			Messages.sendMessages(sender, requirementsMessages,
					l -> RequirementsComponent.updateMsg(l, requirementsComponent));
		}
		FireworkComponent fireworkComponent = rank.getFireworkComponent();
		sendMsg(sender, fireworkComponent == null ? "&7&mFirework:&f none" : "&7Firework:");

		if (fireworkComponent != null) {
			sendMsg(sender, " Power: " + fireworkComponent.getPower());
			fireworkComponent.getFireworkEffects().forEach(effect -> {
				sendMsg(sender, " Type: " + effect.getType().name());
				sendMsg(sender, " Colors: " + FireworkColor.stringify(effect.getColors()));
				sendMsg(sender, " FadeColors: " + FireworkColor.stringify(effect.getFadeColors()));
				sendMsg(sender, " Flicker: " + effect.hasFlicker());
				sendMsg(sender, " Trail: " + effect.hasTrail());
			});
		}
	}

	public void displayPrestigeInfo(CommandSender sender, Prestige prestige) {
		sendMsg(sender, prestige.getDisplayName());
		sendMsg(sender, "Cost: " + prestige.getCost());
		sendMsg(sender, "Next Prestige: " + prestige.getNextName());
		sendMsg(sender, "Prestige Num: " + prestige.getNumber());
	}

	private void sendMsg(CommandSender sender, String s) {
		sender.sendMessage(StringManager.parseColors(s));
	}

	public void reload() {
		ConfigCreator.reloadConfigs("config.yml", "guis.yml", "infinite_prestige.yml", "messages.yml", "prestiges.yml",
				"rebirths.yml", "ranks.yml");
		plugin.initGlobalSettings();

		if (plugin.getGlobalSettings().isRankEnabled()) {
			plugin.prepareRanks();
		}
		if (plugin.getGlobalSettings().isPrestigeEnabled()) {
			plugin.preparePrestiges();
		}
		if (plugin.getGlobalSettings().isRebirthEnabled()) {
			plugin.prepareRebirths();
		}
		if (plugin.getGlobalSettings().isHologramsPlugin() && (plugin.getHologramSettings().isHologramsEnabled()))
			plugin.getHologramSettings().setup();
		if (plugin.getGlobalSettings().isPlaceholderAPILoaded()) plugin.getPlaceholderAPISettings().setup();
		if (plugin.getGlobalSettings().isGuiRankList()) plugin.initRanksGUIList();
		Messages.reload();
	}

	public void save() {
		ConfigCreator.saveConfigs("prestiges.yml",
				"rebirths.yml", "ranks.yml");
		plugin.getUserController().saveUsers(true);
	}

}
