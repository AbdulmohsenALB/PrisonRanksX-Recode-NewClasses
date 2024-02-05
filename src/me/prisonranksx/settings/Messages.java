package me.prisonranksx.settings;

import java.util.List;
import java.util.function.Function;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.prisonranksx.managers.StringManager;

public class Messages {

	private static final MessagesSettings SETTINGS = new MessagesSettings();

	public static void reload() {
		SETTINGS.setup();
	}

	public static void sendMessage(CommandSender sender, String message) {
		if (message == null) return;
		sender.sendMessage(message);
	}

	public static void sendMessage(CommandSender sender, String message, Function<String, String> stringFunction) {
		if (message == null) return;
		sender.sendMessage(stringFunction.apply(message));
	}

	public static void sendMessage(Player player, String message) {
		if (message == null) return;
		player.sendMessage(StringManager.parsePlaceholders(message, player));
	}

	public static void sendMessage(Player player, String message, Function<String, String> stringFunction) {
		if (message == null) return;
		player.sendMessage(StringManager.parsePlaceholders(stringFunction.apply(message), player));
	}

	public static void sendMessage(Player player, List<String> message) {
		if (message == null) return;
		message.forEach(messageLine -> player.sendMessage(StringManager.parsePlaceholders(messageLine, player)));
	}

	public static void sendMessage(Player player, List<String> message, Function<String, String> stringFunction) {
		if (message == null) return;
		message.forEach(messageLine -> player
				.sendMessage(StringManager.parsePlaceholders(stringFunction.apply(messageLine), player)));
	}

	public static String getNoPermission() {
		return SETTINGS.noPermission;
	}

	public static void setNoPermission(String noPermission) {
		SETTINGS.noPermission = noPermission;
	}

	public static String getUnknownPlayer() {
		return SETTINGS.unknownPlayer;
	}

	public static void setUnknownPlayer(String unknownPlayer) {
		SETTINGS.unknownPlayer = unknownPlayer;
	}

	public static String getDeleteRank() {
		return SETTINGS.deleteRank;
	}

	public static void setDeleteRank(String deleteRank) {
		SETTINGS.deleteRank = deleteRank;
	}

	public static String getSetRank() {
		return SETTINGS.setRank;
	}

	public static void setSetRank(String setRank) {
		SETTINGS.setRank = setRank;
	}

	public static String getResetRank() {
		return SETTINGS.resetRank;
	}

	public static void setResetRank(String resetRank) {
		SETTINGS.resetRank = resetRank;
	}

	public static String getCreateRank() {
		return SETTINGS.createRank;
	}

	public static void setCreateRank(String createRank) {
		SETTINGS.createRank = createRank;
	}

	public static String getSetNextRank() {
		return SETTINGS.setNextRank;
	}

	public static void setSetNextRank(String setNextRank) {
		SETTINGS.setNextRank = setNextRank;
	}

	public static String getSetRankDisplay() {
		return SETTINGS.setRankDisplay;
	}

	public static void setSetRankDisplay(String setRankDisplay) {
		SETTINGS.setRankDisplay = setRankDisplay;
	}

	public static String getSetRankCost() {
		return SETTINGS.setRankCost;
	}

	public static void setSetRankCost(String setRankCost) {
		SETTINGS.setRankCost = setRankCost;
	}

	public static String getDeletePlayerRank() {
		return SETTINGS.deletePlayerRank;
	}

	public static void setDeletePlayerRank(String deletePlayerRank) {
		SETTINGS.deletePlayerRank = deletePlayerRank;
	}

	public static String getAddRankCommand() {
		return SETTINGS.addRankCommand;
	}

	public static void setAddRankCommand(String addRankCommand) {
		SETTINGS.addRankCommand = addRankCommand;
	}

	public static String getAddRankBroadcast() {
		return SETTINGS.addRankBroadcast;
	}

	public static void setAddRankBroadcast(String addRankBroadcast) {
		SETTINGS.addRankBroadcast = addRankBroadcast;
	}

	public static String getAddRankMsg() {
		return SETTINGS.addRankMsg;
	}

	public static void setAddRankMsg(String addRankMsg) {
		SETTINGS.addRankMsg = addRankMsg;
	}

	public static String getSetDefaultRank() {
		return SETTINGS.setDefaultRank;
	}

	public static void setSetDefaultRank(String setDefaultRank) {
		SETTINGS.setDefaultRank = setDefaultRank;
	}

	public static String getSetLastRank() {
		return SETTINGS.setLastRank;
	}

	public static void setSetLastRank(String setLastRank) {
		SETTINGS.setLastRank = setLastRank;
	}

	public static String getSetPlayerPath() {
		return SETTINGS.setPlayerPath;
	}

	public static void setSetPlayerPath(String setPlayerPath) {
		SETTINGS.setPlayerPath = setPlayerPath;
	}

	public static String getSetDefaultPath() {
		return SETTINGS.setDefaultPath;
	}

	public static void setSetDefaultPath(String setDefaultPath) {
		SETTINGS.setDefaultPath = setDefaultPath;
	}

	public static String getSetRankPath() {
		return SETTINGS.setRankPath;
	}

	public static void setSetRankPath(String setRankPath) {
		SETTINGS.setRankPath = setRankPath;
	}

	public static String getReload() {
		return SETTINGS.reload;
	}

	public static void setReload(String reload) {
		SETTINGS.reload = reload;
	}

	public static String getSave() {
		return SETTINGS.save;
	}

	public static void setSave(String save) {
		SETTINGS.save = save;
	}

	public static String getUnknownPath() {
		return SETTINGS.unknownPath;
	}

	public static void setUnknownPath(String unknownPath) {
		SETTINGS.unknownPath = unknownPath;
	}

	public static String getUnknownRank() {
		return SETTINGS.unknownRank;
	}

	public static void setUnknownRank(String unknownRank) {
		SETTINGS.unknownRank = unknownRank;
	}

	public static String getUnknownPrestige() {
		return SETTINGS.unknownPrestige;
	}

	public static void setUnknownPrestige(String unknownPrestige) {
		SETTINGS.unknownPrestige = unknownPrestige;
	}

	public static String getUnknownRebirth() {
		return SETTINGS.unknownRebirth;
	}

	public static void setUnknownRebirth(String unknownRebirth) {
		SETTINGS.unknownRebirth = unknownRebirth;
	}

	public static String getSetFirstRebirth() {
		return SETTINGS.setFirstRebirth;
	}

	public static void setSetFirstRebirth(String setFirstRebirth) {
		SETTINGS.setFirstRebirth = setFirstRebirth;
	}

	public static String getSetLastRebirth() {
		return SETTINGS.setLastRebirth;
	}

	public static void setSetLastRebirth(String setLastRebirth) {
		SETTINGS.setLastRebirth = setLastRebirth;
	}

	public static String getSetRebirth() {
		return SETTINGS.setRebirth;
	}

	public static void setSetRebirth(String setRebirth) {
		SETTINGS.setRebirth = setRebirth;
	}

	public static String getResetRebirth() {
		return SETTINGS.resetRebirth;
	}

	public static void setResetRebirth(String resetRebirth) {
		SETTINGS.resetRebirth = resetRebirth;
	}

	public static String getCreateRebirth() {
		return SETTINGS.createRebirth;
	}

	public static void setCreateRebirth(String createRebirth) {
		SETTINGS.createRebirth = createRebirth;
	}

	public static String getSetRebirthDisplay() {
		return SETTINGS.setRebirthDisplay;
	}

	public static void setSetRebirthDisplay(String setRebirthDisplay) {
		SETTINGS.setRebirthDisplay = setRebirthDisplay;
	}

	public static String getSetRebirthCost() {
		return SETTINGS.setRebirthCost;
	}

	public static void setSetRebirthCost(String setRebirthCost) {
		SETTINGS.setRebirthCost = setRebirthCost;
	}

	public static String getPlayerOnlyCommand() {
		return SETTINGS.playerOnlyCommand;
	}

	public static void setPlayerOnlyCommand(String playerOnlyCommand) {
		SETTINGS.playerOnlyCommand = playerOnlyCommand;
	}

	public static String getDisallowedWorld() {
		return SETTINGS.disallowedWorld;
	}

	public static void setDisallowedWorld(String disallowedWorld) {
		SETTINGS.disallowedWorld = disallowedWorld;
	}

	public static String getRankup() {
		return SETTINGS.rankup;
	}

	public static void setRankup(String rankup) {
		SETTINGS.rankup = rankup;
	}

	public static String getRankupOther() {
		return SETTINGS.rankupOther;
	}

	public static void setRankupOther(String rankupOther) {
		SETTINGS.rankupOther = rankupOther;
	}

	public static String getRankupOtherRecipient() {
		return SETTINGS.rankupOtherRecipient;
	}

	public static void setRankupOtherRecipient(String rankupOtherRecipient) {
		SETTINGS.rankupOtherRecipient = rankupOtherRecipient;
	}

	public static String getDeletePrestige() {
		return SETTINGS.deletePrestige;
	}

	public static void setDeletePrestige(String deletePrestige) {
		SETTINGS.deletePrestige = deletePrestige;
	}

	public static String getSetFirstPrestige() {
		return SETTINGS.setFirstPrestige;
	}

	public static void setSetFirstPrestige(String setFirstPrestige) {
		SETTINGS.setFirstPrestige = setFirstPrestige;
	}

	public static String getSetLastPrestige() {
		return SETTINGS.setLastPrestige;
	}

	public static void setSetLastPrestige(String setLastPrestige) {
		SETTINGS.setLastPrestige = setLastPrestige;
	}

	public static String getSetPrestige() {
		return SETTINGS.setPrestige;
	}

	public static void setSetPrestige(String setPrestige) {
		SETTINGS.setPrestige = setPrestige;
	}

	public static String getResetPrestige() {
		return SETTINGS.resetPrestige;
	}

	public static void setResetPrestige(String resetPrestige) {
		SETTINGS.resetPrestige = resetPrestige;
	}

	public static String getCreatePrestige() {
		return SETTINGS.createPrestige;
	}

	public static void setCreatePrestige(String createPrestige) {
		SETTINGS.createPrestige = createPrestige;
	}

	public static String getSetNextPrestige() {
		return SETTINGS.setNextPrestige;
	}

	public static void setSetNextPrestige(String setNextPrestige) {
		SETTINGS.setNextPrestige = setNextPrestige;
	}

	public static String getSetPrestigeDisplay() {
		return SETTINGS.setPrestigeDisplay;
	}

	public static void setSetPrestigeDisplay(String setPrestigeDisplay) {
		SETTINGS.setPrestigeDisplay = setPrestigeDisplay;
	}

	public static String getSetPrestigeCost() {
		return SETTINGS.setPrestigeCost;
	}

	public static void setSetPrestigeCost(String setPrestigeCost) {
		SETTINGS.setPrestigeCost = setPrestigeCost;
	}

	public static String getPrestige() {
		return SETTINGS.prestige;
	}

	public static void setPrestige(String prestige) {
		SETTINGS.prestige = prestige;
	}

	public static String getDisallowedPrestige() {
		return SETTINGS.disallowedPrestige;
	}

	public static void setDisallowedPrestige(String disallowedPrestige) {
		SETTINGS.disallowedPrestige = disallowedPrestige;
	}

	public static String getDisallowedRebirth() {
		return SETTINGS.disallowedRebirth;
	}

	public static void setDisallowedRebirth(String disallowedRebirth) {
		SETTINGS.disallowedRebirth = disallowedRebirth;
	}

	public static String getDeletePlayerPrestige() {
		return SETTINGS.deletePlayerPrestige;
	}

	public static void setDeletePlayerPrestige(String deletePlayerPrestige) {
		SETTINGS.deletePlayerPrestige = deletePlayerPrestige;
	}

	public static String getDeletePlayerRebirth() {
		return SETTINGS.deletePlayerRebirth;
	}

	public static void setDeletePlayerRebirth(String deletePlayerRebirth) {
		SETTINGS.deletePlayerRebirth = deletePlayerRebirth;
	}

	public static String getForceRankup() {
		return SETTINGS.forceRankup;
	}

	public static void setForceRankup(String forceRankup) {
		SETTINGS.forceRankup = forceRankup;
	}

	public static String getForceRankupLastRank() {
		return SETTINGS.forceRankupLastRank;
	}

	public static void setForceRankupLastRank(String forceRankupLastRank) {
		SETTINGS.forceRankupLastRank = forceRankupLastRank;
	}

	public static String getForceRankupNoPermission() {
		return SETTINGS.forceRankupNoPermission;
	}

	public static void setForceRankupNoPermission(String forceRankupNoPermission) {
		SETTINGS.forceRankupNoPermission = forceRankupNoPermission;
	}

	public static String getRankupNoPermission() {
		return SETTINGS.rankupNoPermission;
	}

	public static void setRankupNoPermission(String rankupNoPermission) {
		SETTINGS.rankupNoPermission = rankupNoPermission;
	}

	public static String getRankupOtherNoPermission() {
		return SETTINGS.rankupOtherNoPermission;
	}

	public static void setRankupOtherNoPermission(String rankupOtherNoPermission) {
		SETTINGS.rankupOtherNoPermission = rankupOtherNoPermission;
	}

	public static String getAutoRankupEnabled() {
		return SETTINGS.autoRankupEnabled;
	}

	public static void setAutoRankupEnabled(String autoRankupEnabled) {
		SETTINGS.autoRankupEnabled = autoRankupEnabled;
	}

	public static String getAutoRankupDisabled() {
		return SETTINGS.autoRankupDisabled;
	}

	public static void setAutoRankupDisabled(String autoRankupDisabled) {
		SETTINGS.autoRankupDisabled = autoRankupDisabled;
	}

	public static String getAutoRankupEnabledOther() {
		return SETTINGS.autoRankupEnabledOther;
	}

	public static void setAutoRankupEnabledOther(String autoRankupEnabledOther) {
		SETTINGS.autoRankupEnabledOther = autoRankupEnabledOther;
	}

	public static String getAutoRankupDisabledOther() {
		return SETTINGS.autoRankupDisabledOther;
	}

	public static void setAutoRankupDisabledOther(String autoRankupDisabledOther) {
		SETTINGS.autoRankupDisabledOther = autoRankupDisabledOther;
	}

	public static String getAutoRankupNoPermission() {
		return SETTINGS.autoRankupNoPermission;
	}

	public static void setAutoRankupNoPermission(String autoRankupNoPermission) {
		SETTINGS.autoRankupNoPermission = autoRankupNoPermission;
	}

	public static String getAutoRankupLastRank() {
		return SETTINGS.autoRankupLastRank;
	}

	public static void setAutoRankupLastRank(String autoRankupLastRank) {
		SETTINGS.autoRankupLastRank = autoRankupLastRank;
	}

	public static String getAutoPrestigeEnabled() {
		return SETTINGS.autoPrestigeEnabled;
	}

	public static void setAutoPrestigeEnabled(String autoPrestigeEnabled) {
		SETTINGS.autoPrestigeEnabled = autoPrestigeEnabled;
	}

	public static String getAutoPrestigeDisabled() {
		return SETTINGS.autoPrestigeDisabled;
	}

	public static void setAutoPrestigeDisabled(String autoPrestigeDisabled) {
		SETTINGS.autoPrestigeDisabled = autoPrestigeDisabled;
	}

	public static String getAutoRebirthEnabled() {
		return SETTINGS.autoRebirthEnabled;
	}

	public static void setAutoRebirthEnabled(String autoRebirthEnabled) {
		SETTINGS.autoRebirthEnabled = autoRebirthEnabled;
	}

	public static String getAutoRebirthDisabled() {
		return SETTINGS.autoRebirthDisabled;
	}

	public static void setAutoRebirthDisabled(String autoRebirthDisabled) {
		SETTINGS.autoRebirthDisabled = autoRebirthDisabled;
	}

	public static String getRebirth() {
		return SETTINGS.rebirth;
	}

	public static void setRebirth(String rebirth) {
		SETTINGS.rebirth = rebirth;
	}

	public static String getCommandSpam() {
		return SETTINGS.commandSpam;
	}

	public static void setCommandSpam(String commandSpam) {
		SETTINGS.commandSpam = commandSpam;
	}

	public static String getRankupMaxIsOn() {
		return SETTINGS.rankupMaxIsOn;
	}

	public static void setRankupMaxIsOn(String rankupMaxIsOn) {
		SETTINGS.rankupMaxIsOn = rankupMaxIsOn;
	}

	public static String getPrestigeMaxIsOn() {
		return SETTINGS.prestigeMaxIsOn;
	}

	public static void setPrestigeMaxIsOn(String prestigeMaxIsOn) {
		SETTINGS.prestigeMaxIsOn = prestigeMaxIsOn;
	}

	public static String getRankListLastPageReached() {
		return SETTINGS.ranksListLastPageReached;
	}

	public static void setRankListLastPageReached(String rankListLastPageReached) {
		SETTINGS.ranksListLastPageReached = rankListLastPageReached;
	}

	public static String getRankListInvalidPage() {
		return SETTINGS.ranksListInvalidPage;
	}

	public static void setRankListInvalidPage(String rankListInvalidPage) {
		SETTINGS.ranksListInvalidPage = rankListInvalidPage;
	}

	public static String getPrestigeLastLastPageReached() {
		return SETTINGS.prestigeLastLastPageReached;
	}

	public static void setPrestigeLastLastPageReached(String prestigeLastLastPageReached) {
		SETTINGS.prestigeLastLastPageReached = prestigeLastLastPageReached;
	}

	public static String getPrestigeListLastPageReached() {
		return SETTINGS.prestigesListLastPageReached;
	}

	public static void setPrestigeListLastPageReached(String prestigeListLastPageReached) {
		SETTINGS.prestigesListLastPageReached = prestigeListLastPageReached;
	}

	public static String getPrestigeListInvalidPage() {
		return SETTINGS.prestigesListInvalidPage;
	}

	public static void setPrestigeListInvalidPage(String prestigeListInvalidPage) {
		SETTINGS.prestigesListInvalidPage = prestigeListInvalidPage;
	}

	public static String getRebirthListLastPageReached() {
		return SETTINGS.rebirthsListLastPageReached;
	}

	public static void setRebirthListLastPageReached(String rebirthListLastPageReached) {
		SETTINGS.rebirthsListLastPageReached = rebirthListLastPageReached;
	}

	public static String getRebirthListInvalidPage() {
		return SETTINGS.rebirthsListInvalidPage;
	}

	public static void setRebirthListInvalidPage(String rebirthListInvalidPage) {
		SETTINGS.rebirthsListInvalidPage = rebirthListInvalidPage;
	}

	public static String getRankListConsole() {
		return SETTINGS.rankListConsole;
	}

	public static void setRankListConsole(String rankListConsole) {
		SETTINGS.rankListConsole = rankListConsole;
	}

	public static String getPrestigeListConsole() {
		return SETTINGS.prestigeListConsole;
	}

	public static void setPrestigeListConsole(String prestigeListConsole) {
		SETTINGS.prestigeListConsole = prestigeListConsole;
	}

	public static String getRebirthListConsole() {
		return SETTINGS.rebirthListConsole;
	}

	public static void setRebirthListConsole(String rebirthListConsole) {
		SETTINGS.rebirthListConsole = rebirthListConsole;
	}

	public static String getForceRankupNoArgs() {
		return SETTINGS.forceRankupMissingArgument;
	}

	public static void setForceRankupNoArgs(String forceRankupNoArgs) {
		SETTINGS.forceRankupMissingArgument = forceRankupNoArgs;
	}

	public static String getTopPrestigesLastPageReached() {
		return SETTINGS.topPrestigesLastPageReached;
	}

	public static void setTopPrestigesLastPageReached(String topPrestigesLastPageReached) {
		SETTINGS.topPrestigesLastPageReached = topPrestigesLastPageReached;
	}

	public static String getTopRebirthsLastPageReached() {
		return SETTINGS.topRebirthsLastPageReached;
	}

	public static void setTopRebirthsLastPageReached(String topRebirthsLastPageReached) {
		SETTINGS.topRebirthsLastPageReached = topRebirthsLastPageReached;
	}

	public static String getRebirthFailed() {
		return SETTINGS.rebirthNotEnoughPrestiges;
	}

	public static void setRebirthFailed(String rebirthFailed) {
		SETTINGS.rebirthNotEnoughPrestiges = rebirthFailed;
	}

	public static String getRankupMax() {
		return SETTINGS.rankupMax;
	}

	public static void setRankupMax(String rankupMax) {
		SETTINGS.rankupMax = rankupMax;
	}

	public static String getPrestigeMax() {
		return SETTINGS.prestigeMax;
	}

	public static void setPrestigeMax(String prestigeMax) {
		SETTINGS.prestigeMax = prestigeMax;
	}

	public static List<String> getNotEnoughBalance() {
		return SETTINGS.notEnoughBalance;
	}

	public static void setNotEnoughBalance(List<String> notEnoughBalance) {
		SETTINGS.notEnoughBalance = notEnoughBalance;
	}

	public static List<String> getNotEnoughBalanceOther() {
		return SETTINGS.notEnoughBalanceOther;
	}

	public static void setNotEnoughBalanceOther(List<String> notEnoughBalanceOther) {
		SETTINGS.notEnoughBalanceOther = notEnoughBalanceOther;
	}

	public static List<String> getPrestigeNotEnoughBalance() {
		return SETTINGS.prestigeNotEnoughBalance;
	}

	public static void setPrestigeNotEnoughBalance(List<String> prestigeNotEnoughBalance) {
		SETTINGS.prestigeNotEnoughBalance = prestigeNotEnoughBalance;
	}

	public static List<String> getLastPrestige() {
		return SETTINGS.lastPrestige;
	}

	public static void setLastPrestige(List<String> lastPrestige) {
		SETTINGS.lastPrestige = lastPrestige;
	}

	public static List<String> getLastPrestigeOther() {
		return SETTINGS.lastPrestigeOther;
	}

	public static void setLastPrestigeOther(List<String> lastPrestigeOther) {
		SETTINGS.lastPrestigeOther = lastPrestigeOther;
	}

	public static List<String> getLastRank() {
		return SETTINGS.lastRank;
	}

	public static void setLastRank(List<String> lastRank) {
		SETTINGS.lastRank = lastRank;
	}

	public static List<String> getRebirthNotEnoughBalance() {
		return SETTINGS.rebirthNotEnoughBalance;
	}

	public static void setRebirthNotEnoughBalance(List<String> rebirthNotEnoughBalance) {
		SETTINGS.rebirthNotEnoughBalance = rebirthNotEnoughBalance;
	}

	public static List<String> getLastRebirth() {
		return SETTINGS.lastRebirth;
	}

	public static void setLastRebirth(List<String> lastRebirth) {
		SETTINGS.lastRebirth = lastRebirth;
	}

	public static List<String> getTopPrestiges() {
		return SETTINGS.topPrestiges;
	}

	public static void setTopPrestiges(List<String> topPrestiges) {
		SETTINGS.topPrestiges = topPrestiges;
	}

	public static List<String> getTopRebirths() {
		return SETTINGS.topRebirths;
	}

	public static void setTopRebirths(List<String> topRebirths) {
		SETTINGS.topRebirths = topRebirths;
	}

	public static String getDataConversionSuccess() {
		return SETTINGS.dataConversionSuccess;
	}

	public static void setDataConversionSuccess(String dataConversionSuccess) {
		SETTINGS.dataConversionSuccess = dataConversionSuccess;
	}

	public static String getDataConversionFail() {
		return SETTINGS.dataConversionFail;
	}

	public static void setDataConversionFail(String dataConversionFail) {
		SETTINGS.dataConversionFail = dataConversionFail;
	}

	public static String getDataConversion() {
		return SETTINGS.dataConversion;
	}

	public static void setDataConversion(String dataConversion) {
		SETTINGS.dataConversion = dataConversion;
	}

	public static class MessagesSettings extends Settings {

		private String noPermission, unknownPlayer, deleteRank, setRank, resetRank, createRank, setNextRank,
				setRankDisplay, setRankCost, deletePlayerRank, addRankCommand, addRankBroadcast, addRankMsg,
				setDefaultRank, setLastRank, setPlayerPath, setDefaultPath, setRankPath, reload, save, unknownPath,
				unknownRank, unknownPrestige, unknownRebirth, setFirstRebirth, setLastRebirth, setRebirth, resetRebirth,
				createRebirth, setRebirthDisplay, setRebirthCost, playerOnlyCommand, disallowedWorld, rankup,
				rankupOther, rankupOtherRecipient, deletePrestige, setFirstPrestige, setLastPrestige, setPrestige,
				resetPrestige, createPrestige, setNextPrestige, setPrestigeDisplay, setPrestigeCost, prestige,
				disallowedPrestige, disallowedRebirth, deletePlayerPrestige, deletePlayerRebirth, forceRankup,
				forceRankupLastRank, forceRankupNoPermission, rankupNoPermission, rankupOtherNoPermission,
				autoRankupEnabled, autoRankupDisabled, autoRankupEnabledOther, autoRankupDisabledOther,
				autoRankupNoPermission, autoRankupLastRank, autoPrestigeEnabled, autoPrestigeDisabled,
				autoRebirthEnabled, autoRebirthDisabled, rebirth, commandSpam, rankupMaxIsOn, prestigeMaxIsOn,
				ranksListLastPageReached, ranksListInvalidPage, prestigeLastLastPageReached,
				prestigesListLastPageReached, prestigesListInvalidPage, rebirthsListLastPageReached,
				rebirthsListInvalidPage, rankListConsole, prestigeListConsole, rebirthListConsole,
				forceRankupMissingArgument, topPrestigesLastPageReached, topRebirthsLastPageReached,
				rebirthNotEnoughPrestiges, rankupMax, prestigeMax, dataConversionSuccess, dataConversionFail,
				dataConversion;

		private List<String> notEnoughBalance, notEnoughBalanceOther, prestigeNotEnoughBalance, lastPrestige,
				lastPrestigeOther, lastRank, rebirthNotEnoughBalance, lastRebirth, topPrestiges, topRebirths;

		public MessagesSettings() {
			super("Messages", "messages.yml");
			setup();
		}

		@Override
		public void setup() {
			notEnoughBalance = getStringList("not-enough-balance", true);
			notEnoughBalanceOther = getStringList("not-enough-balance-other", true);
			prestigeNotEnoughBalance = getStringList("prestige-not-enough-balance", true);
			lastPrestige = getStringList("last-prestige", true);
			lastPrestigeOther = getStringList("last-prestige-other", true);
			lastRank = getStringList("last-rank", true);
			rebirthNotEnoughBalance = getStringList("rebirth-not-enough-balance", true);
			lastRebirth = getStringList("last-rebirth", true);
			topPrestiges = getStringList("top-prestiges", true);
			topRebirths = getStringList("top-rebirths", true);
			noPermission = getString("no-permission", true);
			unknownPlayer = getString("unknown-player", true);
			deleteRank = getString("delete-rank", true);
			setRank = getString("set-rank", true);
			resetRank = getString("reset-rank", true);
			createRank = getString("create-rank", true);
			setNextRank = getString("set-next-rank", true);
			setRankDisplay = getString("set-rank-display", true);
			setRankCost = getString("set-rank-cost", true);
			deletePlayerRank = getString("delete-player-rank", true);
			addRankCommand = getString("add-rank-command", true);
			addRankBroadcast = getString("add-rank-broadcast", true);
			addRankMsg = getString("add-rank-message", true);
			setDefaultRank = getString("set-default-rank", true);
			setLastRank = getString("set-last-rank", true);
			setPlayerPath = getString("set-player-path", true);
			setDefaultPath = getString("set-default-path", true);
			setRankPath = getString("set-rank-path", true);
			reload = getString("plugin-reload", true);
			save = getString("plugin-save", true);
			unknownPath = getString("unknown-path", true);
			unknownRank = getString("unknown-rank", true);
			unknownPrestige = getString("unknown-prestige", true);
			unknownRebirth = getString("unknown-rebirth", true);
			setFirstRebirth = getString("set-first-rebirth", true);
			setLastRebirth = getString("set-last-rebirth", true);
			setRebirth = getString("set-rebirth", true);
			resetRebirth = getString("reset-rebirth", true);
			createRebirth = getString("create-rebirth", true);
			setRebirthDisplay = getString("set-rebirth-display", true);
			setRebirthCost = getString("set-rebirth-cost", true);
			playerOnlyCommand = getString("player-only-command", true);
			disallowedWorld = getString("disallowed-world", true);
			rankup = getString("rankup", true);
			rankupOther = getString("rankup-other", true);
			rankupOtherRecipient = getString("rankup-other-recipient", true);
			deletePrestige = getString("delete-prestige", true);
			setFirstPrestige = getString("set-first-prestige", true);
			setLastPrestige = getString("set-last-prestige", true);
			setPrestige = getString("set-prestige", true);
			resetPrestige = getString("reset-prestige", true);
			createPrestige = getString("create-prestige", true);
			setNextPrestige = getString("set-next-prestige", true);
			setPrestigeDisplay = getString("set-prestige-display", true);
			setPrestigeCost = getString("set-prestige-cost", true);
			prestige = getString("prestige", true);
			disallowedPrestige = getString("disallowed-prestige", true);
			disallowedRebirth = getString("disallowed-rebirth", true);
			deletePlayerPrestige = getString("delete-player-prestige", true);
			deletePlayerRebirth = getString("delete-player-rebirth", true);
			forceRankup = getString("force-rankup", true);
			forceRankupLastRank = getString("force-rankup-last-rank", true);
			forceRankupNoPermission = getString("force-rankup-no-permission", true);
			rankupNoPermission = getString("rankup-no-permission", true);
			rankupOtherNoPermission = getString("rankup-other-no-permission", true);
			autoRankupEnabled = getString("auto-rankup-enabled", true);
			autoRankupDisabled = getString("auto-rankup-disabled", true);
			autoRankupEnabledOther = getString("auto-rankup-enabled-other", true);
			autoRankupDisabledOther = getString("auto-rankup-disabled-other", true);
			autoRankupNoPermission = getString("auto-rankup-no-permission", true);
			autoRankupLastRank = getString("auto-rankup-last-rank", true);
			autoPrestigeEnabled = getString("auto-prestige-enabled", true);
			autoPrestigeDisabled = getString("auto-prestige-disabled", true);
			autoRebirthEnabled = getString("auto-rebirth-enabled", true);
			autoRebirthDisabled = getString("auto-rebirth-disabled", true);
			rebirth = getString("rebirth", true);
			rankupMaxIsOn = getString("rankupmax-is-on", true);
			prestigeMaxIsOn = getString("prestigemax-is-on", true);
			ranksListLastPageReached = getString("ranks-list-last-page-reached", true);
			ranksListInvalidPage = getString("ranks-list-invalid-page", true);
			prestigesListLastPageReached = getString("prestiges-list-last-page-reached", true);
			prestigesListInvalidPage = getString("prestiges-list-invalid-page", true);
			rebirthsListLastPageReached = getString("rebirths-list-last-page-reached", true);
			rebirthsListInvalidPage = getString("rebirths-list-invalid-page", true);
			forceRankupMissingArgument = getString("force-rankup-missing-argument", true);
			topPrestigesLastPageReached = getString("top-prestiges-last-page-reached", true);
			topRebirthsLastPageReached = getString("top-rebirths-last-page-reached", true);
			rebirthNotEnoughPrestiges = getString("rebirth-not-enough-prestiges", true);
			rankupMax = getString("rankupmax", true);
			prestigeMax = getString("prestigemax", true);
			dataConversionSuccess = getString("data-conversion-success", true);
			dataConversionFail = getString("data-conversion-fail", true);
			dataConversion = getString("data-conversion", true);
		}

		public String getNoPermission() {
			return noPermission;
		}

		public void setNoPermission(String noPermission) {
			this.noPermission = noPermission;
		}

		public String getUnknownPlayer() {
			return unknownPlayer;
		}

		public void setUnknownPlayer(String unknownPlayer) {
			this.unknownPlayer = unknownPlayer;
		}

		public String getDeleteRank() {
			return deleteRank;
		}

		public void setDeleteRank(String deleteRank) {
			this.deleteRank = deleteRank;
		}

		public String getSetRank() {
			return setRank;
		}

		public void setSetRank(String setRank) {
			this.setRank = setRank;
		}

		public String getResetRank() {
			return resetRank;
		}

		public void setResetRank(String resetRank) {
			this.resetRank = resetRank;
		}

		public String getCreateRank() {
			return createRank;
		}

		public void setCreateRank(String createRank) {
			this.createRank = createRank;
		}

		public String getSetNextRank() {
			return setNextRank;
		}

		public void setSetNextRank(String setNextRank) {
			this.setNextRank = setNextRank;
		}

		public String getSetRankDisplay() {
			return setRankDisplay;
		}

		public void setSetRankDisplay(String setRankDisplay) {
			this.setRankDisplay = setRankDisplay;
		}

		public String getSetRankCost() {
			return setRankCost;
		}

		public void setSetRankCost(String setRankCost) {
			this.setRankCost = setRankCost;
		}

		public String getDeletePlayerRank() {
			return deletePlayerRank;
		}

		public void setDeletePlayerRank(String deletePlayerRank) {
			this.deletePlayerRank = deletePlayerRank;
		}

		public String getAddRankCommand() {
			return addRankCommand;
		}

		public void setAddRankCommand(String addRankCommand) {
			this.addRankCommand = addRankCommand;
		}

		public String getAddRankBroadcast() {
			return addRankBroadcast;
		}

		public void setAddRankBroadcast(String addRankBroadcast) {
			this.addRankBroadcast = addRankBroadcast;
		}

		public String getAddRankMsg() {
			return addRankMsg;
		}

		public void setAddRankMsg(String addRankMsg) {
			this.addRankMsg = addRankMsg;
		}

		public String getSetDefaultRank() {
			return setDefaultRank;
		}

		public void setSetDefaultRank(String setDefaultRank) {
			this.setDefaultRank = setDefaultRank;
		}

		public String getSetLastRank() {
			return setLastRank;
		}

		public void setSetLastRank(String setLastRank) {
			this.setLastRank = setLastRank;
		}

		public String getSetPlayerPath() {
			return setPlayerPath;
		}

		public void setSetPlayerPath(String setPlayerPath) {
			this.setPlayerPath = setPlayerPath;
		}

		public String getSetDefaultPath() {
			return setDefaultPath;
		}

		public void setSetDefaultPath(String setDefaultPath) {
			this.setDefaultPath = setDefaultPath;
		}

		public String getSetRankPath() {
			return setRankPath;
		}

		public void setSetRankPath(String setRankPath) {
			this.setRankPath = setRankPath;
		}

		public String getReload() {
			return reload;
		}

		public void setReload(String reload) {
			this.reload = reload;
		}

		public String getSave() {
			return save;
		}

		public void setSave(String save) {
			this.save = save;
		}

		public String getUnknownPath() {
			return unknownPath;
		}

		public void setUnknownPath(String unknownPath) {
			this.unknownPath = unknownPath;
		}

		public String getUnknownRank() {
			return unknownRank;
		}

		public void setUnknownRank(String unknownRank) {
			this.unknownRank = unknownRank;
		}

		public String getUnknownPrestige() {
			return unknownPrestige;
		}

		public void setUnknownPrestige(String unknownPrestige) {
			this.unknownPrestige = unknownPrestige;
		}

		public String getUnknownRebirth() {
			return unknownRebirth;
		}

		public void setUnknownRebirth(String unknownRebirth) {
			this.unknownRebirth = unknownRebirth;
		}

		public String getSetFirstRebirth() {
			return setFirstRebirth;
		}

		public void setSetFirstRebirth(String setFirstRebirth) {
			this.setFirstRebirth = setFirstRebirth;
		}

		public String getSetLastRebirth() {
			return setLastRebirth;
		}

		public void setSetLastRebirth(String setLastRebirth) {
			this.setLastRebirth = setLastRebirth;
		}

		public String getSetRebirth() {
			return setRebirth;
		}

		public void setSetRebirth(String setRebirth) {
			this.setRebirth = setRebirth;
		}

		public String getResetRebirth() {
			return resetRebirth;
		}

		public void setResetRebirth(String resetRebirth) {
			this.resetRebirth = resetRebirth;
		}

		public String getCreateRebirth() {
			return createRebirth;
		}

		public void setCreateRebirth(String createRebirth) {
			this.createRebirth = createRebirth;
		}

		public String getSetRebirthDisplay() {
			return setRebirthDisplay;
		}

		public void setSetRebirthDisplay(String setRebirthDisplay) {
			this.setRebirthDisplay = setRebirthDisplay;
		}

		public String getSetRebirthCost() {
			return setRebirthCost;
		}

		public void setSetRebirthCost(String setRebirthCost) {
			this.setRebirthCost = setRebirthCost;
		}

		public String getPlayerOnlyCommand() {
			return playerOnlyCommand;
		}

		public void setPlayerOnlyCommand(String playerOnlyCommand) {
			this.playerOnlyCommand = playerOnlyCommand;
		}

		public String getDisallowedWorld() {
			return disallowedWorld;
		}

		public void setDisallowedWorld(String disallowedWorld) {
			this.disallowedWorld = disallowedWorld;
		}

		public String getRankup() {
			return rankup;
		}

		public void setRankup(String rankup) {
			this.rankup = rankup;
		}

		public String getDeletePrestige() {
			return deletePrestige;
		}

		public void setDeletePrestige(String deletePrestige) {
			this.deletePrestige = deletePrestige;
		}

		public String getSetFirstPrestige() {
			return setFirstPrestige;
		}

		public void setSetFirstPrestige(String setFirstPrestige) {
			this.setFirstPrestige = setFirstPrestige;
		}

		public String getSetLastPrestige() {
			return setLastPrestige;
		}

		public void setSetLastPrestige(String setLastPrestige) {
			this.setLastPrestige = setLastPrestige;
		}

		public String getSetPrestige() {
			return setPrestige;
		}

		public void setSetPrestige(String setPrestige) {
			this.setPrestige = setPrestige;
		}

		public String getResetPrestige() {
			return resetPrestige;
		}

		public void setResetPrestige(String resetPrestige) {
			this.resetPrestige = resetPrestige;
		}

		public String getCreatePrestige() {
			return createPrestige;
		}

		public void setCreatePrestige(String createPrestige) {
			this.createPrestige = createPrestige;
		}

		public String getSetNextPrestige() {
			return setNextPrestige;
		}

		public void setSetNextPrestige(String setNextPrestige) {
			this.setNextPrestige = setNextPrestige;
		}

		public String getSetPrestigeDisplay() {
			return setPrestigeDisplay;
		}

		public void setSetPrestigeDisplay(String setPrestigeDisplay) {
			this.setPrestigeDisplay = setPrestigeDisplay;
		}

		public String getSetPrestigeCost() {
			return setPrestigeCost;
		}

		public void setSetPrestigeCost(String setPrestigeCost) {
			this.setPrestigeCost = setPrestigeCost;
		}

		public String getPrestige() {
			return prestige;
		}

		public void setPrestige(String prestige) {
			this.prestige = prestige;
		}

		public String getDisallowedPrestige() {
			return disallowedPrestige;
		}

		public void setDisallowedPrestige(String disallowedPrestige) {
			this.disallowedPrestige = disallowedPrestige;
		}

		public String getDisallowedRebirth() {
			return disallowedRebirth;
		}

		public void setDisallowedRebirth(String disallowedRebirth) {
			this.disallowedRebirth = disallowedRebirth;
		}

		public String getDeletePlayerPrestige() {
			return deletePlayerPrestige;
		}

		public void setDeletePlayerPrestige(String deletePlayerPrestige) {
			this.deletePlayerPrestige = deletePlayerPrestige;
		}

		public String getDeletePlayerRebirth() {
			return deletePlayerRebirth;
		}

		public void setDeletePlayerRebirth(String deletePlayerRebirth) {
			this.deletePlayerRebirth = deletePlayerRebirth;
		}

		public String getForceRankup() {
			return forceRankup;
		}

		public void setForceRankup(String forceRankup) {
			this.forceRankup = forceRankup;
		}

		public String getForceRankupLastRank() {
			return forceRankupLastRank;
		}

		public void setForceRankupLastRank(String forceRankupLastRank) {
			this.forceRankupLastRank = forceRankupLastRank;
		}

		public String getForceRankupNoPermission() {
			return forceRankupNoPermission;
		}

		public void setForceRankupNoPermission(String forceRankupNoPermission) {
			this.forceRankupNoPermission = forceRankupNoPermission;
		}

		public String getRankupNoPermission() {
			return rankupNoPermission;
		}

		public void setRankupNoPermission(String rankupNoPermission) {
			this.rankupNoPermission = rankupNoPermission;
		}

		public String getRankupOtherNoPermission() {
			return rankupOtherNoPermission;
		}

		public void setRankupOtherNoPermission(String rankupOtherNoPermission) {
			this.rankupOtherNoPermission = rankupOtherNoPermission;
		}

		public String getAutoRankupEnabled() {
			return autoRankupEnabled;
		}

		public void setAutoRankupEnabled(String autoRankupEnabled) {
			this.autoRankupEnabled = autoRankupEnabled;
		}

		public String getAutoRankupDisabled() {
			return autoRankupDisabled;
		}

		public void setAutoRankupDisabled(String autoRankupDisabled) {
			this.autoRankupDisabled = autoRankupDisabled;
		}

		public String getAutoRankupEnabledOther() {
			return autoRankupEnabledOther;
		}

		public void setAutoRankupEnabledOther(String autoRankupEnabledOther) {
			this.autoRankupEnabledOther = autoRankupEnabledOther;
		}

		public String getAutoRankupDisabledOther() {
			return autoRankupDisabledOther;
		}

		public void setAutoRankupDisabledOther(String autoRankupDisabledOther) {
			this.autoRankupDisabledOther = autoRankupDisabledOther;
		}

		public String getAutoRankupNoPermission() {
			return autoRankupNoPermission;
		}

		public void setAutoRankupNoPermission(String autoRankupNoPermission) {
			this.autoRankupNoPermission = autoRankupNoPermission;
		}

		public String getAutoRankupLastRank() {
			return autoRankupLastRank;
		}

		public void setAutoRankupLastRank(String autoRankupLastRank) {
			this.autoRankupLastRank = autoRankupLastRank;
		}

		public String getAutoPrestigeEnabled() {
			return autoPrestigeEnabled;
		}

		public void setAutoPrestigeEnabled(String autoPrestigeEnabled) {
			this.autoPrestigeEnabled = autoPrestigeEnabled;
		}

		public String getAutoPrestigeDisabled() {
			return autoPrestigeDisabled;
		}

		public void setAutoPrestigeDisabled(String autoPrestigeDisabled) {
			this.autoPrestigeDisabled = autoPrestigeDisabled;
		}

		public String getAutoRebirthEnabled() {
			return autoRebirthEnabled;
		}

		public void setAutoRebirthEnabled(String autoRebirthEnabled) {
			this.autoRebirthEnabled = autoRebirthEnabled;
		}

		public String getAutoRebirthDisabled() {
			return autoRebirthDisabled;
		}

		public void setAutoRebirthDisabled(String autoRebirthDisabled) {
			this.autoRebirthDisabled = autoRebirthDisabled;
		}

		public String getRebirth() {
			return rebirth;
		}

		public void setRebirth(String rebirth) {
			this.rebirth = rebirth;
		}

		public String getCommandSpam() {
			return commandSpam;
		}

		public void setCommandSpam(String commandSpam) {
			this.commandSpam = commandSpam;
		}

		public String getRankupMaxIsOn() {
			return rankupMaxIsOn;
		}

		public void setRankupMaxIsOn(String rankupMaxIsOn) {
			this.rankupMaxIsOn = rankupMaxIsOn;
		}

		public String getPrestigeMaxIsOn() {
			return prestigeMaxIsOn;
		}

		public void setPrestigeMaxIsOn(String prestigeMaxIsOn) {
			this.prestigeMaxIsOn = prestigeMaxIsOn;
		}

		public String getRankListLastPageReached() {
			return ranksListLastPageReached;
		}

		public void setRankListLastPageReached(String rankListLastPageReached) {
			this.ranksListLastPageReached = rankListLastPageReached;
		}

		public String getRankListInvalidPage() {
			return ranksListInvalidPage;
		}

		public void setRankListInvalidPage(String rankListInvalidPage) {
			this.ranksListInvalidPage = rankListInvalidPage;
		}

		public String getPrestigeLastLastPageReached() {
			return prestigeLastLastPageReached;
		}

		public void setPrestigeLastLastPageReached(String prestigeLastLastPageReached) {
			this.prestigeLastLastPageReached = prestigeLastLastPageReached;
		}

		public String getPrestigeListLastPageReached() {
			return prestigesListLastPageReached;
		}

		public void setPrestigeListLastPageReached(String prestigeListLastPageReached) {
			this.prestigesListLastPageReached = prestigeListLastPageReached;
		}

		public String getPrestigeListInvalidPage() {
			return prestigesListInvalidPage;
		}

		public void setPrestigeListInvalidPage(String prestigeListInvalidPage) {
			this.prestigesListInvalidPage = prestigeListInvalidPage;
		}

		public String getRebirthListLastPageReached() {
			return rebirthsListLastPageReached;
		}

		public void setRebirthListLastPageReached(String rebirthListLastPageReached) {
			this.rebirthsListLastPageReached = rebirthListLastPageReached;
		}

		public String getRebirthListInvalidPage() {
			return rebirthsListInvalidPage;
		}

		public void setRebirthListInvalidPage(String rebirthListInvalidPage) {
			this.rebirthsListInvalidPage = rebirthListInvalidPage;
		}

		public String getRankListConsole() {
			return rankListConsole;
		}

		public void setRankListConsole(String rankListConsole) {
			this.rankListConsole = rankListConsole;
		}

		public String getPrestigeListConsole() {
			return prestigeListConsole;
		}

		public void setPrestigeListConsole(String prestigeListConsole) {
			this.prestigeListConsole = prestigeListConsole;
		}

		public String getRebirthListConsole() {
			return rebirthListConsole;
		}

		public void setRebirthListConsole(String rebirthListConsole) {
			this.rebirthListConsole = rebirthListConsole;
		}

		public String getForceRankupNoArgs() {
			return forceRankupMissingArgument;
		}

		public void setForceRankupNoArgs(String forceRankupNoArgs) {
			this.forceRankupMissingArgument = forceRankupNoArgs;
		}

		public String getTopPrestigesLastPageReached() {
			return topPrestigesLastPageReached;
		}

		public void setTopPrestigesLastPageReached(String topPrestigesLastPageReached) {
			this.topPrestigesLastPageReached = topPrestigesLastPageReached;
		}

		public String getTopRebirthsLastPageReached() {
			return topRebirthsLastPageReached;
		}

		public void setTopRebirthsLastPageReached(String topRebirthsLastPageReached) {
			this.topRebirthsLastPageReached = topRebirthsLastPageReached;
		}

		public String getRebirthFailed() {
			return rebirthNotEnoughPrestiges;
		}

		public void setRebirthFailed(String rebirthFailed) {
			this.rebirthNotEnoughPrestiges = rebirthFailed;
		}

		public String getRankupMax() {
			return rankupMax;
		}

		public void setRankupMax(String rankupMax) {
			this.rankupMax = rankupMax;
		}

		public String getPrestigeMax() {
			return prestigeMax;
		}

		public void setPrestigeMax(String prestigeMax) {
			this.prestigeMax = prestigeMax;
		}

		public List<String> getNotEnoughMoney() {
			return notEnoughBalance;
		}

		public void setNotEnoughMoney(List<String> notEnoughMoney) {
			this.notEnoughBalance = notEnoughMoney;
		}

		public List<String> getNotEnoughMoneyOther() {
			return notEnoughBalanceOther;
		}

		public void setNotEnoughMoneyOther(List<String> notEnoughMoneyOther) {
			this.notEnoughBalanceOther = notEnoughMoneyOther;
		}

		public List<String> getPrestigeNotEnoughMoney() {
			return prestigeNotEnoughBalance;
		}

		public void setPrestigeNotEnoughMoney(List<String> prestigeNotEnoughMoney) {
			this.prestigeNotEnoughBalance = prestigeNotEnoughMoney;
		}

		public List<String> getLastPrestige() {
			return lastPrestige;
		}

		public void setLastPrestige(List<String> lastPrestige) {
			this.lastPrestige = lastPrestige;
		}

		public List<String> getLastPrestigeOther() {
			return lastPrestigeOther;
		}

		public void setLastPrestigeOther(List<String> lastPrestigeOther) {
			this.lastPrestigeOther = lastPrestigeOther;
		}

		public List<String> getLastRank() {
			return lastRank;
		}

		public void setLastRank(List<String> lastRank) {
			this.lastRank = lastRank;
		}

		public List<String> getRebirthNotEnoughMoney() {
			return rebirthNotEnoughBalance;
		}

		public void setRebirthNotEnoughMoney(List<String> rebirthNotEnoughMoney) {
			this.rebirthNotEnoughBalance = rebirthNotEnoughMoney;
		}

		public List<String> getLastRebirth() {
			return lastRebirth;
		}

		public void setLastRebirth(List<String> lastRebirth) {
			this.lastRebirth = lastRebirth;
		}

		public List<String> getTopPrestiges() {
			return topPrestiges;
		}

		public void setTopPrestiges(List<String> topPrestiges) {
			this.topPrestiges = topPrestiges;
		}

		public List<String> getTopRebirths() {
			return topRebirths;
		}

		public void setTopRebirths(List<String> topRebirths) {
			this.topRebirths = topRebirths;
		}

		public String getRankupOther() {
			return rankupOther;
		}

		public void setRankupOther(String rankupOther) {
			this.rankupOther = rankupOther;
		}

		public String getRankupOtherRecipient() {
			return rankupOtherRecipient;
		}

		public void setRankupOtherRecipient(String rankupOtherRecipient) {
			this.rankupOtherRecipient = rankupOtherRecipient;
		}

		public String getDataConversionSuccess() {
			return dataConversionSuccess;
		}

		public void setDataConversionSuccess(String dataConversionSuccess) {
			this.dataConversionSuccess = dataConversionSuccess;
		}

		public String getDataConversionFail() {
			return dataConversionFail;
		}

		public void setDataConversionFail(String dataConversionFail) {
			this.dataConversionFail = dataConversionFail;
		}

		public String getDataConversion() {
			return dataConversion;
		}

		public void setDataConversion(String dataConversion) {
			this.dataConversion = dataConversion;
		}
	}

}
