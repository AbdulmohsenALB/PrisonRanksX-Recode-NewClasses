package me.prisonranksx.executors;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.util.concurrent.AtomicDouble;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.HeavyBukkit;
import me.prisonranksx.bukkitutils.HeavyBukkit.PreparedLoop;
import me.prisonranksx.components.RequirementsComponent;
import me.prisonranksx.components.RequirementsComponent.RequirementEvaluationResult;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.UserController;
import me.prisonranksx.events.AsyncAutoRankupEvent;
import me.prisonranksx.events.AsyncRankupMaxEvent;
import me.prisonranksx.events.PreRankupMaxEvent;
import me.prisonranksx.events.RankUpdateCause;
import me.prisonranksx.events.RankUpdateEvent;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.User;
import me.prisonranksx.hooks.IHologram;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.HologramManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.UniqueRandom;

public class PrimaryRankupExecutor implements RankupExecutor {

	private PrisonRanksX plugin;
	private BukkitTask autoRankupTask;
	private double hologramHeight;
	private int hologramDelay;

	public PrimaryRankupExecutor(PrisonRanksX plugin) {
		this.plugin = plugin;
		int speed = plugin.getGlobalSettings().getAutoRankupDelay();
		if (plugin.getGlobalSettings().isHologramsPlugin()) {
			hologramHeight = plugin.getHologramSettings().getRankupHeight();
			hologramDelay = plugin.getHologramSettings().getRankupRemoveDelay();
		}
		autoRankupTask = plugin.doAsyncRepeating(() -> AUTO_RANKUP_PLAYERS.forEach(this::silentRankup), 1, speed);
	}

	private RankupResult silentRankup(UUID uniqueId) {
		return rankup(UniqueId.getPlayer(uniqueId), true);
	}

	private UserController controlUsers() {
		return plugin.getUserController();
	}

	@Override
	public RankupResult canRankup(Player player) {
		return canRankup(player, EconomyManager.getBalance(player));
	}

	@Override
	public RankupResult canRankup(Player player, double balance) {
		User user = controlUsers().getUser(UniqueId.getUUID(player));
		String rankName = user.getRankName();
		String pathName = user.getPathName();
		Rank rank = RankStorage.getRank(rankName, pathName);
		String nextRankName = rank.getNextRankName();
		if (nextRankName == null) return RankupResult.FAIL_LAST_RANK.withUser(user).withRank(rank);

		Rank nextRank = RankStorage.getRank(nextRankName, pathName);

		if (!plugin.getGlobalSettings().isPerRankPermission()
				&& !player.hasPermission("prisonranksx.rankup." + nextRankName))
			return RankupResult.FAIL_NO_PERMISSION.withUser(user).withString(nextRankName).withRank(nextRank);

		double nextRankCost = nextRank.getCost();
		if (balance < nextRankCost) return RankupResult.FAIL_NOT_ENOUGH_BALANCE.withUser(user)
				.withDouble(nextRankCost)
				.withString(nextRankName)
				.withRank(nextRank);

		RequirementsComponent requirementsComponent = nextRank.getRequirementsComponent();
		if (requirementsComponent != null) {
			RequirementEvaluationResult evaluationResult = requirementsComponent.evaluateRequirements(player);
			if (!evaluationResult.hasSucceeded())
				return RankupResult.FAIL_REQUIREMENTS_NOT_MET.withRequirementEvaluation(evaluationResult)
						.withUser(user)
						.withRank(nextRank);
		}

		return RankupResult.SUCCESS.withUser(user).withDouble(nextRankCost).withString(nextRankName).withRank(nextRank);
	}

	@Override
	public boolean toggleAutoRankup(Player player) {
		UUID uniqueId = UniqueId.getUUID(player);
		return AUTO_RANKUP_PLAYERS.contains(uniqueId) ? AUTO_RANKUP_PLAYERS.remove(uniqueId)
				: AUTO_RANKUP_PLAYERS.add(uniqueId);
	}

	@Override
	public boolean toggleAutoRankup(Player player, boolean enable) {
		UUID uniqueId = UniqueId.getUUID(player);
		return enable ? AUTO_RANKUP_PLAYERS.add(uniqueId) : AUTO_RANKUP_PLAYERS.remove(uniqueId);
	}

	@Override
	public boolean isAutoRankupEnabled(Player player) {
		return AUTO_RANKUP_PLAYERS.contains(UniqueId.getUUID(player));
	}

	@Override
	public RankupResult rankup(Player player) {
		RankupResult rankupResult = canRankup(player);
		if (!callRankUpdateEvent(player, RankUpdateCause.RANKUP, rankupResult, rankupResult.getStringResult()))
			return rankupResult;
		switch (rankupResult) {
			case FAIL_LAST_RANK:
				Messages.sendMessage(player, Messages.getLastRank());
				break;
			case FAIL_NOT_ENOUGH_BALANCE:
				Messages.sendMessage(player, Messages.getNotEnoughBalance(),
						updatedLine -> updatedLine
								.replace("%rankup_cost%", String.valueOf(rankupResult.getDoubleResult()))
								.replace("%rankup_cost_formatted%",
										EconomyManager.shortcutFormat(rankupResult.getDoubleResult()))
								.replace("%rankup%", rankupResult.getStringResult())
								.replace("%rankup_display%", rankupResult.getRankResult().getDisplayName()));
				break;
			case FAIL_NO_PERMISSION:
				Messages.sendMessage(player, Messages.getRankupNoPermission(),
						updatedLine -> updatedLine.replace("%rankup%", rankupResult.getStringResult())
								.replace("%rankup_display%", rankupResult.getRankResult().getDisplayName()));
				break;
			case FAIL_REQUIREMENTS_NOT_MET:
				Messages.sendMessage(player, rankupResult.getRankResult().getRequirementsMessages());
				break;
			case SUCCESS:
				EconomyManager.takeBalance(player, rankupResult.getDoubleResult());
				executeComponents(rankupResult.getRankResult(), player);
				Messages.sendMessage(player, Messages.getRankup(),
						s -> s.replace("%rankup%", rankupResult.getStringResult())
								.replace("%rankup_display%", rankupResult.getRankResult().getDisplayName()));
				rankupResult.getUserResult().setRankName(rankupResult.getStringResult());
				spawnHologram(rankupResult.getRankResult(), player, true);
				updateGroup(player);
				break;
			default:
				break;
		}
		return rankupResult;
	}

	@Override
	public RankupResult rankup(Player player, Player target) {
		RankupResult rankupResult = canRankup(target, EconomyManager.getBalance(player));
		if (!callRankUpdateEvent(target, RankUpdateCause.RANKUP, rankupResult, rankupResult.getStringResult()))
			return rankupResult;
		switch (rankupResult) {
			case FAIL_LAST_RANK:
				Messages.sendMessage(player, Messages.getLastRank());
				break;
			case FAIL_NOT_ENOUGH_BALANCE:
				Messages.sendMessage(player, Messages.getNotEnoughBalanceOther(),
						updatedLine -> updatedLine
								.replace("%rankup_cost%", String.valueOf(rankupResult.getDoubleResult()))
								.replace("%rankup_cost_formatted%",
										EconomyManager.shortcutFormat(rankupResult.getDoubleResult()))
								.replace("%rankup%", rankupResult.getStringResult())
								.replace("%rankup_display%", rankupResult.getRankResult().getDisplayName())
								.replace("%player%", target.getName()));
				break;
			case FAIL_NO_PERMISSION:
				Messages.sendMessage(player, Messages.getRankupOtherNoPermission(),
						updatedLine -> updatedLine.replace("%rankup%", rankupResult.getStringResult())
								.replace("%rankup_display%", rankupResult.getRankResult().getDisplayName())
								.replace("%player%", target.getName()));
				break;
			case FAIL_REQUIREMENTS_NOT_MET:
				Messages.sendMessage(player, rankupResult.getRankResult().getRequirementsMessages());
				break;
			case SUCCESS:
				EconomyManager.takeBalance(player, rankupResult.getDoubleResult());
				executeComponents(rankupResult.getRankResult(), player);
				String rankupName = rankupResult.getStringResult();
				String rankupDisplayName = rankupResult.getRankResult().getDisplayName();
				Messages.sendMessage(target, Messages.getRankup(),
						s -> s.replace("%rankup%", rankupName).replace("%rankup_display%", rankupDisplayName));
				Messages.sendMessage(target, Messages.getRankupOtherRecipient(),
						s -> s.replace("%rankup%", rankupName)
								.replace("%rankup_display%", rankupDisplayName)
								.replace("%player%", player.getName()));
				Messages.sendMessage(player, Messages.getRankupOther(),
						s -> s.replace("%rankup%", rankupName)
								.replace("%rankup_display%", rankupDisplayName)
								.replace("%player%", target.getName()));
				rankupResult.getUserResult().setRankName(rankupName);
				spawnHologram(rankupResult.getRankResult(), player, false);
				updateGroup(player);
				break;
			default:
				break;
		}
		return rankupResult;
	}

	@Override
	public RankupResult rankup(Player player, boolean silent) {
		if (!silent) return rankup(player);
		RankupResult rankupResult = canRankup(player);
		if (!callAsyncAutoRankupEvent(player, rankupResult, rankupResult.getStringResult())) return rankupResult;
		if (rankupResult.isSuccessful()) {
			EconomyManager.takeBalance(player, rankupResult.getDoubleResult());
			executeComponents(rankupResult.getRankResult(), player);
			rankupResult.getUserResult().setRankName(rankupResult.getStringResult());
			spawnHologram(rankupResult.getRankResult(), player, true);
			updateGroup(player);
		}
		return rankupResult;
	}

	@Override
	public RankupResult forceRankup(Player player) {
		RankupResult rankupResult = canRankup(player);
		if (!callRankUpdateEvent(player, RankUpdateCause.FORCE_RANKUP, rankupResult, rankupResult.getStringResult()))
			return rankupResult;
		switch (rankupResult) {
			case FAIL_LAST_RANK:
				Messages.sendMessage(player, Messages.getLastRank());
				break;
			default:
				executeComponents(rankupResult.getRankResult(), player);
				rankupResult.getUserResult().setRankName(rankupResult.getStringResult());
				spawnHologram(rankupResult.getRankResult(), player, false);
				updateGroup(player);
				break;
		}
		return rankupResult;
	}

	@Override
	public CompletableFuture<RankupResult> maxRankup(Player player) {
		return maxRankup(player, null);
	}

	@Override
	public CompletableFuture<RankupResult> maxRankup(Player player, @Nullable String lastRank) {

		User user = controlUsers().getUser(UniqueId.getUUID(player));

		// Pre rankup max stuff
		Rank currentRank = RankStorage.getRank(user.getRankName(), user.getPathName());
		if (currentRank.getNextRankName() == null) {
			Messages.sendMessage(player, Messages.getLastRank());
			return CompletableFuture.completedFuture(RankupResult.FAIL_LAST_RANK.withRank(currentRank).withUser(user));
		}

		// Clone of the ranks
		Set<Rank> ranks = new LinkedHashSet<>(RankStorage.getPathRanks(user.getPathName()));

		// Remove unneccessary ranks to make sure we don't go through them in the loop
		ranks.removeIf(rank -> rank.getIndex() <= currentRank.getIndex());

		if (!callPreRankupMaxEvent(player, currentRank, ranks)) return CompletableFuture.completedFuture(
				RankupResult.FAIL_OTHER.withRank(currentRank).withUser(user).withString(currentRank.getName()));

		PreparedLoop<Rank> rankupLoop = HeavyBukkit.prepareLoop(ranks);

		// Setup a way to decrease player balance without lagging the server by
		// performing actual operations.
		AtomicDouble takenBalance = new AtomicDouble(0.0);
		AtomicDouble originalBalance = new AtomicDouble(EconomyManager.getBalance(player));

		// Count rankups so they are sent to the event
		AtomicInteger rankups = new AtomicInteger(0);

		// Set player balance to 0.0, so they doesn't abuse it by purchasing other
		// things
		// in the middle of a rankup max
		EconomyManager.setBalance(player, 0.0);

		CompletableFuture<RankupResult> finalRankupResult = new CompletableFuture<>();
		rankupLoop.forEach(rank -> {
			RankupResult rankupResult = canRankup(player, originalBalance.get());
			rankupLoop.storeObject(rankupResult);
			if (rankupResult.isSuccessful()) {
				executeComponents(rankupResult.getRankResult(), player);
				rankupResult.getUserResult().setRankName(rankupResult.getStringResult());
				originalBalance.set(originalBalance.get() - rankupResult.getDoubleResult());
				takenBalance.addAndGet(rankupResult.getDoubleResult());
				rankups.incrementAndGet();
			} else {
				rankupLoop.forceBreak();
			}
			if (lastRank != null && rankupResult.getStringResult().equals(lastRank)) rankupLoop.forceBreak();
		}).whenCompleteDoAsync(() -> {
			// Give back player balance
			EconomyManager.setBalance(player, originalBalance.get());
			RankupResult rankupResult = (RankupResult) rankupLoop.getObject();
			switch (rankupResult) {
				case FAIL_LAST_RANK:
					Messages.sendMessage(player, Messages.getLastRank());
					break;
				case FAIL_NOT_ENOUGH_BALANCE:
					Messages.sendMessage(player, Messages.getNotEnoughBalance(),
							updatedLine -> updatedLine
									.replace("%rankup_cost%", String.valueOf(rankupResult.getDoubleResult()))
									.replace("%rankup_cost_formatted%",
											EconomyManager.shortcutFormat(rankupResult.getDoubleResult()))
									.replace("%rankup%", rankupResult.getStringResult())
									.replace("%rankup_display%", rankupResult.getRankResult().getDisplayName()));
					break;
				case FAIL_NO_PERMISSION:
					Messages.sendMessage(player, Messages.getNoPermission(),
							updatedLine -> updatedLine.replace("%rankup%", rankupResult.getStringResult())
									.replace("%rankup_display%", rankupResult.getRankResult().getDisplayName()));
					break;
				case FAIL_REQUIREMENTS_NOT_MET:
					Messages.sendMessage(player, rankupResult.getRankResult().getRequirementsMessages());
					break;
				default:
					break;
			}
			double cost = takenBalance.get();
			callAsyncRankupMaxEvent(player, rankupResult, currentRank.getName(), rankupResult.getStringResult(),
					rankups.get(), cost, false);
			spawnHologram(rankupResult.getRankResult(), player, true);
			updateGroup(player);
			Messages.sendMessage(player, Messages.getRankupMax(),
					updatedLine -> updatedLine.replace("%rank%", currentRank.getName())
							.replace("%rank_display%", currentRank.getDisplayName())
							.replace("%rankup%", rankupResult.getStringResult())
							.replace("%rankup_display%", rankupResult.getRankResult().getDisplayName())
							.replace("%cost%", String.valueOf(cost))
							.replace("%cost_formatted%", EconomyManager.shortcutFormat(cost))
							.replace("%cost_us_format%", EconomyManager.commaFormatWithDecimals(cost)));
			finalRankupResult.complete(rankupResult);
		});
		return finalRankupResult;
	}

	@Override
	public boolean callRankUpdateEvent(Player player, RankUpdateCause cause, RankupResult result, String updatedRank) {
		RankUpdateEvent rankUpdateEvent = new RankUpdateEvent(player, cause, result, updatedRank);
		Bukkit.getPluginManager().callEvent(rankUpdateEvent);
		return !rankUpdateEvent.isCancelled();
	}

	@Override
	public boolean callAsyncAutoRankupEvent(Player player, RankupResult result, String updatedRank) {
		AsyncAutoRankupEvent asyncAutoRankupEvent = new AsyncAutoRankupEvent(player, result, updatedRank);
		Bukkit.getPluginManager().callEvent(asyncAutoRankupEvent);
		return !asyncAutoRankupEvent.isCancelled();
	}

	@Override
	public boolean callPreRankupMaxEvent(Player player, Rank rankupFrom, Set<Rank> ranksToBePassed) {
		PreRankupMaxEvent preRankupMaxEvent = new PreRankupMaxEvent(player, rankupFrom, ranksToBePassed);
		Bukkit.getPluginManager().callEvent(preRankupMaxEvent);
		return !preRankupMaxEvent.isCancelled();
	}

	@Override
	public void callAsyncRankupMaxEvent(Player player, RankupResult lastResult, String fromRank, String toRank,
			int totalRankups, double takenBalance, boolean limited) {
		AsyncRankupMaxEvent asyncRankupMaxEvent = new AsyncRankupMaxEvent(player, lastResult, fromRank, toRank,
				totalRankups, takenBalance, limited);
		Bukkit.getPluginManager().callEvent(asyncRankupMaxEvent);
	}

	@Override
	public void executeComponents(Rank rank, Player player) {
		String rankName = rank.getName();
		double cost = rank.getCost();
		String definition = "prx_" + rankName + player.getName();
		Map<String, String> replacements = new HashMap<>();
		replacements.put("player", player.getName());
		replacements.put("rankup", rankName);
		replacements.put("rankup_display", rank.getDisplayName());
		replacements.put("rankup_cost", String.valueOf(cost));
		replacements.put("rankup_cost_formatted", EconomyManager.shortcutFormat(cost));
		replacements.put("rankup_cost_us_format", EconomyManager.commaFormatWithDecimals(cost));
		StringManager.defineReplacements(definition, replacements);

		// Messages
		Messages.sendMessage(player, rank.getMessages(), s -> StringManager.parseReplacements(s, definition));
		Messages.sendMessage(player, rank.getBroadcastMessages(), s -> StringManager.parseReplacements(s, definition));

		// Console and Player Commands
		rank.useCommandsComponent(
				component -> component.dispatchCommands(player, s -> StringManager.parseReplacements(s, definition)));

		// Action Bar Messages
		rank.useActionBarComponent(
				component -> component.sendActionBar(player, s -> StringManager.parseReplacements(s, definition)));

		// Random Commands
		rank.useRandomCommandsComponent(
				component -> component.dispatchCommands(player, s -> StringManager.parseReplacements(s, definition)));

		// Permissions Addition and Deletion
		rank.usePermissionsComponent(component -> component.updatePermissions(player));

		// Firework
		rank.useFireworkComponent(component -> HeavyBukkit.run(() -> component.spawnFirework(player)));

		StringManager.deleteReplacements(definition);
	}

	private void spawnHologram(Rank rank, Player player, boolean async) {
		if (!plugin.getGlobalSettings().isHologramsPlugin() || !plugin.getHologramSettings().isRankupEnabled()) return;
		IHologram hologram = HologramManager.createHologram(
				"prx_" + player.getName() + rank.getName() + UniqueRandom.global().generate(async),
				player.getLocation().add(0, hologramHeight, 0), async);
		plugin.getHologramSettings()
				.getRankupFormat()
				.forEach(line -> hologram
						.addLine(StringManager.parsePlaceholders(line.replace("%player%", player.getName())
								.replace("%nextrank%", rank.getName())
								.replace("%nextrank_display%", rank.getDisplayName()), player), async));
		hologram.delete(hologramDelay);
	}

	@Override
	public void updateGroup(Player player) {
		if (plugin.getGlobalSettings().isVaultGroups())
			if (plugin.getPlayerGroupUpdater() != null) plugin.getPlayerGroupUpdater().update(player);
	}

	public BukkitTask getAutoRankupTask() {
		return autoRankupTask;
	}

}
