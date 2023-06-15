package me.prisonranksx.permissions;

import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.holders.User;
import me.prisonranksx.hooks.GroupManagerHook;
import me.prisonranksx.managers.PermissionsManager;
import me.prisonranksx.reflections.UniqueId;

public class GMVaultDataUpdater implements VaultDataUpdater {

	private PrisonRanksX plugin;
	private GroupManagerHook groupManagerHook;

	public GMVaultDataUpdater(PrisonRanksX plugin) {
		this.plugin = plugin;
		this.groupManagerHook = new GroupManagerHook(plugin);
	}

	@Override
	public void set(Player player, String group, String oldGroup) {
		groupManagerHook.setGroup(player, group);
	}

	@Override
	public void set(Player player, String group) {
		groupManagerHook.setGroup(player, group);
	}

	@Override
	public void remove(Player player) {
		PermissionsManager.getPermissionService().playerRemoveGroup(player, get(player));
	}

	@Override
	public void remove(Player player, String group) {
		PermissionsManager.getPermissionService().playerRemoveGroup(player, group);
	}

	@Override
	public String get(Player player) {
		return groupManagerHook.getGroup(player);
	}

	@Override
	public void update(Player player) {
		String group = groupManagerHook.getGroup(player);
		User user = plugin.getUserController().getUser(UniqueId.getUUID(player));
		String playerRankName = user.getRankName();
		if (!group.equalsIgnoreCase(playerRankName)) user.setRankName(group);
	}

}
