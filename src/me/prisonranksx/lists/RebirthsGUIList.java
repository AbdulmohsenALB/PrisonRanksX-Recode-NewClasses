package me.prisonranksx.lists;

import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.NBTEditor;
import me.prisonranksx.bukkitutils.PlayerPagedGUI.GUIItem;
import me.prisonranksx.data.RebirthStorage;
import me.prisonranksx.holders.Rebirth;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.reflections.UniqueId;

public class RebirthsGUIList extends GUIList {

	public RebirthsGUIList(PrisonRanksX plugin) {
		super(plugin, "Rebirths");
		setup();
	}

	@Override
	public void refreshGUI(Player player) {
		User user = getPlugin().getUserController().getUser(UniqueId.getUUID(player));
		String pathName = user.getPathName();
		Rebirth currentRebirth = RebirthStorage.getRebirth(user.getRebirthName());
		long currentRebirthIndex = currentRebirth.getIndex();
		RebirthStorage.getRebirths().forEach(rebirth -> {
			long rebirthIndex = rebirth.getIndex();
			String rebirthName = rebirth.getName();
			if (rebirthIndex < currentRebirthIndex) {
				GUIItem specialItem = getSpecialCompletedItems().get(rebirthName);
				getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getCompletedItem().clone(),
						rebirthName, pathName, fun(rebirth, rebirthName)), player);
			} else if (rebirthIndex == currentRebirthIndex) {
				GUIItem specialItem = getSpecialCurrentItems().get(rebirthName);
				getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getCurrentItem().clone(),
						rebirthName, pathName, fun(rebirth, rebirthName)), player);
			} else if (rebirthIndex > currentRebirthIndex) {
				GUIItem specialItem = getSpecialOtherItems().get(rebirthName);
				getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getOtherItem().clone(),
						rebirthName, pathName, fun(rebirth, rebirthName)), player);
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

	protected static Function<String, String> fun(Rebirth rebirth, String rebirthName) {
		return str -> str.replace("%rebirth%", rebirthName)
				.replace("%rebirth_display%", rebirth.getDisplayName())
				.replace("%rebirth_cost%", String.valueOf(rebirth.getCost()))
				.replace("%rebirth_cost_formatted%", EconomyManager.shortcutFormat(rebirth.getCost()));
	}

	protected GUIItem update(GUIItem guiItem, String rebirthName, String pathName, Function<String, String> function) {
		ItemStack itemStack = guiItem.getItemStack();
		ItemMeta meta = itemStack.getItemMeta();
		String displayName = meta.getDisplayName();
		meta.setDisplayName(function.apply(displayName));
		List<String> lore = meta.getLore();
		lore.clear();
		meta.getLore().forEach(loreLine -> lore.add(function.apply(loreLine)));
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		itemStack = NBTEditor.set(itemStack, rebirthName, "prx-rebirth");
		itemStack = NBTEditor.set(itemStack, pathName, "prx-path");
		guiItem.setItemStack(itemStack);
		return guiItem;
	}

}
