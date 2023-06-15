package me.prisonranksx.permissions;

import java.util.UUID;

import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.hooks.EZLuckPerms;
import me.prisonranksx.reflections.UniqueId;
import net.luckperms.api.model.user.User;

public class LPVaultDataUpdater implements VaultDataUpdater {

	private PrisonRanksX plugin;

	public LPVaultDataUpdater(PrisonRanksX plugin) {
		this.plugin = plugin;
		EZLuckPerms.getLuckPerms();
	}

	@Override
	public void set(Player player, String group, String oldGroup) {
		plugin.newSharedChain("LuckPerms").async(() -> {
			EZLuckPerms.setPlayerGroup(player.getUniqueId(), EZLuckPerms.getGroup(group), true);
		}).execute();
	}

	@Override
	public void set(Player player, String group) {
		EZLuckPerms.setPlayerGroup(player.getUniqueId(), EZLuckPerms.getGroup(group), true);
	}

	@Override
	public void remove(Player player) {
		EZLuckPerms.deletePlayerGroups(player.getUniqueId(), true);
	}

	@Override
	public void remove(Player player, String group) {
		EZLuckPerms.deletePlayerGroup(player.getUniqueId(), group);
	}

	@Override
	public String get(Player player) {
		return EZLuckPerms.getPlayerGroup(player.getUniqueId()).getName();
	}

	@Override
	public void update(Player player) {
		plugin.newSharedChain("LuckPerms").async(() -> {
			UUID uuid = player.getUniqueId();
			User lpUser = EZLuckPerms.getUser(uuid);
			me.prisonranksx.holders.User user = plugin.getUserController().getUser(UniqueId.getUUID(player));
			String playerRankName = user.getRankName();
			if (!lpUser.getPrimaryGroup().equalsIgnoreCase(playerRankName)) user.setRankName(lpUser.getPrimaryGroup());
		}).execute();
	}

}
