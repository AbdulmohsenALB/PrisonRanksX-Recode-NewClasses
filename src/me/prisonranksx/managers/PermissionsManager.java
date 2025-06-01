package me.prisonranksx.managers;

import me.prisonranksx.common.StaticCache;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PermissionsManager extends StaticCache {

	private static final Permission PERMISSION = setupPermissions();

	private static Permission setupPermissions() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) return null;
		RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
		return rsp == null ? null : rsp.getProvider();
	}

	@Nullable
	public static Permission getPermissionService() {
		return PERMISSION;
	}

	public static boolean givePermission(OfflinePlayer player, String permission) {
		return givePermission(player, permission, null);
	}

	public static boolean givePermission(OfflinePlayer player, String permission, @Nullable String worldName) {
		return PERMISSION.playerAdd(worldName, player, permission);
	}

	public static boolean removePermission(OfflinePlayer player, String permission) {
		return PERMISSION.playerRemove(null, player, permission);
	}

	public static boolean removePermission(OfflinePlayer player, String permission, @Nullable String worldName) {
		return PERMISSION.playerRemove(worldName, player, permission);
	}

	public static CompletableFuture<Void> givePermissions(OfflinePlayer player, Iterable<String> permissions) {
		return CompletableFuture.runAsync(() -> permissions.forEach(permission -> givePermission(player, permission)));
	}

	public static CompletableFuture<Void> givePermissions(OfflinePlayer player, Iterable<String> permissions,
														  @Nullable String worldName) {
		return CompletableFuture
				.runAsync(() -> permissions.forEach(permission -> givePermission(player, permission, worldName)));
	}

	public static CompletableFuture<Void> givePermissions(OfflinePlayer player,
														  Map<String, Set<String>> permissionsMap) {
		return CompletableFuture
				.runAsync(() -> permissionsMap.forEach((worldName, permissions) -> permissionsMap.get(worldName)
						.forEach(permission -> givePermission(player, permission, worldName))));
	}

	public static CompletableFuture<Void> removePermissions(OfflinePlayer player, Iterable<String> permissions) {
		return CompletableFuture
				.runAsync(() -> permissions.forEach(permission -> removePermission(player, permission)));
	}

	public static CompletableFuture<Void> removePermissions(OfflinePlayer player, Iterable<String> permissions,
															@Nullable String worldName) {
		return CompletableFuture
				.runAsync(() -> permissions.forEach(permission -> removePermission(player, permission, worldName)));
	}

	/**
	 * @param player
	 * @param permissionsMap world name to permissions collection map
	 * @return
	 */
	public static CompletableFuture<Void> removePermissions(OfflinePlayer player,
															Map<String, Set<String>> permissionsMap) {
		return CompletableFuture
				.runAsync(() -> permissionsMap.forEach((worldName, permissions) -> permissionsMap.get(worldName)
						.forEach(permission -> removePermission(player, permission, worldName))));
	}

}
