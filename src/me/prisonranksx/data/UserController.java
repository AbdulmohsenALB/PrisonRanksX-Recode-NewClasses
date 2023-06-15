package me.prisonranksx.data;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import me.prisonranksx.holders.User;

public interface UserController {

	public CompletableFuture<Void> saveUser(UUID uniqueId);

	public CompletableFuture<Void> saveUser(UUID uniqueId, boolean saveToDisk);

	public CompletableFuture<Void> saveUser(User user);

	public CompletableFuture<Void> saveUser(User user, boolean saveToDisk);

	public CompletableFuture<Void> saveUsers();

	public CompletableFuture<Void> saveUsers(boolean saveToDisk);

	public CompletableFuture<User> loadUser(UUID uniqueId, String name);

	public void unloadUser(UUID uniqueId);

	public boolean isLoaded(UUID uniqueId);

	public User getUser(UUID uniqueId);

}
