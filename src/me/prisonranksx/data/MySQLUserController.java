package me.prisonranksx.data;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.ConfigCreator;
import me.prisonranksx.bukkitutils.StupidMySQL;
import me.prisonranksx.bukkitutils.UserConfig;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.MySQLManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLUserController implements UserController {

    private PrisonRanksX plugin;
    private Map<UUID, User> users;
    private StupidMySQL stupidMySQL;

    public MySQLUserController(PrisonRanksX plugin) {
        this.plugin = plugin;
        this.stupidMySQL = StupidMySQL.use(MySQLManager.getConnection(), MySQLManager.getDatabase(),
                MySQLManager.getTable());
        users = new HashMap<>();
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
        return CompletableFuture.runAsync(() -> {
            stupidMySQL
                    .setOrInsert("uuid", user.getUniqueId().toString(), "name", user.getName(), "rank",
                            user.getRankName(), "path", user.getPathName(), "prestige", user.getPrestigeName(),
                            "rebirth", user.getRebirthName(), "score", "0")
                    .execute();
        }).exceptionally(th -> {
            th.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> saveUsers() {
        return saveUsers(false);
    }

    @Override
    public CompletableFuture<Void> saveUsers(boolean saveToDisk) {
        return CompletableFuture.runAsync(() -> {
            stupidMySQL.prepareSet("uuid", "name", "rank", "path", "prestige", "rebirth", "score");
            for (User user : users.values()) {
                stupidMySQL.addToPrepared(user.getName(), user.getRankName(), user.getPathName(),
                        user.getPrestigeName(), user.getRebirthName(), 0, user.getUniqueId().toString());
            }
            stupidMySQL.execute();
        }).exceptionally(th -> {
            th.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> saveUsers(Iterable<User> users) {
        return CompletableFuture.runAsync(() -> {
            stupidMySQL.prepareSet("uuid", "name", "rank", "path", "prestige", "rebirth", "score");
            for (User user : users) {
                stupidMySQL.addToPrepared(user.getName(), user.getRankName(), user.getPathName(),
                        user.getPrestigeName(), user.getRebirthName(), 0, user.getUniqueId().toString());
            }
            stupidMySQL.execute();
        }).exceptionally(th -> {
            th.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<User> loadUser(UUID uniqueId, String name) {
        return CompletableFuture.supplyAsync(() -> {
            User user = new User(uniqueId, name);
            ResultSet resultSet = stupidMySQL.get("uuid", uniqueId.toString());
            try {
                if (resultSet != null && resultSet.next()) {
                    String rank = resultSet.getString("rank");
                    if (rank == null && plugin.isRankEnabled()) rank = RankStorage.getFirstRankName();
                    String path = resultSet.getString("path");
                    if (path == null && plugin.isRankEnabled()) path = RankStorage.getDefaultPath();
                    String prestige = resultSet.getString("prestige");
                    String rebirth = resultSet.getString("rebirth");
                    user.setRankName(rank);
                    user.setPathName(path);
                    user.setPrestigeName(plugin.isPrestigeEnabled() ? prestige : null);
                    user.setRebirthName(plugin.isRebirthEnabled() ? rebirth : null);
                } else {
                    if (plugin.isRankEnabled()) {
                        user.setRankName(RankStorage.getFirstRankName());
                        user.setPathName(RankStorage.getDefaultPath());
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            users.put(uniqueId, user);
            return user;
        }).exceptionally(th -> {
            th.printStackTrace();
            return null;
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
            if (type == UserControllerType.YAML) {
                ResultSet resultSet = stupidMySQL.get();
                while (true) {
                    try {
                        if (!resultSet.next()) break;
                        String stringUniqueId = resultSet.getString("uuid");
                        String id = "players." + stringUniqueId;
                        ConfigManager.getRankDataConfig().set(id + ".name", resultSet.getString("name"));
                        ConfigManager.getRankDataConfig().set(id + ".rank", resultSet.getString("rank"));
                        ConfigManager.getRankDataConfig().set(id + ".path", resultSet.getString("path"));
                        ConfigManager.getPrestigeDataConfig().set(id, resultSet.getString("prestige"));
                        ConfigManager.getRebirthDataConfig().set(id, resultSet.getString("rebirth"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                ConfigCreator.saveConfigs("rankdata.yml", "prestigedata.yml", "rebirthdata.yml");
            } else if (type == UserControllerType.YAML_PER_USER) {
                ResultSet resultSet = stupidMySQL.get();
                UserConfig usersConfig = UserConfig.create(plugin, "users");
                while (true) {
                    try {
                        if (!resultSet.next()) break;
                        String stringUniqueId = resultSet.getString("uuid");
                        UUID uniqueId = UUID.fromString(stringUniqueId);
                        FileConfiguration userConfig = usersConfig.loadOrCreate(uniqueId);
                        userConfig.set("name", resultSet.getString("name"));
                        userConfig.set("rank", resultSet.getString("rank"));
                        userConfig.set("path", resultSet.getString("path"));
                        userConfig.set("prestige", resultSet.getString("prestige"));
                        userConfig.set("rebirth", resultSet.getString("rebirth"));
                        userConfig.save(usersConfig.getUserDirectory(uniqueId));
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return users;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            PrisonRanksX.logSevere("Data conversion failed! Please report the error above to the developer.");
            return null;
        });
    }

    public UserControllerType getType() {
        return UserControllerType.MYSQL;
    }

    @Override
    public void unloadUsers() {
        users.clear();
    }

    public void setUsers(Map<UUID, User> users) {
        this.users = users;
    }

}
