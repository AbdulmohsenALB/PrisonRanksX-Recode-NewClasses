package me.prisonranksx.commands;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.managers.StringManager;

public class RanksCommand extends PluginCommand {

	private PrisonRanksX plugin;

	public static boolean isEnabled() {
		return CommandSetting.getSetting("ranks", "enable");
	}

	public RanksCommand(PrisonRanksX plugin) {
		super(CommandSetting.getStringSetting("ranks", "name", "ranks"));
		this.plugin = plugin;
		setLabel(getCommandSection().getString("label", "ranks"));
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
			RankStorage.PATHS.keySet().forEach(pathName -> {
				Set<Rank> ranks = Sets.newLinkedHashSet(RankStorage.getPathRanks(pathName));
				ranks.forEach(rank -> {
					sender.sendMessage(
							StringManager.parseColors("&7Path: &f" + pathName + " &cRank: &f" + rank.getName()));
				});
			});
			return true;
		}
		if (plugin.getGlobalSettings().isGuiRankList()) {
			if (args.length == 0)
				plugin.getRanksGUIList().openGUI((Player) sender);
			else
				plugin.getRanksGUIList().getPlayerPagedGUI().openInventory((Player) sender, Integer.parseInt(args[0]));
			return true;
		}
		plugin.getRanksTextList().sendPagedList(sender, args.length == 0 ? "1" : args[0]);
		return true;
	}
}
