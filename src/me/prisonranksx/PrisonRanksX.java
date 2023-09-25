package me.prisonranksx;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import me.prisonranksx.commands.CommandLoader;
import me.prisonranksx.commands.PRXCommand;
import me.prisonranksx.commands.PrestigeCommand;
import me.prisonranksx.commands.RanksCommand;
import me.prisonranksx.commands.RankupCommand;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.RebirthStorage;
import me.prisonranksx.data.UserController;
import me.prisonranksx.data.YamlUserController;
import me.prisonranksx.executors.AdminExecutor;
import me.prisonranksx.executors.InfinitePrestigeExecutor;
import me.prisonranksx.executors.PrestigeExecutor;
import me.prisonranksx.executors.PrimaryRankupExecutor;
import me.prisonranksx.executors.RankupExecutor;
import me.prisonranksx.hooks.IHologramManager;
import me.prisonranksx.listeners.PlayerChatListener;
import me.prisonranksx.listeners.PlayerLoginListener;
import me.prisonranksx.lists.RanksGUIList;
import me.prisonranksx.lists.RanksTextList;
import me.prisonranksx.managers.ActionBarManager;
import me.prisonranksx.managers.ConversionManager;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.HologramManager;
import me.prisonranksx.managers.MySQLManager;
import me.prisonranksx.managers.PermissionsManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.permissions.PlayerGroupUpdater;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.GlobalSettings;
import me.prisonranksx.settings.HologramSettings;
import me.prisonranksx.settings.PlaceholderAPISettings;
import me.prisonranksx.settings.PrestigeSettings;
import me.prisonranksx.settings.RankSettings;
import me.prisonranksx.settings.RanksListSettings;
import me.prisonranksx.settings.RebirthSettings;

// Will continue only if dep is gone
public class PrisonRanksX extends JavaPlugin {

	private static final String PREFIX = "§e[§3PrisonRanks§cX§e]";
	private static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();
	private static final ConsoleCommandSender CONSOLE = Bukkit.getConsoleSender();
	private static PrisonRanksX instance;

	// Settings loaded from config files
	private GlobalSettings globalSettings;
	private RankSettings rankSettings;
	private PrestigeSettings prestigeSettings;
	private RebirthSettings rebirthSettings;
	private PlaceholderAPISettings placeholderAPISettings;
	private HologramSettings hologramSettings;
	private RanksListSettings ranksListSettings;

	// Executors
	private RankupExecutor rankupExecutor;
	private PrestigeExecutor prestigeExecutor;
	private IHologramManager hologramManager;
	private AdminExecutor adminExecutor;

	// Interfaces holding classes
	private PlayerGroupUpdater playerGroupUpdater;

	// Commands
	private PRXCommand prxCommand;
	private RankupCommand rankupCommand;
	private RanksCommand ranksCommand;
	private PrestigeCommand prestigeCommand;

	// User Management
	private UserController userController;

	// Listeners
	protected PlayerLoginListener playerLoginListener;
	protected PlayerChatListener playerChatListener;

	// Lists
	private RanksTextList ranksTextList;
	private RanksGUIList ranksGUIList;



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
		userController = new YamlUserController(this);
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
		if (globalSettings.isGuiRankList())
			ranksGUIList = new RanksGUIList(this);
		else
			ranksTextList = new RanksTextList(this);
	}

	public void preparePrestiges() {
		if (globalSettings.isPrestigeEnabled()) {
			prestigeSettings = new PrestigeSettings();
			boolean infinitePrestige = globalSettings.isInfinitePrestige();
			PrestigeStorage.initAndLoad(true);
			prestigeExecutor = new InfinitePrestigeExecutor(this);
			if (PrestigeCommand.isEnabled()) {
				prestigeCommand = new PrestigeCommand(this);
				prestigeCommand.register();
			}
		}
	}

	public void prepareRebirths() {
		if (globalSettings.isRebirthEnabled()) {
			RebirthStorage.loadRebirths();
			rebirthSettings = new RebirthSettings();
		}
	}

	public void registerListeners() {
		playerLoginListener = new PlayerLoginListener(this,
				EventPriority.valueOf(globalSettings.getLoginEventHandlingPriority()));
		if (globalSettings.isFormatChat()) playerChatListener = new PlayerChatListener(this,
				EventPriority.valueOf(globalSettings.getChatEventHandlingPriority()));
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

	public AdminExecutor getAdminExecutor() {
		return adminExecutor;
	}

	public RanksTextList getRanksTextList() {
		return ranksTextList;
	}

	public RanksGUIList getRanksGUIList() {
		return ranksGUIList;
	}

	public void initRanksGUIList() {
		ranksGUIList = new RanksGUIList(this);
	}

	public void initGlobalSettings() {
		globalSettings = new GlobalSettings();
	}

}
