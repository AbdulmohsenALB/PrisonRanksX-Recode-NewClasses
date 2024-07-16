package me.prisonranksx.lists;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.RebirthStorage;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.Rebirth;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.CollectionUtils;
import me.prisonranksx.utils.CollectionUtils.PaginatedList;

public class RebirthsTextList {

	private PrisonRanksX plugin;
	private String rebirthCurrentFormat;
	private String rebirthCompletedFormat;
	private String rebirthOtherFormat;
	private boolean enablePages;
	private int rebirthPerPage;
	private List<String> rebirthWithPagesListFormat;
	private List<String> rebirthListFormat;
	boolean isCustomList;
	private List<String> rebirthListFormatHeader;
	private List<String> rebirthListFormatFooter;

	public RebirthsTextList(PrisonRanksX plugin) {
		this.plugin = plugin;
		setup();
	}

	public void setup() {
		rebirthCurrentFormat = plugin.getRebirthsListSettings().getRebirthCurrentFormat();
		rebirthCompletedFormat = plugin.getRebirthsListSettings().getRebirthCompletedFormat();
		rebirthOtherFormat = plugin.getRebirthsListSettings().getRebirthOtherFormat();
		enablePages = plugin.getRebirthsListSettings().isEnablePages();
		rebirthPerPage = plugin.getRebirthsListSettings().getRebirthPerPage();
		rebirthWithPagesListFormat = plugin.getRebirthsListSettings().getRebirthWithPagesListFormat();
		rebirthListFormat = plugin.getRebirthsListSettings().getRebirthListFormat();
		rebirthListFormatHeader = new ArrayList<>();
		rebirthListFormatFooter = new ArrayList<>();
		int ignoreIndex = rebirthWithPagesListFormat.indexOf("[rebirthslist]");
		if (rebirthListFormatHeader.isEmpty() && rebirthListFormatFooter.isEmpty() && rebirthListFormat.size() > 1) {
			for (int i = 0; i < rebirthWithPagesListFormat.size(); i++) {
				if (ignoreIndex > i)
					rebirthListFormatHeader.add(rebirthWithPagesListFormat.get(i));
				else if (ignoreIndex < i) rebirthListFormatFooter.add(rebirthWithPagesListFormat.get(i));
			}
		}
		if (enablePages) {
			if (!rebirthWithPagesListFormat.contains("[rebirthslist]")) isCustomList = true;
		} else {
			if (!rebirthListFormat.contains("[rebirthslist]")) isCustomList = true;
		}
	}

	/**
	 * Sends rebirths list as configured in config file, if pageNumber is null or
	 * enable pages is false, then a non-paged list will be sent.
	 * 
	 * @param sender     to send to
	 * @param pageNumber number of page
	 */
	public void send(CommandSender sender, @Nullable String pageNumber) {
		if (enablePages && pageNumber != null)
			sendPagedList(sender, pageNumber);
		else
			sendList(sender);
	}

	public void sendList(CommandSender sender) {
		if (isCustomList) {
			List<String> customList = rebirthListFormat;
			if (sender instanceof Player) customList = StringManager.parsePlaceholders(customList, (Player) sender);
			customList.forEach(sender::sendMessage);
		}
		Player p = (Player) sender;
		User user = plugin.getUserController().getUser(UniqueId.getUUID(p));
		String rebirthName = user.getRebirthName();
		List<String> rebirthsCollection = new ArrayList<>(RebirthStorage.getRebirthNames());
		// Header Setup
		List<String> header = new ArrayList<>(rebirthListFormatHeader);

		// Rebirths List Organization
		List<String> currentRebirths = new ArrayList<>(), completedRebirths = new ArrayList<>(),
				otherRebirths = new ArrayList<>(), nonPagedRebirths = new ArrayList<>();
		int currentRebirthIndex = rebirthsCollection.indexOf(rebirthName);
		for (String cyclingRebirthName : rebirthsCollection) {
			Rebirth rebirth = RebirthStorage.getRebirth(cyclingRebirthName);
			// Current Rebirth Format
			if (currentRebirthIndex == rebirthsCollection.indexOf(cyclingRebirthName)) {
				if (rebirth.getNextName() != null) {
					Rebirth nextRebirth = RebirthStorage.getRebirth(rebirth.getNextName());
					String format = StringManager
							.parseAll(rebirthCurrentFormat.replace("%rebirth_name%", cyclingRebirthName)
									.replace("%rebirth_displayname%", rebirth.getDisplayName())
									.replace("%nextrebirth_name%", rebirth.getNextName())
									.replace("%nextrebirth_displayname%", nextRebirth.getDisplayName())
									.replace("%nextrebirth_cost%", String.valueOf(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_formatted%",
											EconomyManager.shortcutFormat(nextRebirth.getCost())),
									p);

					currentRebirths.add(format);
				}
			}
			// Completed Rebirth Format
			if (currentRebirthIndex > rebirthsCollection.indexOf(cyclingRebirthName)) {
				if (rebirth.getNextName() != null) {
					Rebirth nextRebirth = RebirthStorage.getRebirth(rebirth.getNextName());
					String format = StringManager
							.parseAll(rebirthCompletedFormat.replace("%rebirth_name%", cyclingRebirthName)
									.replace("%rebirth_displayname%", rebirth.getDisplayName())
									.replace("%nextrebirth_name%", rebirth.getNextName())
									.replace("%nextrebirth_displayname%", nextRebirth.getDisplayName())
									.replace("%nextrebirth_cost%", String.valueOf(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_formatted%",
											EconomyManager.shortcutFormat(nextRebirth.getCost())),
									p);
					completedRebirths.add(format);
				}
			}
			// Other Rebirth Format
			if (currentRebirthIndex < rebirthsCollection.indexOf(cyclingRebirthName)) {
				if (rebirth.getNextName() != null) {
					Rebirth nextRebirth = RebirthStorage.getRebirth(rebirth.getNextName());
					String format = StringManager
							.parseAll(rebirthOtherFormat.replace("%rebirth_name%", cyclingRebirthName)
									.replace("%rebirth_displayname%", rebirth.getDisplayName())
									.replace("%nextrebirth_name%", rebirth.getNextName())
									.replace("%nextrebirth_displayname%", nextRebirth.getDisplayName())
									.replace("%nextrebirth_cost%", String.valueOf(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_formatted%",
											EconomyManager.shortcutFormat(nextRebirth.getCost())),
									p);
					otherRebirths.add(format);
				}
			}
		}
		nonPagedRebirths.addAll(completedRebirths);
		nonPagedRebirths.addAll(currentRebirths);
		nonPagedRebirths.addAll(otherRebirths);

		List<String> footer = new ArrayList<>(rebirthListFormatFooter);

		header.forEach(sender::sendMessage);
		nonPagedRebirths.forEach(sender::sendMessage);
		footer.forEach(sender::sendMessage);
	}

	public void sendPagedList(CommandSender sender, String pageNumber) {
		if (isCustomList) {
			List<String> customList = CollectionUtils.paginateList(rebirthWithPagesListFormat, rebirthPerPage,
					Integer.parseInt(pageNumber));
			if (sender instanceof Player) customList = StringManager.parsePlaceholders(customList, (Player) sender);
			customList.forEach(sender::sendMessage);
			return;
		}
		Player p = (Player) sender;
		User user = plugin.getUserController().getUser(UniqueId.getUUID(p));
		String rebirthName = user.getRebirthName();
		List<String> rebirthsCollection = new ArrayList<>(RebirthStorage.getRebirthNames());
		PaginatedList paginatedList = CollectionUtils.paginateListCollectable(rebirthsCollection, rebirthPerPage,
				Integer.parseInt(pageNumber));
		int currentPage = paginatedList.getCurrentPage();
		int finalPage = paginatedList.getFinalPage();
		if (currentPage > finalPage) {
			Messages.sendMessage(p, Messages.getRebirthListLastPageReached(),
					s -> s.replace("%page%", String.valueOf(finalPage)));
			return;
		}
		rebirthsCollection = paginatedList.collect();
		// Header Setup
		List<String> header = new ArrayList<>();
		for (String headerLine : rebirthListFormatHeader) header.add(
				headerLine.replace("%currentpage%", pageNumber).replace("%totalpages%", String.valueOf(finalPage)));

		// Rebirths List Organization
		List<String> currentRebirths = new ArrayList<>(), completedRebirths = new ArrayList<>(),
				otherRebirths = new ArrayList<>(), nonPagedRebirths = new ArrayList<>();
		int currentRebirthIndex = rebirthsCollection.indexOf(rebirthName);
		for (String cyclingRebirthName : rebirthsCollection) {
			Rebirth rebirth = RebirthStorage.getRebirth(cyclingRebirthName);

			// Current Rebirth Format
			if (currentRebirthIndex == rebirthsCollection.indexOf(cyclingRebirthName)) {
				if (rebirth.getIndex() == 1) {
					Rebirth nextRebirth = rebirth;
					Rank lastRank = RankStorage.getLastRank(user.getPathName());
					String format = StringManager
							.parseAll(rebirthCurrentFormat.replace("%rebirth_name%", lastRank.getName())
									.replace("%rebirth_displayname%", lastRank.getDisplayName())
									.replace("%nextrebirth_name%", nextRebirth.getName())
									.replace("%nextrebirth_displayname%", nextRebirth.getDisplayName())
									.replace("%nextrebirth_cost%", String.valueOf(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_formatted%",
											EconomyManager.shortcutFormat(nextRebirth.getCost())),
									p);
					currentRebirths.add(format);
				}
				if (rebirth.getNextName() != null) {
					Rebirth nextRebirth = RebirthStorage.getRebirth(rebirth.getNextName());
					String format = StringManager
							.parseAll(rebirthCurrentFormat.replace("%rebirth_name%", cyclingRebirthName)
									.replace("%rebirth_displayname%", rebirth.getDisplayName())
									.replace("%nextrebirth_name%", rebirth.getNextName())
									.replace("%nextrebirth_displayname%", nextRebirth.getDisplayName())
									.replace("%nextrebirth_cost%", String.valueOf(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_formatted%",
											EconomyManager.shortcutFormat(nextRebirth.getCost())),
									p);

					currentRebirths.add(format);
				}

			}
			// Completed Rebirth Format
			if (currentRebirthIndex > rebirthsCollection.indexOf(cyclingRebirthName)) {
				if (rebirth.getIndex() == 1) {
					Rebirth nextRebirth = rebirth;
					Rank lastRank = RankStorage.getLastRank(user.getPathName());
					String format = StringManager
							.parseAll(rebirthCompletedFormat.replace("%rebirth_name%", lastRank.getName())
									.replace("%rebirth_displayname%", lastRank.getDisplayName())
									.replace("%nextrebirth_name%", nextRebirth.getName())
									.replace("%nextrebirth_displayname%", nextRebirth.getDisplayName())
									.replace("%nextrebirth_cost%", String.valueOf(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_formatted%",
											EconomyManager.shortcutFormat(nextRebirth.getCost())),
									p);
					completedRebirths.add(format);
				}
				if (rebirth.getNextName() != null) {
					Rebirth nextRebirth = RebirthStorage.getRebirth(rebirth.getNextName());
					String format = StringManager
							.parseAll(rebirthCompletedFormat.replace("%rebirth_name%", cyclingRebirthName)
									.replace("%rebirth_displayname%", rebirth.getDisplayName())
									.replace("%nextrebirth_name%", rebirth.getNextName())
									.replace("%nextrebirth_displayname%", nextRebirth.getDisplayName())
									.replace("%nextrebirth_cost%", String.valueOf(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_formatted%",
											EconomyManager.shortcutFormat(nextRebirth.getCost())),
									p);
					completedRebirths.add(format);
				}
			}
			// Other Rebirth Format
			if (currentRebirthIndex < rebirthsCollection.indexOf(cyclingRebirthName)) {
				if (rebirth.getIndex() == 1) {
					Rebirth nextRebirth = rebirth;
					Rank lastRank = RankStorage.getLastRank(user.getPathName());
					String format = StringManager
							.parseAll(rebirthOtherFormat.replace("%rebirth_name%", lastRank.getName())
									.replace("%rebirth_displayname%", lastRank.getDisplayName())
									.replace("%nextrebirth_name%", nextRebirth.getName())
									.replace("%nextrebirth_displayname%", nextRebirth.getDisplayName())
									.replace("%nextrebirth_cost%", String.valueOf(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_formatted%",
											EconomyManager.shortcutFormat(nextRebirth.getCost())),
									p);
					otherRebirths.add(format);
				}
				if (rebirth.getNextName() != null) {
					Rebirth nextRebirth = RebirthStorage.getRebirth(rebirth.getNextName());
					String format = StringManager
							.parseAll(rebirthOtherFormat.replace("%rebirth_name%", cyclingRebirthName)
									.replace("%rebirth_displayname%", rebirth.getDisplayName())
									.replace("%nextrebirth_name%", rebirth.getNextName())
									.replace("%nextrebirth_displayname%", nextRebirth.getDisplayName())
									.replace("%nextrebirth_cost%", String.valueOf(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextRebirth.getCost()))
									.replace("%nextrebirth_cost_formatted%",
											EconomyManager.shortcutFormat(nextRebirth.getCost())),
									p);
					otherRebirths.add(format);
				}
			}
		}
		nonPagedRebirths.addAll(completedRebirths);
		nonPagedRebirths.addAll(currentRebirths);
		nonPagedRebirths.addAll(otherRebirths);

		List<String> footer = new ArrayList<>();
		for (String footerLine : rebirthListFormatFooter) footer.add(
				footerLine.replace("%currentpage%", pageNumber).replace("%totalpages%", String.valueOf(finalPage)));

		header.forEach(sender::sendMessage);
		nonPagedRebirths.forEach(sender::sendMessage);
		footer.forEach(sender::sendMessage);
	}

}
