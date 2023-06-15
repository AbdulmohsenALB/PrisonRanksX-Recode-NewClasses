package me.prisonranksx.permissions;

import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.PermissionsManager;
import me.prisonranksx.reflections.UniqueId;

public class DefaultVaultDataUpdater implements VaultDataUpdater {

	private PrisonRanksX plugin;

	public DefaultVaultDataUpdater(PrisonRanksX plugin) {
		this.plugin = plugin;
	}

	@Override
	public void set(Player player, String group, String oldGroup) {
		if (PermissionsManager.getPermissionService().playerInGroup(player, oldGroup))
			PermissionsManager.getPermissionService().playerRemoveGroup(player, oldGroup);
		PermissionsManager.getPermissionService().playerAddGroup(player, group);
	}

	@Override
	public void set(Player player, String group) {
		PermissionsManager.getPermissionService()
				.playerRemoveGroup(player, PermissionsManager.getPermissionService().getPrimaryGroup(player));
		PermissionsManager.getPermissionService().playerAddGroup(player, group);
	}

	@Override
	public void remove(Player player) {
		PermissionsManager.getPermissionService()
				.playerRemoveGroup(player, PermissionsManager.getPermissionService().getPrimaryGroup(player));
	}

	@Override
	public void remove(Player player, String group) {
		PermissionsManager.getPermissionService().playerRemoveGroup(player, group);
	}

	@Override
	public String get(Player player) {
		return PermissionsManager.getPermissionService().getPrimaryGroup(player);
	}

	@Override
	public void update(Player player) {
		String group = PermissionsManager.getPermissionService().getPrimaryGroup(player);
		User user = plugin.getUserController().getUser(UniqueId.getUUID(player));
		String playerRankName = user.getRankName();
		if (!group.equalsIgnoreCase(playerRankName)) user.setRankName(group);
	}

}
