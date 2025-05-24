package me.prisonranksx;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import me.prisonranksx.commands.*;
import me.prisonranksx.data.*;
import me.prisonranksx.executors.*;
import me.prisonranksx.holders.User;
import me.prisonranksx.hooks.PlaceholderAPIHook;
import me.prisonranksx.listeners.PlayerChatListener;
import me.prisonranksx.listeners.PlayerJoinListener;
import me.prisonranksx.listeners.PlayerLoginListener;
import me.prisonranksx.listeners.PlayerQuitListener;
import me.prisonranksx.lists.*;
import me.prisonranksx.managers.*;
import me.prisonranksx.permissions.PlayerGroupUpdater;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

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
	 * 'Options' from config.yml
	 */
	private GlobalSettings globalSettings;
	/**
	 * 'Rank-Options' from config.yml
	 */
	private RankSettings rankSettings;
	/**
	 * 'Prestige-Options' from config.yml
	 */
	private PrestigeSettings prestigeSettings;
	/**
	 * 'Rebirth-Options' from config.yml
	 */
	private RebirthSettings rebirthSettings;
	/**
	 * 'PlaceholderAPI-Options' from config.yml
	 */
	private PlaceholderAPISettings placeholderAPISettings;
	/**
	 * 'Holograms' from config.yml
	 */
	private HologramSettings hologramSettings;
	/**
	 * 'Ranks-List-Options' from config.yml
	 */
	private RanksListSettings ranksListSettings;
	private PrestigesListSettings prestigesListSettings;
	private RebirthsListSettings rebirthsListSettings;

	// Executors
	// Rank up executor, for example has: auto rank up toggling, max rank up, force
	// rank up, silent
	// rank up, rank up by other, default rank up
	/**
	 * For ranking up the players
	 */
	private RankupExecutor rankupExecutor;
	/**
	 * For prestiging the players
	 */
	private PrestigeExecutor prestigeExecutor;
	/**
	 * For rebirthing the players
	 */
	private RebirthExecutor rebirthExecutor;
	/**
	 * Provides methods for creating and editing
	 * ranks, and managing players data
	 */
	private AdminExecutor adminExecutor;

	// Interfaces holding classes
	/**
	 * For updating players groups through permissions plugins' APIs
	 */
	private PlayerGroupUpdater playerGroupUpdater;

	// Commands

	// Admin command.
	private PRXCommand prxCommand;

	// Self-promotion commands
	private RankupCommand rankupCommand;
	private PrestigeCommand prestigeCommand;
	private RebirthCommand rebirthCommand;

	// Auto self-promotion commands
	private AutoRankupCommand autoRankupCommand;
	private AutoPrestigeCommand autoPrestigeCommand;
	private AutoRebirthCommand autoRebirthCommand;

	// Max self-promotion commands
	private RankupMaxCommand rankupMaxCommand;

	public PrestigeMaxCommand getPrestigeMaxCommand() {
		return prestigeMaxCommand;
	}

	public RankupMaxCommand getRankupMaxCommand() {
		return rankupMaxCommand;
	}

	private PrestigeMaxCommand prestigeMaxCommand;

	// List commands
	private RanksCommand ranksCommand;
	private PrestigesCommand prestigesCommand;
	private RebirthsCommand rebirthsCommand;

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
	private RebirthsTextList rebirthsTextList;
	private RebirthsGUIList rebirthsGUIList;

	// Hooks
	private PlaceholderAPIHook placeholderAPIHook;

	public static boolean debug = true;

	// To reduce method calls for methods that are called a lot, we introduce these variables
	private boolean forceSave;

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

		initGlobalSettings();
		initUserController();
		logInfo("Data storage type: " + userController.getType().name());
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
		CommandLoader.unregisterCommand(prxCommand, rankupCommand, ranksCommand, prestigeCommand);
		userController.saveUsers(true).thenRun(() -> log("Data saved.")).thenRunAsync(() -> {
			userController.unloadUsers();
			if (prestigeExecutor != null) prestigeExecutor.stopTasks();
			if (rankupExecutor != null) rankupExecutor.stopTasks();
			if (rebirthExecutor != null) rebirthExecutor.stopTasks();
		}).exceptionally(throwable -> {
			logSevere("Failed to save data. Please report the stack trace below to the developer.");
			throwable.printStackTrace();
			return null;
		});
	}

	public void prepareHooks() {
		if (StringManager.isPlaceholderAPI()) {
			logInfo("Loading PlaceholderAPI placeholders...");
			placeholderAPISettings = new PlaceholderAPISettings();
			placeholderAPIHook = new PlaceholderAPIHook(this);
			if (placeholderAPIHook.register()) {
				log("PlaceholderAPI placeholders are ready to use! '/papi ecloud' is not needed.");
			} else {
				logWarning("Failed to load PlaceholderAPI placeholders.");
			}
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
		if (RankupMaxCommand.isEnabled()) {
			rankupMaxCommand = new RankupMaxCommand(this);
			rankupMaxCommand.register();
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
			if (PrestigeMaxCommand.isEnabled()) {
				prestigeMaxCommand = new PrestigeMaxCommand(this);
				prestigeMaxCommand.register();
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
			rebirthsListSettings = new RebirthsListSettings();
			rebirthExecutor = new PrimaryRebirthExecutor(this);
			if (RebirthCommand.isEnabled()) {
				rebirthCommand = new RebirthCommand(this);
				rebirthCommand.register();
			}
			// Not needed
			/**
			 if (AutoRebirthCommand.isEnabled()) {
			 // autoRebirthCommand = new AutoRebirthCommand(this);
			 // autoRebirthCommand.register();
			 }
			 */
			if (RebirthsCommand.isEnabled()) {
				rebirthsCommand = new RebirthsCommand(this);
				rebirthsCommand.register();
			}
			if (globalSettings.isGuiRebirthList())
				rebirthsGUIList = new RebirthsGUIList(this);
			else
				rebirthsTextList = new RebirthsTextList(this);
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

	public static void logInfo(String message) {
		CONSOLE.sendMessage(PREFIX + " §7" + message);
	}

	public static void logWarning(String message) {
		CONSOLE.sendMessage(PREFIX + " §e[!] " + StringManager.parseColors(message));
	}

	public static void logSevere(String message) {
		String formattedMessage = StringManager.parseColors(PREFIX + " §4[!] §c" + message);
		CONSOLE.sendMessage(formattedMessage);
		Bukkit.getOperators().stream()
				.filter(OfflinePlayer::isOnline)
				.map(OfflinePlayer::getPlayer)
				.filter(Objects::nonNull)
				.forEach(player -> player.sendMessage(formattedMessage));
	}

	public static void logDebug(String message) {
		CONSOLE.sendMessage(PREFIX + " §b" + message);
	}

	public BukkitTask doSyncLater(Runnable runnable, long delay) {
		return SCHEDULER.runTaskLater(this, runnable, delay);
	}

	public BukkitTask doSync(Runnable runnable) {
		return SCHEDULER.runTask(this, runnable);
	}

	public static BukkitTask sync(Runnable runnable) {
		return SCHEDULER.runTask(instance, runnable);
	}

	public BukkitTask doSyncRepeating(Runnable runnable, long delay, long speed) {
		return SCHEDULER.runTaskTimer(this, runnable, delay, speed);
	}

	public BukkitTask doAsync(Runnable runnable) {
		return SCHEDULER.runTaskAsynchronously(this, runnable);
	}

	public static BukkitTask async(Runnable runnable) {
		return SCHEDULER.runTaskAsynchronously(instance, runnable);
	}

	public BukkitTask doAsyncRepeating(Runnable runnable, long delay, long speed) {
		return SCHEDULER.runTaskTimerAsynchronously(this, runnable, delay, speed);
	}

	public BukkitTask doAsyncLater(Runnable runnable, long delay) {
		return SCHEDULER.runTaskLaterAsynchronously(this, runnable, delay);
	}

	public static BukkitTask task(boolean async, Runnable runnable) {
		return async ? async(runnable) : sync(runnable);
	}

	public BukkitTask doTask(boolean async, Runnable runnable) {
		return async ? doAsync(runnable) : doSync(runnable);
	}

	public BukkitTask doTaskLater(boolean async, Runnable runnable) {
		return async ? doAsyncLater(runnable, 1) : doSyncLater(runnable, 1);
	}

	public BukkitTask doTaskLater(boolean async, Runnable runnable, long delay) {
		return async ? doAsyncLater(runnable, delay) : doSyncLater(runnable, delay);
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
		return UserControllerType.matchType(globalSettings.getDataStorageType());
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

	public RebirthExecutor getRebirthExecutor() {
		return rebirthExecutor;
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

	public HologramSettings getHologramSettings() {
		return hologramSettings;
	}

	public RanksListSettings getRanksListSettings() {
		return ranksListSettings;
	}

	public PrestigesListSettings getPrestigesListSettings() {
		return prestigesListSettings;
	}

	public RebirthsListSettings getRebirthsListSettings() {
		return rebirthsListSettings;
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

	public RebirthsGUIList getRebirthsGUIList() {
		return rebirthsGUIList;
	}

	public RebirthsTextList getRebirthsTextList() {
		return rebirthsTextList;
	}

	public void initRanksGUIList() {
		ranksGUIList = new RanksGUIList(this);
	}

	public void initGlobalSettings() {
		globalSettings = new GlobalSettings();
		forceSave = globalSettings.isForceSave();
	}

	public void forceSave(User user) {
		userController.saveUser(user, true);
	}

	public PRXCommand getPrxCommand() {
		return prxCommand;
	}

	public void initUserController() {
		switch (getDataStorageType()) {
			case MYSQL:
				userController = new MySQLUserController(this);
				break;
			case YAML_PER_USER:
				userController = new YamlPerUserController(this);
				break;
			default:
				userController = new YamlUserController(this);
				break;
		}
	}

	public boolean isRankEnabled() {
		return globalSettings.isRankEnabled();
	}

	public boolean isPrestigeEnabled() {
		return globalSettings.isPrestigeEnabled();
	}

	public boolean isRebirthEnabled() {
		return globalSettings.isRebirthEnabled();
	}

	public boolean isForceSave() {
		return forceSave;
	}
}
