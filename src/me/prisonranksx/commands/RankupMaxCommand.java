package me.prisonranksx.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.executors.RankupExecutor;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;

public class RankupMaxCommand extends PluginCommand {

	private PrisonRanksX plugin;

	public static boolean isEnabled() {
		return CommandSetting.getSetting("rankupmax", "enable");
	}

	public RankupMaxCommand(PrisonRanksX plugin) {
		super(CommandSetting.getStringSetting("rankupmax", "name", "rankupmax"));
		this.plugin = plugin;
		setLabel(getCommandSection().getString("label", "rankupmax"));
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
			Messages.sendMessage(sender, "Do you mean?: /forcerankup <player>");
			return true;
		}
		Player p = (Player) sender;
		if (args.length == 0) {
			if (plugin.getGlobalSettings().isWorldIncluded(p.getWorld())) return true;
			if (!RankupExecutor.isMaxRankup(p))
				plugin.getRankupExecutor().maxRankup(p);
			else
				plugin.getRankupExecutor().breakMaxRankup(UniqueId.getUUID(p));
		} else if (args.length == 1) {
			String rankName = RankStorage.findRankName(args[0], PRXAPI.getPlayerPathOrDefault(p));
			if (rankName == null) {
				Messages.sendMessage(sender, Messages.getUnknownRank(), s -> s.replace("%rank%", args[0]));
				return true;
			}
			if (!RankupExecutor.isMaxRankup(p))
				plugin.getRankupExecutor().maxRankup(p, rankName);
			else
				plugin.getRankupExecutor().breakMaxRankup(UniqueId.getUUID(p));
		}
		return true;
	}

}
