package me.prisonranksx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.lists.RanksGUIList;
import me.prisonranksx.lists.RanksTextList;
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
			if (plugin.getGlobalSettings().isWorldIncluded(p.getWorld())) return true;
			plugin.getRankupExecutor().rankup(p);
		} else if (args.length == 1) {
			if (args[0].equals("gui")) {
				RanksGUIList gl = new RanksGUIList(plugin);
				gl.openGUI(p);
				return true;
			} else if (args[0].equals("max")) {
				plugin.getRankupExecutor().maxRankup(p);
				return true;
			} else if (args[0].startsWith("list")) {
				RanksTextList rtl = new RanksTextList(plugin);
				rtl.setup();
				rtl.sendPagedList(sender, args[0].replace("list", ""));
				return true;
			}
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				Messages.sendMessage(p, Messages.getUnknownPlayer(), s -> s.replace("%player%", args[0]));
				return true;
			}
			if (!p.hasPermission(getPermission() + ".other")) {
				Messages.sendMessage(p, Messages.getRankupOtherNoPermission(), s -> s.replace("%player%", args[0]));
				return true;
			}
			plugin.getRankupExecutor().rankup(p, target);
		}
		return true;
	}

}
