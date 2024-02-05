package me.prisonranksx.lists;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.CollectionUtils;
import me.prisonranksx.utils.CollectionUtils.PaginatedList;

public class RanksTextList {

	private PrisonRanksX plugin;
	private String rankCurrentFormat;
	private String rankCompletedFormat;
	private String rankOtherFormat;
	private boolean enablePages;
	private int rankPerPage;
	private List<String> rankWithPagesListFormat;
	private List<String> rankListFormat;
	boolean isCustomList;
	private List<String> rankListFormatHeader;
	private List<String> rankListFormatFooter;

	public RanksTextList(PrisonRanksX plugin) {
		this.plugin = plugin;
		setup();
	}

	public void setup() {
		rankCurrentFormat = plugin.getRanksListSettings().getRankCurrentFormat();
		rankCompletedFormat = plugin.getRanksListSettings().getRankCompletedFormat();
		rankOtherFormat = plugin.getRanksListSettings().getRankOtherFormat();
		enablePages = plugin.getRanksListSettings().isEnablePages();
		rankPerPage = plugin.getRanksListSettings().getRankPerPage();
		rankWithPagesListFormat = plugin.getRanksListSettings().getRankWithPagesListFormat();
		rankListFormat = plugin.getRanksListSettings().getRankListFormat();
		rankListFormatHeader = new ArrayList<>();
		rankListFormatFooter = new ArrayList<>();
		int ignoreIndex = rankWithPagesListFormat.indexOf("[rankslist]");
		if (rankListFormatHeader.isEmpty() && rankListFormatFooter.isEmpty() && rankListFormat.size() > 1) {
			for (int i = 0; i < rankWithPagesListFormat.size(); i++) {
				if (ignoreIndex > i)
					rankListFormatHeader.add(rankWithPagesListFormat.get(i));
				else if (ignoreIndex < i) rankListFormatFooter.add(rankWithPagesListFormat.get(i));
			}
		}
		if (enablePages) {
			if (!rankWithPagesListFormat.contains("[rankslist]")) isCustomList = true;
		} else {
			if (!rankListFormat.contains("[rankslist]")) isCustomList = true;
		}
	}

	/**
	 * Sends ranks list as configured in config file, if pageNumber is null or
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
			List<String> customList = rankListFormat;
			if (sender instanceof Player) customList = StringManager.parsePlaceholders(customList, (Player) sender);
			customList.forEach(sender::sendMessage);
		}
		Player p = (Player) sender;
		User user = plugin.getUserController().getUser(UniqueId.getUUID(p));
		String pathName = user.getPathName();
		String rankName = user.getRankName();
		List<String> ranksCollection = new ArrayList<>(RankStorage.getPathRankNames(pathName));
		// Header Setup
		List<String> header = new ArrayList<>(rankListFormatHeader);

		// Ranks List Organization
		List<String> currentRanks = new ArrayList<>(), completedRanks = new ArrayList<>(),
				otherRanks = new ArrayList<>(), nonPagedRanks = new ArrayList<>();
		int currentRankIndex = ranksCollection.indexOf(rankName);
		for (String cyclingRankName : ranksCollection) {
			Rank rank = RankStorage.getRank(cyclingRankName, pathName);
			// Current Rank Format
			if (currentRankIndex == ranksCollection.indexOf(cyclingRankName)) {
				if (rank.getNextName() != null) {
					Rank nextRank = RankStorage.getRank(rank.getNextName(), pathName);
					String format = StringManager.parseAll(rankCurrentFormat.replace("%rank_name%", cyclingRankName)
							.replace("%rank_displayname%", rank.getDisplayName())
							.replace("%nextrank_name%", rank.getNextName())
							.replace("%nextrank_displayname%", nextRank.getDisplayName())
							.replace("%nextrank_cost%", String.valueOf(PRXAPI.getRankFinalCost(nextRank, p)))
							.replace("%nextrank_cost_formatted%",
									EconomyManager.shortcutFormat(PRXAPI.getRankFinalCost(nextRank, p))),
							p);

					currentRanks.add(format);
				}
			}
			// Completed Rank Format
			if (currentRankIndex > ranksCollection.indexOf(cyclingRankName)) {
				if (rank.getNextName() != null) {
					Rank nextRank = RankStorage.getRank(rank.getNextName(), pathName);
					String format = StringManager.parseAll(rankCompletedFormat.replace("%rank_name%", cyclingRankName)
							.replace("%rank_displayname%", rank.getDisplayName())
							.replace("%nextrank_name%", rank.getNextName())
							.replace("%nextrank_displayname%", nextRank.getDisplayName())
							.replace("%nextrank_cost%", String.valueOf(PRXAPI.getRankFinalCost(nextRank, p)))
							.replace("%nextrank_cost_formatted%",
									EconomyManager.shortcutFormat(PRXAPI.getRankFinalCost(nextRank, p))),
							p);
					completedRanks.add(format);
				}
			}
			// Other Rank Format
			if (currentRankIndex < ranksCollection.indexOf(cyclingRankName)) {
				if (rank.getNextName() != null) {
					Rank nextRank = RankStorage.getRank(rank.getNextName(), pathName);
					String format = StringManager.parseAll(rankOtherFormat.replace("%rank_name%", cyclingRankName)
							.replace("%rank_displayname%", rank.getDisplayName())
							.replace("%nextrank_name%", rank.getNextName())
							.replace("%nextrank_displayname%", nextRank.getDisplayName())
							.replace("%nextrank_cost%", String.valueOf(PRXAPI.getRankFinalCost(nextRank, p)))
							.replace("%nextrank_cost_formatted%",
									EconomyManager.shortcutFormat(PRXAPI.getRankFinalCost(nextRank, p))),
							p);
					otherRanks.add(format);
				}
			}
		}
		nonPagedRanks.addAll(completedRanks);
		nonPagedRanks.addAll(currentRanks);
		nonPagedRanks.addAll(otherRanks);

		List<String> footer = new ArrayList<>(rankListFormatFooter);

		header.forEach(sender::sendMessage);
		nonPagedRanks.forEach(sender::sendMessage);
		footer.forEach(sender::sendMessage);
	}

	public void sendPagedList(CommandSender sender, String pageNumber) {
		if (isCustomList) {
			List<String> customList = CollectionUtils.paginateList(rankWithPagesListFormat, rankPerPage,
					Integer.parseInt(pageNumber));
			if (sender instanceof Player) customList = StringManager.parsePlaceholders(customList, (Player) sender);
			customList.forEach(sender::sendMessage);
			return;
		}
		Player p = (Player) sender;
		User user = plugin.getUserController().getUser(UniqueId.getUUID(p));
		String pathName = user.getPathName();
		String rankName = user.getRankName();
		List<String> ranksCollection = new ArrayList<>(RankStorage.getPathRankNames(pathName));
		PaginatedList paginatedList = CollectionUtils.paginateListCollectable(ranksCollection, rankPerPage,
				Integer.parseInt(pageNumber));
		int currentPage = paginatedList.getCurrentPage();
		int finalPage = paginatedList.getFinalPage();
		if (currentPage > finalPage) {
			Messages.sendMessage(p, Messages.getRankListLastPageReached(),
					s -> s.replace("%page%", String.valueOf(finalPage)));
			return;
		}
		ranksCollection = paginatedList.collect();
		// Header Setup
		List<String> header = new ArrayList<>();
		for (String headerLine : rankListFormatHeader) header.add(
				headerLine.replace("%currentpage%", pageNumber).replace("%totalpages%", String.valueOf(finalPage)));

		// Ranks List Organization
		List<String> currentRanks = new ArrayList<>(), completedRanks = new ArrayList<>(),
				otherRanks = new ArrayList<>(), nonPagedRanks = new ArrayList<>();
		int currentRankIndex = ranksCollection.indexOf(rankName);
		for (String cyclingRankName : ranksCollection) {
			Rank rank = RankStorage.getRank(cyclingRankName, pathName);
			// Current Rank Format
			if (currentRankIndex == ranksCollection.indexOf(cyclingRankName)) {
				if (rank.getNextName() != null) {
					Rank nextRank = RankStorage.getRank(rank.getNextName(), pathName);
					String format = StringManager.parseAll(rankCurrentFormat.replace("%rank_name%", cyclingRankName)
							.replace("%rank_displayname%", rank.getDisplayName())
							.replace("%nextrank_name%", rank.getNextName())
							.replace("%nextrank_displayname%", nextRank.getDisplayName())
							.replace("%nextrank_cost%", String.valueOf(PRXAPI.getRankFinalCost(nextRank, p)))
							.replace("%nextrank_cost_formatted%",
									EconomyManager.shortcutFormat(PRXAPI.getRankFinalCost(nextRank, p))),
							p);

					currentRanks.add(format);
				}
			}
			// Completed Rank Format
			if (currentRankIndex > ranksCollection.indexOf(cyclingRankName)) {
				if (rank.getNextName() != null) {
					Rank nextRank = RankStorage.getRank(rank.getNextName(), pathName);
					String format = StringManager.parseAll(rankCompletedFormat.replace("%rank_name%", cyclingRankName)
							.replace("%rank_displayname%", rank.getDisplayName())
							.replace("%nextrank_name%", rank.getNextName())
							.replace("%nextrank_displayname%", nextRank.getDisplayName())
							.replace("%nextrank_cost%", String.valueOf(PRXAPI.getRankFinalCost(nextRank, p)))
							.replace("%nextrank_cost_formatted%",
									EconomyManager.shortcutFormat(PRXAPI.getRankFinalCost(nextRank, p))),
							p);
					completedRanks.add(format);
				}
			}
			// Other Rank Format
			if (currentRankIndex < ranksCollection.indexOf(cyclingRankName)) {
				if (rank.getNextName() != null) {
					Rank nextRank = RankStorage.getRank(rank.getNextName(), pathName);
					String format = StringManager.parseAll(rankOtherFormat.replace("%rank_name%", cyclingRankName)
							.replace("%rank_displayname%", rank.getDisplayName())
							.replace("%nextrank_name%", rank.getNextName())
							.replace("%nextrank_displayname%", nextRank.getDisplayName())
							.replace("%nextrank_cost%", String.valueOf(PRXAPI.getRankFinalCost(nextRank, p)))
							.replace("%nextrank_cost_formatted%",
									EconomyManager.shortcutFormat(PRXAPI.getRankFinalCost(nextRank, p))),
							p);
					otherRanks.add(format);
				}
			}
		}
		nonPagedRanks.addAll(completedRanks);
		nonPagedRanks.addAll(currentRanks);
		nonPagedRanks.addAll(otherRanks);

		List<String> footer = new ArrayList<>();
		for (String footerLine : rankListFormatFooter) footer.add(
				footerLine.replace("%currentpage%", pageNumber).replace("%totalpages%", String.valueOf(finalPage)));

		header.forEach(sender::sendMessage);
		nonPagedRanks.forEach(sender::sendMessage);
		footer.forEach(sender::sendMessage);
	}

}
