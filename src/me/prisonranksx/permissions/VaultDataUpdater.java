package me.prisonranksx.permissions;

import org.bukkit.entity.Player;

/**
 * For updating player's groups in permission plugins or vault if no permission plugin is used
 */
public interface VaultDataUpdater {

	void set(Player player, String group);

	void remove(Player player);

	void remove(Player player, String group);

	String get(Player player);

	void set(Player player, String group, String oldGroup);

	void update(Player player);

}
