package me.prisonranksx.executors;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.bukkitutils.bukkittickbalancer.BukkitTickBalancer;
import me.prisonranksx.bukkitutils.bukkittickbalancer.ConcurrentTask;
import me.prisonranksx.bukkitutils.bukkittickbalancer.DistributedTask;
import me.prisonranksx.commands.CommandSetting;
import me.prisonranksx.components.RequirementsComponent;
import me.prisonranksx.components.RequirementsComponent.RequirementEvaluationResult;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.UserController;
import me.prisonranksx.events.*;
import me.prisonranksx.holders.Level;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.TemporaryMaxRankup;
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

	private double hologramHeight;

	private int hologramDelay;

	private Map<UUID, TemporaryMaxRankup> maxRankupData = new ConcurrentHashMap<>();

	private ConcurrentTask<Player> maxRankupTask;

	private DistributedTask<Player> autoRankupTask;

	public PrimaryRankupExecutor(PrisonRanksX plugin) {
		this.plugin = plugin;
		int speed = plugin.getGlobalSettings().getAutoRankupDelay();
		if (plugin.getGlobalSettings().isHologramsPlugin()) {
			hologramHeight = plugin.getHologramSettings().getRankupHeight();
			hologramDelay = plugin.getHologramSettings().getRankupRemoveDelay();
		}
		setupMaxRankup();
		setupAutoRankup();
	}

	private void setupAutoRankup() {
		int speed = plugin.getGlobalSettings().getAutoRankupDelay();
		autoRankupTask = BukkitTickBalancer.scheduleDistributedTask(this::silentPromote, p -> !isAutoRankupEnabled(p),
				speed);
		autoRankupTask.initAsync(plugin, speed, speed);
	}

	private void setupMaxRankup() {
		maxRankupTask = BukkitTickBalancer.scheduleConcurrentTask(player -> {
			RankupResult rankupResult = canRankup(player);
			UUID uniqueId = UniqueId.getUUID(player);
			if (rankupResult.isSuccessful() && !MAX_RANKUP_BREAKER.contains(uniqueId)) {
				executeComponents(rankupResult.getRankResult(), player);
				rankupResult.getUserResult().setRankName(rankupResult.getStringResult());
				EconomyManager.takeBalance(player, rankupResult.getDoubleResult());
				TemporaryMaxRankup tempHolder = maxRankupData.get(uniqueId);
				tempHolder.setTakenBalance(tempHolder.getTakenBalance() + rankupResult.getDoubleResult());
				tempHolder.setRankups(tempHolder.getRankups() + 1);
				tempHolder.setCurrentRankupResult(rankupResult);
				updateGroup(player);
				Optional.ofNullable(tempHolder.getLastAllowedRankName()).ifPresent(s -> {
					breakMaxRankup(uniqueId);
					if (plugin.getGlobalSettings().isRankupMaxWithPrestige()) {
						plugin.getPrestigeExecutor().maxPrestige(player);
					}
				});
			} else {
				MAX_RANKUP_BREAKER.add(uniqueId);
			}
		}, player -> finishBreakMaxRankup(player), player -> {
			UUID uniqueId = UniqueId.getUUID(player);
			TemporaryMaxRankup tempHolder = maxRankupData.get(uniqueId);
			RankupExecutor.removeMaxRankupPlayer(uniqueId);
			RankupResult rankupResult = tempHolder.getCurrentRankupResult();
			double cost = tempHolder.getTakenBalance();
			callAsyncRankupMaxEvent(player, rankupResult, tempHolder.getFirstRankName(), rankupResult.getStringResult(),
					(int) tempHolder.getRankups(), cost, false);
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
			spawnHologram(rankupResult.getRankResult(), player, true);
			updateGroup(player);
			Messages.sendMessage(player, Messages.getRankupMax(),
					updatedLine -> updatedLine.replace("%rank%", tempHolder.getFirstRankName())
							.replace("%rank_display%", tempHolder.getFirstRankDisplayName())
							.replace("%rankup%", rankupResult.getStringResult())
							.replace("%rankup_display%", rankupResult.getRankResult().getDisplayName())
							.replace("%cost%", String.valueOf(cost))
							.replace("%cost_formatted%", EconomyManager.shortcutFormat(cost))
							.replace("%cost_us_format%", EconomyManager.commaFormatWithDecimals(cost)));
			plugin.getUserController().getUser(uniqueId).setRankName(rankupResult.getStringResult());
			maxRankupData.remove(uniqueId);
			tempHolder.getFinalRankupResult().complete(rankupResult);
			if (plugin.getGlobalSettings().isRankupMaxWithPrestige()) {
				plugin.getPrestigeExecutor().maxPrestige(player);
			}
		});
		maxRankupTask.initAsync();
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
		String nextRankName = rank.getNextName();
		if (nextRankName == null) return RankupResult.FAIL_LAST_RANK.withUser(user).withString(rankName).withRank(rank);

		boolean continueChecking = balance != -1;

		Rank nextRank = RankStorage.getRank(nextRankName, pathName);
		if (continueChecking && !plugin.getGlobalSettings().isPerRankPermission()
				&& !player.hasPermission(CommandSetting.getStringSetting("rankup", "permission") + "." + nextRankName))
			return RankupResult.FAIL_NO_PERMISSION.withUser(user).withString(nextRankName).withRank(nextRank);

		double nextRankCost = PRXAPI.getRankFinalCost(nextRank, player);
		if (continueChecking && balance < nextRankCost) return RankupResult.FAIL_NOT_ENOUGH_BALANCE.withUser(user)
				.withDouble(nextRankCost)
				.withString(nextRankName)
				.withRank(nextRank);

		RequirementsComponent requirementsComponent = nextRank.getRequirementsComponent();
		if (continueChecking && requirementsComponent != null) {
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
		if (!AUTO_RANKUP_PLAYERS.contains(uniqueId)) {
			AUTO_RANKUP_PLAYERS.add(uniqueId);
			autoRankupTask.addValue(() -> player);
			return true;
		}
		AUTO_RANKUP_PLAYERS.remove(uniqueId);
		return false;
	}

	@Override
	public boolean toggleAutoRankup(Player player, boolean enable) {
		UUID uniqueId = UniqueId.getUUID(player);
		if (enable) {
			if (!AUTO_RANKUP_PLAYERS.contains(uniqueId)) {
				AUTO_RANKUP_PLAYERS.add(uniqueId);
				autoRankupTask.addValue(() -> player);
			}
			return true;
		}
		AUTO_RANKUP_PLAYERS.remove(uniqueId);
		return false;
	}

	@Override
	public boolean isAutoRankupEnabled(Player player) {
		return AUTO_RANKUP_PLAYERS.contains(UniqueId.getUUID(player));
	}

	@Override
	public RankupResult rankup(Player player) {
		RankUpdateEvent event = callRankUpdateEvent(player, RankUpdateCause.RANKUP, canRankup(player));
		if (event.isCancelled()) return event.getRankupResult();
		RankupResult rankupResult = event.getRankupResult();
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
		RankUpdateEvent event = callRankUpdateEvent(player, RankUpdateCause.RANKUP_OTHER,
				canRankup(target, EconomyManager.getBalance(player)));
		if (event.isCancelled()) return event.getRankupResult();
		RankupResult rankupResult = event.getRankupResult();
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
		RankupResult tempRankupResult = canRankup(player);
		AsyncAutoRankupEvent event = callAsyncAutoRankupEvent(player, tempRankupResult);
		if (event.isCancelled()) return tempRankupResult;
		RankupResult rankupResult = event.getRankupResult();
		if (rankupResult.isSuccessful()) {
			EconomyManager.takeBalance(player, rankupResult.getDoubleResult());
			executeComponents(rankupResult.getRankResult(), player);
			Messages.sendMessage(player, Messages.getRankup(),
					s -> s.replace("%rankup%", rankupResult.getStringResult())
							.replace("%rankup_display%", rankupResult.getRankResult().getDisplayName()));
			rankupResult.getUserResult().setRankName(rankupResult.getStringResult());
			spawnHologram(rankupResult.getRankResult(), player, true);
			updateGroup(player);
		}
		return rankupResult;
	}

	@Override
	public RankupResult forceRankup(Player player) {
		RankUpdateEvent event = callRankUpdateEvent(player, RankUpdateCause.FORCE_RANKUP, canRankup(player, -1));
		if (event.isCancelled()) return event.getRankupResult();
		RankupResult rankupResult = event.getRankupResult();
		switch (rankupResult) {
			case FAIL_LAST_RANK:
				Messages.sendMessage(player, Messages.getLastRank());
				break;
			default:
				executeComponents(rankupResult.getRankResult(), player);
				rankupResult.getUserResult().setRankName(rankupResult.getStringResult());
				Messages.sendMessage(player, Messages.getRankup(),
						s -> s.replace("%rankup%", rankupResult.getStringResult())
								.replace("%rankup_display%", rankupResult.getRankResult().getDisplayName()));
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
		if (currentRank.getNextName() == null) {
			Messages.sendMessage(player, Messages.getLastRank());
			return CompletableFuture.completedFuture(RankupResult.FAIL_LAST_RANK.withRank(currentRank).withUser(user));
		}

		// Clone of the ranks
		Set<Rank> ranks = new LinkedHashSet<>(RankStorage.getPathRanks(user.getPathName()));

		// Remove unnecessary ranks to make sure we don't go through them in the loop
		ranks.removeIf(rank -> rank.getIndex() <= currentRank.getIndex());

		if (!callPreRankupMaxEvent(player, currentRank, ranks)) return CompletableFuture.completedFuture(
				RankupResult.FAIL_OTHER.withRank(currentRank).withUser(user).withString(currentRank.getName()));

		// Player is already at last rank, so don't continue
		if (currentRank.getNextName() == null) {
			Messages.sendMessage(player, Messages.getLastRank());
			return CompletableFuture.completedFuture(RankupResult.FAIL_LAST_RANK.withUser(user));
		}

		UUID uniqueId = user.getUniqueId();
		maxRankupData.put(uniqueId,
				TemporaryMaxRankup.hold(uniqueId)
						.setFirstRankName(currentRank.getName())
						.setFirstRankDisplayName(currentRank.getDisplayName()));
		RankupExecutor.addMaxRankupPlayer(uniqueId);
		maxRankupTask.addValue(() -> player);
		return maxRankupData.get(uniqueId).getFinalRankupResult();
	}

	@Override
	public RankUpdateEvent callRankUpdateEvent(Player player, RankUpdateCause cause, RankupResult result) {
		RankUpdateEvent rankUpdateEvent = new RankUpdateEvent(player, cause, result);
		Bukkit.getPluginManager().callEvent(rankUpdateEvent);
		return rankUpdateEvent;
	}

	@Override
	public AsyncAutoRankupEvent callAsyncAutoRankupEvent(Player player, RankupResult result) {
		AsyncAutoRankupEvent asyncAutoRankupEvent = new AsyncAutoRankupEvent(player, result);
		Bukkit.getPluginManager().callEvent(asyncAutoRankupEvent);
		return asyncAutoRankupEvent;
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
	public void executeComponents(Level rank, Player player) {
		// not affected by do sync, still under pseudo async from segmented tasks
		// when needed, this for delaying only in case a /prx command is executed under
		// commands
		plugin.doSyncLater(() -> {
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
			Messages.sendMessage(player, rank.getBroadcastMessages(),
					s -> StringManager.parseReplacements(s, definition));

			// Console and Player Commands
			rank.useCommandsComponent(component -> component.dispatchCommands(player,
					s -> StringManager.parseReplacements(s, definition)));

			// Action Bar Messages
			rank.useActionBarComponent(
					component -> component.sendActionBar(player, s -> StringManager.parseReplacements(s, definition)));

			// Random Commands
			rank.useRandomCommandsComponent(component -> component.dispatchCommands(player,
					s -> StringManager.parseReplacements(s, definition)));

			// Permissions Addition and Deletion
			rank.usePermissionsComponent(component -> component.updatePermissions(player));

			// Firework
			rank.useFireworkComponent(component -> BukkitTickBalancer.sync(() -> component.spawnFirework(player)));

			StringManager.deleteReplacements(definition);
		}, 1);
	}

	@Override
	public void promote(Player player) {
		rankup(player);
	}

	@Override
	public void silentPromote(Player player) {
		rankup(player, true);
	}

	public void spawnHologram(Level rank, Player player, boolean async) {
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

	@Override
	public DistributedTask<Player> getAutoRankupTask() {
		return autoRankupTask;
	}

	@Override
	public ConcurrentTask<Player> getMaxRankupTask() {
		return maxRankupTask;
	}

}
