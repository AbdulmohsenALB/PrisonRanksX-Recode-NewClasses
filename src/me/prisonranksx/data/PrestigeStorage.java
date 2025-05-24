package me.prisonranksx.data;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.components.*;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.UniversalPrestige;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.utils.HashedLongRange;
import me.prisonranksx.utils.HashedModuloRange;
import me.prisonranksx.utils.NumParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.function.Consumer;

public class PrestigeStorage {

	private static final PrestigeStorageHandler PRESTIGE_STORAGE_HANDLER = new PrestigeStorageHandler();

	public static PrestigeStorageHandler getHandler() {
		return PRESTIGE_STORAGE_HANDLER;
	}

	public static void loadPrestiges() {
		PRESTIGE_STORAGE_HANDLER.loadPrestiges();
	}

	public static PrestigeStorageHandler init(boolean infinite) {
		PRESTIGE_STORAGE_HANDLER.create(infinite);
		return PRESTIGE_STORAGE_HANDLER;
	}

	public static PrestigeStorageHandler initAndLoad(boolean infinite) {
		init(infinite).loadPrestiges();
		return PRESTIGE_STORAGE_HANDLER;
	}

	public static boolean isCreated() {
		return PRESTIGE_STORAGE_HANDLER.isCreated();
	}

	public static boolean prestigeExists(String name) {
		return PRESTIGE_STORAGE_HANDLER.prestigeExists(name);
	}

	public static boolean prestigeExists(long number) {
		return PRESTIGE_STORAGE_HANDLER.prestigeExists(number);
	}

	public static Prestige getPrestige(String name) {
		return PRESTIGE_STORAGE_HANDLER.getPrestige(name);
	}

	public static Prestige getPrestige(long number) {
		return PRESTIGE_STORAGE_HANDLER.getPrestige(number);
	}

	public static String getFirstPrestigeName() {
		return PRESTIGE_STORAGE_HANDLER.getFirstPrestigeName();
	}

	public static long getFirstPrestigeAsNumber() {
		return PRESTIGE_STORAGE_HANDLER.getFirstPrestigeAsNumber();
	}

	public static String getLastPrestigeName() {
		return PRESTIGE_STORAGE_HANDLER.getLastPrestigeName();
	}

	public static long getLastPrestigeAsNumber() {
		return PRESTIGE_STORAGE_HANDLER.getLastPrestigeAsNumber();
	}

	/**
	 * @return a hash set of all registered prestiges names, or null if
	 * infinite prestige is enabled
	 */
	public static Set<String> getPrestigeNames() {
		return PRESTIGE_STORAGE_HANDLER.getPrestigeNames();
	}

	/**
	 * @return a hash set of all registered prestiges, or null if infinite
	 * prestige is enabled
	 */
	public static Collection<Prestige> getPrestiges() {
		return PRESTIGE_STORAGE_HANDLER.getPrestiges();
	}

	/**
	 * @param name prestige name to match with (case-insensitive)
	 * @return prestigeName with matching name in correct case, or null if no
	 * matching prestige name was found.
	 */
	public static String matchPrestigeName(String name) {
		return PRESTIGE_STORAGE_HANDLER.matchPrestigeName(name);
	}

	/**
	 * @param name prestige name to match with (case-insensitive)
	 * @return prestige with matching name, or null if no matching prestige name was
	 * found.
	 */
	public static Prestige matchPrestige(String name) {
		return PRESTIGE_STORAGE_HANDLER.matchPrestige(name);
	}

	/**
	 * Gets correct display name from prestige number
	 *
	 * @param prestige number of prestige to get display name for
	 * @return Infinite prestige: gets prestige display name from constant settings
	 * if available, otherwise default one. <br>
	 * <br>
	 * Regular prestige: prestige
	 * display name.
	 */
	public static String getRangedDisplay(long prestige) {
		return PRESTIGE_STORAGE_HANDLER.getRangedDisplay(prestige);
	}

	/**
	 * Executes continuous components if available for infinite prestige, does
	 * nothing for regular prestige.
	 *
	 * @param prestige         number of prestige to execute components from
	 * @param componentsAction handle the components
	 */
	public static void useContinuousComponents(long prestige, Consumer<ComponentsHolder> componentsAction) {
		PRESTIGE_STORAGE_HANDLER.useContinuousComponents(prestige, componentsAction);
	}

	public static CommandsComponent getCommandsComponent() {
		return PRESTIGE_STORAGE_HANDLER.getCommandsComponent();
	}

	public static void useCommandsComponent(Consumer<CommandsComponent> action) {
		PRESTIGE_STORAGE_HANDLER.useCommandsComponent(action);
	}

	public static String getCostExpression() {
		return PRESTIGE_STORAGE_HANDLER.getCostExpression();
	}

	public static class PrestigeStorageHandler {

		private IPrestigeStorage prestigeStorage;

		public void create(boolean infinite) {
			prestigeStorage = infinite ? new InfinitePrestigeStorage() : new RegularPrestigeStorage();
		}

		public boolean isCreated() {
			return prestigeStorage != null;
		}

		public void loadPrestiges() {
			prestigeStorage.loadPrestiges();
		}

		public IPrestigeStorage getStorage() {
			return prestigeStorage;
		}

		public boolean prestigeExists(String name) {
			return prestigeStorage.prestigeExists(name);
		}

		public boolean prestigeExists(long number) {
			return prestigeStorage.prestigeExists(number);
		}

		public Prestige getPrestige(String name) {
			return prestigeStorage.getPrestige(name);
		}

		public Prestige getPrestige(long number) {
			return prestigeStorage.getPrestige(number);
		}

		public String getFirstPrestigeName() {
			return prestigeStorage.getFirstPrestigeName();
		}

		public long getFirstPrestigeAsNumber() {
			return prestigeStorage.getFirstPrestigeAsNumber();
		}

		public String getLastPrestigeName() {
			return prestigeStorage.getLastPrestigeName();
		}

		public long getLastPrestigeAsNumber() {
			return prestigeStorage.getLastPrestigeAsNumber();
		}

		public Set<String> getPrestigeNames() {
			return prestigeStorage.getPrestigeNames();
		}

		public Collection<Prestige> getPrestiges() {
			return prestigeStorage.getPrestiges();
		}

		public String matchPrestigeName(String name) {
			return prestigeStorage.matchPrestigeName(name);
		}

		public Prestige matchPrestige(String name) {
			return prestigeStorage.matchPrestige(name);
		}

		public boolean isInfinite() {
			return prestigeStorage.isInfinite();
		}

		public long getPrestigeNumber(String name) {
			return prestigeStorage.getPrestigeNumber(name);
		}

		public String getRangedDisplay(long prestige) {
			return prestigeStorage.getRangedDisplay(prestige);
		}

		public void useContinuousComponents(long prestige, Consumer<ComponentsHolder> componentsAction) {
			prestigeStorage.useContinuousComponents(prestige, componentsAction);
		}

		public CommandsComponent getCommandsComponent() {
			return prestigeStorage.getCommandsComponent();
		}

		public void useCommandsComponent(Consumer<CommandsComponent> action) {
			prestigeStorage.useCommandsComponent(action);
		}

		public String getCostExpression() {
			return prestigeStorage.getCostExpression();
		}

	}

	private static interface IPrestigeStorage {

		void loadPrestiges();

		boolean prestigeExists(String name);

		boolean prestigeExists(long number);

		Prestige getPrestige(String name);

		Prestige getPrestige(long number);

		String getFirstPrestigeName();

		long getFirstPrestigeAsNumber();

		String getLastPrestigeName();

		long getLastPrestigeAsNumber();

		Set<String> getPrestigeNames();

		Collection<Prestige> getPrestiges();

		String matchPrestigeName(String name);

		Prestige matchPrestige(String name);

		boolean isInfinite();

		long getPrestigeNumber(String name);

		void useCommandsComponent(Consumer<CommandsComponent> action);

		public String getRangedDisplay(long prestige);

		public void useContinuousComponents(long prestige, Consumer<ComponentsHolder> componentsAction);

		public CommandsComponent getCommandsComponent();

		public String getCostExpression();

	}

	public static class RegularPrestigeStorage implements IPrestigeStorage {

		private Map<String, Prestige> prestiges = new HashMap<>();
		private Map<String, String> alternativeNames = new HashMap<>();
		private List<String> prestigeNames = new ArrayList<>();
		private String firstPrestigeName;
		private String lastPrestigeName;
		private long lastPrestigeNumber;
		private CommandsComponent prestigeCommands;

		@SuppressWarnings("unchecked")
		@Override
		public void loadPrestiges() {
			prestiges.clear();
			alternativeNames.clear();
			prestigeNames.clear();
			firstPrestigeName = null;
			lastPrestigeName = null;
			lastPrestigeNumber = 0;
			FileConfiguration prestigesConfig = ConfigManager.getPrestigesConfig();
			ConfigurationSection prestigeSection = prestigesConfig.getConfigurationSection("Prestiges");
			for (String prestigeName : prestigeSection.getKeys(false)) {
				ConfigurationSection current = prestigeSection.getConfigurationSection(prestigeName);
				Prestige prestige = new Prestige(prestigeName,
						StringManager.parseColorsAndSymbols(
								ConfigManager.getPossible(current, String.class, "display-name", "display", "prefix")),
						ConfigManager.getPossible(current, "next-prestige", "nextprestige"),
						ConfigManager.getPossibleDouble(current, "cost", "price"),
						StringManager.parseColorsAndSymbols(prestigeSection.getStringList("broadcast")),
						StringManager.parseColorsAndSymbols(
								ConfigManager.getPossible(current, List.class, "message", "msg", "messages")),
						CommandsComponent.parseCommands(
								ConfigManager.getPossible(current, "commands", "executecmds", "command", "cmd")),
						RequirementsComponent.parseRequirements(
								ConfigManager.getPossible(current, "requirements", "requirement", "require", "requires")),
						ActionBarComponent.parseActionBar(ConfigManager.getPossible(current, "action-bar", "actionbar")),
						PermissionsComponent.parsePermissions(
								ConfigManager.getPossible(current, "add-permissions", "addpermission", "add-permission",
										"addperm", "add-perm", "add-perms"),
								ConfigManager.getPossible(current, "delete-permissions", "delpermission",
										"del-permission", "delete-permission", "remove-permissions",
										"remove-permission", "del-perms")),
						FireworkComponent.parseFirework(ConfigManager.getPossible(current, "firework", "firework-builder",
								"fireworks", "fire-work")),
						RandomCommandsComponent.parseRandomCommands(ConfigManager.getPossible(current, "random-commands",
								"randomcmds", "random-command", "randomcmd", "random-cmds", "random-cmd")),
						StringManager.parseColorsAndSymbols(ConfigManager.getPossible(current, List.class,
								"requirements-fail-message", "custom-requirement-message",
								"custom-requirements-message", "requirement-fail-message", "requirements-fail-messages",
								"requirements-message", "requirement-message")),
						ConfigManager.getPossibleDouble(current, "cost-increase", "rankup_cost_increase_percentage",
								"cost-increase-percentage", "cost_increase", "rankup-cost-increase-percentage"));
				lastPrestigeNumber += 1;
				prestiges.put(prestigeName, prestige);
				alternativeNames.put(prestigeName.toLowerCase(), prestigeName);
				prestigeNames.add(prestigeName);
				if (firstPrestigeName == null) firstPrestigeName = prestigeName;
			}
			lastPrestigeName = prestigeNames.get((int) (lastPrestigeNumber - 1));
			prestigeCommands = PrisonRanksX.getInstance().getPrestigeSettings().getPrestigeCommands();
		}

		@Override
		public boolean prestigeExists(String name) {
			return getPrestige(name) != null;
		}

		@Override
		public boolean prestigeExists(long number) {
			return number > 0 && number <= lastPrestigeNumber;
		}

		@Override
		public Prestige getPrestige(String name) {
			if (name == null) return null;
			return prestiges.get(name);
		}

		@Override
		public Prestige getPrestige(long number) {
			return getPrestige(prestigeNames.get((int) (number - 1)));
		}

		@Override
		public String getFirstPrestigeName() {
			return firstPrestigeName;
		}

		@Override
		public long getFirstPrestigeAsNumber() {
			return prestigeNames.indexOf(firstPrestigeName) + 1;
		}

		@Override
		public String getLastPrestigeName() {
			return lastPrestigeName;
		}

		@Override
		public long getLastPrestigeAsNumber() {
			return lastPrestigeNumber;
		}

		@Override
		public Set<String> getPrestigeNames() {
			return prestiges.keySet();
		}

		@Override
		public Collection<Prestige> getPrestiges() {
			return prestiges.values();
		}

		public List<String> getPrestigeNamesList() {
			return prestigeNames;
		}

		@Override
		public String matchPrestigeName(String name) {
			Prestige prestige = prestiges.get(name);
			if (prestige != null) return prestige.getName();
			String altName = alternativeNames.get(name.toLowerCase());
			if (altName != null) return altName;
			int intName = NumParser.asInt(name, -1);
			if (intName == -1) return null;
			return prestigeNames.get(intName + 1);
		}

		@Override
		public Prestige matchPrestige(String name) {
			return prestiges.get(matchPrestigeName(name));
		}

		@Override
		public boolean isInfinite() {
			return false;
		}

		@Override
		public long getPrestigeNumber(String name) {
			return prestiges.get(name).getNumber();
		}

		@Override
		public String getRangedDisplay(long prestige) {
			return getPrestige(prestige).getDisplayName();
		}

		@Override
		public void useContinuousComponents(long prestige, Consumer<ComponentsHolder> componentsAction) {
			// Does nothing...
		}

		@Override
		public CommandsComponent getCommandsComponent() {
			return prestigeCommands;
		}

		@Override
		public void useCommandsComponent(Consumer<CommandsComponent> action) {
			if (prestigeCommands == null) return;
			action.accept(prestigeCommands);
		}

		@Override
		public String getCostExpression() {
			return null;
		}

	}

	public static class InfinitePrestigeStorage implements IPrestigeStorage {

		/**
		 * For infinite prestige, we don't store all prestiges, instead we use one
		 * universal prestige object for prestiges that don't have unique settings.
		 */
		private Map<Long, Prestige> prestiges = new HashMap<>();
		private final String firstPrestigeName = "1";
		private final long firstPrestigeNumber = 1;
		private String lastPrestigeName;
		private long lastPrestigeNumber;
		private Prestige universalPrestige;

		public Map<HashedLongRange, String> getConstantSettings() {
			return constantSettings;
		}

		private Map<HashedLongRange, String> constantSettings = new HashMap<>();

		public Map<HashedModuloRange, ComponentsHolder> getContinuousSettings() {
			return continuousSettings;
		}

		private Map<HashedModuloRange, ComponentsHolder> continuousSettings = new HashMap<>();
		private CommandsComponent maxPrestigeCommands;
		private String costExpression;

		@SuppressWarnings("unchecked")
		@Override
		public void loadPrestiges() {
			prestiges.clear();
			constantSettings.clear();
			continuousSettings.clear();
			lastPrestigeName = null;
			lastPrestigeNumber = 0;
			FileConfiguration infinitePrestigeConfig = ConfigManager.getInfinitePrestigeConfig();
			ConfigurationSection prestigeSection = infinitePrestigeConfig.getConfigurationSection("Prestiges-Settings");
			ConfigurationSection globalSection = infinitePrestigeConfig.getConfigurationSection("Global-Settings");
			ConfigurationSection constantSection = infinitePrestigeConfig
					.getConfigurationSection("Constant-Prestiges-Settings");
			ConfigurationSection continuousSection = infinitePrestigeConfig
					.getConfigurationSection("Continuous-Prestiges-Settings");
			for (String prestigeName : prestigeSection.getKeys(false)) {
				ConfigurationSection current = prestigeSection.getConfigurationSection(prestigeName);
				Prestige prestige = new UniversalPrestige(prestigeName,
						StringManager.parseColorsAndSymbols(
								ConfigManager.getPossible(globalSection, String.class, StorageFields.DISPLAY_FIELDS)),
						null, 0.0, StringManager.parseColorsAndSymbols(current.getStringList("broadcast")),
						StringManager.parseColorsAndSymbols(
								ConfigManager.getPossible(current, List.class, StorageFields.MESSAGE_FIELDS)),
						CommandsComponent
								.parseCommands(ConfigManager.getPossible(current, StorageFields.COMMANDS_FIELDS)),
						null, null, null, null, null, null, 0.0);
				try {
					prestiges.put(Long.parseLong(prestigeName), prestige);
				} catch (NumberFormatException ex) {
					PrisonRanksX.logSevere("Failed to parse infinite prestige number: '" + prestigeName + "' It's NOT a number! It must be a number.");
					ex.printStackTrace();
				}

			}
			for (String prestigeName : constantSection.getKeys(false)) {
				long maxRange = ConfigManager.getPossibleLong(constantSection.getConfigurationSection(prestigeName),
						StorageFields.NEXT_FIELDS);
				long minRange = NumParser.readLong(prestigeName);
				String display = StringManager.parseColorsAndSymbols(
						ConfigManager.getPossible(constantSection.getConfigurationSection(prestigeName), String.class,
								StorageFields.DISPLAY_FIELDS));
				constantSettings.put(HashedLongRange.newRange(minRange, maxRange), display);
			}
			for (String prestigeName : continuousSection.getKeys(false)) {
				ConfigurationSection rangeSection = continuousSection.getConfigurationSection(prestigeName);
				HashedModuloRange range = HashedModuloRange.newRange(Long.parseLong(prestigeName));
				ComponentsHolder componentsHolder = ComponentsHolder.hold()
						.commands(CommandsComponent
								.parseCommands(ConfigManager.getPossible(rangeSection, StorageFields.COMMANDS_FIELDS)))
						.messages(StringManager.parseColorsAndSymbols(
								ConfigManager.getPossible(rangeSection, List.class, StorageFields.MESSAGE_FIELDS)))
						.broadcastMessages(StringManager
								.parseColorsAndSymbols(ConfigManager.getPossible(rangeSection, List.class, "broadcast")));
				continuousSettings.put(range, componentsHolder);
			}
			maxPrestigeCommands = CommandsComponent.parseCommands(globalSection.getStringList("max-prestige-commands"));
			lastPrestigeNumber = ConfigManager.getPossibleLong(globalSection, "last-prestige", "final-prestige");
			lastPrestigeName = String.valueOf(lastPrestigeNumber);
			universalPrestige = new UniversalPrestige("0",
					StringManager.parseColorsAndSymbols(
							ConfigManager.getPossible(globalSection, String.class, StorageFields.DISPLAY_FIELDS)),
					null, 0.0d, globalSection.getStringList("broadcast"),
					ConfigManager.getPossible(globalSection, List.class, StorageFields.MESSAGE_FIELDS),
					CommandsComponent.parseCommands(
							ConfigManager.getPossible(globalSection, List.class, StorageFields.COMMANDS_FIELDS)),
					RequirementsComponent.parseRequirements(
							ConfigManager.getPossible(globalSection, StorageFields.REQUIREMENTS_FIELDS)),
					null, null, null, null, null, firstPrestigeNumber);
			costExpression = globalSection.getString("cost-expression");
		}

		public Prestige getUniversalPrestige() {
			return universalPrestige;
		}

		public Set<HashedLongRange> getRegisteredLongRanges() {
			return constantSettings.keySet();
		}

		public Collection<String> getRangedDisplays() {
			return constantSettings.values();
		}

		@Override
		public boolean prestigeExists(String name) {
			return NumParser.checkLong(name, this::prestigeExists, str -> false);
		}

		@Override
		public boolean prestigeExists(long number) {
			return number > 0 && number <= lastPrestigeNumber;
		}

		@Override
		public Prestige getPrestige(String name) {
			if (name == null) return null;
			Prestige regPrestige = prestiges.get(Long.parseLong(name));
			if (regPrestige == null) {
				regPrestige = universalPrestige;
				return regPrestige.setName(name);
			}
			return regPrestige;
		}

		@Override
		public Prestige getPrestige(long number) {
			return getPrestige(String.valueOf(number));
		}

		@Override
		public String getFirstPrestigeName() {
			return firstPrestigeName;
		}

		@Override
		public long getFirstPrestigeAsNumber() {
			return firstPrestigeNumber;
		}

		@Override
		public String getLastPrestigeName() {
			return lastPrestigeName;
		}

		@Override
		public long getLastPrestigeAsNumber() {
			return lastPrestigeNumber;
		}

		@Override
		public Set<String> getPrestigeNames() {
			return null;
		}

		@Override
		public Collection<Prestige> getPrestiges() {
			return null;
		}

		@Override
		public String matchPrestigeName(String name) {
			return prestigeExists(name) ? String.valueOf(NumParser.readLong(name)) : null;
		}

		@Override
		public Prestige matchPrestige(String name) {
			return prestigeExists(name) ? prestiges.get(Long.parseLong(name)) : null;
		}

		@Override
		public boolean isInfinite() {
			return true;
		}

		@Override
		public long getPrestigeNumber(String name) {
			if (name == null) return 0;
			return NumParser.asLong(name, 0);
		}

		@Override
		public String getRangedDisplay(long prestige) {
			return constantSettings.get(HashedLongRange.matchingHash(prestige));
		}

		@Override
		public void useContinuousComponents(long prestige, Consumer<ComponentsHolder> componentsAction) {
			HashedModuloRange.forEachMatchingHash(continuousSettings, prestige, componentsAction);
		}

		@Override
		public CommandsComponent getCommandsComponent() {
			return maxPrestigeCommands;
		}

		@Override
		public void useCommandsComponent(Consumer<CommandsComponent> action) {
			if (maxPrestigeCommands == null) return;
			action.accept(maxPrestigeCommands);
		}

		@Override
		public String getCostExpression() {
			return costExpression;
		}

	}

}
