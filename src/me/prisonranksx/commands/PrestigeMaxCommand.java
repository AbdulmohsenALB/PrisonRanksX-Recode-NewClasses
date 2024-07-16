package me.prisonranksx.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.executors.PrestigeExecutor;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;

public class PrestigeMaxCommand extends PluginCommand {

	private PrisonRanksX plugin;

	public static boolean isEnabled() {
		return CommandSetting.getSetting("prestigemax", "enable");
	}

	public PrestigeMaxCommand(PrisonRanksX plugin) {
		super(CommandSetting.getStringSetting("prestigemax", "name", "prestigemax"));
		this.plugin = plugin;
		setLabel(getCommandSection().getString("label", "prestigemax"));
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
			Messages.sendMessage(sender, "Do you mean?: /forceprestige <player>");
			return true;
		}
		Player p = (Player) sender;
		if (args.length == 0) {
			maxPrestige(p);
		} else if (args.length == 1) {
			String prestigeName = PrestigeStorage.matchPrestigeName(args[0]);
			if (prestigeName == null) {
				Messages.sendMessage(sender, Messages.getUnknownPrestige(), s -> s.replace("%prestige%", args[0]));
				return true;
			}
			if (PrestigeExecutor.isMaxPrestiging(p))
				plugin.getPrestigeExecutor().breakMaxPrestige(UniqueId.getUUID(p));
			else
				plugin.getPrestigeExecutor().maxPrestige(p);
		}
		return true;
	}

	public void maxPrestige(Player p) {
		if (plugin.getGlobalSettings().isWorldIncluded(p.getWorld())) return;
		if (PrestigeExecutor.isMaxPrestiging(p))
			plugin.getPrestigeExecutor().breakMaxPrestige(UniqueId.getUUID(p));
		else
			plugin.getPrestigeExecutor().maxPrestige(p);
	}

}
