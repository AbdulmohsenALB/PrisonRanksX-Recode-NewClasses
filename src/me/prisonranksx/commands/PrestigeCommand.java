package me.prisonranksx.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.settings.Messages;

public class PrestigeCommand extends PluginCommand {

	private PrisonRanksX plugin;

	public static boolean isEnabled() {
		return CommandSetting.getSetting("prestige", "enable");
	}

	public PrestigeCommand(PrisonRanksX plugin) {
		super(CommandSetting.getStringSetting("prestige", "name", "prestige"));
		this.plugin = plugin;
		setLabel(getCommandSection().getString("label", "prestige"));
		setDescription(getCommandSection().getString("description"));
		setUsage(getCommandSection().getString("usage"));
		setPermission(getCommandSection().getString("permission"));
		setPermissionMessage(getCommandSection().getString("permission-message"));
		setAliases(getCommandSection().getStringList("aliases"));
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!testPermission(sender)) return true;
		if (!(sender instanceof Player)) {
			Messages.sendMessage(sender, Messages.getPlayerOnlyCommand());
			return true;
		}
		Player p = (Player) sender;
		if (args.length == 0) {
			if (plugin.getGlobalSettings().isWorldIncluded(p.getWorld())) return true;
			plugin.getPrestigeExecutor().prestige(p);
		} else if (args.length == 1) {
			plugin.getPrestigeExecutor().toggleAutoPrestige(p);
		}
		return true;
	}

}
