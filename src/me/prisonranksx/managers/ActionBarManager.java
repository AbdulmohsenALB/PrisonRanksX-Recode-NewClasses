package me.prisonranksx.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Sets;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.bukkitutils.XMaterial;
import me.prisonranksx.common.StaticCache;
import me.prisonranksx.reflections.ActionBar;
import me.prisonranksx.reflections.UniqueId;

public class ActionBarManager extends StaticCache {

	private static final Map<UUID, Integer> ACTION_BAR_ANIMATION_HOLDER = new HashMap<>();
	private static final Map<UUID, BukkitTask> ACTION_BAR_TASK_HOLDER = new HashMap<>();
	private static final PrisonRanksX PLUGIN = (PrisonRanksX) UniqueId.getProvidingPlugin(ActionBarManager.class);
	private static final ActionBarProgress ACTION_BAR_PROGRESS = new ActionBarProgress(PLUGIN);

	public static void reload(PrisonRanksX plugin) {
		ACTION_BAR_PROGRESS.setup(plugin);
	}

	public ActionBarProgress getActionBarProgress() {
		return ACTION_BAR_PROGRESS;
	}

	public static boolean isBeingUsed(UUID uniqueId) {
		return ACTION_BAR_TASK_HOLDER.containsKey(uniqueId);
	}

	public static boolean isBeingUsed(Player player) {
		return isBeingUsed(UniqueId.getUUID(player));
	}

	public static void send(Player player, String actionBarMessage) {
		if (actionBarMessage == null) return;
		ActionBar.sendActionBar(player, actionBarMessage);
	}

	public static void sendAnimated(Player player, List<String> actionBarMessages, int interval) {
		if (actionBarMessages == null || actionBarMessages.isEmpty()) return;
		UUID uniqueId = player.getUniqueId();
		String name = player.getName();
		int linesCount = actionBarMessages.size();
		BukkitTask animationTask = ACTION_BAR_TASK_HOLDER.get(uniqueId);
		if (animationTask != null) animationTask.cancel();
		ACTION_BAR_TASK_HOLDER.put(uniqueId, null);
		ACTION_BAR_ANIMATION_HOLDER.put(uniqueId, 0);
		AtomicReference<BukkitTask> animationTaskHolder = new AtomicReference<>();
		animationTaskHolder.set(new BukkitRunnable() {
			@Override
			public void run() {
				if (ACTION_BAR_TASK_HOLDER.containsKey(uniqueId))
					ACTION_BAR_TASK_HOLDER.put(uniqueId, animationTaskHolder.get());
				boolean animationEnded = ACTION_BAR_ANIMATION_HOLDER.get(uniqueId) >= linesCount;
				if (animationEnded) {
					PLUGIN.doAsyncLater(() -> ACTION_BAR_TASK_HOLDER.remove(uniqueId), 20);
					cancel();
					return;
				}
				String currentLine = actionBarMessages.get(ACTION_BAR_ANIMATION_HOLDER.get(uniqueId));
				ActionBar.sendActionBar(player, currentLine.replace("%player%", name));
				if (!animationEnded)
					ACTION_BAR_ANIMATION_HOLDER.put(uniqueId, ACTION_BAR_ANIMATION_HOLDER.get(uniqueId) + 1);
			}
		}.runTaskTimerAsynchronously(PLUGIN, 0, interval));
	}

	public static void sendAnimated(Player player, List<String> actionBarMessages, int interval,
			Function<String, String> function) {
		if (actionBarMessages == null || actionBarMessages.isEmpty()) return;
		UUID uniqueId = player.getUniqueId();
		String name = player.getName();
		int linesCount = actionBarMessages.size();
		BukkitTask animationTask = ACTION_BAR_TASK_HOLDER.get(uniqueId);
		if (animationTask != null) animationTask.cancel();
		ACTION_BAR_TASK_HOLDER.put(uniqueId, null);
		ACTION_BAR_ANIMATION_HOLDER.put(uniqueId, 0);
		AtomicReference<BukkitTask> animationTaskHolder = new AtomicReference<>();
		animationTaskHolder.set(new BukkitRunnable() {
			@Override
			public void run() {
				if (ACTION_BAR_TASK_HOLDER.containsKey(uniqueId))
					ACTION_BAR_TASK_HOLDER.put(uniqueId, animationTaskHolder.get());
				boolean animationEnded = ACTION_BAR_ANIMATION_HOLDER.get(uniqueId) >= linesCount;
				if (animationEnded) {
					PLUGIN.doAsyncLater(() -> ACTION_BAR_TASK_HOLDER.remove(uniqueId), 20);
					cancel();
					return;
				}
				String currentLine = actionBarMessages.get(ACTION_BAR_ANIMATION_HOLDER.get(uniqueId));
				ActionBar.sendActionBar(player, function.apply(currentLine.replace("%player%", name)));
				if (!animationEnded)
					ACTION_BAR_ANIMATION_HOLDER.put(uniqueId, ACTION_BAR_ANIMATION_HOLDER.get(uniqueId) + 1);
			}
		}.runTaskTimerAsynchronously(PLUGIN, 0, interval));
	}

	public static class ActionBarProgress {

		private Set<UUID> players;
		private PrisonRanksX plugin;
		private boolean isTaskOn;
		private String actionBarMessage;
		private int actionBarUpdater;
		private BukkitTask scheduler;
		private boolean actionBarProgressOnlyPickaxe;
		private Set<Material> pickaxes = Sets.newHashSet(XMaterial.DIAMOND_PICKAXE.parseMaterial(),
				XMaterial.IRON_PICKAXE.parseMaterial(), XMaterial.STONE_PICKAXE.parseMaterial(),
				XMaterial.WOODEN_PICKAXE.parseMaterial(), XMaterial.GOLDEN_PICKAXE.parseMaterial());

		public ActionBarProgress(PrisonRanksX plugin) {
			setup(plugin);
		}

		public void setup(PrisonRanksX plugin) {
			this.plugin = plugin;
			this.players = Sets.newConcurrentHashSet();
			this.isTaskOn = false;
			this.actionBarMessage = plugin.getGlobalSettings().getActionBarProgressFormat();
			this.actionBarUpdater = plugin.getGlobalSettings().getActionBarProgressUpdater();
			this.actionBarProgressOnlyPickaxe = plugin.getGlobalSettings().isActionBarProgressOnlyPickaxe();
			if (XMaterial.supports(16)) pickaxes.add(XMaterial.NETHERITE_PICKAXE.parseMaterial());
		}

		public boolean isOnlyPickaxe() {
			return actionBarProgressOnlyPickaxe;
		}

		public void setOnlyPickaxe(boolean onlyPickaxe) {
			this.actionBarProgressOnlyPickaxe = onlyPickaxe;
		}

		public void setActionBarMessage(String actionBarMessage) {
			this.actionBarMessage = actionBarMessage;
		}

		public String getActionBarMessage() {
			return actionBarMessage;
		}

		public Set<UUID> getPlayers() {
			return players;
		}

		public void addPickaxe(Material material) {
			pickaxes.add(material);
		}

		public Set<Material> getPickaxes() {
			return pickaxes;
		}

		public boolean isEnabled(Player p) {
			return players.contains(p.getUniqueId());
		}

		public void enable(Player p) {
			players.add(p.getUniqueId());
			if (!isTaskOn) {
				isTaskOn = true;
				if (isOnlyPickaxe()) {
					startProgressTaskAdvanced();
					return;
				}
				startProgressTask();
			}
		}

		public void disable(Player p) {
			players.remove(p.getUniqueId());
			if (players.size() == 0 && isTaskOn) {
				isTaskOn = false;
				scheduler.cancel();
			}
		}

		public void clear() {
			players.clear();
		}

		public void clear(boolean completely) {
			if (players != null) players.clear();
			if (completely) {
				if (isTaskOn) {
					isTaskOn = false;
					scheduler.cancel();
				}
			}
		}

		@SuppressWarnings("deprecation")
		public boolean isHoldingPickaxe(Player player) {
			return player == null || !player.isOnline() ? false : pickaxes.contains(player.getItemInHand().getType());
		}

		private void startProgressTask() {
			scheduler = plugin.doAsyncRepeating(() -> {
				players.forEach(uniqueId -> {
					if (isBeingUsed(uniqueId)) return;

					Player p = Bukkit.getPlayer(uniqueId);
					if (p == null || !p.isOnline()) return;

					send(p, StringManager.parsePlaceholders(actionBarMessage, p));
				});
			}, actionBarUpdater, actionBarUpdater);
		}

		private void startProgressTaskAdvanced() {
			scheduler = plugin.doAsyncRepeating(() -> {
				players.forEach(uniqueId -> {
					if (isBeingUsed(uniqueId)) return;

					Player p = Bukkit.getPlayer(uniqueId);
					if (isHoldingPickaxe(p)) send(p, StringManager.parsePlaceholders(actionBarMessage, p));
				});
			}, actionBarUpdater, actionBarUpdater);
		}

	}

}
