package me.prisonranksx.data;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.segmentedtasks.SegmentedTasks;
import me.prisonranksx.common.Common;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.ConfigManager;

public class YamlUserController implements UserController {

	private Map<UUID, User> users = new ConcurrentHashMap<>();
	private PrisonRanksX plugin;

	public YamlUserController(PrisonRanksX plugin) {
		this.plugin = plugin;
		users.clear();
	}

	@Override
	public CompletableFuture<Void> saveUser(@NotNull UUID uniqueId) {
		return saveUser(getUser(uniqueId));
	}

	@Override
	public CompletableFuture<Void> saveUser(@NotNull UUID uniqueId, boolean saveToDisk) {
		return saveUser(getUser(uniqueId), saveToDisk);
	}

	@Override
	public CompletableFuture<Void> saveUser(@NotNull User user) {
		return saveUser(user, false);
	}

	@Override
	public CompletableFuture<Void> saveUser(@NotNull User user, boolean saveToDisk) {
		CompletableFuture<Void> saveUserFuture = new CompletableFuture<>();
		SegmentedTasks.async(() -> {
			String stringUniqueId = user.getUniqueId().toString();
			if (plugin.getGlobalSettings().isRankEnabled()) {
				ConfigManager.getRankDataConfig().set("players." + stringUniqueId + ".name", user.getName());
				ConfigManager.getRankDataConfig().set("players." + stringUniqueId + ".rank", user.getRankName());
				ConfigManager.getRankDataConfig().set("players." + stringUniqueId + ".path", user.getPathName());
				if (saveToDisk) ConfigManager.saveConfig("rankdata.yml");
			}
			if (plugin.getGlobalSettings().isPrestigeEnabled()) {
				ConfigManager.getPrestigeDataConfig().set("players." + stringUniqueId, user.getPrestigeName());
				if (saveToDisk) ConfigManager.saveConfig("prestigedata.yml");
			}
			if (plugin.getGlobalSettings().isRebirthEnabled()) {
				ConfigManager.getRebirthDataConfig().set("players." + stringUniqueId, user.getRebirthName());
				if (saveToDisk) ConfigManager.saveConfig("rebirthdata.yml");
			}
			saveUserFuture.complete(null);
		});
		return saveUserFuture;
	}

	@Override
	public CompletableFuture<Void> saveUsers() {
		return saveUsers(false);
	}

	@Override
	public CompletableFuture<Void> saveUsers(boolean saveToDisk) {
		return CompletableFuture.runAsync(() -> {
			ConfigurationSection rankDataSection = plugin.getGlobalSettings().isRankEnabled()
					? ConfigManager.getRankDataConfig().getConfigurationSection("players") : null;
			ConfigurationSection prestigeDataSection = plugin.getGlobalSettings().isPrestigeEnabled()
					? ConfigManager.getPrestigeDataConfig().getConfigurationSection("players") : null;
			ConfigurationSection rebirthDataSection = plugin.getGlobalSettings().isRebirthEnabled()
					? ConfigManager.getRebirthDataConfig().getConfigurationSection("players") : null;
			users.forEach((uniqueId, user) -> {
				String stringUniqueId = uniqueId.toString();
				if (rankDataSection != null) {
					rankDataSection.set(stringUniqueId + ".name", user.getName());
					rankDataSection.set(stringUniqueId + ".path", user.getPathName());
					rankDataSection.set(stringUniqueId + ".rank", user.getRankName());
				}
				if (prestigeDataSection != null) prestigeDataSection.set(stringUniqueId, user.getPrestigeName());
				if (rebirthDataSection != null) rebirthDataSection.set(stringUniqueId, user.getRebirthName());
			});
			if (saveToDisk) {
				if (rankDataSection != null) ConfigManager.saveConfig("rankdata.yml");
				if (prestigeDataSection != null) ConfigManager.saveConfig("prestigedata.yml");
				if (rebirthDataSection != null) ConfigManager.saveConfig("rebirthdata.yml");
			}
		});
	}

	@Override
	public CompletableFuture<User> loadUser(UUID uniqueId, String name) {
		return CompletableFuture.supplyAsync(() -> {
			User user = new User(uniqueId, name);
			String stringUniqueId = uniqueId.toString();
			if (plugin.getGlobalSettings().isRankEnabled()) user.setRankAndPathName(
					Optional.ofNullable(
							ConfigManager.getRankDataConfig().getString("players." + stringUniqueId + ".rank"))
							.orElse(RankStorage.getFirstRank()),
					Optional.ofNullable(
							ConfigManager.getRankDataConfig().getString("players." + stringUniqueId + ".path"))
							.orElse(RankStorage.getDefaultPath()));
			if (plugin.getGlobalSettings().isPrestigeEnabled())
				user.setPrestigeName(ConfigManager.getPrestigeDataConfig().getString("players." + stringUniqueId));
			if (plugin.getGlobalSettings().isRebirthEnabled())
				user.setRebirthName(ConfigManager.getRebirthDataConfig().getString(stringUniqueId));
			users.put(uniqueId, user);
			return user;
		});
	}

	@Override
	public void unloadUser(UUID uniqueId) {
		users.remove(uniqueId);
	}

	@Override
	public boolean isLoaded(UUID uniqueId) {
		return users.containsKey(uniqueId);
	}

	@Override
	@Nullable
	public User getUser(UUID uniqueId) {
		return users.get(uniqueId);
	}

	public void printInfo(UUID uniqueId) {
		User user = users.get(uniqueId);
		Common.print("UUID: " + uniqueId.toString() + " Rank: " + user.getRankName() + " Prestige: "
				+ user.getPrestigeName() + " Rebirth: " + user.getRebirthName());
	}

}
