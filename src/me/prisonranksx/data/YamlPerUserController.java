package me.prisonranksx.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.ConfigCreator;
import me.prisonranksx.bukkitutils.StupidMySQL;
import me.prisonranksx.bukkitutils.UserConfig;
import me.prisonranksx.bukkitutils.bukkittickbalancer.BukkitTickBalancer;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.MySQLManager;

public class YamlPerUserController implements UserController {

	private Map<UUID, User> users = new HashMap<>();
	private UserConfig userConfig;
	private PrisonRanksX plugin;

	public YamlPerUserController(PrisonRanksX plugin) {
		this.plugin = plugin;
		this.userConfig = UserConfig.create(plugin, "users");
		users.clear();
	}

	@Override
	public CompletableFuture<Void> saveUser(UUID uniqueId) {
		return saveUser(getUser(uniqueId));
	}

	@Override
	public CompletableFuture<Void> saveUser(UUID uniqueId, boolean saveToDisk) {
		return saveUser(getUser(uniqueId), saveToDisk);
	}

	@Override
	public CompletableFuture<Void> saveUser(User user) {
		return saveUser(user, false);
	}

	@Override
	public CompletableFuture<Void> saveUser(User user, boolean saveToDisk) {
		CompletableFuture<Void> saveUserFuture = new CompletableFuture<>();
		BukkitTickBalancer.async(() -> {
			FileConfiguration userConfig = this.userConfig.loadOrCreate(user.getUniqueId());
			userConfig.set("name", user.getName());
			userConfig.set("rank", user.getRankName());
			userConfig.set("path", user.getPathName());
			userConfig.set("prestige", user.getPrestigeName());
			userConfig.set("rebirth", user.getRebirthName());
			if (saveToDisk) {
				try {
					userConfig.save(this.userConfig.getUserDirectory(user.getUniqueId()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
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
			if (saveToDisk)
				for (User user : users.values()) {
					saveUser(user, true);
				}
			else
				for (User user : users.values()) {
					saveUser(user);
				}
		});
	}

	@Override
	public CompletableFuture<Void> saveUsers(Iterable<User> users) {
		return CompletableFuture.runAsync(() -> {
			for (User user : users) saveUser(user, true);
		});
	}

	@Override
	public CompletableFuture<User> loadUser(UUID uniqueId, String name) {
		return CompletableFuture.supplyAsync(() -> {
			User user = new User(uniqueId, name);
			FileConfiguration userConfig = this.userConfig.loadOrCreate(uniqueId);
			if (plugin.getGlobalSettings().isRankEnabled()) {
				String pathName = Optional.ofNullable(userConfig.getString("path"))
						.orElse(RankStorage.getDefaultPath());
				String rankName = Optional.ofNullable(userConfig.getString("rank"))
						.orElse(RankStorage.getFirstRank(pathName));
				user.setRankAndPathName(rankName, pathName);
			}
			if (plugin.getGlobalSettings().isPrestigeEnabled()) user.setPrestigeName(userConfig.getString("prestige"));
			if (plugin.getGlobalSettings().isRebirthEnabled()) user.setRebirthName(userConfig.getString("rebirth"));
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
				File usersFile = new File(userConfig.getDirectory());
				for (String stringUniqueId : usersFile.list()) {
					stringUniqueId = stringUniqueId.replace(".yml", "");
					FileConfiguration userConfig = this.userConfig.loadOrCreate(UUID.fromString(stringUniqueId));
					String name = userConfig.getString("name");
					String rank = userConfig.getString("rank");
					String path = userConfig.getString("path");
					String prestige = userConfig.getString("prestige");
					String rebirth = userConfig.getString("rebirth");
					stupidMySQL.addToPrepared(stringUniqueId, name, rank, path, prestige, rebirth, 0);
				}
				stupidMySQL.execute();
			} else if (type == UserControllerType.YAML) {
				File usersFile = new File(userConfig.getDirectory());
				ConfigurationSection rankDataSection = ConfigManager.getRankDataConfig()
						.getConfigurationSection("players");
				ConfigurationSection prestigeDataSection = ConfigManager.getPrestigeDataConfig()
						.getConfigurationSection("players");
				ConfigurationSection rebirthDataSection = ConfigManager.getRebirthDataConfig()
						.getConfigurationSection("players");
				for (String stringUniqueId : usersFile.list()) {
					stringUniqueId = stringUniqueId.replace(".yml", "");
					ConfigurationSection uniqueIdSection = rankDataSection.getConfigurationSection(stringUniqueId);
					if (uniqueIdSection == null) uniqueIdSection = rankDataSection.createSection(stringUniqueId);
					UUID uniqueId = UUID.fromString(stringUniqueId);
					FileConfiguration userConfig = this.userConfig.loadOrCreate(uniqueId);
					uniqueIdSection.set("name", userConfig.getString("name"));
					uniqueIdSection.set("rank", userConfig.getString("rank"));
					uniqueIdSection.set("path", userConfig.getString("path"));
					prestigeDataSection.set(stringUniqueId, userConfig.getString("prestige"));
					rebirthDataSection.set(stringUniqueId, userConfig.getString("rebirth"));
				}
				ConfigCreator.saveConfigs("rankdata.yml", "prestigedata.yml", "rebirthdata.yml");
			}
			return users;
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			PrisonRanksX.logSevere("Data conversion failed! Please report the error above to the developer.");
			return null;
		});
	}

	public UserControllerType getType() {
		return UserControllerType.YAML_PER_USER;
	}

	public void setUsers(Map<UUID, User> users) {
		this.users = users;
	}

}
