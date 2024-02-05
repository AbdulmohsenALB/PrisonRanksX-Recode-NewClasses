package me.prisonranksx.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import me.prisonranksx.holders.User;

public interface UserController {

    /**
     * Saves player data to yaml files, or MySQL database.
     *
     * @param uniqueId uuid of player to save.
     * @return CompletableFuture handling data saving for player
     */
    CompletableFuture<Void> saveUser(UUID uniqueId);

    /**
     * Saves player data to yaml files, or MySQL database.
     *
     * @param uniqueId   uuid of player to save.
     * @param saveToDisk whether to save to disk or nah, this doesn't apply to
     *                   MySQL, data will always be saved to database.
     * @return CompletableFuture handling data saving for player
     */
    CompletableFuture<Void> saveUser(UUID uniqueId, boolean saveToDisk);

    /**
     * Saves player data to yaml files, or MySQL database.
     *
     * @param user user in memory to save
     * @return CompletableFuture handling data saving for player
     */
    CompletableFuture<Void> saveUser(User user);

    /**
     * Saves player data to yaml files, or MySQL database.
     *
     * @param user       user in memory to save
     * @param saveToDisk whether to save to disk or nah, this doesn't apply to
     *                   MySQL, data will always be saved to database.
     * @return CompletableFuture handling data saving for player
     */
    CompletableFuture<Void> saveUser(User user, boolean saveToDisk);

    /**
     * Saves all online users data to yaml files, or MySQL database.
     *
     * @return CompletableFuture that gets completed once all users' data have been
     * saved.
     */
    CompletableFuture<Void> saveUsers();

    /**
     * Saves all online users data to yaml files, or MySQL database.
     *
     * @param saveToDisk whether to save to disk or nah, this doesn't apply to
     *                   MySQL, data will always be saved to database.
     * @return CompletableFuture that gets completed once all users' data have been
     * saved.
     */
    CompletableFuture<Void> saveUsers(boolean saveToDisk);

    /**
     * Efficiently saves a large amount of users to disk, or MySQL database.
     * However, the efficiency is not applicable to YamlPerUserController due to the
     * way it works. It will be saved normally like {@linkplain #saveUser(User)}
     *
     * @param users to save
     * @return CompletableFuture that gets completed once all users' data have been
     * saved.
     */
    CompletableFuture<Void> saveUsers(Iterable<User> users);

    /**
     * Loads user from yaml files, or MySQL database into memory (a hashmap).
     * Generally used on async player login.
     *
     * @param uniqueId uuid of user
     * @param name     name of user
     * @return CompletableFuture that gets completed once player data is loaded.
     */
    CompletableFuture<User> loadUser(UUID uniqueId, String name);

    /**
     * Unloads user from memory (remove from hashmap). Used on player leave.
     *
     * @param uniqueId uuid to remove from memory.
     */
    void unloadUser(UUID uniqueId);

    /**
     * Checks whether user is loaded in memory or not.
     *
     * @param uniqueId uuid of user to check
     * @return true if user is loaded, false otherwise.
     */
    boolean isLoaded(UUID uniqueId);

    /**
     * Gets user from memory.
     *
     * @param uniqueId uuid of user
     * @return User if online or loaded, null otherwise.
     */
    User getUser(UUID uniqueId);

    /**
     * Moves player data to another type of data storage.
     *
     * @param type type of data storage to move data to.
     * @return CompletableFuture that gets completed with all loaded users once
     * conversion is done.
     */
    CompletableFuture<Map<UUID, User>> convert(UserControllerType type);

    /**
     * Changes currently loaded users to given hashmap.
     *
     * @param users hashmap of users to change to.
     */
    void setUsers(Map<UUID, User> users);

    /**
     * Gets type of user controller currently being used.
     *
     * @return type of this user controller.
     */
    UserControllerType getType();

}
