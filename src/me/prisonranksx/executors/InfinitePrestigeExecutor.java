package me.prisonranksx.executors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.HeavyBukkit;
import me.prisonranksx.components.RequirementsComponent;
import me.prisonranksx.components.RequirementsComponent.RequirementEvaluationResult;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.data.PrestigeStorage.InfinitePrestigeStorage;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.UserController;
import me.prisonranksx.events.PrestigeUpdateCause;
import me.prisonranksx.events.PrestigeUpdateEvent;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.UniversalPrestige;
import me.prisonranksx.holders.User;
import me.prisonranksx.hooks.IHologram;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.HologramManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.UniqueRandom;

public class InfinitePrestigeExecutor implements PrestigeExecutor {

	private PrisonRanksX plugin;
	private BukkitTask autoPrestigeTask;
	private double hologramHeight;
	private int hologramDelay;
	private InfinitePrestigeStorage storage;

	public InfinitePrestigeExecutor(PrisonRanksX plugin) {
		this.plugin = plugin;
		int speed = plugin.getGlobalSettings().getAutoPrestigeDelay();
		if (plugin.getGlobalSettings().isHologramsPlugin()) {
			hologramHeight = plugin.getHologramSettings().getPrestigeHeight();
			hologramDelay = plugin.getHologramSettings().getPrestigeRemoveDelay();
		}
		autoPrestigeTask = plugin.doAsyncRepeating(() -> AUTO_PRESTIGE_PLAYERS.forEach(this::silentPrestige), 1, speed);
		storage = (InfinitePrestigeStorage) PrestigeStorage.getHandler().getStorage();
	}

	private PrestigeResult silentPrestige(UUID uniqueId) {
		return prestige(UniqueId.getPlayer(uniqueId), true);
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
		String rankName = user.getPrestigeName();
		String pathName = user.getPathName();
		String prestigeName = user.getPrestigeName();
		Prestige prestige = PrestigeStorage.getPrestige(prestigeName);
		String nextPrestigeName = prestige.getNextPrestigeName();
		Rank rank = RankStorage.getRank(rankName, pathName);
		if (nextPrestigeName == null) return PrestigeResult.FAIL_LAST_PRESTIGE.withUser(user).withPrestige(prestige);

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
						.withUser(user)
						.withPrestige(nextPrestige);
		}

		if (rank.getNextRankName() != null && !rank.isAllowPrestige() && !skipLastRankCheck)
			return PrestigeResult.FAIL_NOT_LAST_RANK.withUser(user).withPrestige(prestige).withString(rank.getName());

		return PrestigeResult.SUCCESS.withUser(user)
				.withDouble(nextPrestigeCost)
				.withString(nextPrestigeName)
				.withPrestige(nextPrestige);
	}

	@Override
	public boolean toggleAutoPrestige(Player player) {
		return PrestigeExecutor.switchAutoPrestige(player);
	}

	@Override
	public boolean toggleAutoPrestige(Player player, boolean enable) {
		return PrestigeExecutor.switchAutoPrestige(player, enable);
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
				UniversalPrestige prestige = (UniversalPrestige) prestigeResult.getPrestigeResult();
				EconomyManager.takeBalance(player, prestigeResult.getDoubleResult());
				executeComponents(prestige, player);
				Messages.sendMessage(player, Messages.getPrestige(),
						s -> s.replace("%nextprestige%", prestigeResult.getStringResult())
								.replace("%nextprestige_display%", prestige.getDisplayName()));
				prestigeResult.getUserResult().setPrestigeName(prestigeResult.getStringResult());
				// max prestige commands
				storage.useCommandsComponent(component -> component.dispatchCommands(player,
						s -> s.replace("%amount%", prestige.getName())));
				// continuous prestiges settings
				storage.useContinuousComponents(prestige.getNumber(), ch -> {
					ch.useCommandsComponent(component -> component.dispatchCommands(player));
					Messages.sendMessage(player, ch.getBroadcastMessages());
					Messages.sendMessage(player, ch.getMessages());
				});
				spawnHologram(prestigeResult.getPrestigeResult(), player, true);
				if (plugin.getGlobalSettings().isRankEnabled() && plugin.getPrestigeSettings().isResetRank()) {
					plugin.getAdminExecutor()
							.setPlayerRank(user.getUniqueId(), RankStorage.getFirstRank(user.getPathName()));
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
	public PrestigeResult maxPrestige(Player player) {
		// TODO Auto-generated method stub
		return null;
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
		return false;
	}

	@Override
	public boolean callPrePrestigeMaxEvent(Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void callAsyncPrestigeMaxEvent(Player player, PrestigeResult lastResult, String fromPrestige,
			String toPrestige, int totalPrestiges, double takenBalance) {
		// TODO Auto-generated method stub

	}

	public void executeComponents(UniversalPrestige prestige, Player player) {
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
		prestige.useCommandsComponent(component -> component.dispatchCommands(player,
				s -> StringManager.parseReplacements(s.replace("{number}", prestigeName), definition)));

		// Action Bar Messages
		prestige.useActionBarComponent(
				component -> component.sendActionBar(player, s -> StringManager.parseReplacements(s, definition)));

		// Random Commands
		prestige.useRandomCommandsComponent(
				component -> component.dispatchCommands(player, s -> StringManager.parseReplacements(s, definition)));

		// Permissions Addition and Deletion
		prestige.usePermissionsComponent(component -> component.updatePermissions(player));

		// Firework
		prestige.useFireworkComponent(component -> HeavyBukkit.run(() -> component.spawnFirework(player)));

		StringManager.deleteReplacements(definition);
	}

	private void spawnHologram(Prestige prestige, Player player, boolean async) {
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

	public BukkitTask getAutoPrestigeTask() {
		return autoPrestigeTask;
	}

}
