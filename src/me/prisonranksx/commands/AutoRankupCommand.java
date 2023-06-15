package me.prisonranksx.commands;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.settings.Messages;

public class AutoRankupCommand extends PluginCommand {

	private PrisonRanksX plugin;
	private final Set<String> enableSubCommands = Sets.newHashSet("enable", "on", "true", "yes");
	private final Set<String> disableSubCommands = Sets.newHashSet("disable", "off", "false", "no");

	public static boolean isEnabled() {
		return CommandSetting.getSetting("autorankup", "enable");
	}

	public AutoRankupCommand(PrisonRanksX plugin) {
		super(CommandSetting.getStringSetting("autorankup", "name", "autorankup"));
		this.plugin = plugin;
		setLabel(getCommandSection().getString("label", "autorankup"));
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
			boolean state = plugin.getRankupExecutor().toggleAutoRankup(p);
			Messages.sendMessage(sender, state ? Messages.getAutoRankupEnabled() : Messages.getAutoRankupDisabled());
		} else if (args.length == 1) {
			if (enableSubCommands.contains(args[0].toLowerCase())) {
				if (!(sender instanceof Player)) {
					Messages.sendMessage(sender, Messages.getPlayerOnlyCommand());
					return true;
				}
				Player p = (Player) sender;
				plugin.getRankupExecutor().toggleAutoRankup(p, true);
				Messages.sendMessage(sender, Messages.getAutoRankupEnabled());
				return true;
			} else if (disableSubCommands.contains(args[0].toLowerCase())) {
				if (!(sender instanceof Player)) {
					Messages.sendMessage(sender, Messages.getPlayerOnlyCommand());
					return true;
				}
				Player p = (Player) sender;
				plugin.getRankupExecutor().toggleAutoRankup(p, false);
				Messages.sendMessage(sender, Messages.getAutoRankupDisabled());
				return true;
			}
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				Messages.sendMessage(sender, Messages.getUnknownPlayer(), s -> s.replace("%player%", args[0]));
				return true;
			}
			if (!sender.hasPermission(getPermission() + ".other")) {
				Messages.sendMessage(sender, Messages.getRankupOtherNoPermission(),
						s -> s.replace("%player%", args[0]));
				return true;
			}
			boolean state = plugin.getRankupExecutor().toggleAutoRankup(target);
			Messages.sendMessage(sender,
					state ? Messages.getAutoRankupEnabledOther() : Messages.getAutoRankupDisabledOther(),
					s -> s.replace("%player%", target.getName()));
			Messages.sendMessage(target, state ? Messages.getAutoRankupEnabled() : Messages.getAutoRankupDisabled());
		} else if (args.length == 2) {
			if (enableSubCommands.contains(args[0].toLowerCase())) {
				Player target = Bukkit.getPlayer(args[1]);
				if (target == null) {
					Messages.sendMessage(sender, Messages.getUnknownPlayer(), s -> s.replace("%player%", args[1]));
				} else {
					plugin.getRankupExecutor().toggleAutoRankup(target, true);
					Messages.sendMessage(sender, Messages.getAutoRankupEnabledOther(),
							s -> s.replace("%player%", target.getName()));
				}
				return true;
			} else if (disableSubCommands.contains(args[0].toLowerCase())) {
				Player target = Bukkit.getPlayer(args[1]);
				if (target == null) {
					Messages.sendMessage(sender, Messages.getUnknownPlayer(), s -> s.replace("%player%", args[1]));
				} else {
					plugin.getRankupExecutor().toggleAutoRankup(target, false);
					Messages.sendMessage(sender, Messages.getAutoRankupDisabledOther(),
							s -> s.replace("%player%", target.getName()));
				}
				return true;
			}
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				Messages.sendMessage(sender, Messages.getUnknownPlayer(), s -> s.replace("%player%", args[1]));
				return true;
			}
			if (!sender.hasPermission(getPermission() + ".other")) {
				Messages.sendMessage(sender, Messages.getRankupOtherNoPermission(),
						s -> s.replace("%player%", args[0]));
				return true;
			}
			if (enableSubCommands.contains(args[1].toLowerCase())) {
				plugin.getRankupExecutor().toggleAutoRankup(target, true);
				Messages.sendMessage(sender, Messages.getAutoRankupEnabledOther(),
						s -> s.replace("%player%", target.getName()));

				return true;
			} else if (disableSubCommands.contains(args[1].toLowerCase())) {
				plugin.getRankupExecutor().toggleAutoRankup(target, false);
				Messages.sendMessage(sender, Messages.getAutoRankupDisabledOther(),
						s -> s.replace("%player%", target.getName()));

				return true;
			}
		}
		return true;
	}

}
