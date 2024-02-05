package me.prisonranksx.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.EventExecutor;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.reflections.UniqueId;

public class PlayerJoinListener implements EventExecutor, Listener {

	private PrisonRanksX plugin;

	public PlayerJoinListener(PrisonRanksX plugin, EventPriority priority) {
		this.plugin = plugin;
		this.plugin.getServer()
				.getPluginManager()
				.registerEvent(PlayerJoinEvent.class, this, priority, this, plugin, true);
	}

	public static PlayerJoinListener register(PrisonRanksX plugin, String priority) {
		return new PlayerJoinListener(plugin, EventPriority.valueOf(priority.toUpperCase()));
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		onJoin((PlayerJoinEvent) event);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		UUID uniqueId = UniqueId.getUUID(player);
		if (!plugin.getUserController().isLoaded(uniqueId))
			plugin.getUserController().loadUser(uniqueId, player.getName());
	}

}
