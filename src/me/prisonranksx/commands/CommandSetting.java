package me.prisonranksx.commands;

import java.util.List;

import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.StringManager;

public interface CommandSetting {

	static String getStringSetting(String commandName, String setting) {
		return StringManager.parseColorsAndSymbols(
				ConfigManager.getCommandsConfig().getString("commands." + commandName + "." + setting));
	}

	static String getStringSetting(String commandName, String setting, String fallback) {
		return StringManager.parseColorsAndSymbols(
				ConfigManager.getCommandsConfig().getString("commands." + commandName + "." + setting, fallback));
	}

	static List<String> getListSetting(String commandName, String setting) {
		return StringManager.parseColorsAndSymbols(
				ConfigManager.getCommandsConfig().getStringList("commands." + commandName + "." + setting));
	}

	@SuppressWarnings("unchecked")
	static <T> T getSetting(String commandName, String setting) {
		return (T) ConfigManager.getCommandsConfig().get("commands." + commandName + "." + setting);
	}

}
