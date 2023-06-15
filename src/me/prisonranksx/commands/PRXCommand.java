package me.prisonranksx.commands;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;

public class PRXCommand extends PluginCommand {

	private PrisonRanksX plugin;
	private final double invalidDouble = -69420.69420;
	private final int invalidInt = -69420;
	private final Set<String> availableSubCommands = Sets.newHashSet("help", "setrank", "changerank", "resetrank",
			"createrank", "newrank", "addrank", "setrankdisplay", "changerankdisplay", "setrankcost", "changerankcost",
			"delrank", "deleterank", "moverankpath", "setrankpath", "ranks", "reload", "forcerankup", "setprestige",
			"changeprestige", "test", "test2");
	private final List<String> helpMessage = StringManager.parseColorsAndSymbols(Lists.newArrayList(
			"&3[&bPrisonRanks&cX&3] &7<> = required, [] = optional", "&7&m+------------------------------------------+",
			"&7/prx &chelp &f[page]", "&7/prx &creload", "&7/prx &csetrank &f<player> <rank> [path]",
			"&7/prx &cresetrank &f<player>", "&7/prx &ccreaterank &f<name> <cost> [display] [-path:<name>]",
			"&7/prx &cdelrank &f<name> [path]", "&7/prx &cmoverankpath &f<rank> <frompath> <topath>",
			"&7/prx &csetrankdisplay &f<rank> <display> [-path:<name>]", "&7/prx &csetrankcost &f<rank> <cost> [path]",
			"&7/prx &cforcerankup &f<player>", "&7Page: &8(&c1&8/&c3&8)",
			"&7&m+------------------------------------------+"));
	private final List<String> helpMessage2 = StringManager
			.parseColorsAndSymbols(Lists.newArrayList("&3[&bPrisonRanks&cX&3] &7<> = required, [] = optional",
					"&7&m+------------------------------------------+", "&7/prx &csetprestige &f<player> <prestige>",
					"&7Page: &8(&c2&8/&c3&8)", "&7&m+------------------------------------------+"));

	public static boolean isEnabled() {
		return CommandSetting.getSetting("prx", "enable");
	}

	public PRXCommand(PrisonRanksX plugin) {
		super(CommandSetting.getStringSetting("prx", "name", "prx"));
		this.plugin = plugin;
		setLabel(getCommandSection().getString("label", "prx"));
		setDescription(getCommandSection().getString("description"));
		setUsage(getCommandSection().getString("usage"));
		setPermission(getCommandSection().getString("permission"));
		setPermissionMessage(getCommandSection().getString("permission-message"));
		setAliases(getCommandSection().getStringList("aliases"));
	}

	private String testSubCommand(CommandSender sender, String arg) {
		String subCommand = arg.toLowerCase();
		if (!availableSubCommands.contains(subCommand)) {
			sender.sendMessage(
					StringManager.parseColors("&4Subcommand &c" + subCommand + " &4doesn't exist. See &e/prx help&4."));
			helpMessage.forEach(sender::sendMessage);
			return null;
		}
		return subCommand;
	}

	private Player testTarget(CommandSender sender, String name) {
		Player target = Bukkit.getPlayer(name);
		if (target == null) {
			Messages.sendMessage(sender, Messages.getUnknownPlayer(), s -> s.replace("%player%", name));
			return null;
		}
		return target;
	}

	private String testRankName(CommandSender sender, String rankName, String pathName) {
		String foundRankName = pathName == null ? null : RankStorage.findRankName(rankName, pathName);
		if (foundRankName == null) {
			Messages.sendMessage(sender, Messages.getUnknownRank(), s -> s.replace("%rank%", rankName));
			return null;
		}
		return foundRankName;
	}

	private String testPrestigeName(CommandSender sender, String prestigeName) {
		String foundPrestigeName = PrestigeStorage.matchPrestigeName(prestigeName);
		if (foundPrestigeName == null) {
			Messages.sendMessage(sender, Messages.getUnknownPrestige(), s -> s.replace("%prestige%", prestigeName));
			return null;
		}
		return foundPrestigeName;
	}

	private String testPathName(CommandSender sender, String pathName) {
		boolean pathExists = RankStorage.pathExists(pathName);
		if (pathExists) return pathName;
		Messages.sendMessage(sender, Messages.getUnknownPath(), s -> s.replace("%path%", pathName));
		return null;
	}

	private double testDouble(CommandSender sender, String numberArgument) {
		double parsedDouble;
		try {
			parsedDouble = Double.parseDouble(numberArgument);
		} catch (NumberFormatException ex) {
			sender.sendMessage(StringManager.parseColors("&c" + numberArgument + " &4is not a valid decimal number."));
			return invalidDouble;
		}
		return parsedDouble;
	}

	private int testInt(CommandSender sender, String numberArgument) {
		int parsedInt;
		try {
			parsedInt = Integer.parseInt(numberArgument);
		} catch (NumberFormatException ex) {
			sender.sendMessage(StringManager.parseColors("&c" + numberArgument + " &4is not a valid integer number."));
			return invalidInt;
		}
		return parsedInt;
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!testPermission(sender)) return true;
		switch (args.length) {
			case 0:
				helpMessage.forEach(sender::sendMessage);
				return true;
			case 1:
				String subCommand = testSubCommand(sender, args[0]);
				if (subCommand == null) return true;
				switch (subCommand) {
					case "help":
						helpMessage.forEach(sender::sendMessage);
						return true;
					case "2":
						helpMessage2.forEach(sender::sendMessage);
						return true;
					case "reload":
						plugin.getAdminExecutor().reload();
						Messages.sendMessage(sender, Messages.getReload());
						return true;
					case "setrank":
					case "changerank":
						sender.sendMessage(
								StringManager.parseColors("&4Syntax: &7/prx &csetrank &f<player> <rank> [path]"));
						return true;
					case "setprestige":
					case "changeprestige":
						sender.sendMessage(
								StringManager.parseColors("&4Syntax: &7/prx &csetprestige &f<player> <prestige>"));
						return true;
					case "resetrank":
						sender.sendMessage(StringManager.parseColors("&4Syntax: &7/prx &cresetrank &f<player>"));
						return true;
					case "forcerankup":
						sender.sendMessage(StringManager.parseColors("&4Syntax: &7/prx &cforcerankup &f<player>"));
						return true;
					case "createrank":
					case "newrank":
					case "addrank":
						sender.sendMessage(StringManager
								.parseColors("&4Syntax: &7/prx &ccreaterank &f<rank> <cost> [prefix] [-path:<name>]"));
						sender.sendMessage(
								StringManager.parseColors("&41. Example:\n&7/prx &ccreaterank &fC 2500 ") + "&b[C]");
						sender.sendMessage(StringManager.parseColors("&42. Example:\n&7/prx &ccreaterank &fAlpha 5000 ")
								+ "&4[Alpha] -path:newpath");
						return true;
					case "setrankdisplay":
					case "changerankdisplay":
						sender.sendMessage(StringManager
								.parseColors("&4Syntax: &7/prx &csetrankdisplay &f<rank> <display> [-path:<name>]"));
						return true;
					case "setrankcost":
					case "changerankcost":
						sender.sendMessage(
								StringManager.parseColors("&4Syntax: &7/prx &csetrankcost &f<rank> <cost> [path]"));
						return true;
					case "delrank":
					case "deleterank":
						sender.sendMessage(StringManager.parseColors("&4Syntax: &7/prx &cdelrank &f<rank> [path]"));
						return true;
					case "setrankpath":
					case "moverankpath":
						sender.sendMessage(StringManager
								.parseColors("&4Syntax: &7/prx &cmoverankpath &f<rank> <currentpath> <newpath>"));
						return true;
					case "ranks":
						RankStorage.PATHS.keySet().forEach(pathName -> {
							Set<Rank> ranks = Sets.newLinkedHashSet(RankStorage.getPathRanks(pathName));
							ranks.forEach(rank -> {
								sender.sendMessage(StringManager
										.parseColors("&7Path: &f" + pathName + " &cRank: &f" + rank.getName()));
							});
						});
						return true;
					case "test":
						//
						return true;
					case "test2":
						//
						return true;
				}
			case 2:
				subCommand = testSubCommand(sender, args[0]);
				if (subCommand == null) return true;
				switch (subCommand) {
					case "setrank":
					case "changerank":
						sender.sendMessage(StringManager.parseColors("&4Missing arguments: &c<rank>"));
						sender.sendMessage(
								StringManager.parseColors("&4Syntax: &7/prx &csetrank &f<player> <rank> [path]"));
						return true;
					case "setprestige":
					case "changeprestige":
						sender.sendMessage(StringManager.parseColors("&4Missing arguments: &c<prestige>"));
						sender.sendMessage(
								StringManager.parseColors("&4Syntax: &7/prx &csetprestige &f<player> <prestige>"));
						return true;
					case "resetrank": {
						Player target = testTarget(sender, args[1]);
						if (target == null) return true;
						plugin.getAdminExecutor().setPlayerRank(UniqueId.getUUID(target), RankStorage.getFirstRank());
						Messages.sendMessage(sender, Messages.getResetRank(),
								s -> s.replace("%player%", target.getName())
										.replace("%rank%", RankStorage.getFirstRank()));
						return true;
					}
					case "forcerankup": {
						Player target = testTarget(sender, args[1]);
						if (target == null) return true;
						plugin.getRankupExecutor().forceRankup(target);
						return true;
					}
					case "createrank":
					case "newrank":
					case "addrank":
						sender.sendMessage(StringManager.parseColors("&4Missing arguments: &c<cost>"));
						sender.sendMessage(StringManager
								.parseColors("&4Syntax: &7/prx &ccreaterank &f<rank> <cost> [display] [-path:<name>]"));
						sender.sendMessage(
								StringManager.parseColors("&41. Example:\n&7/prx &ccreaterank &fC 2500 ") + "&b[C]");
						sender.sendMessage(StringManager.parseColors("&42. Example:\n&7/prx &ccreaterank &fAlpha 5000 ")
								+ "&4[Alpha] -path:newpath");
						return true;
					case "setrankdisplay":
					case "changerankdisplay":
						sender.sendMessage(StringManager.parseColors("&4Missing arguments: &c<display>"));
						sender.sendMessage(StringManager
								.parseColors("&4Syntax: &7/prx &csetrankdisplay &f<rank> <display> [-path:<name>]"));
						sender.sendMessage(
								StringManager.parseColors("&4Example: \n&7/prx &csetrankdisplay &f") + "A &7[&bA&7]");
						return true;
					case "setrankcost":
					case "changerankcost":
						sender.sendMessage(StringManager.parseColors("&4Missing arguments: &c<cost>"));
						sender.sendMessage(
								StringManager.parseColors("&4Syntax: &7/prx &csetrankcost &f<rank> <cost> [path]"));
						sender.sendMessage(
								StringManager.parseColors("&4Example: \n&7/prx &csetrankcost &f") + "A 25000");
						return true;
					case "delrank":
					case "deleterank":
						String rankName = testRankName(sender, args[1], RankStorage.getDefaultPath());
						if (rankName == null) return true;
						plugin.getAdminExecutor().deleteRank(rankName, RankStorage.getDefaultPath());
						Messages.sendMessage(sender, Messages.getDeleteRank(), s -> s.replace("%args1%", rankName));
						return true;
					case "setrankpath":
					case "moverankpath":
						sender.sendMessage(StringManager
								.parseColors("&4Syntax: &7/prx &cmoverankpath &f<rank> <currentpath> <newpath>"));
						return true;
					case "test":

						return true;
					case "test2":

						return true;
				}
			case 3:
				subCommand = testSubCommand(sender, args[0]);
				if (subCommand == null) return true;
				switch (subCommand) {
					case "setrank":
					case "changerank": {
						Player target = testTarget(sender, args[1]);
						if (target == null) return true;
						String rankName = testRankName(sender, args[2], PRXAPI.getPlayerPathOrDefault(target));
						if (rankName == null) return true;
						plugin.getAdminExecutor().setPlayerRank(UniqueId.getUUID(target), rankName);
						Messages.sendMessage(sender, Messages.getSetRank(),
								s -> s.replace("%player%", target.getName()).replace("%rank%", rankName));
						return true;
					}
					case "setprestige":
					case "changeprestige": {
						Player target = testTarget(sender, args[1]);
						if (target == null) return true;
						String prestigeName = testPrestigeName(sender, args[2]);
						if (prestigeName == null) return true;
						plugin.getAdminExecutor().setPlayerPrestige(UniqueId.getUUID(target), prestigeName);
						Messages.sendMessage(sender, Messages.getSetPrestige(),
								s -> s.replace("%player%", target.getName()).replace("%prestige%", prestigeName));
						return true;
					}
					case "createrank":
					case "newrank":
					case "addrank": {
						double cost = testDouble(sender, args[2]);
						if (cost == invalidDouble) return true;
						plugin.getAdminExecutor()
								.createRank(args[1], cost, RankStorage.getDefaultPath(), "[" + args[1] + "]");
						Messages.sendMessage(sender, Messages.getCreateRank(),
								s -> s.replace("%rank%", args[1]).replace("%cost%", args[2]));
						return true;
					}
					case "setrankcost":
					case "changerankcost": {
						String rankName = testRankName(sender, args[1], RankStorage.getDefaultPath());
						if (rankName == null) return true;
						double cost = testDouble(sender, args[2]);
						if (cost == invalidDouble) return true;
						plugin.getAdminExecutor().setRankCost(rankName, RankStorage.getDefaultPath(), cost);
						Messages.sendMessage(sender, Messages.getSetRankCost(),
								s -> s.replace("%args1%", args[1]).replace("%args2%", args[2]));
						return true;
					}
					case "delrank":
					case "deleterank":
						String pathName = testPathName(sender, args[2]);
						if (pathName == null) return true;
						String rankName = testRankName(sender, args[1], pathName);
						if (rankName == null) return true;
						plugin.getAdminExecutor().deleteRank(rankName, pathName);
						Messages.sendMessage(sender, Messages.getDeleteRank(), s -> s.replace("%args1%", rankName));
						return true;
					case "setrankpath":
					case "moverankpath":
						sender.sendMessage(StringManager
								.parseColors("&4Syntax: &7/prx &cmoverankpath &f<rank> <currentpath> <newpath>"));
						return true;
				}
			case 4:
				subCommand = testSubCommand(sender, args[0]);
				if (subCommand == null) return true;
				switch (subCommand) {
					case "setrank":
					case "changerank": {
						Player target = testTarget(sender, args[1]);
						if (target == null) return true;
						String pathName = testPathName(sender, args[3]);
						if (pathName == null) return true;
						String rankName = testRankName(sender, args[2], pathName);
						if (rankName == null) return true;
						plugin.getAdminExecutor().setPlayerRank(UniqueId.getUUID(target), rankName, pathName);
						Messages.sendMessage(sender, Messages.getSetRank(),
								s -> s.replace("%player%", target.getName()).replace("%rank%", rankName));
						return true;
					}
					case "setrankcost":
					case "changerankcost": {
						String pathName = testPathName(sender, args[3]);
						if (pathName == null) return true;
						String rankName = testRankName(sender, args[1], pathName);
						if (rankName == null) return true;
						double cost = testDouble(sender, args[2]);
						if (cost == invalidDouble) return true;
						plugin.getAdminExecutor().setRankCost(rankName, pathName, cost);
						Messages.sendMessage(sender, Messages.getSetRankCost(),
								s -> s.replace("%args1%", args[1]).replace("%args2%", args[2]));
						return true;
					}
					case "setrankpath":
					case "moverankpath":
						String pathName = testPathName(sender, args[2]);
						if (pathName == null) return true;
						String rankName = testRankName(sender, args[1], pathName);
						if (rankName == null) return true;
						String newPathName = args[3].toLowerCase();
						plugin.getAdminExecutor().moveRankPath(rankName, pathName, newPathName);
						Messages.sendMessage(sender, Messages.getSetRankPath(),
								s -> s.replace("%args1%", rankName)
										.replace("%args2%", newPathName)
										.replace("%args3%", pathName));
						return true;
				}
			default:
				// Commands with arguments that allow spaces, like display name.
				subCommand = testSubCommand(sender, args[0]);
				if (subCommand == null) return true;
				switch (subCommand) {
					case "createrank":
					case "newrank":
					case "addrank": {
						String lastArg = StringManager.getArgs(args, 3);
						String[] spaces = lastArg.split(" ");
						String pathName = RankStorage.getDefaultPath();
						StringBuilder initialDisplayName = new StringBuilder();
						for (String arg : spaces) if (arg.startsWith("-path:")) pathName = arg.replace("-path:", "");
						initialDisplayName.append(lastArg.replace(" -path:" + pathName, ""));
						String displayName = initialDisplayName.length() == 0 ? args[2] : initialDisplayName.toString();
						double cost = testDouble(sender, args[2]);
						if (cost == invalidDouble) return true;
						plugin.getAdminExecutor().createRank(args[1], cost, pathName, displayName);
						Messages.sendMessage(sender, Messages.getCreateRank(),
								s -> s.replace("%rank%", StringManager.parseColors(displayName.toString()))
										.replace("%cost%", args[2]));
						return true;
					}
					case "setrankdisplay":
					case "changerankdisplay":
						String lastArg = StringManager.getArgs(args, 2);
						String[] spaces = lastArg.split(" ");
						String pathName = RankStorage.getDefaultPath();
						StringBuilder initialDisplayName = new StringBuilder();
						// allows user to place -path: anywhere, not necessarily in the end.
						for (String arg : spaces) if (arg.startsWith("-path:")) pathName = arg.replace("-path:", "");
						pathName = testPathName(sender, pathName);
						if (pathName == null) return true;
						String rankName = testRankName(sender, args[1], pathName);
						if (rankName == null) return true;
						initialDisplayName.append(lastArg.replace(" -path:" + pathName, ""));
						String displayName = initialDisplayName.length() == 0 ? args[2] : initialDisplayName.toString();
						plugin.getAdminExecutor().setRankDisplayName(rankName, pathName, displayName);
						Messages.sendMessage(sender, Messages.getSetRankDisplay(), s -> s.replace("%args1%", rankName)
								.replace("%args2%", StringManager.parseColors(displayName.toString())));
						return true;
				}
				break;
		}
		return true;
	}

}
