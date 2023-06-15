package me.prisonranksx.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.EventExecutor;

import me.prisonranksx.PrisonRanksX;

public class PlayerLoginListener implements EventExecutor, Listener {

	private PrisonRanksX plugin;

	public PlayerLoginListener(PrisonRanksX plugin, EventPriority priority) {
		this.plugin = plugin;
		this.plugin.getServer()
				.getPluginManager()
				.registerEvent(AsyncPlayerPreLoginEvent.class, this, priority, this, plugin, true);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		onLogin((AsyncPlayerPreLoginEvent) event);
	}

	@EventHandler
	public void onLogin(AsyncPlayerPreLoginEvent e) {
		UUID uniqueId = e.getUniqueId();
		Bukkit.broadcastMessage("Loaded");
		if (!plugin.getUserController().isLoaded(uniqueId)) plugin.getUserController().loadUser(uniqueId, e.getName());
	}

}
