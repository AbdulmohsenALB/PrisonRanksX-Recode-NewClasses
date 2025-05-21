package me.prisonranksx.lists;

import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.managers.EconomyManager;
import org.bukkit.entity.Player;

import java.util.function.Function;

public interface PrestigesGUIList {
	void refreshGUI(Player player);

	void openGUI(Player player);

	void openGUI(Player player, int page);

	static Function<String, String> fun(Player player, Prestige prestige, String prestigeName) {
		return str -> str.replace("%prestige%", prestigeName)
				.replace("%prestige_display%", prestige.getDisplayName())
				.replace("%prestige_cost_normal%", String.valueOf(prestige.getCost()))
				.replace("%prestige_cost%", String.valueOf(PRXAPI.getPrestigeFinalCost(prestige, player)))
				.replace("%prestige_cost_comma%", EconomyManager.commaFormat(PRXAPI.getPrestigeFinalCost(prestige, player)))
				.replace("%prestige_cost_comma_decimals%", EconomyManager.commaFormatWithDecimals(PRXAPI.getPrestigeFinalCost(prestige, player)))
				.replace("%prestige_cost_formatted%", EconomyManager.shortcutFormat(PRXAPI.getPrestigeFinalCost(prestige, player)));
	}
}
