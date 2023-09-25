package me.prisonranksx.lists;

import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.NBTEditor;
import me.prisonranksx.bukkitutils.PlayerPagedGUI.GUIItem;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.reflections.UniqueId;

public class RanksGUIList extends GUIList {

	public RanksGUIList(PrisonRanksX plugin) {
		super(plugin, "Ranks");
		setup();
	}

	@Override
	public void refreshGUI(Player player) {
		User user = getPlugin().getUserController().getUser(UniqueId.getUUID(player));
		String pathName = user.getPathName();
		Rank currentRank = RankStorage.getRank(user.getRankName(), pathName);
		long currentRankIndex = currentRank.getIndex();
		RankStorage.getPathRanks(pathName).forEach(rank -> {
			long rankIndex = rank.getIndex();
			String rankName = rank.getName();
			if (rankIndex < currentRankIndex) {
				GUIItem specialItem = getSpecialCompletedItems().get(rankName);
				getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getCompletedItem().clone(),
						rankName, pathName, fun(rank, rankName)), player);
			} else if (rankIndex == currentRankIndex) {
				GUIItem specialItem = getSpecialCurrentItems().get(rankName);
				getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getCurrentItem().clone(),
						rankName, pathName, fun(rank, rankName)), player);
			} else if (rankIndex > currentRankIndex) {
				GUIItem specialItem = getSpecialOtherItems().get(rankName);
				getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getOtherItem().clone(),
						rankName, pathName, fun(rank, rankName)), player);
			}
		});
	}

	@Override
	public void openGUI(Player player) {
		refreshGUI(player);
		getPlayerPagedGUI().openInventory(player);
	}

	@Override
	public void openGUI(Player player, int page) {
		refreshGUI(player);
		getPlayerPagedGUI().openInventory(player, page);
	}

	protected static Function<String, String> fun(Rank rank, String rankName) {
		return str -> str.replace("%rank%", rankName)
				.replace("%rank_display%", rank.getDisplayName())
				.replace("%rank_cost%", String.valueOf(rank.getCost()))
				.replace("%rank_cost_formatted%", EconomyManager.shortcutFormat(rank.getCost()));
	}

	protected GUIItem update(GUIItem guiItem, String rankName, String pathName, Function<String, String> function) {
		ItemStack itemStack = guiItem.getItemStack();
		ItemMeta meta = itemStack.getItemMeta();
		String displayName = meta.getDisplayName();
		meta.setDisplayName(function.apply(displayName));
		List<String> lore = meta.getLore();
		lore.clear();
		meta.getLore().forEach(loreLine -> lore.add(function.apply(loreLine)));
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		itemStack = NBTEditor.set(itemStack, rankName, "prx-rank");
		itemStack = NBTEditor.set(itemStack, pathName, "prx-path");
		guiItem.setItemStack(itemStack);
		return guiItem;
	}

}
