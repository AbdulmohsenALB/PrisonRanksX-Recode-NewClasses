package me.prisonranksx.managers;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.SenileConfigConverter;
import me.prisonranksx.reflections.UniqueId;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;

/**
 * Converts pre-3.0.0 PrisonRanksX config files to new ones.
 */
public class ConversionManager {

	private static SenileConfigConverter mainConfigConverter;
	private static SenileConfigConverter messagesConfigConverter;
	private static JavaPlugin plugin = UniqueId.getProvidingPlugin(ConversionManager.class);

	/**
	 * Performs checks to determine which config files need to be converted, then
	 * converts them, otherwise does nothing.
	 */
	public static void convertConfigFiles() {
		mainConfigConverter = new SenileConfigConverter(plugin.getDataFolder() + "/config.yml");
		messagesConfigConverter = new SenileConfigConverter(plugin.getDataFolder() + "/messages.yml");
		if (mainConfigConverter.checkSenility(config -> config.contains("defaultrank"))) {
			PrisonRanksX.logWarning("Detected old main config file! Converting...");
			addMainConfigConversions();
			mainConfigConverter.process(ConfigManager.getConfig());
			ConfigManager.saveConfig("config.yml");
			PrisonRanksX.log("Successfully converted old main config to new config.");
		}
		if (messagesConfigConverter.checkSenility(config -> config.contains("notenoughmoney"))) {
			PrisonRanksX.logWarning("Detected old messages config file! Converting...");
			addMessagesConfigConversions();
			messagesConfigConverter.process(ConfigManager.getMessagesConfig());
			ConfigManager.saveConfig("messages.yml");
			PrisonRanksX.log("Successfully converted old messages config to new config");
		}
	}

	private static void addMessagesConfigConversions() {
		messagesConfigConverter.addFieldRename(false, "Messages", null, "notenoughmoney", "not-enough-balance",
				"notenoughmoney-other", "not-enough-balance-other", "nopermission", "no-permission", "playernotfound",
				"unknown-player", "delrank", "delete-rank", "setrank", "set-rank", "resetrank", "reset-rank", "createrank",
				"create-rank", "setnextrank", "set-next-rank", "setrankdisplay", "set-rank-display", "setrankcost",
				"set-rank-cost", "delplayerrank", "delete-player-rank", "addrankcmd", "add-rank-command",
				"addrankbroadcast", "add-rank-broadcast", "addrankmsg", "add-rank-msg", "setdefaultrank",
				"set-default-rank", "setlastrank", "set-last-rank", "setplayerpath", "set-player-path",
				"setdefaultpath", "set-default-path", "setrankpath", "set-rank-path", "reload", "plugin-reload", "save",
				"plugin-save", "path-notfound", "unknown-path", "rank-notfound", "unknown-rank", "prestige-notfound",
				"unknown-prestige", "rebirth-notfound", "unknown-rebirth", "setfirstrebirth", "set-first-rebirth",
				"setlastrebirth", "set-last-rebirth", "setrebirth", "set-rebirth", "resetrebirth", "reset-rebirth",
				"createrebirth", "create-rebirth", "setrebirthdisplay", "set-rebirth-display", "setrebirthcost",
				"set-rebirth-cost", "runfromconsole", "player-only-command", "notcorrectworld", "disallowed-world",
				"delprestige", "delete-prestige", "setfirstprestige", "set-first-prestige", "setlastprestige",
				"set-last-prestige", "setprestige", "set-prestige", "resetprestige", "reset-prestige", "createprestige",
				"create-prestige", "setnextprestige", "set-next-prestige", "setprestigedisplay", "set-prestige-display",
				"setprestigecost", "set-prestige-cost", "forceprestige", "force-prestige", "noprestige",
				"disallowed-prestige", "norebirth", "disallowed-rebirth", "delplayerprestige", "delete-player-prestige",
				"delplayerrebirth", "delete-player-rebirth", "prestige-notenoughmoney", "prestige-not-enough-money",
				"lastprestige", "last-prestige", "lastprestige-other", "last-prestige-other", "lastrank", "last-rank",
				"forcerankup-msg", "force-rankup", "forcerankup-lastrank", "force-rankup-last-rank",
				"forcerankup-nopermission", "force-rankup-no-permission", "rankup-nopermission", "rankup-no-permission",
				"rankup-other-nopermission", "rankup-other-no-permission", "autorankup-enabled", "auto-rankup-enabled",
				"autorankup-disabled", "auto-rankup-disabled", "autorankup-enabled-other", "auto-rankup-enabled-other",
				"autorankup-disabled-other", "auto-rankup-disabled-other", "autoprestige-enabled",
				"auto-prestige-enabled", "autoprestige-disabled", "auto-prestige-disabled", "autorebirth-enabled",
				"auto-rebirth-enabled", "autorebirth-disabled", "auto-rebirth-enabled", "rebirth-notenoughmoney",
				"rebirth-not-enough-money", "lastrebirth", "last-rebirth", "ranklist-last-page-reached",
				"ranks-list-last-page-reached", "ranklist-invalid-page", "ranks-list-invalid-page",
				"prestigelist-last-page-reached", "prestiges-list-last-page-reached", "prestigelist-invalid-page",
				"prestiges-list-invalid-page", "rebirthlist-last-page-reached", "rebirths-list-last-page-reached",
				"rebirthlist-invalid-page", "rebirths-list-invalid-page", "ranklist-console", "ranks-list-console",
				"forcerankup-noargs", "force-rankup-missing-argument", "rebirth-failed",
				"rebirth-not-enough-prestiges");
		messagesConfigConverter.addAnyStringReplaceUpdate("Messages", "%settedrank%", "%rank%")
				.addAnyStringReplaceUpdate("Messages", "%firstrank%", "%rank%")
				.addAnyStringReplaceUpdate("Messages", "%createdrank%", "%rank%")
				.addAnyStringReplaceUpdate("Messages", "%rankcost%", "%cost%")
				.addAnyStringReplaceUpdate("Messages", "%settedrebirth%", "%rebirth%")
				.addAnyStringReplaceUpdate("Messages", "%firstrebirth%", "%rebirth%")
				.addAnyStringReplaceUpdate("Messages", "%createdrebirth%", "%rebirth%")
				.addAnyStringReplaceUpdate("Messages", "%rebirthcost%", "%cost%")
				.addAnyStringReplaceUpdate("Messages", "%changeddisplay%", "%display%")
				.addAnyStringReplaceUpdate("Messages", "%settedprestige%", "%prestige%")
				.addAnyStringReplaceUpdate("Messages", "%firstprestige%", "%prestige%")
				.addAnyStringReplaceUpdate("Messages", "%createdprestige%", "%prestige%")
				.addAnyStringReplaceUpdate("Messages", "%prestigecost%", "%cost%")
				.addAnyStringReplaceUpdate("Messages", "%previousprestige%", "%prestige%")
				.addAnyStringReplaceUpdate("Messages", "%target%", "%player%");
	}

	private static void addMainConfigConversions() {
		// Options
		mainConfigConverter.addFieldRename(false, "Options", null, "allworlds-broadcast", "all-worlds-broadcast",
				"send-rankupmsg", "send-rankup-msg", "send-prestigemsg", "send-prestige-msg", "send-rebirthmsg",
				"send-rebirth-msg", "send-rankupmaxmsg", "send-rankupmax-msg", "GUI-RANKLIST", "gui-rank-list",
				"GUI-PRESTIGELIST", "gui-prestige-list", "GUI-REBIRTHLIST", "gui-rebirth-list", "rankupsound-name",
				"rankup-sound-name", "rankupsound-volume", "rankup-sound-volume", "rankupsound-pitch",
				"rankup-sound-pitch", "prestigesound-name", "prestige-sound-name", "prestigesound-volume",
				"prestige-sound-volume", "prestigesound-pitch", "prestige-sound-pitch", "rebirthsound-name",
				"rebirth-sound-name", "rebirthsound-volume", "rebirth-sound-volume", "rebirthsound-pitch",
				"rebirth-sound-pitch", "rankupmax-broadcastlastrankonly", "rankupmax-broadcast-last-rank-only",
				"rankupmax-msglastrankonly", "rankupmax-msg-last-rank-only", "rankupmax-rankupmsglastrankonly",
				"rankupmax-rankup-msg-last-rank-only", "autorankup-delay", "auto-rankup-delay", "autoprestige-delay",
				"auto-prestige-delay", "autorebirth-delay", "auto-rebirth-delay", "actionbar-progress",
				"action-bar-progress", "actionbar-progress-only-pickaxe", "action-bar-progress-only-pickaxe",
				"actionbar-progress-format", "action-bar-progress-format", "actionbar-progress-updater",
				"action-bar-progress-updater", "expbar-progress", "exp-bar-progress", "expbar-progress-updater",
				"exp-bar-progress-updater", "expbar-progress-format", "exp-bar-progress-format", "autosave",
				"auto-save", "autosave-time", "auto-save-time", "forcesave", "force-save",
				"prestigemax-prestigemsglastprestigeonly", "prestigemax-prestige-msg-last-prestige-only");

		// Text Lists
		mainConfigConverter.addConfigurationSectionRename("Ranklist-text", "Ranks-List-Options");
		mainConfigConverter.addConfigurationSectionRename("Prestigelist-text", "Prestiges-List-Options");
		mainConfigConverter.addConfigurationSectionRename("Rebirthlist-text", "Rebirths-List-Options");

		// Holograms
		mainConfigConverter.addFieldRename(false, "Holograms", null, "rankup.remove-time", "rankup.remove-delay",
				"prestige.remove-time", "prestige.remove-delay", "rebirth.remove-time", "rebirth.remove-delay");

		// MySQL
		mainConfigConverter.addFieldRename(false, "MySQL", null, "useSSL", "use-ssl", "autoReconnect", "auto-reconnect",
				"useCursorFetch", "use-cursor-fetch");

		// Rank Options
		mainConfigConverter.addConfigurationSectionRename("RankOptions", "Rank-Options");
		mainConfigConverter.addFieldRename(true, "RankOptions", "Rank-Options", "rank-delete-cmds",
				"rank-delete-commands", "rank-reset-cmds", "rank-reset-commands");
		mainConfigConverter.addConditionToBoolean(
				mainConfigConverter.getSenileConfig().getStringList("RankOptions.rank-delete-cmds"),
				condition -> condition.contains("[rankpermissions] remove"),
				"Rank-Options.remove-rank-permissions-on-rank-deletion");
		mainConfigConverter.addConditionToBoolean(
				mainConfigConverter.getSenileConfig().getStringList("RankOptions.rank-reset-cmds"),
				condition -> condition.contains("[rankpermissions] remove"),
				"Rank-Options.remove-rank-permissions-on-rank-reset");
		mainConfigConverter.addStringListUpdate("Rank-Options.rank-delete-commands",
				mainConfigConverter.getSenileConfig()
						.getStringList("RankOptions.rank-delete-cmds")
						.stream()
						.filter(line -> !line.contains("permissions] remove"))
						.collect(Collectors.toList()));
		mainConfigConverter.addStringListUpdate("Rank-Options.rank-reset-commands",
				mainConfigConverter.getSenileConfig()
						.getStringList("RankOptions.rank-reset-cmds")
						.stream()
						.filter(line -> !line.contains("permissions] remove"))
						.collect(Collectors.toList()));

		// Prestige Options
		mainConfigConverter.addConfigurationSectionRename("PrestigeOptions", "Prestige-Options");
		mainConfigConverter.addFieldRename(true, "PrestigeOptions", "Prestige-Options", "ResetMoney", "reset-money",
				"ResetRank", "reset-rank", "rankup_cost_increase_percentage", "rank-cost-increase-percentage",
				"cost_increase_expression", "increase-expression", "prestige-cmds", "prestige-commands",
				"prestige-delete-cmds", "prestige-delete-commands", "prestige-reset-cmds", "prestige-reset-commands");
		mainConfigConverter.addStringUpdate("Prestige-Options.increase-expression",
				mainConfigConverter.getSenileConfig()
						.getString("PrestigeOptions.cost_increase_expression")
						.replace("{cost_increase}", "{increase_percentage}")
						.replace("{rankcost}", "{rank_cost}")
						.replace("{prestigenumber}", "{prestige_number}"));
		mainConfigConverter.addStringListUpdate("Prestige-Options.prestige-commands",
				mainConfigConverter.getSenileConfig()
						.getStringList("PrestigeOptions.prestige-cmds")
						.stream()
						.filter(line -> !line.contains("permissions] remove"))
						.collect(Collectors.toList()));
		mainConfigConverter.addStringListUpdate("Prestige-Options.prestige-delete-commands",
				mainConfigConverter.getSenileConfig()
						.getStringList("PrestigeOptions.prestige-delete-cmds")
						.stream()
						.filter(line -> !line.contains("permissions] remove"))
						.collect(Collectors.toList()));
		mainConfigConverter.addStringListUpdate("Prestige-Options.prestige-reset-commands",
				mainConfigConverter.getSenileConfig()
						.getStringList("PrestigeOptions.prestige-reset-cmds")
						.stream()
						.filter(line -> !line.contains("permissions$1] remove"))
						.collect(Collectors.toList()));
		mainConfigConverter.addConditionToBoolean(
				mainConfigConverter.getSenileConfig().getStringList("PrestigeOptions.prestige-cmds"),
				condition -> condition.contains("[rankpermissions] remove"),
				"Prestige-Options.remove-rank-permissions-on-prestige");
		mainConfigConverter.addConditionToBoolean(
				mainConfigConverter.getSenileConfig().getStringList("PrestigeOptions.prestige-delete-cmds"),
				condition -> condition.contains("[prestigepermissions] remove"),
				"Prestige-Options.remove-prestige-permissions-on-prestige-deletion");
		mainConfigConverter.addConditionToBoolean(
				mainConfigConverter.getSenileConfig().getStringList("PrestigeOptions.prestige-reset-cmds"),
				condition -> condition.contains("[prestigepermissions$1] remove"),
				"Prestige-Options.remove-prestige-permissions-on-prestige-reset");

		// Rebirth Options
		mainConfigConverter.addConfigurationSectionRename("RebirthOptions", "Rebirth-Options");
		mainConfigConverter.addFieldRename(true, "RebirthOptions", "Rebirth-Options", "ResetMoney", "reset-money",
				"ResetRank", "reset-rank", "ResetPrestige", "reset-prestige", "prestige_cost_increase_percentage",
				"prestige-cost-increase-percentage", "cost_increase_expression", "increase-expression", "rebirth-cmds",
				"rebirth-commands", "rebirth-delete-cmds", "rebirth-delete-commands");
		mainConfigConverter.addStringUpdate("Rebirth-Options.increase-expression",
				mainConfigConverter.getSenileConfig()
						.getString("RebirthOptions.cost_increase_expression")
						.replace("{cost_increase}", "{increase_percentage}")
						.replace("{prestigecost}", "{prestige_cost}")
						.replace("{rebirthnumber}", "{rebirth_number}"));
		mainConfigConverter.addStringListUpdate("Rebirth-Options.rebirth-commands",
				mainConfigConverter.getSenileConfig()
						.getStringList("RebirthOptions.rebirth-cmds")
						.stream()
						.filter(line -> !line.contains("permissions] remove"))
						.collect(Collectors.toList()));
		mainConfigConverter.addStringListUpdate("Rebirth-Options.rebirth-delete-commands",
				mainConfigConverter.getSenileConfig()
						.getStringList("RebirthOptions.rebirth-delete-cmds")
						.stream()
						.filter(line -> !line.contains("permissions] remove"))
						.collect(Collectors.toList()));
		mainConfigConverter.addConditionToBoolean(
				mainConfigConverter.getSenileConfig().getStringList("RebirthOptions.rebirth-cmds"),
				condition -> condition.contains("[rankpermissions] remove"),
				"Rebirth-Options.remove-rank-permissions-on-rebirth");
		mainConfigConverter.addConditionToBoolean(
				mainConfigConverter.getSenileConfig().getStringList("RebirthOptions.rebirth-cmds"),
				condition -> condition.contains("[prestigepermissions] remove"),
				"Rebirth-Options.remove-prestige-permissions-on-rebirth");
		mainConfigConverter.addConditionToBoolean(
				mainConfigConverter.getSenileConfig().getStringList("RebirthOptions.rebirth-delete-cmds"),
				condition -> condition.contains("[rebirthpermissions] remove"),
				"Rebirth-Options.remove-rebirth-permissions-on-rebirth-deletion");

		// PlaceholderAPI Options
		mainConfigConverter.addConfigurationSectionRename("PlaceholderAPI", "PlaceholderAPI-Options");
		mainConfigConverter.addFieldRename(true, "PlaceholderAPI", "PlaceholderAPI-Options", "rankup-progress-style",
				"next-rank-progress-bar-style", "rankup-progress-filled", "next-rank-progress-bar-filled",
				"rankup-progress-needed", "next-rank-progress-bar-needed", "rankup-progress-full-enabled",
				"next-rank-progress-bar-full-enabled", "rankup-progress-full", "next-rank-progress-bar-full",
				"rankup-progress-lastrank", "next-rank-progress-bar-last-rank", "rankup-percentage-lastrank",
				"next-rank-percentage-last-rank", "rankup-cost-lastrank", "next-rank-cost-last-rank", "rankup-lastrank",
				"next-rank-last-rank", "currentrank-lastrank-enabled", "current-rank-last-rank-enabled",
				"currentrank-lastrank", "current-rank-last-rank", "prestige-lastprestige", "prestige-last-prestige",
				"prestige-notprestiged", "prestige-no-prestige", "nextprestige-notprestiged",
				"next-prestige-no-prestige", "rebirth-notrebirthed", "rebirth-no-rebirth", "nextrebirth-notrebirthed",
				"nextrebirth-no-rebirth", "rebirth-lastrebirth", "rebirth-last-rebirth",
				"next-progress-full-isrankup-enabled", "next-progress-bar-full-is-rankup-enabled",
				"next-progress-full-isprestige-enabled", "next-progress-bar-full-is-prestige-enabled",
				"next-progress-full-isrebirth-enabled", "next-progress-bar-full-is-rebirth-enabled",
				"next-progress-full-islast-enabled", "next-progress-bar-full-is-last-enabled",
				"next-progress-full-isrankup", "next-progress-bar-full-is-rankup", "next-progress-full-isprestige",
				"next-progress-bar-full-is-prestige", "next-progress-full-isrebirth",
				"next-progress-bar-full-is-rebirth", "next-progress-full-islast", "next-progress-bar-full-is-last");
		mainConfigConverter.addConfigurationSectionRename("MoneyFormatter", "Balance-Formatter");
		mainConfigConverter.addFieldRename(true, "MoneyFormatter", "Balance-Formatter", "Duodecillion",
				"duo-decillion");
	}

}
