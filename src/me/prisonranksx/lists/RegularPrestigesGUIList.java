package me.prisonranksx.lists;

import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.NBTEditor;
import me.prisonranksx.bukkitutils.PlayerPagedGUI.GUIItem;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.reflections.UniqueId;

public class RegularPrestigesGUIList extends GUIList implements PrestigesGUIList {

	public RegularPrestigesGUIList(PrisonRanksX plugin) {
		super(plugin, "Prestiges");
		setup();
	}

	@Override
	public void refreshGUI(Player player) {
		User user = getPlugin().getUserController().getUser(UniqueId.getUUID(player));
		String pathName = user.getPathName();
		Prestige currentPrestige = PrestigeStorage.getPrestige(user.getPrestigeName());
		long currentPrestigeIndex = currentPrestige.getIndex();
		PrestigeStorage.getPrestiges().forEach(prestige -> {
			long prestigeIndex = prestige.getIndex();
			String prestigeName = prestige.getName();
			if (prestigeIndex < currentPrestigeIndex) {
				GUIItem specialItem = getSpecialCompletedItems().get(prestigeName);
				getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getCompletedItem().clone(),
						prestigeName, pathName, fun(prestige, prestigeName)), player);
			} else if (prestigeIndex == currentPrestigeIndex) {
				GUIItem specialItem = getSpecialCurrentItems().get(prestigeName);
				getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getCurrentItem().clone(),
						prestigeName, pathName, fun(prestige, prestigeName)), player);
			} else if (prestigeIndex > currentPrestigeIndex) {
				GUIItem specialItem = getSpecialOtherItems().get(prestigeName);
				getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getOtherItem().clone(),
						prestigeName, pathName, fun(prestige, prestigeName)), player);
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

	protected static Function<String, String> fun(Prestige prestige, String prestigeName) {
		return str -> str.replace("%prestige%", prestigeName)
				.replace("%prestige_display%", prestige.getDisplayName())
				.replace("%prestige_cost%", String.valueOf(prestige.getCost()))
				.replace("%prestige_cost_formatted%", EconomyManager.shortcutFormat(prestige.getCost()));
	}

	protected GUIItem update(GUIItem guiItem, String prestigeName, String pathName, Function<String, String> function) {
		ItemStack itemStack = guiItem.getItemStack();
		ItemMeta meta = itemStack.getItemMeta();
		String displayName = meta.getDisplayName();
		meta.setDisplayName(function.apply(displayName));
		List<String> lore = meta.getLore();
		lore.clear();
		meta.getLore().forEach(loreLine -> lore.add(function.apply(loreLine)));
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		itemStack = NBTEditor.set(itemStack, prestigeName, "prx-prestige");
		itemStack = NBTEditor.set(itemStack, pathName, "prx-path");
		guiItem.setItemStack(itemStack);
		return guiItem;
	}

}
