package me.prisonranksx.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.executors.PrestigeExecutor;
import me.prisonranksx.executors.RankupExecutor;
import me.prisonranksx.executors.RebirthExecutor;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.Rebirth;
import me.prisonranksx.holders.UniversalPrestige;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.settings.PlaceholderAPISettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PlaceholderAPIHook extends PlaceholderExpansion {

	private PrisonRanksX plugin;
	private Map<String, BiFunction<Player, String, String>> startsWithFunctions;

	public PlaceholderAPIHook(PrisonRanksX plugin) {
		this.plugin = plugin;
		initStartsWithPlaceholders();
	}

	private void initStartsWithPlaceholders() {
		startsWithFunctions = new HashMap<>();
		setupPrefix("rank_progress_percentage_cumulative_", (player, rankName) -> PRXAPI.getRank(rankName, player), percentTD(100), (player, rank) -> percent100TD(PRXAPI.getLevelProgressPercentageCumulative(rank, player)));
		setupPrefix("prestige_progress_percentage_cumulative_", (player, prestigeName) -> PRXAPI.getPrestige(prestigeName), percentTD(100), (player, prestige) -> percent100TD(PRXAPI.getLevelProgressPercentageCumulative(prestige, player)));
		setupPrefix("rank_progress_percentage_", (player, rankName) -> PRXAPI.getRank(rankName, player), percentTD(100), (player, rank) -> percent100TD(PRXAPI.getLevelProgressPercentage(rank, player)));
		setupPrefix("prestige_progress_percentage_", (player, prestigeName) -> PRXAPI.getPrestige(prestigeName), percentTD(100), (player, prestige) -> percent100TD(PRXAPI.getLevelProgressPercentage(prestige, player)));
		setupPrefix("rank_number_", (player, rankName) -> PRXAPI.getRank(rankName, player), 0, (player, rank) -> rank.getNumber());
		setupPrefix("prestige_number_", (player, prestigeName) -> PRXAPI.getPrestige(prestigeName), 0, (player, prestige) -> prestige.getNumber());
		setupPrefix("rank_display_name_", (player, rankName) -> PRXAPI.getRank(rankName, player), "none", (player, rank) -> rank.getDisplayName());
		setupPrefix("prestige_display_name_", (player, prestigeName) -> PRXAPI.getPrestige(prestigeName), "none", (player, prestige) -> prestige.getDisplayName());
		setupPrefix("player_rank_name_", (player, playerName) -> PRXAPI.getPlayerRank(getPlayer(playerName)), "none", (player, rank) -> rank.getName());
		setupPrefix("player_prestige_name_", (player, playerName) -> PRXAPI.getPlayerPrestige(getPlayer(playerName)), "none", (player, prestige) -> prestige.getName());
		setupPrefix("player_rebirth_name_", (player, playerName) -> PRXAPI.getPlayerRebirth(getPlayer(playerName)), "none", (player, rebirth) -> rebirth.getName());
		setupPrefix("player_rank_number_", (player, playerName) -> PRXAPI.getPlayerRank(getPlayer(playerName)), 0, (player, rank) -> rank.getIndex());
		setupPrefix("player_prestige_number_", (player, playerName) -> PRXAPI.getPlayerPrestige(getPlayer(playerName)), 0, (player, prestige) -> prestige.getIndex());
		setupPrefix("player_prestige_number_short_", (player, playerName) -> PRXAPI.getPlayerPrestige(getPlayer(playerName)), 0, (player, prestige) -> EconomyManager.shortcutFormat(prestige.getIndex()));
		setupPrefix("player_prestige_number_comma_", (player, playerName) -> PRXAPI.getPlayerPrestige(getPlayer(playerName)), 0, (player, prestige) -> EconomyManager.commaFormat(prestige.getIndex()));
		setupPrefix("player_rebirth_number_", (player, playerName) -> PRXAPI.getPlayerRebirth(getPlayer(playerName)), 0, (player, rebirth) -> rebirth.getIndex());
		setupPrefix("player_rank_display_name_", (player, playerName) -> PRXAPI.getPlayerRank(getPlayer(playerName)), "none", (player, rank) -> rank.getDisplayName());
		setupPrefix("player_prestige_display_name_", (player, playerName) -> PRXAPI.getPlayerPrestige(getPlayer(playerName)), "none", (player, prestige) -> prestige.getDisplayName());
		setupPrefix("player_rebirth_display_name_", (player, playerName) -> PRXAPI.getPlayerRebirth(getPlayer(playerName)), "none", (player, rebirth) -> rebirth.getDisplayName());
	}


	private void setupPrefixRaw(String startsWith, BiFunction<Player, String, String> valueFunction) {
		startsWithFunctions.put(startsWith.substring(0, startsWith.lastIndexOf("_") + 1), valueFunction);
	}

	public Player getPlayer(String playerName) {
		Player player = Bukkit.getPlayer(playerName);
		if (player == null) PrisonRanksX.logWarning("Placeholder tried to get non-existent player: " + playerName);
		return player;
	}

	private void setupPrefixNonOptional(String startsWith, BiFunction<Player, String, String> valueFunction) {
		BiFunction<Player, String, String> shortenedFunc = (player, placeholder) ->
				valueFunction.apply(player, placeholder.substring(startsWith.length()));
		setupPrefixRaw(startsWith, shortenedFunc);
	}

	/**
	 * @param startsWith                       The string that the placeholder starts with, such as "rank_number_"
	 * @param retrievedPlaceholderValueMapping A function that returns the value of the placeholder from the last string after "_".
	 *                                         First param is player that the placeholder is gonna be applied on. Second param is the last string after "_". In other words,
	 *                                         {@code (player, lastStringPart) -> doSomethingWithGivenPlayerAndLastStringPartAndReturnValue(player, lastStringPart)}
	 * @param nullValue                        The final value to return if the returned placeholder value (the one above) is null
	 * @param mappingIfNotNull                 A function that returns the value to return if the placeholder value is not null. First param is player that the placeholder is gonna be applied on. Second param is the returned placeholder value.
	 *                                         {@code (player, returnedValueFromAbove) -> doSomething(player, returnedValueFromAbove) }
	 * @param <T>                              return type of retrievedPlaceholderValueMapping function
	 * @param <U>                              return type of mappingIfNotNull
	 */
	public <T, U> void setupPrefix(String startsWith, BiFunction<Player, String, T> retrievedPlaceholderValueMapping, U nullValue, BiFunction<Player, T, U> mappingIfNotNull) {
		BiFunction<Player, String, String> shortenedFunc = (player, placeholder) ->
				Optional.ofNullable(retrievedPlaceholderValueMapping.apply(player, placeholder.substring(startsWith.length())))
						.map(value -> String.valueOf(mappingIfNotNull.apply(player, value)))
						.orElse(String.valueOf(nullValue));
		setupPrefixRaw(startsWith, shortenedFunc);
	}

	@NotNull
	private Optional<String> matchPrefix(Player player, String startsWith) {
		if (startsWith.indexOf("_") == -1) return Optional.empty();
		BiFunction<Player, String, String> startsWithFunction = startsWithFunctions.get(startsWith.substring(0, startsWith.lastIndexOf("_") + 1));
		return startsWithFunction == null ? Optional.empty() : Optional.ofNullable(startsWithFunction.apply(player, startsWith));
	}

	private PrestigeExecutor prestigeExecutor() {
		return plugin.getPrestigeExecutor();
	}

	private RankupExecutor rankupExecutor() {
		return plugin.getRankupExecutor();
	}

	private RebirthExecutor rebirthExecutor() {
		return plugin.getRebirthExecutor();
	}

	private <T> Optional<T> optional(T t) {
		return Optional.ofNullable(t);
	}

	private <T, U> String strOptional(T objectToMapAndToCheck, U ifObjectIsIsNullValue, Function<T, U> mappingIfNotNull) {
		return String.valueOf(optional(objectToMapAndToCheck).map(mappingIfNotNull).orElse(ifObjectIsIsNullValue));
	}

	private PlaceholderAPISettings settings() {
		return plugin.getPlaceholderAPISettings();
	}

	/**
	 * Determines where to put the currency symbol behind or after the string and places it there.
	 *
	 * @param string string to add the currency symbol to
	 * @return the string with the currency symbol
	 */
	private String currency(String string) {
		return settings().isCurrencySymbolBehind() ? settings().getCurrencySymbol() + string : string + settings().getCurrencySymbol();
	}

	/**
	 * Determines where to put the currency symbol behind or after the number and places it there.
	 *
	 * @param dbl number to add the currency symbol to
	 * @return the number with the currency symbol as a string
	 */
	private String currency(double dbl) {
		return settings().isCurrencySymbolBehind() ? settings().getCurrencySymbol() + dbl : dbl + settings().getCurrencySymbol();
	}

	/**
	 * Determines where to put the percent symbol behind or after the string and places it there.
	 *
	 * @param string string to add the percent symbol to
	 * @return the string with the percent symbol
	 */
	private String percent(String string) {
		return settings().isPercentSignBehind() ? settings().getPercentSign() + string : string + settings().getPercentSign();
	}

	/**
	 * TD = rounded to two decimal places.
	 * <br>
	 * This can go above 100!
	 * <br><br>
	 * Determines where to put the percent symbol behind or after the number and places it there. In addition,
	 * the number is rounded to 2 decimal places with commas formatting (1,000.00).
	 *
	 * @param dbl number to add the percent symbol to
	 * @return the number with the percent symbol
	 */
	private String percentTD(double dbl) {
		return settings().isPercentSignBehind() ?
				settings().getPercentSign() + EconomyManager.commaFormatWithDecimals(dbl) :
				EconomyManager.commaFormatWithDecimals(dbl) + settings().getPercentSign();
	}

	/**
	 * 100 = can't go over 100 percent.
	 * <br>
	 * TD = rounded to two decimal places.
	 * <br><br>
	 * Determines where to put the percent symbol behind or after the number and places it there. In addition,
	 * the number is rounded to two decimal places, and it also gets limited to 100.
	 *
	 * @param dbl number to add the percent symbol to
	 * @return the number with the percent symbol and limited to 100.
	 */
	private String percent100TD(double dbl) {
		return settings().isPercentSignBehind() ?
				settings().getPercentSign() + EconomyManager.commaFormatWithDecimals(Math.min(100, dbl)) :
				EconomyManager.commaFormatWithDecimals(Math.min(100, dbl)) + settings().getPercentSign();
	}

	/**
	 * Places the percent symbol in its appropriate place as specified in config.yml.
	 *
	 * @param dbl number to add the percent symbol to
	 * @return the number with the percent symbol.
	 */
	private String percent(double dbl) {
		return settings().isPercentSignBehind() ? settings().getPercentSign() + dbl : dbl + settings().getPercentSign();
	}

	/**
	 * 100 = can't go over 100 percent
	 * <br><br>
	 * Places the percent symbol in its appropriate place as specified in config.yml, and limits the number to 100.
	 *
	 * @param dbl number to add the percent symbol to
	 * @return the number with the percent symbol limited to 100.
	 */
	private String percent100(double dbl) {
		return settings().isPercentSignBehind() ? settings().getPercentSign() + Math.min(100, dbl) : Math.min(100, dbl) + settings().getPercentSign();
	}

	/**
	 * Whether server is using normal prestige or infinite prestige.
	 *
	 * @return true if normal prestige is being used, false otherwise.
	 */
	private boolean isNormalPrestige() {
		return !plugin.getGlobalSettings().isInfinitePrestige();
	}


	@Override
	public String onPlaceholderRequest(Player player, String params) {
		switch (params) {
			case "can_rankup": return strOptional(rankupExecutor(), "false", executor -> executor.canRankup(player).isSuccessful());
			case "can_prestige": return strOptional(prestigeExecutor(), "false", executor -> executor.canPrestige(player).isSuccessful());
			case "can_rebirth": return strOptional(rebirthExecutor(), "false", executor -> executor.canRebirth(player).isSuccessful());

			case "can_rankup_result_name": return strOptional(rankupExecutor(), "n/a", executor -> executor.canRankup(player).name());
			case "can_prestige_result_name": return strOptional(prestigeExecutor(), "n/a", executor -> executor.canPrestige(player).name());
			case "can_rebirth_result_name": return strOptional(rebirthExecutor(), "n/a", executor -> executor.canRebirth(player).name());

			case "current_rank_name":
				return strOptional(PRXAPI.getPlayerRank(player), "none", Rank::getName);
			case "current_prestige_name":
				return strOptional(PRXAPI.getPlayerPrestige(player), settings().getPrestigeNoPrestige(), Prestige::getName);
			case "current_rebirth_name":
				return strOptional(PRXAPI.getPlayerRebirth(player), settings().getRebirthNoRebirth(), Rebirth::getName);

			case "current_rank_number": return strOptional(PRXAPI.getPlayerRank(player), 0, Rank::getIndex);
			case "current_prestige_number": return strOptional(PRXAPI.getPlayerPrestige(player), 0, Prestige::getIndex);
			case "current_rebirth_number": return strOptional(PRXAPI.getPlayerRebirth(player), 0, Rebirth::getIndex);

			case "current_rank_display_name":
				return strOptional(PRXAPI.getPlayerRank(player), "none", Rank::getDisplayName);
			case "current_prestige_display_name":
				return strOptional(PRXAPI.getPlayerPrestige(player), settings().getPrestigeNoPrestige(), Prestige::getDisplayName);
			case "current_rebirth_display_name":
				return strOptional(PRXAPI.getPlayerRebirth(player), settings().getRebirthNoRebirth(), Rebirth::getDisplayName);

			// Special placeholders for infinite prestige
			case "current_prestige_display_name_comma": return strOptional(PRXAPI.getPlayerPrestige(player), 0, prestige -> {
				if (isNormalPrestige()) return prestige.getDisplayName();
				UniversalPrestige universalPrestige = (UniversalPrestige) prestige;
				return universalPrestige.getNonReplacedDisplayName().replace("{number}",
						EconomyManager.commaFormat(universalPrestige.getNumber()));
			});
			case "current_prestige_display_name_short": return strOptional(PRXAPI.getPlayerPrestige(player), 0, prestige -> {
				if (isNormalPrestige()) return prestige.getDisplayName();
				UniversalPrestige universalPrestige = (UniversalPrestige) prestige;
				return universalPrestige.getNonReplacedDisplayName().replace("{number}",
						EconomyManager.shortcutFormat(universalPrestige.getNumber()));
			});

			case "current_rank_cost":
				return strOptional(PRXAPI.getPlayerRank(player), 0, rank -> currency(PRXAPI.getRankFinalCost(rank, player)));
			case "current_prestige_cost":
				return strOptional(PRXAPI.getPlayerPrestige(player), 0, prestige -> currency(PRXAPI.getPrestigeFinalCost(prestige, player)));
			case "current_rebirth_cost":
				return strOptional(PRXAPI.getPlayerRebirth(player), 0, rebirth -> currency(rebirth.getCost()));

			case "next_rank_name":
				return strOptional(PRXAPI.getPlayerRank(player), "none", Rank::getNextName);
			case "next_prestige_name":
				return strOptional(PRXAPI.getPlayerPrestige(player), settings().getNextPrestigeNoPrestige(), Prestige::getNextName);
			case "next_rebirth_name":
				return strOptional(PRXAPI.getPlayerRebirth(player), settings().getNextRebirthNoRebirth(), Rebirth::getNextName);

			case "next_rank_display_name":
				return strOptional(PRXAPI.getPlayerNextRank(player), "none", Rank::getDisplayName);
			case "next_prestige_display_name":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), settings().getNextPrestigeNoPrestige(), Prestige::getDisplayName);
			case "next_rebirth_display_name":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), settings().getNextRebirthNoRebirth(), Rebirth::getDisplayName);

			case "next_rank_cost":
				return strOptional(PRXAPI.getPlayerNextRank(player), 0, rank -> currency(PRXAPI.getRankFinalCost(rank, player)));
			case "next_prestige_cost":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), 0, prestige ->
						currency(PRXAPI.getPrestigeFinalCost(prestige, player)));
			case "next_rebirth_cost":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), 0, rebirth -> currency(rebirth.getCost()));

			case "next_rank_cost_plain":
				return strOptional(PRXAPI.getPlayerNextRank(player), 0, rank -> PRXAPI.getRankFinalCost(rank, player));
			case "next_prestige_cost_plain":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), 0, prestige -> PRXAPI.getPrestigeFinalCost(prestige, player));
			case "next_rebirth_cost_plain":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), 0, Rebirth::getCost);

			case "next_rank_cost_short":
				return strOptional(PRXAPI.getPlayerNextRank(player), 0, rank -> currency(EconomyManager.shortcutFormat(rank.getCost())));
			case "next_prestige_cost_short":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), 0, prestige ->
						currency(EconomyManager.shortcutFormat(prestige.getCost())));
			case "next_rebirth_cost_short":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), 0, rebirth ->
						currency(EconomyManager.shortcutFormat(rebirth.getCost())));

			case "next_rank_cost_short_plain":
				return strOptional(PRXAPI.getPlayerNextRank(player), 0, rank -> EconomyManager.shortcutFormat(rank.getCost()));
			case "next_prestige_cost_short_plain":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), 0, prestige -> EconomyManager.shortcutFormat(prestige.getCost()));
			case "next_rebirth_cost_short_plain":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), 0, rebirth -> EconomyManager.shortcutFormat(rebirth.getCost()));

			case "next_rank_cost_comma":
				return strOptional(PRXAPI.getPlayerNextRank(player), 0, rank -> currency(EconomyManager.commaFormat(rank.getCost())));
			case "next_prestige_cost_comma":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), 0, prestige -> currency(EconomyManager.commaFormat(prestige.getCost())));
			case "next_rebirth_cost_comma":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), 0, rebirth -> currency(EconomyManager.commaFormat(rebirth.getCost())));

			case "next_rank_cost_comma_plain":
				return strOptional(PRXAPI.getPlayerNextRank(player), 0, rank -> EconomyManager.commaFormat(rank.getCost()));
			case "next_prestige_cost_comma_plain":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), 0, prestige -> EconomyManager.commaFormat(prestige.getCost()));
			case "next_rebirth_cost_comma_plain":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), 0, rebirth -> EconomyManager.commaFormat(rebirth.getCost()));

			case "next_rank_cost_normal":
				return strOptional(PRXAPI.getPlayerNextRank(player), 0, rank -> currency(rank.getCost()));
			case "next_prestige_cost_normal":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), 0, prestige -> currency(prestige.getCost()));

			case "next_rank_cost_normal_plain":
				return strOptional(PRXAPI.getPlayerNextRank(player), 0, Rank::getCost);
			case "next_prestige_cost_normal_plain":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), 0, Prestige::getCost);

			case "next_rank_number":
				return strOptional(PRXAPI.getPlayerNextRank(player), 0, Rank::getIndex);
			case "next_prestige_number":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), 0, Prestige::getIndex);
			case "next_rebirth_number":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), 0, Rebirth::getIndex);

			case "next_rank_number_comma":
				return strOptional(PRXAPI.getPlayerNextRank(player), 0, rank -> EconomyManager.commaFormat(rank.getIndex()));
			case "next_prestige_number_comma":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), 0, prestige -> EconomyManager.commaFormat(prestige.getIndex()));
			case "next_rebirth_number_comma":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), 0, rebirth -> EconomyManager.commaFormat(rebirth.getIndex()));

			case "next_rank_number_short":
				return strOptional(PRXAPI.getPlayerNextRank(player), 0, rank -> EconomyManager.shortcutFormat(rank.getIndex()));
			case "next_prestige_number_short":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), 0, prestige -> EconomyManager.shortcutFormat(prestige.getIndex()));
			case "next_rebirth_number_short":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), 0, rebirth -> EconomyManager.shortcutFormat(rebirth.getIndex()));

			case "next_rank_progress_percentage":
				return strOptional(PRXAPI.getPlayerNextRank(player), percent100TD(100.0), rank -> percent100TD(PRXAPI.getLevelProgressPercentage(rank, player)));
			case "next_prestige_progress_percentage":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), percent100TD(100.0), prestige -> percent100TD(PRXAPI.getLevelProgressPercentage(prestige, player)));
			case "next_rebirth_progress_percentage":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), percent100TD(100.0), rebirth -> percent100TD(PRXAPI.getLevelProgressPercentage(rebirth, player)));

			case "next_rank_progress_percentage_plain":
				return strOptional(PRXAPI.getPlayerNextRank(player), "100", rank -> Math.min(100, PRXAPI.getLevelProgressPercentage(rank, player)));
			case "next_prestige_progress_percentage_plain":
				return strOptional(PRXAPI.getPlayerNextPrestige(player), "100", prestige -> Math.min(100, PRXAPI.getLevelProgressPercentage(prestige, player)));
			case "next_rebirth_progress_percentage_plain":
				return strOptional(PRXAPI.getPlayerNextRebirth(player), "100", rebirth -> Math.min(100, PRXAPI.getLevelProgressPercentage(rebirth, player)));

			default:
				Optional<String> result = matchPrefix(player, params);
				return result.orElse("! Misspelled/Wrong Placeholder: 'prisonranksx_" + params + "'");
		}
	}

	@Override
	public @NotNull String getAuthor() {
		return "TheGaming999";
	}

	@Override
	public @NotNull String getIdentifier() {
		return "prisonranksx";
	}

	@Override
	public @NotNull String getVersion() {
		return "3.0";
	}

}
