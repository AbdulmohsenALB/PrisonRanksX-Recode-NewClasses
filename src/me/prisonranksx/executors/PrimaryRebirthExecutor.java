package me.prisonranksx.executors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.bukkittickbalancer.BukkitTickBalancer;
import me.prisonranksx.bukkitutils.bukkittickbalancer.ConcurrentTask;
import me.prisonranksx.bukkitutils.bukkittickbalancer.DistributedTask;
import me.prisonranksx.components.RequirementsComponent;
import me.prisonranksx.components.RequirementsComponent.RequirementEvaluationResult;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.RebirthStorage;
import me.prisonranksx.data.UserController;
import me.prisonranksx.events.*;
import me.prisonranksx.holders.*;
import me.prisonranksx.hooks.IHologram;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.HologramManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.UniqueRandom;

public class PrimaryRebirthExecutor implements RebirthExecutor {

	private PrisonRanksX plugin;

	private double hologramHeight;

	private int hologramDelay;

	private Map<UUID, TemporaryMaxRebirth> maxRebirthData = new ConcurrentHashMap<>();

	private ConcurrentTask<Player> maxRebirthTask;

	private DistributedTask<Player> autoRebirthTask;

	public PrimaryRebirthExecutor(PrisonRanksX plugin) {
		this.plugin = plugin;
		if (plugin.getGlobalSettings().isHologramsPlugin()) {
			hologramHeight = plugin.getHologramSettings().getRebirthHeight();
			hologramDelay = plugin.getHologramSettings().getRebirthRemoveDelay();
		}
		// setupMaxRebirth();
		// setupAutoRebirth();
	}

	private void setupAutoRebirth() {
		int speed = plugin.getGlobalSettings().getAutoRebirthDelay();
		autoRebirthTask = BukkitTickBalancer.scheduleDistributedTask(this::silentRebirth, p -> !isAutoRebirthEnabled(p),
				speed < 1 ? 1 : speed);
		// autoRebirthTask.initAsync(plugin, speed, speed);
	}

	private void setupMaxRebirth() {
		maxRebirthTask = BukkitTickBalancer.scheduleConcurrentTask(player -> {
			UUID uniqueId = UniqueId.getUUID(player);
			RebirthResult rebirthResult = canRebirth(player, false);
			if (rebirthResult.isSuccessful() && !MAX_REBIRTH_BREAKER.contains(uniqueId)) {
				String rebirthName = rebirthResult.getStringResult();
				Rebirth rebirth = rebirthResult.getRebirthResult();
				Messages.sendMessage(player, Messages.getRebirth(), s -> s.replace("%nextrebirth%", rebirthName)
						.replace("%nextrebirth_display%", rebirth.getDisplayName()));
				executeComponents(rebirth, player);
				EconomyManager.takeBalance(player, rebirthResult.getDoubleResult());
				TemporaryMaxRebirth tempHolder = maxRebirthData.get(uniqueId);
				tempHolder.setTakenBalance(tempHolder.getTakenBalance() + rebirthResult.getDoubleResult());
				tempHolder.setRebirths(tempHolder.getRebirths() + 1);
				tempHolder.setCurrentRebirthResult(rebirthResult);
				rebirthResult.getUserResult().setRebirthName(rebirthName);
				if (plugin.getGlobalSettings().isRankEnabled() && plugin.getRebirthSettings().isResetRank()) {
					plugin.getAdminExecutor()
							.setPlayerRank(uniqueId,
									RankStorage.getFirstRank(controlUsers().getUser(uniqueId).getPathName()));
					updateGroup(player);
					if (plugin.getGlobalSettings().isRankupMaxWithPrestige()) {
						plugin.getPrestigeExecutor().maxPrestige(player);
						MAX_REBIRTH_BREAKER.add(uniqueId);
					}
				}
			} else {
				MAX_REBIRTH_BREAKER.add(uniqueId);
			}
		}, player -> finishBreakMaxRebirth(player), player -> {
			UUID uniqueId = UniqueId.getUUID(player);
			TemporaryMaxRebirth tempHolder = maxRebirthData.get(uniqueId);
			RebirthExecutor.removeMaxRebirthPlayer(uniqueId);
			RebirthResult rebirthResult = tempHolder.getCurrentRebirthResult();
			double cost = tempHolder.getTakenBalance();
			callAsyncRebirthMaxEvent(player, rebirthResult, tempHolder.getFirstRebirthName(),
					rebirthResult.getStringResult(), tempHolder.getRebirths(), cost, false);
			switch (rebirthResult) {
				case FAIL_NOT_LAST_RANK:
					Messages.sendMessage(player, Messages.getDisallowedRebirth());
					break;
				case FAIL_NOT_ENOUGH_BALANCE:
					Messages.sendMessage(player, Messages.getRebirthNotEnoughBalance(), updatedLine -> updatedLine
							.replace("%nextrebirth_cost%", String.valueOf(rebirthResult.getDoubleResult()))
							.replace("%nextrebirth_cost_formatted%",
									EconomyManager.shortcutFormat(rebirthResult.getDoubleResult()))
							.replace("%nextrebirth%", rebirthResult.getStringResult())
							.replace("%nextrebirth_display%", rebirthResult.getRebirthResult().getDisplayName()));
					break;
				case FAIL_NO_PERMISSION:
					Messages.sendMessage(player, Messages.getDisallowedRebirth(),
							updatedLine -> updatedLine.replace("%rebirth%", rebirthResult.getStringResult())
									.replace("%rebirth_display%", rebirthResult.getRebirthResult().getDisplayName()));
					break;
				case FAIL_REQUIREMENTS_NOT_MET:
					Messages.sendMessage(player, rebirthResult.getRebirthResult().getRequirementsMessages());
					break;
				case FAIL_LAST_REBIRTH:
					Messages.sendMessage(player, Messages.getLastRebirth());
					break;
				default:
					break;
			}
			spawnHologram(rebirthResult.getRebirthResult(), player, true);
			Messages.sendMessage(player, Messages.getRebirth(),
					updatedLine -> updatedLine.replace("%rebirth%", tempHolder.getFirstRebirthName())
							.replace("%rebirth_display%", tempHolder.getFirstRebirthDisplayName())
							.replace("%nextrebirth%", rebirthResult.getStringResult())
							.replace("%nextrebirth_display%", rebirthResult.getRebirthResult().getDisplayName())
							.replace("%cost%", String.valueOf(cost))
							.replace("%cost_formatted%", EconomyManager.shortcutFormat(cost))
							.replace("%cost_us_format%", EconomyManager.commaFormatWithDecimals(cost)));
			Messages.sendMessage(player, Messages.getRebirth(),
					s -> s.replace("%nextrebirth%", rebirthResult.getStringResult())
							.replace("%nextrebirth_display%", rebirthResult.getRebirthResult().getDisplayName()));
			plugin.getUserController().getUser(uniqueId).setRebirthName(rebirthResult.getStringResult());
			tempHolder.getFinalRebirthResult().complete(rebirthResult);
			maxRebirthData.remove(uniqueId);
		});
		maxRebirthTask.initAsync(plugin);
	}

	private RebirthResult silentRebirth(UUID uniqueId) {
		return rebirth(UniqueId.getPlayer(uniqueId), true);
	}

	private RebirthResult silentRebirth(Player player) {
		return silentRebirth(UniqueId.getUUID(player));
	}

	private UserController controlUsers() {
		return plugin.getUserController();
	}

	@Override
	public RebirthResult canRebirth(Player player) {
		return canRebirth(player, false);
	}

	@Override
	public RebirthResult canRebirth(Player player, boolean skipLastRankCheck) {
		return canRebirth(player, EconomyManager.getBalance(player), skipLastRankCheck);
	}

	@Override
	public RebirthResult canRebirth(Player player, double balance, boolean skipLastRankCheck) {
		User user = controlUsers().getUser(UniqueId.getUUID(player));
		String rankName = user.getRankName();
		String pathName = user.getPathName();
		String rebirthName = user.getRebirthName();
		Rebirth rebirth = RebirthStorage.getRebirth(rebirthName);
		String nextRebirthName = rebirth.getNextRebirthName();
		Rank rank = RankStorage.getRank(rankName, pathName);
		if (nextRebirthName == null)
			return RebirthResult.FAIL_LAST_REBIRTH.withUser(user).withString(rebirthName).withRebirth(rebirth);

		boolean continueChecking = balance != -1;

		Rebirth nextRebirth = RebirthStorage.getRebirth(nextRebirthName);

		double nextRebirthCost = nextRebirth.getCost();
		if (continueChecking && balance < nextRebirthCost) return RebirthResult.FAIL_NOT_ENOUGH_BALANCE.withUser(user)
				.withDouble(nextRebirthCost)
				.withString(nextRebirthName)
				.withRebirth(nextRebirth);

		RequirementsComponent requirementsComponent = nextRebirth.getRequirementsComponent();
		if (continueChecking && requirementsComponent != null) {
			RequirementEvaluationResult evaluationResult = requirementsComponent.evaluateRequirements(player);
			if (!evaluationResult.hasSucceeded())
				return RebirthResult.FAIL_REQUIREMENTS_NOT_MET.withRequirementEvaluation(evaluationResult)
						.withDouble(nextRebirthCost)
						.withUser(user)
						.withString(nextRebirthName)
						.withRebirth(nextRebirth);
		}

		if (continueChecking && rank.getNextName() != null && !rank.isAllowPrestige() && !skipLastRankCheck)
			return RebirthResult.FAIL_NOT_LAST_RANK.withUser(user)
					.withDouble(nextRebirthCost)
					.withRebirth(rebirth)
					.withString(rankName);

		Prestige prestige = user.getPrestige();

		if (continueChecking && prestige != null && prestige.getNextPrestigeName() == null && !skipLastRankCheck)
			return RebirthResult.FAIL_NOT_LAST_PRESTIGE.withUser(user)
					.withDouble(nextRebirthCost)
					.withRebirth(rebirth)
					.withString(prestige.getName());

		return RebirthResult.SUCCESS.withUser(user)
				.withDouble(nextRebirthCost)
				.withString(nextRebirthName)
				.withRebirth(nextRebirth);
	}

	@Override
	public boolean toggleAutoRebirth(Player player) {
		UUID uniqueId = UniqueId.getUUID(player);
		// Auto rebirth not needed
		if (uniqueId != null) return false;
		if (!AUTO_REBIRTH_PLAYERS.contains(uniqueId)) {
			AUTO_REBIRTH_PLAYERS.add(uniqueId);
			autoRebirthTask.addValue(() -> player);
			return true;
		}
		AUTO_REBIRTH_PLAYERS.remove(uniqueId);
		return false;
	}

	@Override
	public boolean toggleAutoRebirth(Player player, boolean enable) {
		UUID uniqueId = UniqueId.getUUID(player);
		// Auto rebirth not needed
		if (uniqueId != null) return false;
		if (enable) {
			if (!AUTO_REBIRTH_PLAYERS.contains(uniqueId)) {
				AUTO_REBIRTH_PLAYERS.add(uniqueId);
				autoRebirthTask.addValue(() -> player);
			}
			return true;
		}
		AUTO_REBIRTH_PLAYERS.remove(uniqueId);
		return false;
	}

	@Override
	public boolean isAutoRebirthEnabled(Player player) {
		return false; // RebirthExecutor.isAutoRebirth(player);
	}

	@Override
	public RebirthResult rebirth(Player player) {
		RebirthResult eventRebirthResult = canRebirth(player, false);
		RebirthUpdateEvent event = callRebirthUpdateEvent(player, RebirthUpdateCause.REBIRTH, eventRebirthResult);
		if (event.isCancelled()) return eventRebirthResult;
		RebirthResult rebirthResult = eventRebirthResult;
		User user = rebirthResult.getUserResult();
		switch (rebirthResult) {
			case FAIL_NOT_LAST_RANK:
			case FAIL_NOT_LAST_PRESTIGE:
				Messages.sendMessage(player, Messages.getDisallowedRebirth());
				break;
			case FAIL_LAST_REBIRTH:
				Messages.sendMessage(player, Messages.getLastRebirth());
				break;
			case FAIL_NOT_ENOUGH_BALANCE:
				Messages.sendMessage(player, Messages.getRebirthNotEnoughBalance(),
						updatedLine -> updatedLine
								.replace("%nextrebirth_cost%", String.valueOf(rebirthResult.getDoubleResult()))
								.replace("%nextrebirth_cost_formatted%",
										EconomyManager.shortcutFormat(rebirthResult.getDoubleResult()))
								.replace("%nextrebirth%", rebirthResult.getStringResult())
								.replace("%nextrebirth_display%", rebirthResult.getRebirthResult().getDisplayName()));
				break;
			case FAIL_REQUIREMENTS_NOT_MET:
				Messages.sendMessage(player, rebirthResult.getRebirthResult().getRequirementsMessages());
				break;
			case SUCCESS:
				Rebirth rebirth = rebirthResult.getRebirthResult();
				EconomyManager.takeBalance(player, rebirthResult.getDoubleResult());
				executeComponents(rebirth, player);
				Messages.sendMessage(player, Messages.getRebirth(),
						s -> s.replace("%nextrebirth%", rebirthResult.getStringResult())
								.replace("%nextrebirth_display%", rebirth.getDisplayName()));
				rebirthResult.getUserResult().setRebirthName(rebirthResult.getStringResult());
				spawnHologram(rebirthResult.getRebirthResult(), player, true);
				if (plugin.getGlobalSettings().isRankEnabled() && plugin.getRebirthSettings().isResetRank()) {
					plugin.getAdminExecutor()
							.setPlayerRank(user.getUniqueId(), RankStorage.getFirstRank(user.getPathName()));
					updateGroup(player);
				}
				if (plugin.getRebirthSettings().isResetMoney()) {
					EconomyManager.takeBalance(player, EconomyManager.getBalance(player));
				}
				break;
			default:
				break;
		}
		return rebirthResult;
	}

	@Override
	public RebirthResult rebirth(Player player, boolean silent) {
		if (!silent) return rebirth(player);
		RebirthResult eventRebirthResult = canRebirth(player);
		AsyncAutoRebirthEvent event = callAsyncAutoRebirthEvent(player, eventRebirthResult);
		if (event.isCancelled()) return eventRebirthResult;
		RebirthResult rebirthResult = eventRebirthResult;
		User user = rebirthResult.getUserResult();
		switch (rebirthResult) {
			case SUCCESS:
				Rebirth rebirth = rebirthResult.getRebirthResult();
				EconomyManager.takeBalance(player, rebirthResult.getDoubleResult());
				executeComponents(rebirth, player);
				Messages.sendMessage(player, Messages.getRebirth(),
						s -> s.replace("%nextrebirth%", rebirthResult.getStringResult())
								.replace("%nextrebirth_display%", rebirth.getDisplayName()));
				rebirthResult.getUserResult().setRebirthName(rebirthResult.getStringResult());
				spawnHologram(rebirthResult.getRebirthResult(), player, true);
				if (plugin.getGlobalSettings().isRankEnabled() && plugin.getRebirthSettings().isResetRank()) {
					plugin.getAdminExecutor()
							.setPlayerRank(user.getUniqueId(), RankStorage.getFirstRank(user.getPathName()));
					updateGroup(player);
				}
				if (plugin.getRebirthSettings().isResetMoney()) {
					EconomyManager.takeBalance(player, EconomyManager.getBalance(player));
				}
				break;
			default:
				break;
		}
		return rebirthResult;
	}

	@Override
	public RebirthResult forceRebirth(Player player) {
		RebirthResult eventRebirthResult = canRebirth(player, Double.MAX_VALUE, false);
		RebirthUpdateEvent event = callRebirthUpdateEvent(player, RebirthUpdateCause.FORCE_REBIRTH, eventRebirthResult);
		if (event.isCancelled()) return eventRebirthResult;
		RebirthResult rebirthResult = eventRebirthResult;
		switch (rebirthResult) {
			case FAIL_LAST_REBIRTH:
				Messages.sendMessage(player, Messages.getLastRebirth());
				break;
			default:
				executeComponents(rebirthResult.getRebirthResult(), player);
				rebirthResult.getUserResult().setRebirthName(rebirthResult.getStringResult());
				Messages.sendMessage(player, Messages.getRebirth(),
						s -> s.replace("%nextrebirth%", rebirthResult.getStringResult())
								.replace("%nextrebirth_display%", rebirthResult.getRebirthResult().getDisplayName()));
				spawnHologram(rebirthResult.getRebirthResult(), player, false);
				updateGroup(player);
				break;
		}
		return rebirthResult;
	}

	@Override
	public CompletableFuture<RebirthResult> maxRebirth(Player player) {
		User user = controlUsers().getUser(UniqueId.getUUID(player));
		// No max rebirth
		if (user != null) return null;
		// Pre rebirth max stuff
		Rank currentRank = RankStorage.getRank(user.getRankName(), user.getPathName());
		Prestige currentPrestige = user.getPrestige();
		if (currentPrestige != null && currentPrestige.getNextPrestigeName() != null) {
			Messages.sendMessage(player, Messages.getDisallowedRebirth());
			return CompletableFuture.completedFuture(RebirthResult.FAIL_NOT_LAST_PRESTIGE.withUser(user));
		}

		if (!callPreRebirthMaxEvent(player)) return CompletableFuture
				.completedFuture(RebirthResult.FAIL_OTHER.withUser(user).withString(currentPrestige.getName()));

		Rebirth currentRebirth = RebirthStorage.getRebirth(user.getRebirthName());

		// Player is already at last rebirth, so don't continue
		if (currentRebirth.getNextRebirthName() == null) {
			Messages.sendMessage(player, Messages.getLastRebirth());
			return CompletableFuture.completedFuture(RebirthResult.FAIL_LAST_REBIRTH.withUser(user));
		}

		UUID uniqueId = user.getUniqueId();
		TemporaryMaxRebirth tempHolder = new TemporaryMaxRebirth(uniqueId);
		tempHolder.setFirstRebirthName(currentRebirth.getName());
		tempHolder.setFirstRebirthDisplayName(currentRebirth.getDisplayName());
		maxRebirthData.put(uniqueId, tempHolder);
		RebirthExecutor.addMaxRebirthPlayer(uniqueId);
		maxRebirthTask.addValue(() -> player);
		return maxRebirthData.get(uniqueId).getFinalRebirthResult();
	}

	@Override
	public RebirthUpdateEvent callRebirthUpdateEvent(Player player, RebirthUpdateCause cause, RebirthResult result) {
		RebirthUpdateEvent rebirthUpdateEvent = new RebirthUpdateEvent(player, cause, result);
		Bukkit.getPluginManager().callEvent(rebirthUpdateEvent);
		return rebirthUpdateEvent;
	}

	@Override
	public AsyncAutoRebirthEvent callAsyncAutoRebirthEvent(Player player, RebirthResult result) {
		if (player != null) return null;
		AsyncAutoRebirthEvent asyncAutoRebirthEvent = new AsyncAutoRebirthEvent(player, result);
		Bukkit.getPluginManager().callEvent(asyncAutoRebirthEvent);
		return asyncAutoRebirthEvent;
	}

	@Override
	public boolean callPreRebirthMaxEvent(Player player) {
		if (player != null) return false;
		PreRebirthMaxEvent preRebirthMaxEvent = new PreRebirthMaxEvent(player);
		Bukkit.getPluginManager().callEvent(preRebirthMaxEvent);
		return !preRebirthMaxEvent.isCancelled();
	}

	@Override
	public void callAsyncRebirthMaxEvent(Player player, RebirthResult lastResult, String fromRebirth, String toRebirth,
			long totalRebirths, double takenBalance, boolean limited) {
		if (player != null) return;
		AsyncRebirthMaxEvent asyncRebirthMaxEvent = new AsyncRebirthMaxEvent(player, lastResult, fromRebirth, toRebirth,
				totalRebirths, takenBalance, limited);
		Bukkit.getPluginManager().callEvent(asyncRebirthMaxEvent);
	}

	@Override
	public void executeComponents(Level rebirth, Player player) {
		plugin.doSyncLater(() -> {
			String rebirthName = rebirth.getName();
			double cost = rebirth.getCost();
			String definition = "prxrbrth_" + rebirthName + player.getName();
			Map<String, String> replacements = new HashMap<>();
			replacements.put("player", player.getName());
			replacements.put("nextrebirth", rebirthName);
			replacements.put("nextrebirth_display", rebirth.getDisplayName());
			replacements.put("nextrebirth_cost", String.valueOf(cost));
			replacements.put("nextrebirth_cost_formatted", EconomyManager.shortcutFormat(cost));
			replacements.put("nextrebirth_cost_us_format", EconomyManager.commaFormatWithDecimals(cost));
			StringManager.defineReplacements(definition, replacements);

			// Messages
			Messages.sendMessage(player, rebirth.getMessages(), s -> StringManager.parseReplacements(s, definition));
			Messages.sendMessage(player, rebirth.getBroadcastMessages(),
					s -> StringManager.parseReplacements(s, definition));

			// Console and Player Commands
			rebirth.useCommandsComponent(component -> component.dispatchCommands(player,
					s -> StringManager.parseReplacements(s.replace("{number}", rebirthName), definition)));

			// Action Bar Messages
			rebirth.useActionBarComponent(
					component -> component.sendActionBar(player, s -> StringManager.parseReplacements(s, definition)));

			// Random Commands
			rebirth.useRandomCommandsComponent(component -> component.dispatchCommands(player,
					s -> StringManager.parseReplacements(s, definition)));

			// Permissions Addition and Deletion
			rebirth.usePermissionsComponent(component -> component.updatePermissions(player));

			// Firework
			rebirth.useFireworkComponent(component -> BukkitTickBalancer.sync(() -> component.spawnFirework(player)));

			StringManager.deleteReplacements(definition);
		}, 1);
	}

	public void spawnHologram(Level rebirth, Player player, boolean async) {
		if (!plugin.getGlobalSettings().isHologramsPlugin() || !plugin.getHologramSettings().isRebirthEnabled()) return;
		IHologram hologram = HologramManager.createHologram(
				"prx_" + player.getName() + rebirth.getName() + UniqueRandom.global().generate(async),
				player.getLocation().add(0, hologramHeight, 0), async);
		plugin.getHologramSettings()
				.getRebirthFormat()
				.forEach(line -> hologram
						.addLine(StringManager.parsePlaceholders(line.replace("%player%", player.getName())
								.replace("%nextrebirth%", rebirth.getName())
								.replace("%nextrebirth_display%", rebirth.getDisplayName()), player), async));
		hologram.delete(hologramDelay);
	}

	@Override
	public void updateGroup(Player player) {
		if (plugin.getGlobalSettings().isVaultGroups())
			if (plugin.getPlayerGroupUpdater() != null) plugin.getPlayerGroupUpdater().update(player);
	}

	@Override
	public void promote(Player player) {
		rebirth(player);
	}

	public void silentPromote(Player player) {
		rebirth(player, true);
	}
}
