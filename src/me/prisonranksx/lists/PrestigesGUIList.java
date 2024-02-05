package me.prisonranksx.lists;

import org.bukkit.entity.Player;

public interface PrestigesGUIList {
	void refreshGUI(Player player);

	void openGUI(Player player);

	void openGUI(Player player, int page);
}
