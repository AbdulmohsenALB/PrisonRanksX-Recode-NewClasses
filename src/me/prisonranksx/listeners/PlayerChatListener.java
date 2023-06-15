package me.prisonranksx.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.RebirthStorage;
import me.prisonranksx.holders.User;
import me.prisonranksx.reflections.UniqueId;

public class PlayerChatListener implements EventExecutor, Listener {

	private PrisonRanksX plugin;
	private final String colorReset = "§r";
	private final String empty = "";
	private final String space = " ";

	public PlayerChatListener(PrisonRanksX plugin, EventPriority priority) {
		this.plugin = plugin;
		this.plugin.getServer()
				.getPluginManager()
				.registerEvent(AsyncPlayerChatEvent.class, this, priority, this, plugin, true);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		onChat((AsyncPlayerChatEvent) event);
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		UUID uniqueId = UniqueId.getUUID(player);

		if (!plugin.getUserController().isLoaded(uniqueId))
			plugin.getUserController().loadUser(uniqueId, player.getName());

		if (plugin.getGlobalSettings().isWorldIncluded(player.getWorld())) return;

		String originalFormat = e.getFormat();
		User user = plugin.getUserController().getUser(uniqueId);

		String playerRank = user.getRankName() != null && plugin.getGlobalSettings().isRankEnabled()
				? RankStorage.getRank(user.getRankName(), user.getPathName()).getDisplayName() + colorReset : empty;

		String playerPrestige = user.getPrestigeName() != null && plugin.getGlobalSettings().isPrestigeEnabled()
				? PrestigeStorage.getPrestige(user.getPrestigeName()).getDisplayName() + colorReset + space
				: plugin.getGlobalSettings().getNoPrestigeDisplay();

		String playerRebirth = user.getRebirthName() != null && plugin.getGlobalSettings().isRebirthEnabled()
				? RebirthStorage.getRebirth(user.getRebirthName()).getDisplayName() + colorReset + space
				: plugin.getGlobalSettings().getNoRebirthDisplay();

		String rankDisplayName = plugin.getGlobalSettings().isRankForceDisplay() ? playerRank : empty;
		String prestigeDisplayName = plugin.getGlobalSettings().isPrestigeForceDisplay() ? playerPrestige : empty;
		String rebirthDisplayName = plugin.getGlobalSettings().isRebirthForceDisplay() ? playerRebirth : empty;

		String additionalFormat = plugin.getGlobalSettings()
				.getForceDisplayOrder()
				.replace("{rank}", rankDisplayName)
				.replace("{prestige}", prestigeDisplayName)
				.replace("{rebirth}", rebirthDisplayName);

		String spacer = playerRank.equals(empty) ? playerRank : space;

		e.setFormat(additionalFormat + spacer + originalFormat);
	}

}
