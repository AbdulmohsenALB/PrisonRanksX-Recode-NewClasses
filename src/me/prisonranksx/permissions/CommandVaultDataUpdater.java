package me.prisonranksx.permissions;

import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.components.CommandsComponent;
import me.prisonranksx.managers.PermissionsManager;

public class CommandVaultDataUpdater implements VaultDataUpdater {

	private PrisonRanksX plugin;
	private String commandLine;

	public CommandVaultDataUpdater(PrisonRanksX plugin) {
		this.plugin = plugin;
		this.commandLine = plugin.getGlobalSettings().getVaultGroupsPlugin();
	}

	@Override
	public void set(Player player, String group, String oldGroup) {
		plugin.newSharedChain("Command").sync(() -> {
			CommandsComponent.dispatchConsoleCommand(player, commandLine.replace("%rank%", group));
		}).execute();
	}

	@Override
	public void set(Player player, String group) {
		CommandsComponent.dispatchConsoleCommand(player, commandLine.replace("%rank%", group));
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
	public void update(Player player) {}

}
