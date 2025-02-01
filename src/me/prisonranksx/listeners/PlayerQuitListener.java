package me.prisonranksx.listeners;

import me.prisonranksx.PrisonRanksX;
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
        plugin.getRankupExecutor().toggleAutoRankup(player, false);

        if (plugin.getGlobalSettings().isActionBarProgress()) ActionBarManager.getActionBarProgress().disable(player);

        plugin.getUserController().saveUser(UniqueId.getUUID(player), false).thenRun(() -> {
            plugin.getUserController().unloadUser(UniqueId.getUUID(player));
        });
    }

}
