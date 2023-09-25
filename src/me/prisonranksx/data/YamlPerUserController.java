package me.prisonranksx.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.configuration.file.FileConfiguration;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.DataConfig;
import me.prisonranksx.holders.User;

public class YamlPerUserController implements UserController {

	private Map<UUID, User> users = new HashMap<>();
	// private WorkloadTask workloadTask;
	private DataConfig dataConfig;
	private PrisonRanksX plugin;

	public YamlPerUserController(PrisonRanksX plugin) {
		this.plugin = plugin;
		this.dataConfig = DataConfig.createDataConfig(plugin, "users");
		dataConfig.registerFirstTimeData("name", "%name%");
		// workloadTask = BogusAsync.prepareTask().start(true);
		users.clear();
	}

	@Override
	public CompletableFuture<Void> saveUser(UUID uniqueId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> saveUser(UUID uniqueId, boolean saveToDisk) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> saveUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> saveUser(User user, boolean saveToDisk) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> saveUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> saveUsers(boolean saveToDisk) {
		return null;
	}

	@Override
	public CompletableFuture<User> loadUser(UUID uniqueId, String name) {
		return CompletableFuture.supplyAsync(() -> {
			User user = new User(uniqueId, name);
			FileConfiguration userConfig = dataConfig.initUserConfig(uniqueId, name);
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

}
