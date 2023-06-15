package me.prisonranksx.permissions;

import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;

public class PlayerGroupUpdater implements VaultDataUpdater {

	private PrisonRanksX plugin;
	private VaultDataUpdater vaultDataUpdater;

	public PlayerGroupUpdater(PrisonRanksX plugin) {
		this.plugin = plugin;
		load();
	}

	public void load() {
		String vaultPlugin = null;
		if (plugin.getGlobalSettings().isVaultGroups()) vaultPlugin = plugin.getGlobalSettings().getVaultGroupsPlugin();

		if (vaultPlugin != null) {
			if (plugin.getGlobalSettings().isLuckPermsLoaded() && vaultPlugin.equalsIgnoreCase("luckperms")) {
				vaultDataUpdater = new LPVaultDataUpdater(plugin);
			} else if (plugin.getGlobalSettings().isGroupManagerLoaded()
					&& vaultPlugin.equalsIgnoreCase("groupmanager")) {
				vaultDataUpdater = new GMVaultDataUpdater(plugin);
			} else if (plugin.getGlobalSettings().isPermissionsEXLoaded()
					&& vaultPlugin.equalsIgnoreCase("permissionsex")) {
				vaultDataUpdater = new PEXVaultDataUpdater(plugin);
			} else if (vaultPlugin.equalsIgnoreCase("vault")) {
				vaultDataUpdater = new DefaultVaultDataUpdater(plugin);
			} else {
				vaultDataUpdater = new CommandVaultDataUpdater(plugin);
			}
		}
	}

	public VaultDataUpdater getUpdater() {
		return vaultDataUpdater;
	}

	@Override
	public void set(Player player, String group) {
		vaultDataUpdater.set(player, group);
	}

	@Override
	public void remove(Player player) {
		vaultDataUpdater.remove(player);
	}

	@Override
	public void remove(Player player, String group) {
		vaultDataUpdater.remove(player, group);
	}

	@Override
	public String get(Player player) {
		return vaultDataUpdater.get(player);
	}

	@Override
	public void set(Player player, String group, String oldGroup) {
		vaultDataUpdater.set(player, group, oldGroup);
	}

	@Override
	public void update(Player player) {
		vaultDataUpdater.update(player);
	}

}
