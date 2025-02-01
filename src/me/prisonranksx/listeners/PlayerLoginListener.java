package me.prisonranksx.listeners;

import me.prisonranksx.PrisonRanksX;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.UUID;

public class PlayerLoginListener implements EventExecutor, Listener {

    private PrisonRanksX plugin;

    public PlayerLoginListener(PrisonRanksX plugin, EventPriority priority) {
        this.plugin = plugin;
        this.plugin.getServer()
                .getPluginManager()
                .registerEvent(AsyncPlayerPreLoginEvent.class, this, priority, this, plugin, true);
    }

    public static PlayerLoginListener register(PrisonRanksX plugin, String priority) {
        return new PlayerLoginListener(plugin, EventPriority.valueOf(priority.toUpperCase()));
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        onLogin((AsyncPlayerPreLoginEvent) event);
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        UUID uniqueId = e.getUniqueId();
        if (!plugin.getUserController().isLoaded(uniqueId)) plugin.getUserController().loadUser(uniqueId, e.getName());
    }

}
