package me.prisonranksx.lists;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.CollectionUtils;
import me.prisonranksx.utils.CollectionUtils.PaginatedList;

public class RegularPrestigesTextList implements PrestigesTextList {

	private PrisonRanksX plugin;
	private String prestigeCurrentFormat;
	private String prestigeCompletedFormat;
	private String prestigeOtherFormat;
	private boolean enablePages;
	private int prestigePerPage;
	private List<String> prestigeWithPagesListFormat;
	private List<String> prestigeListFormat;
	boolean isCustomList;
	private List<String> prestigeListFormatHeader;
	private List<String> prestigeListFormatFooter;

	public RegularPrestigesTextList(PrisonRanksX plugin) {
		this.plugin = plugin;
		setup();
	}

	@Override
	public void setup() {
		prestigeCurrentFormat = plugin.getPrestigesListSettings().getPrestigeCurrentFormat();
		prestigeCompletedFormat = plugin.getPrestigesListSettings().getPrestigeCompletedFormat();
		prestigeOtherFormat = plugin.getPrestigesListSettings().getPrestigeOtherFormat();
		enablePages = plugin.getPrestigesListSettings().isEnablePages();
		prestigePerPage = plugin.getPrestigesListSettings().getPrestigePerPage();
		prestigeWithPagesListFormat = plugin.getPrestigesListSettings().getPrestigeWithPagesListFormat();
		prestigeListFormat = plugin.getPrestigesListSettings().getPrestigeListFormat();
		prestigeListFormatHeader = new ArrayList<>();
		prestigeListFormatFooter = new ArrayList<>();
		int ignoreIndex = prestigeWithPagesListFormat.indexOf("[prestigeslist]");
		if (prestigeListFormatHeader.isEmpty() && prestigeListFormatFooter.isEmpty() && prestigeListFormat.size() > 1) {
			for (int i = 0; i < prestigeWithPagesListFormat.size(); i++) {
				if (ignoreIndex > i)
					prestigeListFormatHeader.add(prestigeWithPagesListFormat.get(i));
				else if (ignoreIndex < i) prestigeListFormatFooter.add(prestigeWithPagesListFormat.get(i));
			}
		}
		if (enablePages) {
			if (!prestigeWithPagesListFormat.contains("[prestigeslist]")) isCustomList = true;
		} else {
			if (!prestigeListFormat.contains("[prestigeslist]")) isCustomList = true;
		}
	}

	/**
	 * Sends prestiges list as configured in config file, if pageNumber is null or
	 * enable pages is false, then a non-paged list will be sent.
	 * 
	 * @param sender     to send to
	 * @param pageNumber number of page
	 */
	@Override
	public void send(CommandSender sender, @Nullable String pageNumber) {
		if (enablePages && pageNumber != null)
			sendPagedList(sender, pageNumber);
		else
			sendList(sender);
	}

	@Override
	public void sendList(CommandSender sender) {
		if (isCustomList) {
			List<String> customList = prestigeListFormat;
			if (sender instanceof Player) customList = StringManager.parsePlaceholders(customList, (Player) sender);
			customList.forEach(sender::sendMessage);
		}
		Player p = (Player) sender;
		User user = plugin.getUserController().getUser(UniqueId.getUUID(p));
		String prestigeName = user.getPrestigeName();
		List<String> prestigesCollection = new ArrayList<>(PrestigeStorage.getPrestigeNames());
		// Header Setup
		List<String> header = new ArrayList<>(prestigeListFormatHeader);

		// Prestiges List Organization
		List<String> currentPrestiges = new ArrayList<>(), completedPrestiges = new ArrayList<>(),
				otherPrestiges = new ArrayList<>(), nonPagedPrestiges = new ArrayList<>();
		int currentPrestigeIndex = prestigesCollection.indexOf(prestigeName);
		for (String cyclingPrestigeName : prestigesCollection) {
			Prestige prestige = PrestigeStorage.getPrestige(cyclingPrestigeName);
			// Current Prestige Format
			if (currentPrestigeIndex == prestigesCollection.indexOf(cyclingPrestigeName)) {
				if (prestige.getNextName() != null) {
					Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextName());
					String format = StringManager
							.parseAll(prestigeCurrentFormat.replace("%prestige_name%", cyclingPrestigeName)
									.replace("%prestige_displayname%", prestige.getDisplayName())
									.replace("%nextprestige_name%", prestige.getNextName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);

					currentPrestiges.add(format);
				}
			}
			// Completed Prestige Format
			if (currentPrestigeIndex > prestigesCollection.indexOf(cyclingPrestigeName)) {
				if (prestige.getNextName() != null) {
					Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextName());
					String format = StringManager
							.parseAll(prestigeCompletedFormat.replace("%prestige_name%", cyclingPrestigeName)
									.replace("%prestige_displayname%", prestige.getDisplayName())
									.replace("%nextprestige_name%", prestige.getNextName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);
					completedPrestiges.add(format);
				}
			}
			// Other Prestige Format
			if (currentPrestigeIndex < prestigesCollection.indexOf(cyclingPrestigeName)) {
				if (prestige.getNextName() != null) {
					Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextName());
					String format = StringManager
							.parseAll(prestigeOtherFormat.replace("%prestige_name%", cyclingPrestigeName)
									.replace("%prestige_displayname%", prestige.getDisplayName())
									.replace("%nextprestige_name%", prestige.getNextName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);
					otherPrestiges.add(format);
				}
			}
		}
		nonPagedPrestiges.addAll(completedPrestiges);
		nonPagedPrestiges.addAll(currentPrestiges);
		nonPagedPrestiges.addAll(otherPrestiges);

		List<String> footer = new ArrayList<>(prestigeListFormatFooter);

		header.forEach(sender::sendMessage);
		nonPagedPrestiges.forEach(sender::sendMessage);
		footer.forEach(sender::sendMessage);
	}

	@Override
	public void sendPagedList(CommandSender sender, String pageNumber) {
		if (isCustomList) {
			List<String> customList = CollectionUtils.paginateList(prestigeWithPagesListFormat, prestigePerPage,
					Integer.parseInt(pageNumber));
			if (sender instanceof Player) customList = StringManager.parsePlaceholders(customList, (Player) sender);
			customList.forEach(sender::sendMessage);
			return;
		}
		Player p = (Player) sender;
		User user = plugin.getUserController().getUser(UniqueId.getUUID(p));
		String prestigeName = user.getPrestigeName();
		List<String> prestigesCollection = new ArrayList<>(PrestigeStorage.getPrestigeNames());
		PaginatedList paginatedList = CollectionUtils.paginateListCollectable(prestigesCollection, prestigePerPage,
				Integer.parseInt(pageNumber));
		int currentPage = paginatedList.getCurrentPage();
		int finalPage = paginatedList.getFinalPage();
		if (currentPage > finalPage) {
			Messages.sendMessage(p, Messages.getPrestigeListLastPageReached(),
					s -> s.replace("%page%", String.valueOf(finalPage)));
			return;
		}
		prestigesCollection = paginatedList.collect();
		// Header Setup
		List<String> header = new ArrayList<>();
		for (String headerLine : prestigeListFormatHeader) header.add(
				headerLine.replace("%currentpage%", pageNumber).replace("%totalpages%", String.valueOf(finalPage)));

		// Prestiges List Organization
		List<String> currentPrestiges = new ArrayList<>(), completedPrestiges = new ArrayList<>(),
				otherPrestiges = new ArrayList<>(), nonPagedPrestiges = new ArrayList<>();
		int currentPrestigeIndex = prestigesCollection.indexOf(prestigeName);
		for (String cyclingPrestigeName : prestigesCollection) {
			Prestige prestige = PrestigeStorage.getPrestige(cyclingPrestigeName);

			// Current Prestige Format
			if (currentPrestigeIndex == prestigesCollection.indexOf(cyclingPrestigeName)) {
				if (prestige.getIndex() == 1) {
					Prestige nextPrestige = prestige;
					Rank lastRank = RankStorage.getLastRank(user.getPathName());
					String format = StringManager
							.parseAll(prestigeCurrentFormat.replace("%prestige_name%", lastRank.getName())
									.replace("%prestige_displayname%", lastRank.getDisplayName())
									.replace("%nextprestige_name%", nextPrestige.getName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);
					currentPrestiges.add(format);
				}
				if (prestige.getNextName() != null) {
					Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextName());
					String format = StringManager
							.parseAll(prestigeCurrentFormat.replace("%prestige_name%", cyclingPrestigeName)
									.replace("%prestige_displayname%", prestige.getDisplayName())
									.replace("%nextprestige_name%", prestige.getNextName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);

					currentPrestiges.add(format);
				}

			}
			// Completed Prestige Format
			if (currentPrestigeIndex > prestigesCollection.indexOf(cyclingPrestigeName)) {
				if (prestige.getIndex() == 1) {
					Prestige nextPrestige = prestige;
					Rank lastRank = RankStorage.getLastRank(user.getPathName());
					String format = StringManager
							.parseAll(prestigeCompletedFormat.replace("%prestige_name%", lastRank.getName())
									.replace("%prestige_displayname%", lastRank.getDisplayName())
									.replace("%nextprestige_name%", nextPrestige.getName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);
					completedPrestiges.add(format);
				}
				if (prestige.getNextName() != null) {
					Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextName());
					String format = StringManager
							.parseAll(prestigeCompletedFormat.replace("%prestige_name%", cyclingPrestigeName)
									.replace("%prestige_displayname%", prestige.getDisplayName())
									.replace("%nextprestige_name%", prestige.getNextName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);
					completedPrestiges.add(format);
				}
			}
			// Other Prestige Format
			if (currentPrestigeIndex < prestigesCollection.indexOf(cyclingPrestigeName)) {
				if (prestige.getIndex() == 1) {
					Prestige nextPrestige = prestige;
					Rank lastRank = RankStorage.getLastRank(user.getPathName());
					String format = StringManager
							.parseAll(prestigeOtherFormat.replace("%prestige_name%", lastRank.getName())
									.replace("%prestige_displayname%", lastRank.getDisplayName())
									.replace("%nextprestige_name%", nextPrestige.getName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);
					otherPrestiges.add(format);
				}
				if (prestige.getNextName() != null) {
					Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextName());
					String format = StringManager
							.parseAll(prestigeOtherFormat.replace("%prestige_name%", cyclingPrestigeName)
									.replace("%prestige_displayname%", prestige.getDisplayName())
									.replace("%nextprestige_name%", prestige.getNextName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);
					otherPrestiges.add(format);
				}
			}
		}
		nonPagedPrestiges.addAll(completedPrestiges);
		nonPagedPrestiges.addAll(currentPrestiges);
		nonPagedPrestiges.addAll(otherPrestiges);

		List<String> footer = new ArrayList<>();
		for (String footerLine : prestigeListFormatFooter) footer.add(
				footerLine.replace("%currentpage%", pageNumber).replace("%totalpages%", String.valueOf(finalPage)));

		header.forEach(sender::sendMessage);
		nonPagedPrestiges.forEach(sender::sendMessage);
		footer.forEach(sender::sendMessage);
	}

}
