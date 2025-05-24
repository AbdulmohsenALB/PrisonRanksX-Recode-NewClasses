package me.prisonranksx.listeners;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.bukkitutils.Confirmation;
import me.prisonranksx.executors.PrestigeExecutor;
import me.prisonranksx.managers.ActionBarManager;
import me.prisonranksx.reflections.UniqueId;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

public class PlayerQuitListener implements EventExecutor, Listener {

	private PrisonRanksX plugin;

	public PlayerQuitListener(PrisonRanksX plugin, EventPriority priority) {
		this.plugin = plugin;
		this.plugin.getServer()
				.getPluginManager()
				.registerEvent(PlayerQuitEvent.class, this, priority, this, plugin, true);
	}

	public static PlayerQuitListener register(PrisonRanksX plugin, String priority) {
		return new PlayerQuitListener(plugin, EventPriority.valueOf(priority.toUpperCase()));
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		onQuit((PlayerQuitEvent) event);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (plugin.isRankEnabled()) {
			if (plugin.getRankupExecutor().isAutoRankupEnabled(player)) {
				PrisonRanksX.logInfo(player.getName() + " left while auto rankup is running (last rank: " + PRXAPI.getPlayerRank(player).getName() +
						"). Turning it off for them...");
				plugin.getRankupExecutor().toggleAutoRankup(player, false);
			}
		}

		if (plugin.getGlobalSettings().isActionBarProgress()) ActionBarManager.getActionBarProgress().disable(player);
		if (plugin.getGlobalSettings().isPrestigeEnabled()) {
			if (PrestigeExecutor.isMaxPrestiging(player)) {
				PrisonRanksX.logInfo(player.getName() + " left while max prestige is running (last prestige: " + PRXAPI.getPlayerPrestigeNumber(player) +
						"). Turning it off for them...");
				plugin.getPrestigeExecutor().breakMaxPrestige(UniqueId.getUUID(player));
			}
		}
		Confirmation.clearConfirmation("prestige", player.getName());
		Confirmation.clearConfirmation("rebirth", player.getName());
		plugin.getUserController().saveUser(UniqueId.getUUID(player), false).thenRun(() -> {
			plugin.doAsyncLater(() -> {
				plugin.getUserController().unloadUser(UniqueId.getUUID(player));
			}, 1);
		});
	}

}
