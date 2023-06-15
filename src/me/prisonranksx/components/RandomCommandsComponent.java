package me.prisonranksx.components;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.prisonranksx.managers.StringManager;
import me.prisonranksx.utils.ProbabilityCollection;

public class RandomCommandsComponent extends Component {

	private static final CommandSender CONSOLE = Bukkit.getConsoleSender();
	private ProbabilityCollection<List<String>> randomCommandsCollection;

	public RandomCommandsComponent(ProbabilityCollection<List<String>> randomCommandsCollection) {
		this.randomCommandsCollection = randomCommandsCollection;
	}

	/**
	 * Initializes a random commands component from a configuration section
	 * 
	 * @param randomCommandsSection configuration section to parse into a usable
	 *                              random commands collection
	 * @return a new instance of random commands component parsed from the given
	 *         configuration section
	 *         or null if configuration section is null, empty, or has invalid
	 *         values
	 */
	@Nullable
	public static RandomCommandsComponent parseRandomCommands(@Nullable ConfigurationSection randomCommandsSection) {
		if (randomCommandsSection == null || randomCommandsSection.getKeys(false).isEmpty()) return null;
		ProbabilityCollection<List<String>> randomCommandsCollection = new ProbabilityCollection<>();
		randomCommandsSection.getKeys(false).forEach(sectionNumber -> {
			ConfigurationSection numberSection = randomCommandsSection.getConfigurationSection(sectionNumber);
			if (numberSection == null) return;
			List<String> commandsList = Optional.ofNullable(numberSection.getStringList("commands"))
					.orElse(numberSection.getStringList("executecmds"));
			if (commandsList == null || commandsList.isEmpty()) return;
			int chance = numberSection.getInt("chance");
			randomCommandsCollection.add(commandsList, chance);
		});
		return randomCommandsCollection.isEmpty() ? null : new RandomCommandsComponent(randomCommandsCollection);
	}

	/**
	 * 
	 * @return a random list of command lines choosen by chance
	 */
	public List<String> chooseRandomCommands() {
		return randomCommandsCollection.get();
	}

	/**
	 * Dispatches random console commands with PlaceholderAPI
	 * placeholders and %player% placeholder
	 * 
	 * @param player the player to parse PlaceholderAPI placeholders & %player%
	 *               placeholder for
	 */
	public void dispatchCommands(Player player) {
		chooseRandomCommands().forEach(commandLine -> Bukkit.dispatchCommand(CONSOLE,
				StringManager.parsePlaceholders(commandLine, player).replace("%player%", player.getName())));
	}

	/**
	 * Dispatches random console commands with PlaceholderAPI
	 * placeholders and %player% placeholder, and applies the given function on the
	 * commands
	 * 
	 * @param player   the player to parse PlaceholderAPI placeholders & %player%
	 *                 placeholder for
	 * @param function function to apply on dispatched command lines, specifically
	 *                 made for string replacements
	 */
	public void dispatchCommands(Player player, Function<String, String> function) {
		chooseRandomCommands().forEach(commandLine -> Bukkit.dispatchCommand(CONSOLE,
				StringManager.parsePlaceholders(function.apply(commandLine), player)
						.replace("%player%", player.getName())));
	}

	@Override
	public boolean use(Player player) {
		dispatchCommands(player);
		return true;
	}

	@Override
	public ComponentType getType() {
		return ComponentType.RANDOM_COMMANDS;
	}

}
