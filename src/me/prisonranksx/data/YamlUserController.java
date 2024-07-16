package me.prisonranksx.data;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.StupidMySQL;
import me.prisonranksx.bukkitutils.UserConfig;
import me.prisonranksx.bukkitutils.bukkittickbalancer.BukkitTickBalancer;
import me.prisonranksx.common.Common;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.MySQLManager;

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
		BukkitTickBalancer.async(() -> {
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
	public CompletableFuture<Void> saveUsers(Iterable<User> users) {
		return CompletableFuture.runAsync(() -> {
			ConfigurationSection rankDataSection = plugin.getGlobalSettings().isRankEnabled()
					? ConfigManager.getRankDataConfig().getConfigurationSection("players") : null;
			ConfigurationSection prestigeDataSection = plugin.getGlobalSettings().isPrestigeEnabled()
					? ConfigManager.getPrestigeDataConfig().getConfigurationSection("players") : null;
			ConfigurationSection rebirthDataSection = plugin.getGlobalSettings().isRebirthEnabled()
					? ConfigManager.getRebirthDataConfig().getConfigurationSection("players") : null;
			users.forEach(user -> {
				String stringUniqueId = user.getUniqueId().toString();
				if (rankDataSection != null) {
					rankDataSection.set(stringUniqueId + ".name", user.getName());
					rankDataSection.set(stringUniqueId + ".path", user.getPathName());
					rankDataSection.set(stringUniqueId + ".rank", user.getRankName());
				}
				if (prestigeDataSection != null) prestigeDataSection.set(stringUniqueId, user.getPrestigeName());
				if (rebirthDataSection != null) rebirthDataSection.set(stringUniqueId, user.getRebirthName());
			});
			if (rankDataSection != null) ConfigManager.saveConfig("rankdata.yml");
			if (prestigeDataSection != null) ConfigManager.saveConfig("prestigedata.yml");
			if (rebirthDataSection != null) ConfigManager.saveConfig("rebirthdata.yml");
		});
	}

	@Override
	public CompletableFuture<User> loadUser(UUID uniqueId, String name) {
		return CompletableFuture.supplyAsync(() -> {
			String stringUniqueId = uniqueId.toString();
			if (users.containsKey(uniqueId))
				PrisonRanksX.logWarning("Loading already loaded user: " + name + " " + stringUniqueId);
			User user = new User(uniqueId, name);
			if (plugin.getGlobalSettings().isRankEnabled()) user.setRankAndPathName(
					Optional.ofNullable(
							ConfigManager.getRankDataConfig().getString("players." + stringUniqueId + ".rank"))
							.orElse(RankStorage.getFirstRankName()),
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

	@Override
	public CompletableFuture<Map<UUID, User>> convert(UserControllerType type) {
		return CompletableFuture.supplyAsync(() -> {
			saveUsers();
			if (type == UserControllerType.MYSQL) {
				// Assuming we are already connected
				StupidMySQL stupidMySQL = StupidMySQL.use(MySQLManager.getConnection(), MySQLManager.getDatabase(),
						MySQLManager.getTable());
				stupidMySQL.prepareSetOrInsert("uuid", "name", "rank", "path", "prestige", "rebirth", "score");
				ConfigurationSection rankDataSection = ConfigManager.getRankDataConfig()
						.getConfigurationSection("players");
				ConfigurationSection prestigeDataSection = ConfigManager.getPrestigeDataConfig()
						.getConfigurationSection("players");
				ConfigurationSection rebirthDataSection = ConfigManager.getRebirthDataConfig()
						.getConfigurationSection("players");
				for (String stringUniqueId : rankDataSection.getKeys(false)) {
					ConfigurationSection uniqueIdSection = rankDataSection.getConfigurationSection(stringUniqueId);
					String name = uniqueIdSection.getString("name");
					String rank = uniqueIdSection.getString("rank");
					String path = uniqueIdSection.getString("path");
					String prestige = prestigeDataSection.getString(stringUniqueId);
					String rebirth = rebirthDataSection.getString(stringUniqueId);
					stupidMySQL.addToPrepared(stringUniqueId, name, rank, path, prestige, rebirth, 0);
				}
				stupidMySQL.execute();
			} else if (type == UserControllerType.YAML_PER_USER) {
				ConfigurationSection rankDataSection = ConfigManager.getRankDataConfig()
						.getConfigurationSection("players");
				ConfigurationSection prestigeDataSection = ConfigManager.getPrestigeDataConfig()
						.getConfigurationSection("players");
				ConfigurationSection rebirthDataSection = ConfigManager.getRebirthDataConfig()
						.getConfigurationSection("players");
				UserConfig usersConfig = UserConfig.create(plugin, "users");
				try {
					for (String stringUniqueId : rankDataSection.getKeys(false)) {
						ConfigurationSection uniqueIdSection = rankDataSection.getConfigurationSection(stringUniqueId);
						UUID uniqueId = UUID.fromString(stringUniqueId);
						FileConfiguration userConfig = usersConfig.loadOrCreate(uniqueId);
						userConfig.set("name", uniqueIdSection.getString("name"));
						userConfig.set("rank", uniqueIdSection.getString("rank"));
						userConfig.set("path", uniqueIdSection.getString("path"));
						userConfig.set("prestige", prestigeDataSection.getString(stringUniqueId));
						userConfig.set("rebirth", rebirthDataSection.getString(stringUniqueId));
						userConfig.save(usersConfig.getUserDirectory(uniqueId));
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return users;
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			PrisonRanksX.logSevere("Data conversion failed! Please report the error above to the developer.");
			return null;
		});
	}

	public void printInfo(UUID uniqueId) {
		User user = users.get(uniqueId);
		Common.print("UUID: " + uniqueId.toString() + " Rank: " + user.getRankName() + " Prestige: "
				+ user.getPrestigeName() + " Rebirth: " + user.getRebirthName());
	}

	public UserControllerType getType() {
		return UserControllerType.YAML;
	}

	public void setUsers(Map<UUID, User> users) {
		this.users = users;
	}

}
