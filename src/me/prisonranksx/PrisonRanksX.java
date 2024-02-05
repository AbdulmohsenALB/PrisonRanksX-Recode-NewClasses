package me.prisonranksx;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import me.prisonranksx.commands.*;
import me.prisonranksx.data.*;
import me.prisonranksx.executors.*;
import me.prisonranksx.hooks.IHologramManager;
import me.prisonranksx.listeners.PlayerChatListener;
import me.prisonranksx.listeners.PlayerJoinListener;
import me.prisonranksx.listeners.PlayerLoginListener;
import me.prisonranksx.listeners.PlayerQuitListener;
import me.prisonranksx.lists.*;
import me.prisonranksx.managers.*;
import me.prisonranksx.permissions.PlayerGroupUpdater;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.*;

/**
 * Plugin main class.
 */
public class PrisonRanksX extends JavaPlugin {

	/**
	 * Prefix used for log messages.
	 */
	private static final String PREFIX = "§e[§3PrisonRanks§cX§e]";
	private static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();
	private static final ConsoleCommandSender CONSOLE = Bukkit.getConsoleSender();
	private static PrisonRanksX instance;

	// Settings loaded from config files
	/**
	 * Holds the settings that are under the section named 'Options'
	 * that's inside config.yml
	 */
	private GlobalSettings globalSettings;
	/**
	 * Holds the settings that are under the section named
	 * 'Rank-Options' that's inside config.yml
	 */
	private RankSettings rankSettings;
	/**
	 * Holds the settings that are under the section named
	 * 'Prestige-Options' that's inside config.yml
	 */
	private PrestigeSettings prestigeSettings;
	/**
	 * Holds the settings that are under the section named
	 * 'Rebirth-Options' that's inside config.yml
	 */
	private RebirthSettings rebirthSettings;
	/**
	 * Holds the settings that are under the section named
	 * 'PlaceholderAPI-Options' that's inside config.yml
	 */
	private PlaceholderAPISettings placeholderAPISettings;
	/**
	 * Holds the settings that are under the section named 'Holograms'
	 * that's inside config.yml
	 */
	private HologramSettings hologramSettings;
	/**
	 * Holds the settings that are under the section named
	 * 'Ranks-List-Options' that's inside config.yml
	 */
	private RanksListSettings ranksListSettings;
	private PrestigesListSettings prestigesListSettings;

	// Executors
	/** Provides methods for ranking up the players */
	private RankupExecutor rankupExecutor;
	/** Provides methods for prestiging the players */
	private PrestigeExecutor prestigeExecutor;
	/** Responsible for summoning holograms */
	private IHologramManager hologramManager;
	/**
	 * Provides methods for creating and editing
	 * ranks, and managing players data
	 */
	private AdminExecutor adminExecutor;

	// Interfaces holding classes
	/**
	 * For updating players groups through permissions plugins APIs
	 */
	private PlayerGroupUpdater playerGroupUpdater;

	// Commands
	private PRXCommand prxCommand;
	private RankupCommand rankupCommand;
	private AutoRankupCommand autoRankupCommand;
	private RanksCommand ranksCommand;
	private PrestigeCommand prestigeCommand;
	private AutoPrestigeCommand autoPrestigeCommand;
	private PrestigesCommand prestigesCommand;

	// User Management
	/**
	 * Provides methods for managing players data such
	 * as loading and saving them.
	 */
	private UserController userController;

	// Listeners
	protected PlayerLoginListener playerLoginListener;
	protected PlayerJoinListener playerJoinListener;
	protected PlayerQuitListener playerQuitListener;
	protected PlayerChatListener playerChatListener;

	// Lists
	private RanksTextList ranksTextList;
	private RanksGUIList ranksGUIList;
	private PrestigesTextList prestigesTextList;
	private PrestigesGUIList prestigesGUIList;

	@Override
	public void onEnable() {
		instance = this;
		ConversionManager.convertConfigFiles();

		if (GlobalSettings.SUPPORTS_ACTION_BAR) ActionBarManager.cache(); // Only load if using 1.8+ cuz action bars
																			// didn't exist in the older versions.
		StringManager.cache(); // Parse colors, PlaceholderAPI placeholders if PAPI is installed, and symbols.
		// Might stop supporting 1.6-
		UniqueId.cache(); // UUID support for legacy versions and newer versions.
		EconomyManager.cache(); // Vault economy and Balance Formatter.
		HologramManager.cache(); // Load DecentHolograms, HolographicDisplays, or nothing.
		PermissionsManager.cache(); // Vault permissions.
		MySQLManager.cache(); // A check is inside the class to determine whether MySQL should be enabled or
								// not.
		globalSettings = new GlobalSettings();
		userController = getDataStorageType() == UserControllerType.MYSQL ? new MySQLUserController(this)
				: getDataStorageType() == UserControllerType.YAML_PER_USER ? new YamlPerUserController(this)
				: new YamlUserController(this);
		logNeutral("Data storage type: " + userController.getType().name());
		playerGroupUpdater = new PlayerGroupUpdater(this);

		registerListeners();
		prepareHooks();
		prepareRanks();
		preparePrestiges();
		prepareRebirths();
		prepareAdmin();

		log("Enabled.");
	}

	@Override
	public void onDisable() {
		CommandLoader.unregisterCommand(prxCommand, rankupCommand, ranksCommand);
		userController.saveUsers(true).thenRun(() -> log("Data saved.")).exceptionally(throwable -> {
			logSevere("Failed to save data. Please report the stack trace below to the developer.");
			throwable.printStackTrace();
			return null;
		});
	}

	public void prepareHooks() {
		if (StringManager.isPlaceholderAPI()) {
			logNeutral("Loading PlaceholderAPI placeholders...");
			placeholderAPISettings = new PlaceholderAPISettings();
			// load placeholders here.
			log("PlaceholderAPI placeholders are ready to use! '/papi ecloud' is not needed.");
		} else if (globalSettings.isMvdwPlaceholderAPILoaded()) {
			placeholderAPISettings = new PlaceholderAPISettings();
			// load placeholders here.
			log("MVdWPlaceholderAPI soft dependency loaded.");
		} else {
			logWarning("Recommended plugin 'PlaceholderAPI' is not installed.");
		}
		if (globalSettings.isDecentHologramsLoaded()) {
			hologramSettings = new HologramSettings();
			log("DecentHolograms soft dependency loaded.");
		} else if (globalSettings.isHolographicDisplaysLoaded()) {
			hologramSettings = new HologramSettings();
			log("HolographicDisplays soft dependency loaded.");
		}
	}

	public void prepareAdmin() {
		adminExecutor = new AdminExecutor(this);
		if (PRXCommand.isEnabled()) {
			prxCommand = new PRXCommand(this);
			prxCommand.register();
		}
		// After a "/reload" need to load online players data
		if (GlobalSettings.SUPPORTS_ACTION_BAR) {
			for (Player player : Bukkit.getOnlinePlayers())
				userController.loadUser(UniqueId.getUUID(player), player.getName());
		}
	}

	public void prepareRanks() {
		if (!globalSettings.isRankEnabled()) return;
		RankStorage.loadRanks();
		rankSettings = new RankSettings();
		ranksListSettings = new RanksListSettings();
		rankupExecutor = new PrimaryRankupExecutor(this);
		if (RankupCommand.isEnabled()) {
			rankupCommand = new RankupCommand(this);
			rankupCommand.register();
		}
		if (RanksCommand.isEnabled()) {
			ranksCommand = new RanksCommand(this);
			ranksCommand.register();
		}
		if (AutoRankupCommand.isEnabled()) {
			autoRankupCommand = new AutoRankupCommand(this);
			autoRankupCommand.register();
		}
		if (globalSettings.isGuiRankList())
			ranksGUIList = new RanksGUIList(this);
		else
			ranksTextList = new RanksTextList(this);
	}

	public void preparePrestiges() {
		boolean infinitePrestige = globalSettings.isInfinitePrestige();
		if (globalSettings.isPrestigeEnabled()) {
			prestigeSettings = new PrestigeSettings();
			PrestigeStorage.initAndLoad(infinitePrestige);
			prestigesListSettings = new PrestigesListSettings();
			prestigeExecutor = new InfinitePrestigeExecutor(this);
			if (PrestigeCommand.isEnabled()) {
				prestigeCommand = new PrestigeCommand(this);
				prestigeCommand.register();
			}
			if (AutoPrestigeCommand.isEnabled()) {
				autoPrestigeCommand = new AutoPrestigeCommand(this);
				autoPrestigeCommand.register();
			}
			if (PrestigesCommand.isEnabled()) {
				prestigesCommand = new PrestigesCommand(this);
				prestigesCommand.register();
			}
		}
		if (globalSettings.isGuiPrestigeList()) {
			prestigesGUIList = infinitePrestige ? new InfinitePrestigesGUIList(this)
					: new RegularPrestigesGUIList(this);
		} else {
			prestigesTextList = infinitePrestige ? new InfinitePrestigesTextList(this)
					: new RegularPrestigesTextList(this);
		}
	}

	public void prepareRebirths() {
		if (globalSettings.isRebirthEnabled()) {
			RebirthStorage.loadRebirths();
			rebirthSettings = new RebirthSettings();
		}
	}

	public void registerListeners() {
		playerLoginListener = PlayerLoginListener.register(this, globalSettings.getLoginEventHandlingPriority());
		playerJoinListener = PlayerJoinListener.register(this, globalSettings.getLoginEventHandlingPriority());
		playerQuitListener = PlayerQuitListener.register(this, globalSettings.getLoginEventHandlingPriority());
		if (globalSettings.isFormatChat())
			playerChatListener = PlayerChatListener.register(this, globalSettings.getChatEventHandlingPriority());
	}

	public static void log(String message) {
		CONSOLE.sendMessage(PREFIX + " §a" + message);
	}

	public static void logNeutral(String message) {
		CONSOLE.sendMessage(PREFIX + " §7" + message);
	}

	public static void logWarning(String message) {
		CONSOLE.sendMessage(PREFIX + " §e[!] " + message);
	}

	public static void logSevere(String message) {
		CONSOLE.sendMessage(PREFIX + " §4[!] §c" + message);
	}

	public BukkitTask doSyncLater(Runnable runnable, int delay) {
		return SCHEDULER.runTaskLater(this, runnable, delay);
	}

	public BukkitTask doSync(Runnable runnable) {
		return SCHEDULER.runTask(this, runnable);
	}

	public static BukkitTask sync(Runnable runnable) {
		return SCHEDULER.runTask(instance, runnable);
	}

	public BukkitTask doSyncRepeating(Runnable runnable, int delay, int speed) {
		return SCHEDULER.runTaskTimer(this, runnable, delay, speed);
	}

	public BukkitTask doAsync(Runnable runnable) {
		return SCHEDULER.runTaskAsynchronously(this, runnable);
	}

	public static BukkitTask async(Runnable runnable) {
		return SCHEDULER.runTaskAsynchronously(instance, runnable);
	}

	public BukkitTask doAsyncRepeating(Runnable runnable, int delay, int speed) {
		return SCHEDULER.runTaskTimerAsynchronously(this, runnable, delay, speed);
	}

	public BukkitTask doAsyncLater(Runnable runnable, int delay) {
		return SCHEDULER.runTaskLaterAsynchronously(this, runnable, delay);
	}

	public GlobalSettings getGlobalSettings() {
		return globalSettings;
	}

	public static PrisonRanksX getInstance() {
		return instance;
	}

	public UserController getUserController() {
		return userController;
	}

	public void setUserController(UserController userController) {
		this.userController = userController;
	}

	private UserControllerType getDataStorageType() {
		String dataStorageType = globalSettings.getDataStorageType().toUpperCase();
		return dataStorageType.equals("YAML") ? UserControllerType.YAML
				: dataStorageType.equals("YAML_PER_USER") ? UserControllerType.YAML_PER_USER
				: dataStorageType.equals("MYSQL") ? UserControllerType.MYSQL : UserControllerType.YAML;
	}

	public PlaceholderAPISettings getPlaceholderAPISettings() {
		return placeholderAPISettings;
	}

	public RankupExecutor getRankupExecutor() {
		return rankupExecutor;
	}

	public PrestigeExecutor getPrestigeExecutor() {
		return prestigeExecutor;
	}

	public RankSettings getRankSettings() {
		return rankSettings;
	}

	public PrestigeSettings getPrestigeSettings() {
		return prestigeSettings;
	}

	public RebirthSettings getRebirthSettings() {
		return rebirthSettings;
	}

	private TaskChainFactory taskChainFactory;

	public <T> TaskChain<T> newChain() {
		return taskChainFactory.newChain();
	}

	public <T> TaskChain<T> newSharedChain(String name) {
		return taskChainFactory.newSharedChain(name);
	}

	public PlayerGroupUpdater getPlayerGroupUpdater() {
		return playerGroupUpdater;
	}

	public IHologramManager getHologramManager() {
		return hologramManager;
	}

	public HologramSettings getHologramSettings() {
		return hologramSettings;
	}

	public RanksListSettings getRanksListSettings() {
		return ranksListSettings;
	}

	public PrestigesListSettings getPrestigesListSettings() {
		return prestigesListSettings;
	}

	public AdminExecutor getAdminExecutor() {
		return adminExecutor;
	}

	public RanksTextList getRanksTextList() {
		return ranksTextList;
	}

	public RanksGUIList getRanksGUIList() {
		return ranksGUIList;
	}

	public PrestigesGUIList getPrestigesGUIList() {
		return prestigesGUIList;
	}

	public PrestigesTextList getPrestigesTextList() {
		return prestigesTextList;
	}

	public void initRanksGUIList() {
		ranksGUIList = new RanksGUIList(this);
	}

	public void initGlobalSettings() {
		globalSettings = new GlobalSettings();
	}

}
