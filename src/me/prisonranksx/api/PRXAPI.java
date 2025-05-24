package me.prisonranksx.api;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.common.Common;
import me.prisonranksx.components.RequirementsComponent;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.RebirthStorage;
import me.prisonranksx.holders.*;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.utils.NumParser;
import me.prisonranksx.utils.SumMath;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public class PRXAPI {

	@NotNull
	public static PrisonRanksX getInstance() {
		return PrisonRanksX.getInstance();
	}

	@Nullable
	public static Rank getPlayerRank(UUID uniqueId) {
		User user = getUser(uniqueId);
		return RankStorage.getRank(user.getRankName(), user.getPathName());
	}

	@Nullable
	public static Rank getPlayerRank(Player player) {
		return getPlayerRank(UniqueId.getUUID(player));
	}

	@Nullable
	public static Rank getPlayerNextRank(UUID uniqueId) {
		Rank rank = getPlayerRank(uniqueId);
		if (rank == null) return null;
		return getRank(rank.getNextName());
	}

	@Nullable
	public static Rank getPlayerNextRank(Player player) {
		return getPlayerNextRank(UniqueId.getUUID(player));
	}

	@Nullable
	public static Rebirth getPlayerRebirth(UUID uniqueId) {
		return RebirthStorage.getRebirth(getUser(uniqueId).getRebirthName());
	}

	@Nullable
	public static Rebirth getPlayerRebirth(Player player) {
		return getPlayerRebirth(UniqueId.getUUID(player));
	}

	@Nullable
	public static Rebirth getPlayerNextRebirth(UUID uniqueId) {
		Rebirth rebirth = getPlayerRebirth(uniqueId);
		if (rebirth == null) return null;
		return getRebirth(rebirth.getNextName());
	}

	@Nullable
	public static Rebirth getPlayerNextRebirth(Player player) {
		return getPlayerNextRebirth(UniqueId.getUUID(player));
	}

	@Nullable
	public static Prestige getPlayerPrestige(UUID uniqueId) {
		return PrestigeStorage.getPrestige(getUser(uniqueId).getPrestigeName());
	}

	@Nullable
	public static Prestige getPlayerPrestige(Player player) {
		return getPlayerPrestige(UniqueId.getUUID(player));
	}

	@Nullable
	public static Prestige getPlayerNextPrestige(UUID uniqueId) {
		Prestige prestige = getPlayerPrestige(uniqueId);
		if (prestige == null) return null;
		return getPrestige(prestige.getNextName());
	}

	@Nullable
	public static Prestige getPlayerNextPrestige(Player player) {
		return getPlayerNextPrestige(UniqueId.getUUID(player));
	}

	public static boolean hasPrestige(@NotNull UUID uniqueId) {
		return getUser(uniqueId).getPrestigeName() != null;
	}

	public static boolean hasPrestige(@NotNull Player player) {
		return hasPrestige(UniqueId.getUUID(player));
	}

	public static boolean isLastPrestige(UUID uniqueId) {
		return getPlayerPrestige(uniqueId) != null && getPlayerNextPrestige(uniqueId) == null;
	}

	public static boolean isLastPrestige(Player player) {
		return isLastPrestige(UniqueId.getUUID(player));
	}

	public static boolean isLastRebirth(UUID uniqueId) {
		return getPlayerRebirth(uniqueId) != null && getPlayerNextRebirth(uniqueId) == null;
	}

	public static boolean isLastRebirth(Player player) {
		return isLastRebirth(UniqueId.getUUID(player));
	}

	public static long getPlayerPrestigeNumber(@NotNull UUID uniqueId) {
		return PrestigeStorage.getHandler().getPrestigeNumber(getUser(uniqueId).getPrestigeName());
	}

	public static long getPlayerPrestigeNumber(Player player) {
		return getPlayerPrestigeNumber(UniqueId.getUUID(player));
	}

	public static long getPlayerRebirthNumber(UUID uniqueId) {
		return RebirthStorage.getRebirth(getUser(uniqueId).getRebirthName()).getNumber();
	}

	public static long getPlayerRebirthNumber(Player player) {
		return getPlayerRebirthNumber(UniqueId.getUUID(player));
	}

	public static boolean hasRebirth(UUID uniqueId) {
		return getUser(uniqueId).getRebirthName() != null;
	}

	public static boolean hasRebirth(Player player) {
		return hasRebirth(UniqueId.getUUID(player));
	}

	public static double getRankFinalCost(Rank rank, UUID uniqueId) {
		Prestige prestige = getPlayerPrestige(uniqueId);
		if (!hasPrestige(uniqueId)) return rank.getCost();
		return Common.eval(getInstance().getPrestigeSettings()
				.getIncreaseExpression()
				.replace("{increase_percentage}", String.valueOf(prestige.getCostIncrease()))
				.replace("{rank_cost}", String.valueOf(rank.getCost()))
				.replace("{prestige_number}", String.valueOf(getPlayerPrestigeNumber(uniqueId))));
	}

	public static double getRankFinalCost(Rank rank, Player player) {
		Prestige prestige = getPlayerPrestige(player);
		if (!hasPrestige(player)) return rank.getCost();
		return Common.eval(getInstance().getPrestigeSettings()
				.getIncreaseExpression()
				.replace("{increase_percentage}", String.valueOf(prestige.getCostIncrease()))
				.replace("{rank_cost}", String.valueOf(rank.getCost()))
				.replace("{prestige_number}", String.valueOf(getPlayerPrestigeNumber(player))));
	}

	public static double getPrestigeFinalCost(Prestige prestige, UUID uniqueId) {
		Rebirth rebirth = getPlayerRebirth(uniqueId);
		if (!hasRebirth(uniqueId)) return prestige.getCost();
		return Common.eval(getInstance().getRebirthSettings()
				.getIncreaseExpression()
				.replace("{increase_percentage}", String.valueOf(rebirth.getCostIncrease()))
				.replace("{prestige_cost}", String.valueOf(prestige.getCost()))
				.replace("{rebirth_number}", String.valueOf(getPlayerRebirthNumber(uniqueId))));
	}

	public static double getPrestigeFinalCost(Prestige prestige, Player player) {
		Rebirth rebirth = getPlayerRebirth(player);
		if (!hasRebirth(player)) return prestige.getCost();
		return Common.eval(getInstance().getRebirthSettings()
				.getIncreaseExpression()
				.replace("{increase_percentage}", String.valueOf(rebirth.getCostIncrease()))
				.replace("{prestige_cost}", String.valueOf(prestige.getCost()))
				.replace("{rebirth_number}", String.valueOf(getPlayerRebirthNumber(player))));
	}

	public static String getPlayerPathOrDefault(Player player) {
		UUID uuid = UniqueId.getUUID(player);
		if (getInstance().getUserController().isLoaded(uuid)) return getInstance().getUserController().getUser(uuid).getPathName();
		return RankStorage.getDefaultPath();
	}

	@Nullable
	public static User getUser(Player player) {
		return getUser(UniqueId.getUUID(player));
	}

	@Nullable
	public static User getUser(UUID uniqueId) {
		return getInstance().getUserController().getUser(uniqueId);
	}

	@Nullable
	public static Rank getRank(String rankName, String pathName) {
		return RankStorage.getRank(rankName, pathName);
	}

	@Nullable
	public static Rank getRank(String rankName) {
		return getRank(rankName, RankStorage.getDefaultPath());
	}

	@Nullable
	public static Rank getRank(String rankName, Player player) {
		return getRank(rankName, getPlayerPathOrDefault(player));
	}

	@Nullable
	public static Prestige getPrestige(String prestigeName) {
		return PrestigeStorage.getPrestige(prestigeName);
	}

	@Nullable
	public static Rebirth getRebirth(String rebirthName) {
		return RebirthStorage.getRebirth(rebirthName);
	}

	public static double getLevelProgressPercentage(Level level, UUID uniqueId) {
		User user = getUser(uniqueId);
		Player player = user.getPlayer();
		Function<Level, Double> costFunc;
		switch (level.getLevelType()) {
			case RANK: costFunc = lvl -> PRXAPI.getRankFinalCost((Rank) lvl, uniqueId);
				break;
			case PRESTIGE: costFunc = lvl -> PRXAPI.getPrestigeFinalCost((Prestige) lvl, uniqueId);
				break;
			case REBIRTH: costFunc = lvl -> Optional.ofNullable(PRXAPI.getPlayerNextRebirth(uniqueId)).map(Rebirth::getCost).orElse(1.0D);
				break;
			default: costFunc = lvl -> EconomyManager.getBalance(player);
				break;
		}
		double percentage = EconomyManager.getBalance(player) / Math.max(costFunc.apply(level), 1) * 100;
		RequirementsComponent requirementsComponent = level.getRequirementsComponent();
		if (requirementsComponent == null) return percentage;
		final byte[] numberOfRequirements = {1};
		final double[] addedPercentage = {0};
		requirementsComponent.forEachNumberRequirement((string, doubleValue) -> {
			numberOfRequirements[0]++;
			addedPercentage[0] += (NumParser.readDouble(StringManager.parsePlaceholders(string, player))
					/ Math.max(doubleValue, 1)) * 100;
		});
		return Math.min(percentage, 100) + Math.min(addedPercentage[0], 100) / numberOfRequirements[0];
	}

	public static double getLevelProgressPercentageCumulative(Level level, UUID uniqueId) {
		User user = getUser(uniqueId);
		Player player = user.getPlayer();
		Function<Level, Double> costFunc;
		switch (level.getLevelType()) {
			case RANK: costFunc = lvl -> {
				long currentIndex = user.hasRank() ? user.getRank().getIndex() : 0;
				Predicate<Rank> costsToCalculatePredicate =
						rank -> rank.getIndex() >= currentIndex && rank.getIndex() <= lvl.getIndex();
				return (double) RankStorage.getPathRanks(PRXAPI.getPlayerPathOrDefault(player)).stream()
						.filter(costsToCalculatePredicate)
						.map(rank -> PRXAPI.getRankFinalCost(rank, uniqueId))
						.reduce(Double::sum)
						.orElse(1.0D);
			};
				break;
			case PRESTIGE: costFunc = lvl -> {
				if (getInstance().getGlobalSettings().isInfinitePrestige()) {
					double sumOfPrestiges = SumMath.sum(user.hasPrestige() ? user.getPrestige().getNumber() : 0, lvl.getNumber());
					double sumOfPrestigeCosts = Common.eval(PrestigeStorage.getCostExpression().replace("{number}", String.valueOf(sumOfPrestiges)));
					Rebirth rebirth = getPlayerRebirth(player);
					if (!hasRebirth(player)) return sumOfPrestigeCosts;
					return Common.eval(getInstance().getRebirthSettings()
							.getIncreaseExpression()
							.replace("{increase_percentage}", String.valueOf(rebirth.getCostIncrease()))
							.replace("{prestige_cost}", String.valueOf(sumOfPrestigeCosts))
							.replace("{rebirth_number}", String.valueOf(getPlayerRebirthNumber(player))));
				}

				long currentIndex = user.hasRank() ? user.getRank().getIndex() : 0;

				Predicate<Prestige> costsToCalculatePredicate =
						prestige -> prestige.getIndex() >= currentIndex && prestige.getIndex() <= lvl.getIndex();

				return (double) PrestigeStorage.getPrestiges().stream()
						.filter(costsToCalculatePredicate)
						.map(prestige -> PRXAPI.getPrestigeFinalCost(prestige, uniqueId))
						.reduce(Double::sum)
						.orElse(1.0D);
			};
				break;
			case REBIRTH: costFunc = lvl -> Optional.ofNullable(PRXAPI.getPlayerNextRebirth(uniqueId)).map(Rebirth::getCost).orElse(1.0D);
				break;
			default: costFunc = lvl -> EconomyManager.getBalance(player);
				break;
		}
		double percentage = EconomyManager.getBalance(player) / Math.max(costFunc.apply(level), 1) * 100;
		RequirementsComponent requirementsComponent = level.getRequirementsComponent();
		if (requirementsComponent == null) return percentage;
		final byte[] numberOfRequirements = {1};
		final double[] addedPercentage = {0};
		requirementsComponent.forEachNumberRequirement((string, doubleValue) -> {
			numberOfRequirements[0]++;
			addedPercentage[0] += (NumParser.readDouble(StringManager.parsePlaceholders(string, player))
					/ Math.max(doubleValue, 1)) * 100;
		});
		return Math.min(percentage, 100) + Math.min(addedPercentage[0], 100) / numberOfRequirements[0];
	}

	public static double getLevelProgressPercentageCumulative(Level level, Player player) {
		return getLevelProgressPercentageCumulative(level, UniqueId.getUUID(player));
	}

	public static double getLevelProgressPercentage(Level level, Player player) {
		return getLevelProgressPercentage(level, UniqueId.getUUID(player));
	}

	/**
	 public static Prestige getHighestReachablePrestige(UUID uniqueId) {
	 User user = getUser(uniqueId);
	 Prestige userPrestige = user.getPrestige();
	 if (userPrestige == null) return PrestigeStorage.getPrestige(1);
	 if (getInstance().getGlobalSettings().isInfinitePrestige()) {
	 double playerBalance = EconomyManager.getBalance(UniqueId.getPlayer(uniqueId));
	 // Calculate the highest reachable prestige
	 long currentPrestigeNumber = userPrestige.getNumber();
	 // Parse the cost expression
	 String costExpression = PrestigeStorage.getCostExpression();
	 double costMultiplier = Common.eval(costExpression.replace("{number}", "1"));

	 // Use SumMath to calculate the highest reachable prestige
	 double sumEnd = SumMath.getSumEnd(currentPrestigeNumber + 1, playerBalance / costMultiplier);
	 long highestReachablePrestigeNumber = Math.round(sumEnd);
	 double sumOfPrestiges = SumMath.sum(currentPrestigeNumber + 1, highestReachablePrestigeNumber);
	 double sumOfPrestigeCosts = Common.eval(PrestigeStorage.getCostExpression().replace("{number}", String.valueOf(sumOfPrestiges)));
	 // If for any reason, sum was higher than balance, then reduce it by 1. Most likely that is the highest reachable prestige.
	 if (sumOfPrestigeCosts > playerBalance) {
	 highestReachablePrestigeNumber--;
	 }
	 // Check if the highest reachable prestige is valid
	 if (highestReachablePrestigeNumber <= currentPrestigeNumber ||
	 highestReachablePrestigeNumber > PrestigeStorage.getLastPrestigeAsNumber()) {
	 return null;
	 }
	 return PrestigeStorage.getPrestige(highestReachablePrestigeNumber);
	 }

	 PrestigeStorage.PrestigeStorageHandler storageHandler = PrestigeStorage.getHandler();
	 PrestigeStorage.RegularPrestigeStorage regularStorage = (PrestigeStorage.RegularPrestigeStorage) storageHandler.getStorage();
	 List<String> prestigeNamesList = regularStorage.getPrestigeNamesList();
	 long currentIndex = user.hasPrestige() ? user.getPrestige().getNumber() : 0;
	 double cost = 0.0D;
	 for (int i = prestigeNamesList.indexOf(userPrestige.getName()); i < prestigeNamesList.size(); i++) {
	 Prestige prestige = regularStorage.getPrestige(prestigeNamesList.get(i));
	 if (prestige.getNumber() > currentIndex) cost += prestige.getCost();
	 if (cost > EconomyManager.getBalance(UniqueId.getPlayer(uniqueId))) return prestige;
	 }
	 return null;
	 }

	 public static Prestige getHighestReachablePrestige(Player player) {
	 return getHighestReachablePrestige(UniqueId.getUUID(player));
	 }
	 */

}
