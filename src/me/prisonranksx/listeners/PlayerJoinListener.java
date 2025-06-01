package me.prisonranksx.listeners;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.ActionBarManager;
import me.prisonranksx.reflections.UniqueId;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
		plugin.doAsync(() -> {
			Player player = e.getPlayer();
			UUID uniqueId = UniqueId.getUUID(player);
			CompletableFuture<User> userCompletableFuture;
			if (!plugin.getUserController().isLoaded(uniqueId)) {
				// If for what ever reason user isn't loaded
				userCompletableFuture = plugin.getUserController().loadUser(uniqueId, player.getName());
			} else {
				User user = plugin.getUserController().getUser(uniqueId);
				userCompletableFuture = CompletableFuture.completedFuture(user);
			}
			userCompletableFuture.thenRunAsync(() -> {
				if (plugin.getGlobalSettings().isAutoRankupAlwaysEnabled()
						&& plugin.getGlobalSettings().isRankEnabled()) {
					plugin.getRankupExecutor().toggleAutoRankup(player, true);
				}
				if (plugin.getGlobalSettings().isActionBarProgress()) {
					ActionBarManager.getActionBarProgress().enable(player);
				}
			});
		});
	}

}
