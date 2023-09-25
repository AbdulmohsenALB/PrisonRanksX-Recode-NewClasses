package me.prisonranksx.lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.NBTEditor;
import me.prisonranksx.bukkitutils.PlayerPagedGUI;
import me.prisonranksx.bukkitutils.XEnchantment;
import me.prisonranksx.bukkitutils.XMaterial;
import me.prisonranksx.bukkitutils.PlayerPagedGUI.GUIItem;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.utils.IntParser;

public class GUIItemParser {

	public static GUIItem parse(ConfigurationSection section, PlayerPagedGUI gui) {
		return parse(section, gui, new ItemStack(Material.BEDROCK, 1), null);
	}

	public static GUIItem parse(ConfigurationSection section, PlayerPagedGUI gui, ClickActionsFormatter formatter) {
		return parse(section, gui, new ItemStack(Material.BEDROCK, 1), formatter);
	}

	public static GUIItem parse(ConfigurationSection section, PlayerPagedGUI gui, ItemStack itemStack,
			ClickActionsFormatter formatter) {
		return parse(section, gui, itemStack, formatter, section);
	}

	@SuppressWarnings("deprecation")
	public static GUIItem parse(ConfigurationSection section, PlayerPagedGUI gui, ItemStack itemStack,
			ClickActionsFormatter formatter, ConfigurationSection clickActionsSection) {
		ItemMeta itemMeta = itemStack.getItemMeta();

		String material = section.getString("material", null);
		int amount = section.getInt("amount", 1);
		short data = (short) section.getInt("data", 0);
		String name = StringManager.parseColorsAndSymbols(section.getString("name", null));
		List<String> lore = StringManager.parseColorsAndSymbols(section.getStringList("lore"));
		List<String> enchantments = section.getStringList("enchantments");
		List<String> flags = section.getStringList("flags");

		if (material != null)
			itemStack.setType(XMaterial.matchXMaterial(material).orElse(XMaterial.BEDROCK).parseMaterial());
		itemStack.setAmount(amount);
		if (data != 0) itemStack.setDurability(data);
		if (name != null) itemMeta.setDisplayName(name);
		if (lore != null) itemMeta.setLore(lore);
		if (!enchantments.isEmpty()) enchantments.forEach(line -> {
			String[] split = line.split(" ");
			itemMeta.addEnchant(XEnchantment.matchXEnchantment(split[0]).orElse(XEnchantment.DURABILITY).getEnchant(),
					IntParser.asInt(split[1], s -> PrisonRanksX.logWarning(
							"found invalid enchantment lvl: '" + s + "' under section '" + section.getName() + "'"), 1),
					true);
		});
		if (!flags.isEmpty()) flags.forEach(flag -> itemMeta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase())));
		itemStack.setItemMeta(itemMeta);

		GUIItem guiItem = GUIItem.create(itemStack);
		// Convert list of strings into actual click actions
		List<String> unformattedClickActions = clickActionsSection.getStringList("click-actions");
		List<ClickAction> clickActions = new ArrayList<>(Arrays.asList(new ClickAction(e -> e.setCancelled(true))));
		if (formatter != null) clickActions.addAll(formatter.formatActions(unformattedClickActions));
		guiItem.onClick(e -> clickActions.forEach(clickAction -> clickAction.consumer.accept(e)));
		return guiItem;
	}

	public static String formatPlaceholders(ItemStack itemStack, String unformatted) {
		if (itemStack == null || itemStack.getType() == Material.AIR) return unformatted;
		String rankName = NBTEditor.getString(itemStack, "prx-rank");
		if (rankName == null) return unformatted;
		String pathName = NBTEditor.getString(itemStack, "prx-path");
		if (pathName == null) return unformatted;
		Rank rank = RankStorage.getRank(rankName, pathName);
		double rankCost = rank.getCost();
		String formatted = unformatted.replace("%rank%", rankName)
				.replace("%rank_display%", rank.getDisplayName())
				.replace("%rank_cost%", String.valueOf(rankCost))
				.replace("%rank_cost_formatted%", EconomyManager.shortcutFormat(rankCost));
		return formatted;
	}

	public static ItemStack updateItemMetaFrom(ItemStack itemStack, ItemStack from) {
		if (itemStack.hasItemMeta()) {
			if (from == null) return itemStack;
			String rankName = NBTEditor.getString(from, "prx-rank");
			if (rankName == null) return itemStack;
			String pathName = NBTEditor.getString(from, "prx-path");
			Rank rank = RankStorage.getRank(rankName, pathName);
			ItemMeta im = itemStack.getItemMeta();
			Function<String, String> fun = RanksGUIList.fun(rank, rankName);
			if (im.hasDisplayName()) {
				im.setDisplayName(fun.apply(im.getDisplayName()));
			}
			if (im.hasLore()) {
				List<String> lore = im.getLore();
				lore.clear();
				im.getLore().forEach(loreLine -> lore.add(fun.apply(loreLine)));
				im.setLore(lore);
			}
			itemStack.setItemMeta(im);
			itemStack = NBTEditor.set(itemStack, rankName, "prx-rank");
			itemStack = NBTEditor.set(itemStack, pathName, "prx-path");
		}
		return itemStack;
	}

	public static class ClickActionsFormatter {

		private Set<String> actionNames = new HashSet<>();
		private Map<String, Function<String, ?>> preFunctions = new HashMap<>();
		private Map<String, Function<Object, ClickAction>> functions = new HashMap<>();

		public ClickActionsFormatter() {}

		public <T> void setupBasicAction(String actionName, Consumer<InventoryClickEvent> consume) {
			actionNames.add(actionName);
			Function<Object, ClickAction> fun = f -> ClickAction.create(consume);
			functions.put(actionName, fun);
		}

		@SuppressWarnings("unchecked")
		public <T> void setupAction(String actionName, Function<T, ClickAction> clickActionFunction) {
			actionNames.add(actionName);
			functions.put(actionName, (Function<Object, ClickAction>) clickActionFunction);
		}

		/**
		 * 
		 * @param <T>
		 * @param actionName          action name without brackets
		 * @param preFunction         to setup things, so they don't get executed every time
		 *                            an item is clicked, made for better performance, like
		 *                            parsing an itemstack.
		 * @param clickActionFunction live things
		 */
		@SuppressWarnings("unchecked")
		public <T> void setupAction(String actionName, Function<String, T> preFunction,
				Function<T, ClickAction> clickActionFunction) {
			actionNames.add(actionName);
			preFunctions.put(actionName, preFunction);
			functions.put(actionName, (Function<Object, ClickAction>) clickActionFunction);
		}

		public List<ClickAction> formatActions(List<String> unformattedActions) {
			List<String> actionKeys = new ArrayList<>();
			List<String> actionValues = new ArrayList<>();
			List<ClickAction> clickActions = new ArrayList<>();
			for (String string : unformattedActions) {
				int closingBracketIndex = string.indexOf(']');
				actionKeys.add(string.substring(1, closingBracketIndex));
				actionValues.add(string.substring(closingBracketIndex + (string.contains(" ") ? 2 : 1)));
			}
			for (int i = 0; i < unformattedActions.size(); i++) {
				String key = actionKeys.get(i);
				if (actionNames.contains(key)) {
					String val = actionValues.get(i);
					Function<String, ?> preFunction = preFunctions.get(key);
					Object finalObj = preFunction != null ? preFunction.apply(val) : val;
					clickActions.add(functions.get(key).apply(finalObj));
				}
			}
			return clickActions;
		}

	}

	public static class ClickAction {

		private Consumer<InventoryClickEvent> consumer;

		public ClickAction(Consumer<InventoryClickEvent> consumer) {
			this.consumer = consumer;
		}

		public void perform(InventoryClickEvent e) {
			consumer.accept(e);
		}

		public Consumer<InventoryClickEvent> getConsumer() {
			return consumer;
		}

		public static ClickAction create(Consumer<InventoryClickEvent> consumer) {
			return new ClickAction(consumer);
		}

	}

}
