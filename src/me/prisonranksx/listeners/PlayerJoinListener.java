package me.prisonranksx.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.executors.RankupExecutor;
import me.prisonranksx.reflections.UniqueId;

public class PlayerJoinListener implements EventExecutor, Listener {

	private PrisonRanksX plugin;

	public PlayerJoinListener(PrisonRanksX plugin, EventPriority priority) {
		this.plugin = plugin;
		this.plugin.getServer()
				.getPluginManager()
				.registerEvent(PlayerJoinEvent.class, this, priority, this, plugin, true);
		this.plugin.getServer()
				.getPluginManager()
				.registerEvent(PlayerQuitEvent.class, this, priority, this, plugin, true);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		onJoin((PlayerJoinEvent) event);
		onQuit((PlayerQuitEvent) event);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		UUID uniqueId = UniqueId.getUUID(player);
		if (!plugin.getUserController().isLoaded(uniqueId))
			plugin.getUserController().loadUser(uniqueId, player.getName());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		RankupExecutor.switchAutoRankup(e.getPlayer(), false);
	}

}
