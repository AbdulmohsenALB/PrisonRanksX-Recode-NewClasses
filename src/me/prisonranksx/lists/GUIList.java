package me.prisonranksx.lists;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.InventoryUpdate;
import me.prisonranksx.bukkitutils.ItemStackParser;
import me.prisonranksx.bukkitutils.NBTEditor;
import me.prisonranksx.bukkitutils.PlayerPagedGUI;
import me.prisonranksx.bukkitutils.PlayerPagedGUI.GUIItem;
import me.prisonranksx.components.CommandsComponent;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.lists.GUIItemParser.ClickAction;
import me.prisonranksx.lists.GUIItemParser.ClickActionsFormatter;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.NumParser;
import me.prisonranksx.utils.Scrif;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public abstract class GUIList {

	private PrisonRanksX plugin;
	private String parentSectionName;
	private ConfigurationSection parentSection;
	private PlayerPagedGUI playerPagedGUI;
	private GUIItem currentItem;
	private GUIItem completedItem;
	private GUIItem otherItem;
	protected ClickActionsFormatter clickActionsFormatter;
	private Map<String, GUIItem> specialCurrentItems;
	private Map<String, GUIItem> specialCompletedItems;
	private Map<String, GUIItem> specialOtherItems;
	private List<String> dynamicTitle;
	private boolean dynamic;
	private BukkitTask animationTask;
	private int animationIndex;
	private int animationSpeed;

	public GUIList(PrisonRanksX plugin, String parentSectionName) {
		this.parentSectionName = parentSectionName;
		this.parentSection = ConfigManager.getGUIConfig().getConfigurationSection(parentSectionName);
		this.plugin = plugin;
		clickActionsFormatter = new ClickActionsFormatter();
		specialCurrentItems = new HashMap<>();
		specialCompletedItems = new HashMap<>();
		specialOtherItems = new HashMap<>();
		dynamicTitle = new ArrayList<>();
		animationSpeed = 5;
	}

	public ConfigurationSection getParentSection() {
		return parentSection;
	}

	public String getParentSectionName() {
		return parentSectionName;
	}

	public PlayerPagedGUI getPlayerPagedGUI() {
		return playerPagedGUI;
	}

	public GUIItem getCurrentItem() {
		return currentItem;
	}

	public GUIItem getCompletedItem() {
		return completedItem;
	}

	public GUIItem getOtherItem() {
		return otherItem;
	}

	public Map<String, GUIItem> getSpecialCurrentItems() {
		return specialCurrentItems;
	}

	public Map<String, GUIItem> getSpecialCompletedItems() {
		return specialCompletedItems;
	}

	public Map<String, GUIItem> getSpecialOtherItems() {
		return specialOtherItems;
	}

	public abstract void openGUI(Player player);

	public abstract void refreshGUI(Player player);

	public abstract void openGUI(Player player, int page);

	private String initTitle() {
		String plainTitle = parentSection.getString("title");
		Validate.notNull(plainTitle, "Title of GUI cannot be null. You must put a title!");
		if (!plainTitle.contains(";;")) return StringManager.parseColorsAndSymbols(plainTitle);
		dynamic = true;
		if (parentSection.isInt("speed")) animationSpeed = parentSection.getInt("speed");
		for (String title : plainTitle.split(";;")) dynamicTitle.add(StringManager.parseColorsAndSymbols(title));
		return StringManager.parseColorsAndSymbols(dynamicTitle.get(0));
	}

	public boolean isDynamic() {
		return dynamic;
	}

	private void startAnimations() {
		if (animationTask != null) return;
		animationTask = plugin.doAsyncRepeating(() -> {
			Bukkit.getOnlinePlayers().forEach(player -> {
				if (playerPagedGUI.isOpen(player.getName())) InventoryUpdate.updateInventory(player,
						dynamicTitle.get(animationIndex)
								.replace("%page%", String.valueOf(playerPagedGUI.getCurrentPage(player) + 1))
								.replace("%last_page%", String.valueOf(playerPagedGUI.getLastPage() + 1)));
			});
			if (animationIndex < dynamicTitle.size() - 1)
				animationIndex++;
			else
				animationIndex = 0;
		}, 1, animationSpeed);
	}

	public void stopAnimations() {
		if (animationTask != null) {
			animationTask.cancel();
			animationTask = null;
		}
	}

	public void setupActions() {
		clickActionsFormatter.setupAction("update-title", StringManager::parseColorsAndSymbols,
				newTitle -> ClickAction.create(e -> {
					Player p = (Player) e.getWhoClicked();
					InventoryUpdate.updateInventory(p,
							StringManager.parsePlaceholders(
									GUIItemParser.formatPlaceholders(e.getCurrentItem(), newTitle.replace("%page%", String.valueOf(
											playerPagedGUI.getCurrentPage(p) + 1)).replace("%last_page%", String.valueOf(
											playerPagedGUI.getLastPage() + 1)).replace("[update-title] ", "")), p));
				}));

		clickActionsFormatter.setupAction("switch-page", NumParser::asInt, parsedPageNum -> ClickAction.create(e -> {
			Player p = (Player) e.getWhoClicked();
			int newPage = playerPagedGUI.getCurrentPage(p) + parsedPageNum;
			if (newPage > playerPagedGUI.getPlayerLastPage(p) - 1 || newPage < 0) {
				Messages.sendMessage(p, Messages.getRankListLastPageReached(),
						s -> s.replace("%page%", String.valueOf(playerPagedGUI.getPlayerLastPage(p))));
				return;
			}
			playerPagedGUI.openInventory(p, newPage);
		}));

		clickActionsFormatter.setupAction("go-to-page", NumParser::asInt, parsedPageNum -> ClickAction.create(e -> {
			playerPagedGUI.openInventory((Player) e.getWhoClicked(), parsedPageNum);
		}));

		clickActionsFormatter.setupBasicAction("close", e -> e.getWhoClicked().closeInventory());

		clickActionsFormatter.setupAction("console",
				(String commandLine) -> ClickAction
						.create(e -> CommandsComponent.dispatchConsoleCommand(e.getWhoClicked(),
								GUIItemParser.formatPlaceholders(e.getCurrentItem(), commandLine))));

		clickActionsFormatter.setupAction("player",
				(String commandLine) -> ClickAction
						.create(e -> CommandsComponent.dispatchPlayerCommand(e.getWhoClicked(),
								GUIItemParser.formatPlaceholders(e.getCurrentItem(), commandLine))));

		clickActionsFormatter.setupAction("msg", (String msg) -> ClickAction
				.create(e -> e.getWhoClicked().sendMessage(StringManager.parseAll(msg, (Player) e.getWhoClicked()))));

		clickActionsFormatter.setupAction("msg-raw", (String msg) -> ClickAction
				.create(e -> e.getWhoClicked().sendMessage(msg)));

		clickActionsFormatter.setupAction("switch-item", ItemStackParser::parse, stack ->
				ClickAction.create(e ->
						e.setCurrentItem(GUIItemParser.updateItemMetaFrom((Player) e.getWhoClicked(), stack.clone(), e.getCurrentItem()))));

		clickActionsFormatter.setupAction("switch-item-temp", stringStack -> {
			int duration = 0;
			for (String str : stringStack.split(" "))
				if (str.startsWith("duration=")) duration = NumParser.readInt(str.substring(9));
			return new AbstractMap.SimpleEntry<>(ItemStackParser.parse(stringStack).clone(), duration);

		}, entry -> ClickAction.create(e -> {
			ItemStack originalStack = playerPagedGUI.pageInventories
					.get(playerPagedGUI.getCurrentPage(e.getWhoClicked().getName()))
					.get(e.getSlot())
					.getItemStack();
			e.setCurrentItem(GUIItemParser.updateItemMetaFrom((Player) e.getWhoClicked(), entry.getKey().clone(), originalStack));
			PrisonRanksX.getInstance().doSyncLater(() -> e.setCurrentItem(originalStack), entry.getValue());
		}));

		clickActionsFormatter.setupAction("if", unformattedCondition -> {
			String parsedCondition = unformattedCondition.substring(0, unformattedCondition.indexOf('['));
			ClickAction clickAction = clickActionsFormatter
					.formatActions(Arrays.asList(unformattedCondition.substring(unformattedCondition.indexOf('['))))
					.get(0);
			return new AbstractMap.SimpleEntry<>(Scrif.create(parsedCondition), clickAction);
		}, entry -> ClickAction.create(e -> {
			String rankName = NBTEditor.getString(e.getCurrentItem(), "prx-rank");
			if (rankName == null) rankName = "null";
			String pathName = NBTEditor.getString(e.getCurrentItem(), "prx-path");
			if (pathName == null) pathName = null;
			String prestigeName = NBTEditor.getString(e.getCurrentItem(), "prx-prestige");
			if (prestigeName == null) prestigeName = "null";
			Rank rank = RankStorage.getRank(rankName, pathName);
			String finalRankName = rankName;
			String finalPrestigeName = prestigeName;
			if (!entry.getKey()
					.applyThenEvaluate(s -> StringManager.parsePlaceholders(
							s.replace("%gui_title%", e.getView().getTitle())
									.replace("%spc%", " ")
									.replace("%gui_size%", String.valueOf(e.getClickedInventory().getSize()))
									.replace("%gui_config_title%", playerPagedGUI.getTitle())
									.replace("%gui_config_name%", parentSectionName)
									.replace("%gui_config_size%", String.valueOf(parentSection.getInt("size")))
									.replace("%rank%", finalRankName)
									.replace("%rank_display%", rank != null ? rank.getDisplayName() : "null")
									.replace("%rank_cost%", String.valueOf(rank != null ? rank.getCost() : 0))
									.replace("%prestige%", finalPrestigeName),
							(Player) e.getWhoClicked())))
				return;
			entry.getValue().perform(e);
		}));
	}

	/**
	 * Used for initialization and reloading GUI values
	 */
	public void setup() {
		FileConfiguration config = ConfigManager.getGUIConfig();

		parentSection = config.getConfigurationSection(parentSectionName);
		playerPagedGUI = new PlayerPagedGUI(parentSection.getInt("size"), initTitle());

		setupActions();

		if (isDynamic()) startAnimations();
		/*
		 * Set static GUI items that don't need to be updated and are placed in every
		 * page
		 */
		setStatics();

		ConfigurationSection currentItemSection = parentSection.getConfigurationSection("current-item");
		ConfigurationSection completedItemSection = parentSection.getConfigurationSection("completed-item");
		ConfigurationSection otherItemSection = parentSection.getConfigurationSection("other-item");

		currentItem = GUIItemParser.parse(currentItemSection, playerPagedGUI, clickActionsFormatter);
		completedItem = GUIItemParser.parse(completedItemSection, playerPagedGUI, clickActionsFormatter);
		otherItem = GUIItemParser.parse(otherItemSection, playerPagedGUI, clickActionsFormatter);

		if (currentItemSection.isConfigurationSection("special")) {
			ConfigurationSection specialSection = currentItemSection.getConfigurationSection("special");
			specialSection.getKeys(false)
					.forEach(levelName -> specialCurrentItems.put(levelName,
							GUIItemParser.parse(specialSection.getConfigurationSection(levelName), playerPagedGUI,
									currentItem.getItemStack().clone(), clickActionsFormatter, currentItemSection)));
		}
		if (completedItemSection.isConfigurationSection("special")) {
			ConfigurationSection specialSection = completedItemSection.getConfigurationSection("special");
			specialSection.getKeys(false)
					.forEach(levelName -> specialCompletedItems.put(levelName,
							GUIItemParser.parse(specialSection.getConfigurationSection(levelName), playerPagedGUI,
									completedItem.getItemStack().clone(), clickActionsFormatter,
									completedItemSection)));
		}
		if (otherItemSection.isConfigurationSection("special")) {
			ConfigurationSection specialSection = otherItemSection.getConfigurationSection("special");
			specialSection.getKeys(false)
					.forEach(levelName -> specialOtherItems.put(levelName,
							GUIItemParser.parse(specialSection.getConfigurationSection(levelName), playerPagedGUI,
									otherItem.getItemStack().clone(), clickActionsFormatter, otherItemSection)));
		}
	}

	public void unregister() {
		playerPagedGUI.unregisterEvents();
	}

	private void setStatics() {
		ConfigurationSection globalSection = ConfigManager.getGUIConfig().getConfigurationSection("Global");
		globalSection.getKeys(false).forEach(itemSectionName -> {
			ConfigurationSection itemSection = globalSection.getConfigurationSection(itemSectionName);
			GUIItem guiItem = GUIItemParser.parse(itemSection, playerPagedGUI, clickActionsFormatter);
			if (itemSection.isInt("slot")) {
				playerPagedGUI.setStaticItem(itemSection.getInt("slot"), guiItem);
			} else {
				String stringSlot = itemSection.getString("slot");
				if (stringSlot.contains("->")) {
					String[] splitSlots = stringSlot.split("->");
					int fromSlot = NumParser.readInt(splitSlots[0]);
					int toSlot = NumParser.readInt(splitSlots[1]);
					for (int i = fromSlot; i <= toSlot; i++) {
						playerPagedGUI.setStaticItem(i, guiItem);
					}
				} else {
					String multiSlotString = stringSlot.replace(",", ", ");
					for (String fillerSlot : multiSlotString.split(", ")) {
						playerPagedGUI.setStaticItem(NumParser.readInt(fillerSlot), guiItem);
					}
				}
			}
		});
	}

	public PrisonRanksX getPlugin() {
		return plugin;
	}

}
