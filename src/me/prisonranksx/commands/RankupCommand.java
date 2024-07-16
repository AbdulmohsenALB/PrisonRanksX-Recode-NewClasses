package me.prisonranksx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.settings.Messages;

public class RankupCommand extends PluginCommand {

	private PrisonRanksX plugin;

	public static boolean isEnabled() {
		return CommandSetting.getSetting("rankup", "enable");
	}

	public RankupCommand(PrisonRanksX plugin) {
		super(CommandSetting.getStringSetting("rankup", "name", "rankup"));
		this.plugin = plugin;
		setLabel(getCommandSection().getString("label", "rankup"));
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
			rankup(p);
		} else if (args.length == 1) {
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				Messages.sendMessage(p, Messages.getUnknownPlayer(), s -> s.replace("%player%", args[0]));
				return true;
			}
			if (!p.hasPermission(getPermission() + ".other")) {
				Messages.sendMessage(p, Messages.getRankupOtherNoPermission(), s -> s.replace("%player%", args[0]));
				return true;
			}
			rankupByOther(p, target);
		}
		return true;
	}

	public void rankup(Player p) {
		if (plugin.getGlobalSettings().isWorldIncluded(p.getWorld())) return;
		plugin.getRankupExecutor().rankup(p);
	}

	public void rankupByOther(Player p, Player target) {
		plugin.getRankupExecutor().rankup(p, target);
	}

}
