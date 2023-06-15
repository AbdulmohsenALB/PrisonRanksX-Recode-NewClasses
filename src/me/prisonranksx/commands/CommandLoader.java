package me.prisonranksx.commands;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;

public class CommandLoader {

	private static final CommandMap COMMAND_MAP = loadCommandMap();

	private static CommandMap loadCommandMap() {
		try {
			Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			bukkitCommandMap.setAccessible(true);
			return (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static CommandMap getCommandMap() {
		return COMMAND_MAP;
	}

	public static boolean registerCommand(String pluginName, BukkitCommand bukkitCommandClass) {
		return getCommandMap().register(pluginName, bukkitCommandClass);
	}

	@SuppressWarnings("unchecked")
	public static void unregisterCommand(Command... bukkitCommandClass) {
		for (Command cmd : bukkitCommandClass) {
			try {
				if (cmd == null) return;
				Field knownCommandsField = getCommandMap().getClass().getDeclaredField("knownCommands");
				knownCommandsField.setAccessible(true);
				Map<String, Command> knownCommands = (HashMap<String, Command>) knownCommandsField.get(getCommandMap());
				Field aliasesField = Command.class.getDeclaredField("aliases");
				aliasesField.setAccessible(true);
				List<String> aliases = (List<String>) aliasesField.get(cmd);
				knownCommands.remove(cmd.getName());
				for (String alias : aliases) if (knownCommands.containsKey(alias)) knownCommands.remove(alias);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public static void unregisterCommand(String... commandName) {
		for (String command : commandName) unregisterCommand(getCommandMap().getCommand(command));
	}

}
