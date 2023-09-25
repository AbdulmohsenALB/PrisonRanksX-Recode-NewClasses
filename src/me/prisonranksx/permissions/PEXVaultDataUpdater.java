package me.prisonranksx.permissions;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.holders.User;
import me.prisonranksx.reflections.UniqueId;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class PEXVaultDataUpdater implements VaultDataUpdater {

	private PrisonRanksX plugin;

	public PEXVaultDataUpdater(PrisonRanksX plugin) {
		this.plugin = plugin;
		PermissionsEx.getPermissionManager();
	}

	@Override
	public void set(Player player, String group, String oldGroup) {
		plugin.newSharedChain("PermissionsEX").sync(() -> {
			PermissionUser user = PermissionsEx.getUser(player);
			if (user.inGroup(oldGroup)) user.removeGroup(oldGroup);
			user.addGroup(group);
		}).execute();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void set(Player player, String group) {
		PermissionUser user = PermissionsEx.getUser(player);
		String[] groups = new String[] { group };
		user.setGroups(groups);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void remove(Player player) {
		PermissionUser user = PermissionsEx.getUser(player);
		user.removeGroup(user.getGroupNames()[0]);
	}

	@Override
	public void remove(Player player, String group) {
		PermissionUser user = PermissionsEx.getUser(player);
		user.removeGroup(group);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String get(Player player) {
		PermissionUser user = PermissionsEx.getUser(player);
		return user.getGroupNames()[0];
	}

	@SuppressWarnings("deprecation")
	@Override
	public void update(Player player) {
		plugin.newSharedChain("PermissionsEX").sync(() -> {
			String group = PermissionsEx.getUser(player).getGroups()[0].getName();
			User user = plugin.getUserController().getUser(UniqueId.getUUID(player));
			String playerRankName = user.getRankName();
			if (!group.equalsIgnoreCase(playerRankName)) user.setRankName(group);
		}).execute();
	}

}
