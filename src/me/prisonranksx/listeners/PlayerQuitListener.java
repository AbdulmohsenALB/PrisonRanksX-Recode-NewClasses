package me.prisonranksx.listeners;

import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

import me.prisonranksx.PrisonRanksX;

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
		plugin.getRankupExecutor().toggleAutoRankup(e.getPlayer(), false);
		plugin.getUserController().saveUser(e.getPlayer().getUniqueId(), false);
	}

}
