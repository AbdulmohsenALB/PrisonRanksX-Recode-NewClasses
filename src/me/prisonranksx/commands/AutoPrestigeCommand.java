package me.prisonranksx.commands;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.settings.Messages;

public class AutoPrestigeCommand extends PluginCommand {

	private PrisonRanksX plugin;
	private final Set<String> enableSubCommands = Sets.newHashSet("enable", "on", "true", "yes");
	private final Set<String> disableSubCommands = Sets.newHashSet("disable", "off", "false", "no");

	public static boolean isEnabled() {
		return CommandSetting.getSetting("autoprestige", "enable");
	}

	public AutoPrestigeCommand(PrisonRanksX plugin) {
		super(CommandSetting.getStringSetting("autoprestige", "name", "autoprestige"));
		this.plugin = plugin;
		setLabel(getCommandSection().getString("label", "autoprestige"));
		setDescription(getCommandSection().getString("description"));
		setUsage(getCommandSection().getString("usage"));
		setPermission(getCommandSection().getString("permission"));
		setPermissionMessage(getCommandSection().getString("permission-message"));
		setAliases(getCommandSection().getStringList("aliases"));
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!testPermission(sender)) return true;
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				Messages.sendMessage(sender, Messages.getPlayerOnlyCommand());
				return true;
			}
			Player p = (Player) sender;
			if (plugin.getGlobalSettings().isWorldIncluded(p.getWorld())) return true;
			boolean state = plugin.getPrestigeExecutor().toggleAutoPrestige(p);
			Messages.sendMessage(sender,
					state ? Messages.getAutoPrestigeEnabled() : Messages.getAutoPrestigeDisabled());
		} else if (args.length == 1) {
			if (enableSubCommands.contains(args[0].toLowerCase())) {
				if (!(sender instanceof Player)) {
					Messages.sendMessage(sender, Messages.getPlayerOnlyCommand());
					return true;
				}
				Player p = (Player) sender;
				plugin.getPrestigeExecutor().toggleAutoPrestige(p, true);
				Messages.sendMessage(sender, Messages.getAutoPrestigeEnabled());
				return true;
			} else if (disableSubCommands.contains(args[0].toLowerCase())) {
				if (!(sender instanceof Player)) {
					Messages.sendMessage(sender, Messages.getPlayerOnlyCommand());
					return true;
				}
				Player p = (Player) sender;
				plugin.getPrestigeExecutor().toggleAutoPrestige(p, false);
				Messages.sendMessage(sender, Messages.getAutoPrestigeDisabled());
				return true;
			}
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				Messages.sendMessage(sender, Messages.getUnknownPlayer(), s -> s.replace("%player%", args[0]));
				return true;
			}
		}
		return true;
	}

}
