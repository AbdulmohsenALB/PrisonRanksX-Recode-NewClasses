package me.prisonranksx.hooks;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPIHook extends PlaceholderExpansion {

	@Override
	public String onPlaceholderRequest(Player player, String string) {
		return null;
	}
	
	@Override
	public String getAuthor() {
		return "TheGaming999";
	}

	@Override
	public String getIdentifier() {
		return "prisonranksx";
	}

	@Override
	public String getVersion() {
		return "3.0";
	}
	
}
