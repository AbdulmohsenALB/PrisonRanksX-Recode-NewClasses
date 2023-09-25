package me.prisonranksx.executors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.segmentedtasks.ConcurrentTask;
import me.prisonranksx.bukkitutils.segmentedtasks.DistributedTask;
import me.prisonranksx.bukkitutils.segmentedtasks.SegmentedTasks;
import me.prisonranksx.components.RequirementsComponent;
import me.prisonranksx.components.RequirementsComponent.RequirementEvaluationResult;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.UserController;
import me.prisonranksx.events.PrestigeUpdateCause;
import me.prisonranksx.events.PrestigeUpdateEvent;
import me.prisonranksx.holders.*;
import me.prisonranksx.hooks.IHologram;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.HologramManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.UniqueRandom;

public class InfinitePrestigeExecutor implements PrestigeExecutor {

	private PrisonRanksX plugin;

	private double hologramHeight;

	private int hologramDelay;

	private Map<UUID, TemporaryMaxPrestige> maxPrestigeData = new ConcurrentHashMap<>();

	private ConcurrentTask<Player> maxPrestigeTask;

	private DistributedTask<Player> autoPrestigeTask;

	public InfinitePrestigeExecutor(PrisonRanksX plugin) {
		this.plugin = plugin;
		if (plugin.getGlobalSettings().isHologramsPlugin()) {
			hologramHeight = plugin.getHologramSettings().getPrestigeHeight();
			hologramDelay = plugin.getHologramSettings().getPrestigeRemoveDelay();
		}
		setupMaxPrestige();
		setupAutoPrestige();
		// autoPrestigeTask = plugin.doAsyncRepeating(() ->
		// AUTO_PRESTIGE_PLAYERS.forEach(this::silentPrestige), 1, speed);
	}

	private void setupAutoPrestige() {
		int speed = plugin.getGlobalSettings().getAutoPrestigeDelay();
		autoPrestigeTask = SegmentedTasks.scheduleDistributedTask(this::silentPrestige, p -> !isAutoPrestigeEnabled(p),
				20);
		autoPrestigeTask.initAsync(plugin, speed, speed);
	}

	private void setupMaxPrestige() {
		// Sending messages continuously will slow down the process by a lot as stated
		// in config.yml
		boolean sendMsgContinuously = !plugin.getGlobalSettings().isPrestigeMaxPrestigeMsgLastPrestigeOnly();
		maxPrestigeTask = SegmentedTasks.scheduleConcurrentTask(player -> {
			UUID uniqueId = UniqueId.getUUID(player);
			PrestigeResult prestigeResult = canPrestige(player, false);
			if (prestigeResult.isSuccessful() && !MAX_PRESTIGE_BREAKER.contains(uniqueId)) {
				String prestigeName = prestigeResult.getStringResult();
				Prestige prestige = prestigeResult.getPrestigeResult();
				if (sendMsgContinuously)
					Messages.sendMessage(player, Messages.getPrestige(), s -> s.replace("%nextprestige%", prestigeName)
							.replace("%nextprestige_display%", prestige.getDisplayName()));
				executeComponents(prestige, player);
				EconomyManager.takeBalance(player, prestigeResult.getDoubleResult());
				TemporaryMaxPrestige tempHolder = maxPrestigeData.get(uniqueId);
				if (tempHolder == null) {
					tempHolder = new TemporaryMaxPrestige(uniqueId);
					tempHolder.setFirstPrestigeName(prestigeName)
							.setFirstPrestigeDisplayName(prestige.getDisplayName())
							.setCurrentPrestigeResult(prestigeResult);
					maxPrestigeData.put(uniqueId, tempHolder);
					tempHolder = maxPrestigeData.get(uniqueId);
				}
				tempHolder.setTakenBalance(tempHolder.getTakenBalance() + prestigeResult.getDoubleResult());
				tempHolder.setPrestiges(tempHolder.getPrestiges() + 1);
				tempHolder.setCurrentPrestigeResult(prestigeResult);
				prestigeResult.getUserResult().setPrestigeName(prestigeName);
				if (plugin.getGlobalSettings().isRankEnabled() && plugin.getPrestigeSettings().isResetRank()) {
					plugin.getAdminExecutor()
							.setPlayerRank(uniqueId,
									RankStorage.getFirstRank(controlUsers().getUser(uniqueId).getPathName()));
					updateGroup(player);
				}
			}
		}, player -> finishBreakMaxPrestige(player), player -> {
			UUID uniqueId = UniqueId.getUUID(player);
			TemporaryMaxPrestige tempHolder = maxPrestigeData.get(uniqueId);
			PrestigeExecutor.removeMaxPrestigePlayer(uniqueId);
			PrestigeResult prestigeResult = tempHolder.getCurrentPrestigeResult();
			double cost = tempHolder.getTakenBalance();
			callAsyncPrestigeMaxEvent(player, prestigeResult, tempHolder.getFirstPrestigeName(),
					prestigeResult.getStringResult(), tempHolder.getPrestiges(), cost);
			switch (prestigeResult) {
				case FAIL_NOT_LAST_RANK:
					Messages.sendMessage(player, Messages.getDisallowedPrestige());
					break;
				case FAIL_NOT_ENOUGH_BALANCE:
					Messages.sendMessage(player, Messages.getPrestigeNotEnoughBalance(), updatedLine -> updatedLine
							.replace("%nextprestige_cost%", String.valueOf(prestigeResult.getDoubleResult()))
							.replace("%nextprestige_cost_formatted%",
									EconomyManager.shortcutFormat(prestigeResult.getDoubleResult()))
							.replace("%nextprestige%", prestigeResult.getStringResult())
							.replace("%nextprestige_display%", prestigeResult.getPrestigeResult().getDisplayName()));
					break;
				case FAIL_NO_PERMISSION:
					Messages.sendMessage(player, Messages.getDisallowedPrestige(),
							updatedLine -> updatedLine.replace("%prestige%", prestigeResult.getStringResult())
									.replace("%prestige_display%",
											prestigeResult.getPrestigeResult().getDisplayName()));
					break;
				case FAIL_REQUIREMENTS_NOT_MET:
					Messages.sendMessage(player, prestigeResult.getPrestigeResult().getRequirementsMessages());
					break;
				case FAIL_LAST_PRESTIGE:
					Messages.sendMessage(player, Messages.getLastPrestige());
					break;
				default:
					break;
			}
			spawnHologram(prestigeResult.getPrestigeResult(), player, true);
			Messages.sendMessage(player, Messages.getPrestigeMax(),
					updatedLine -> updatedLine.replace("%prestige%", tempHolder.getFirstPrestigeName())
							.replace("%prestige_display%", tempHolder.getFirstPrestigeDisplayName())
							.replace("%nextprestige%", prestigeResult.getStringResult())
							.replace("%nextprestige_display%", prestigeResult.getPrestigeResult().getDisplayName())
							.replace("%cost%", String.valueOf(cost))
							.replace("%cost_formatted%", EconomyManager.shortcutFormat(cost))
							.replace("%cost_us_format%", EconomyManager.commaFormatWithDecimals(cost)));
			if (!sendMsgContinuously) Messages.sendMessage(player, Messages.getPrestige(),
					s -> s.replace("%nextprestige%", prestigeResult.getStringResult())
							.replace("%nextprestige_display%", prestigeResult.getPrestigeResult().getDisplayName()));
			plugin.getUserController().getUser(uniqueId).setPrestigeName(prestigeResult.getStringResult());
			tempHolder.getFinalPrestigeResult().complete(prestigeResult);
			maxPrestigeData.remove(uniqueId);
		});
		maxPrestigeTask.initAsync(plugin);
	}

	private PrestigeResult silentPrestige(UUID uniqueId) {
		return prestige(UniqueId.getPlayer(uniqueId), true);
	}

	private PrestigeResult silentPrestige(Player player) {
		return silentPrestige(UniqueId.getUUID(player));
	}

	private UserController controlUsers() {
		return plugin.getUserController();
	}

	@Override
	public PrestigeResult canPrestige(Player player) {
		return canPrestige(player, false);
	}

	@Override
	public PrestigeResult canPrestige(Player player, boolean skipLastRankCheck) {
		return canPrestige(player, EconomyManager.getBalance(player), skipLastRankCheck);
	}

	@Override
	public PrestigeResult canPrestige(Player player, double balance, boolean skipLastRankCheck) {
		User user = controlUsers().getUser(UniqueId.getUUID(player));
		String rankName = user.getRankName();
		String pathName = user.getPathName();
		String prestigeName = user.getPrestigeName();
		Prestige prestige = PrestigeStorage.getPrestige(prestigeName);
		String nextPrestigeName = prestige.getNextPrestigeName();
		Rank rank = RankStorage.getRank(rankName, pathName);
		if (nextPrestigeName == null)
			return PrestigeResult.FAIL_LAST_PRESTIGE.withUser(user).withString(prestigeName).withPrestige(prestige);

		Prestige nextPrestige = PrestigeStorage.getPrestige(nextPrestigeName);

		double nextPrestigeCost = nextPrestige.getCost();
		if (balance < nextPrestigeCost) return PrestigeResult.FAIL_NOT_ENOUGH_BALANCE.withUser(user)
				.withDouble(nextPrestigeCost)
				.withString(nextPrestigeName)
				.withPrestige(nextPrestige);

		RequirementsComponent requirementsComponent = nextPrestige.getRequirementsComponent();
		if (requirementsComponent != null) {
			RequirementEvaluationResult evaluationResult = requirementsComponent.evaluateRequirements(player);
			if (!evaluationResult.hasSucceeded())
				return PrestigeResult.FAIL_REQUIREMENTS_NOT_MET.withRequirementEvaluation(evaluationResult)
						.withDouble(nextPrestigeCost)
						.withUser(user)
						.withString(nextPrestigeName)
						.withPrestige(nextPrestige);
		}

		if (rank.getNextName() != null && !rank.isAllowPrestige() && !skipLastRankCheck)
			return PrestigeResult.FAIL_NOT_LAST_RANK.withUser(user)
					.withDouble(nextPrestigeCost)
					.withPrestige(prestige)
					.withString(rank.getName());

		return PrestigeResult.SUCCESS.withUser(user)
				.withDouble(nextPrestigeCost)
				.withString(nextPrestigeName)
				.withPrestige(nextPrestige);
	}

	@Override
	public boolean toggleAutoPrestige(Player player) {
		return false;
	}

	@Override
	public boolean toggleAutoPrestige(Player player, boolean enable) {
		return enable;
	}

	@Override
	public boolean isAutoPrestigeEnabled(Player player) {
		return PrestigeExecutor.isAutoPrestige(player);
	}

	@Override
	public PrestigeResult prestige(Player player) {
		PrestigeResult prestigeResult = canPrestige(player);
		User user = prestigeResult.getUserResult();
		if (!callPrestigeUpdateEvent(player, PrestigeUpdateCause.PRESTIGE, prestigeResult,
				prestigeResult.getStringResult(), prestigeResult.isSuccessful()))
			return prestigeResult;
		switch (prestigeResult) {
			case FAIL_NOT_LAST_RANK:
				Messages.sendMessage(player, Messages.getDisallowedPrestige());
				break;
			case FAIL_LAST_PRESTIGE:
				Messages.sendMessage(player, Messages.getLastPrestige());
				break;
			case FAIL_NOT_ENOUGH_BALANCE:
				Messages.sendMessage(player, Messages.getPrestigeNotEnoughBalance(), updatedLine -> updatedLine
						.replace("%nextprestige_cost%", String.valueOf(prestigeResult.getDoubleResult()))
						.replace("%nextprestige_cost_formatted%",
								EconomyManager.shortcutFormat(prestigeResult.getDoubleResult()))
						.replace("%nextprestige%", prestigeResult.getStringResult())
						.replace("%nextprestige_display%", prestigeResult.getPrestigeResult().getDisplayName()));
				break;
			case FAIL_REQUIREMENTS_NOT_MET:
				Messages.sendMessage(player, prestigeResult.getPrestigeResult().getRequirementsMessages());
				break;
			case SUCCESS:
				Prestige prestige = prestigeResult.getPrestigeResult();
				EconomyManager.takeBalance(player, prestigeResult.getDoubleResult());
				executeComponents(prestige, player);
				Messages.sendMessage(player, Messages.getPrestige(),
						s -> s.replace("%nextprestige%", prestigeResult.getStringResult())
								.replace("%nextprestige_display%", prestige.getDisplayName()));
				prestigeResult.getUserResult().setPrestigeName(prestigeResult.getStringResult());
				spawnHologram(prestigeResult.getPrestigeResult(), player, true);
				if (plugin.getGlobalSettings().isRankEnabled() && plugin.getPrestigeSettings().isResetRank()) {
					plugin.getAdminExecutor()
							.setPlayerRank(user.getUniqueId(), RankStorage.getFirstRank(user.getPathName()));
					updateGroup(player);
				}
				if (plugin.getPrestigeSettings().isResetMoney()) {
					EconomyManager.takeBalance(player, EconomyManager.getBalance(player));
				}
				break;
			default:
				break;
		}
		return prestigeResult;
	}

	@Override
	public PrestigeResult prestige(Player player, boolean silent) {
		if (!silent) return prestige(player);
		return null;
	}

	@Override
	public PrestigeResult forcePrestige(Player player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<PrestigeResult> maxPrestige(Player player) {
		User user = controlUsers().getUser(UniqueId.getUUID(player));

		// Pre prestige max stuff
		Rank currentRank = RankStorage.getRank(user.getRankName(), user.getPathName());
		if (currentRank.getNextName() != null && !currentRank.isAllowPrestige()) {
			Messages.sendMessage(player, Messages.getDisallowedPrestige());
			return CompletableFuture.completedFuture(PrestigeResult.FAIL_NOT_LAST_RANK.withUser(user));
		}

		if (!callPrePrestigeMaxEvent(player)) return CompletableFuture
				.completedFuture(PrestigeResult.FAIL_OTHER.withUser(user).withString(currentRank.getName()));

		Prestige currentPrestige = PrestigeStorage.getPrestige(user.getPrestigeName());

		// Player is already at last prestige, so don't continue
		if (currentPrestige.getNextPrestigeName() == null) {
			Messages.sendMessage(player, Messages.getLastPrestige());
			return CompletableFuture.completedFuture(PrestigeResult.FAIL_LAST_PRESTIGE.withUser(user));
		}

		UUID uniqueId = user.getUniqueId();
		TemporaryMaxPrestige tempHolder = new TemporaryMaxPrestige(uniqueId);
		tempHolder.setFirstPrestigeName(currentPrestige.getName());
		tempHolder.setFirstPrestigeDisplayName(currentPrestige.getDisplayName());
		maxPrestigeData.put(uniqueId, tempHolder);
		PrestigeExecutor.addMaxPrestigePlayer(uniqueId);
		maxPrestigeTask.addValue(() -> player);
		return maxPrestigeData.get(uniqueId).getFinalPrestigeResult();
	}

	@Override
	public boolean callPrestigeUpdateEvent(Player player, PrestigeUpdateCause cause, PrestigeResult result,
			String updatedPrestige, boolean successful) {
		PrestigeUpdateEvent prestigeUpdateEvent = new PrestigeUpdateEvent(player, cause, result, updatedPrestige);
		Bukkit.getPluginManager().callEvent(prestigeUpdateEvent);
		return !prestigeUpdateEvent.isCancelled();
	}

	@Override
	public boolean callAsyncAutoPrestigeEvent(Player player, PrestigeUpdateCause cause, PrestigeResult result,
			String updatedPrestige, String currentPrestige, boolean successful) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean callPrePrestigeMaxEvent(Player player) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void callAsyncPrestigeMaxEvent(Player player, PrestigeResult lastResult, String fromPrestige,
			String toPrestige, long totalPrestiges, double takenBalance) {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeComponents(Level prestige, Player player) {
		String prestigeName = prestige.getName();
		double cost = prestige.getCost();
		String definition = "prxprstg_" + prestigeName + player.getName();
		Map<String, String> replacements = new HashMap<>();
		replacements.put("player", player.getName());
		replacements.put("nextprestige", prestigeName);
		replacements.put("nextprestige_display", prestige.getDisplayName());
		replacements.put("nextprestige_cost", String.valueOf(cost));
		replacements.put("nextprestige_cost_formatted", EconomyManager.shortcutFormat(cost));
		replacements.put("nextprestige_cost_us_format", EconomyManager.commaFormatWithDecimals(cost));
		StringManager.defineReplacements(definition, replacements);

		// Messages
		Messages.sendMessage(player, prestige.getMessages(), s -> StringManager.parseReplacements(s, definition));
		Messages.sendMessage(player, prestige.getBroadcastMessages(),
				s -> StringManager.parseReplacements(s, definition));

		// Console and Player Commands
		prestige.useCommandsComponent(component -> SegmentedTasks.sync(() -> component.dispatchCommands(player,
				s -> StringManager.parseReplacements(s.replace("{number}", prestigeName), definition))));

		// Action Bar Messages
		prestige.useActionBarComponent(
				component -> component.sendActionBar(player, s -> StringManager.parseReplacements(s, definition)));

		// Random Commands
		prestige.useRandomCommandsComponent(component -> SegmentedTasks
				.sync(() -> component.dispatchCommands(player, s -> StringManager.parseReplacements(s, definition))));

		// Permissions Addition and Deletion
		prestige.usePermissionsComponent(component -> component.updatePermissions(player));

		// Firework
		prestige.useFireworkComponent(component -> SegmentedTasks.sync(() -> component.spawnFirework(player)));

		// Max Prestige Commands
		PrestigeStorage.useCommandsComponent(component -> SegmentedTasks
				.sync(() -> component.dispatchCommands(player, s -> s.replace("%amount%", prestige.getName()))));

		// Continuous Prestiges Settings
		PrestigeStorage.useContinuousComponents(prestige.getNumber(), ch -> {
			ch.useCommandsComponent(component -> SegmentedTasks.sync(() -> component.dispatchCommands(player)));
			Messages.sendMessage(player, ch.getBroadcastMessages());
			Messages.sendMessage(player, ch.getMessages());
		});

		StringManager.deleteReplacements(definition);
	}

	public void spawnHologram(Level prestige, Player player, boolean async) {
		if (!plugin.getGlobalSettings().isHologramsPlugin() || !plugin.getHologramSettings().isPrestigeEnabled())
			return;
		IHologram hologram = HologramManager.createHologram(
				"prx_" + player.getName() + prestige.getName() + UniqueRandom.global().generate(async),
				player.getLocation().add(0, hologramHeight, 0), async);
		plugin.getHologramSettings()
				.getRankupFormat()
				.forEach(line -> hologram
						.addLine(StringManager.parsePlaceholders(line.replace("%player%", player.getName())
								.replace("%nextprestige%", prestige.getName())
								.replace("%nextprestige_display%", prestige.getDisplayName()), player), async));
		hologram.delete(hologramDelay);
	}

	@Override
	public void updateGroup(Player player) {
		if (plugin.getGlobalSettings().isVaultGroups())
			if (plugin.getPlayerGroupUpdater() != null) plugin.getPlayerGroupUpdater().update(player);
	}

	@Override
	public void promote(Player player) {
		prestige(player);
	}

	public void silentPromote(Player player) {
		prestige(player, true);
	}
}
