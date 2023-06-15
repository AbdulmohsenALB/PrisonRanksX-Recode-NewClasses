package me.prisonranksx.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import me.prisonranksx.managers.StringManager;

public class CommandsComponent extends Component {

	private List<String> consoleCommands;
	private List<String> playerCommands;
	private static final CommandSender CONSOLE = Bukkit.getConsoleSender();

	public CommandsComponent(List<String> consoleCommands, List<String> playerCommands) {
		this.consoleCommands = consoleCommands;
		this.playerCommands = playerCommands;
	}

	/**
	 * Initializes a commands component from a string list by reading the prefixes
	 * of the command lines
	 * 
	 * @param commandsList string list to parse into a consoleCommands list and a
	 *                     playerCommands list
	 * @return a new instance of commands component parsed from the given list
	 *         or null if list is null or empty
	 */
	@Nullable
	public static CommandsComponent parseCommands(@Nullable List<String> commandsList) {
		if (commandsList == null || commandsList.isEmpty()) return null;
		List<String> playerCommands = new ArrayList<>(), consoleCommands = new ArrayList<>();
		commandsList.stream()
				.map(unusableCommandLine -> unusableCommandLine.toLowerCase())
				.forEach(unusableCommandLine -> {
					if (unusableCommandLine.startsWith("[player]"))
						playerCommands.add(unusableCommandLine.replace("[player] ", "").replace("[player]", ""));
					else
						consoleCommands.add(unusableCommandLine.replace("[console] ", "").replace("[console]", ""));
				});
		return new CommandsComponent(consoleCommands.isEmpty() ? null : consoleCommands,
				playerCommands.isEmpty() ? null : playerCommands);
	}

	/**
	 * 
	 * @return console commands without their prefix ([console]) including the ones
	 *         that don't start with [console], so they are ready to be used and
	 *         dispatched
	 */
	public List<String> getConsoleCommands() {
		return consoleCommands;
	}

	/**
	 * 
	 * @return player commands without their prefix ([player]), so they are ready to
	 *         be used and dispatched
	 */
	public List<String> getPlayerCommands() {
		return playerCommands;
	}

	/**
	 * Dispatches console commands and player commands with PlaceholderAPI
	 * placeholders and %player% placeholder
	 * 
	 * @param player the player to parse PlaceholderAPI placeholders & %player%
	 *               placeholder for
	 */
	public void dispatchCommands(Player player) {
		if (consoleCommands != null) consoleCommands.forEach(commandLine -> Bukkit.dispatchCommand(CONSOLE,
				StringManager.parsePlaceholders(commandLine, player).replace("%player%", player.getName())));
		if (playerCommands != null) playerCommands.forEach(
				commandLine -> Bukkit.dispatchCommand(player, commandLine.replace("%player%", player.getName())));
	}

	/**
	 * Dispatches console commands and player commands with PlaceholderAPI
	 * placeholders and %player% placeholder, and applies the given function on the
	 * commands
	 * 
	 * @param player   the player to parse PlaceholderAPI placeholders & %player%
	 *                 placeholder for
	 * @param function function to apply on dispatched command lines, specifically
	 *                 made for string replacements
	 */
	public void dispatchCommands(Player player, Function<String, String> function) {
		if (consoleCommands != null) consoleCommands.forEach(commandLine -> Bukkit.dispatchCommand(CONSOLE,
				StringManager.parsePlaceholders(function.apply(commandLine), player)
						.replace("%player%", player.getName())));
		if (playerCommands != null) playerCommands.forEach(commandLine -> Bukkit.dispatchCommand(player,
				function.apply(commandLine).replace("%player%", player.getName())));
	}

	/**
	 * Dispatches console commands with PlaceholderAPI
	 * placeholders and %player% placeholder.
	 * 
	 * @param player      the player to parse PlaceholderAPI placeholders & %player%
	 *                    placeholder for
	 * @param commandLine command to dispatch
	 */
	public static void dispatchConsoleCommand(Player player, String commandLine) {
		Bukkit.dispatchCommand(CONSOLE,
				StringManager.parsePlaceholders(commandLine.replace("%player%", player.getName()), player));
	}

	/**
	 * Dispatches console commands with PlaceholderAPI
	 * placeholders and %player% placeholder.
	 * 
	 * @param player      the player to parse PlaceholderAPI placeholders & %player%
	 *                    placeholder for
	 * @param commandLine command to dispatch
	 */
	public static void dispatchPlayerCommand(Player player, String commandLine) {
		Bukkit.dispatchCommand(player,
				StringManager.parsePlaceholders(commandLine.replace("%player%", player.getName()), player));
	}

	/**
	 * Dispatches console commands with PlaceholderAPI
	 * placeholders and %player% placeholder.
	 * 
	 * @param player      the player to parse PlaceholderAPI placeholders & %player%
	 *                    placeholder for
	 * @param commandLine command to dispatch
	 */
	public static void dispatchConsoleCommand(HumanEntity player, String commandLine) {
		Bukkit.dispatchCommand(CONSOLE,
				StringManager.parsePlaceholders(commandLine.replace("%player%", player.getName()), (Player) player));
	}

	/**
	 * Dispatches console commands with PlaceholderAPI
	 * placeholders and %player% placeholder.
	 * 
	 * @param player      the player to parse PlaceholderAPI placeholders & %player%
	 *                    placeholder for
	 * @param commandLine command to dispatch
	 */
	public static void dispatchPlayerCommand(HumanEntity player, String commandLine) {
		Bukkit.dispatchCommand(player,
				StringManager.parsePlaceholders(commandLine.replace("%player%", player.getName()), (Player) player));
	}

	@Override
	public boolean use(Player player) {
		dispatchCommands(player);
		return true;
	}

	@Override
	public ComponentType getType() {
		return ComponentType.COMMANDS;
	}

}
