package me.prisonranksx.listeners;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.UUID;

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

    public static PlayerChatListener register(PrisonRanksX plugin, String priority) {
        return new PlayerChatListener(plugin, EventPriority.valueOf(priority.toUpperCase()));
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

        String playerRank = user.hasRank() && plugin.getGlobalSettings().isRankEnabled()
                ? user.hasRank() ? user.getRank().getDisplayName() + colorReset : empty : empty;

        String playerPrestige = user.hasPrestige() && plugin.getGlobalSettings().isPrestigeEnabled()
                ? user.getPrestige().getDisplayName() + colorReset + space
                : plugin.getGlobalSettings().getNoPrestigeDisplay();

        String playerRebirth = user.hasRebirth() && plugin.getGlobalSettings().isRebirthEnabled()
                ? user.getRebirth().getDisplayName() + colorReset + space
                : plugin.getGlobalSettings().getNoRebirthDisplay();

        String additionalFormat = getAdditionalFormat(playerRank, playerPrestige, playerRebirth);

        String spacer = playerRank.equals(empty) ? playerRank : space;

        e.setFormat(StringManager.parsePlaceholders(additionalFormat + spacer + originalFormat, player));
    }

    private String getAdditionalFormat(String playerRank, String playerPrestige, String playerRebirth) {
        String rankDisplayName = plugin.getGlobalSettings().isRankForceDisplay() ? playerRank : empty;
        String prestigeDisplayName = plugin.getGlobalSettings().isPrestigeForceDisplay() ? playerPrestige : empty;
        String rebirthDisplayName = plugin.getGlobalSettings().isRebirthForceDisplay() ? playerRebirth : empty;

        return plugin.getGlobalSettings()
                .getForceDisplayOrder()
                .replace("{rank}", rankDisplayName)
                .replace("{prestige}", prestigeDisplayName)
                .replace("{rebirth}", rebirthDisplayName);
    }

}
