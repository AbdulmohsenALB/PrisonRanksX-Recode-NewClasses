package me.prisonranksx.executors;

import me.prisonranksx.bukkitutils.bukkittickbalancer.ConcurrentTask;
import me.prisonranksx.bukkitutils.bukkittickbalancer.DistributedTask;
import me.prisonranksx.holders.Level;
import org.bukkit.entity.Player;

public interface PromotionExecutor {

    /**
     * Spawns a hologram if holograms are enabled.
     *
     * @param level  level to take placeholders from for the hologram
     * @param player to spawn the hologram on their location
     * @param async  whether it should be run on async or sync task
     */
    void spawnHologram(Level level, Player player, boolean async);

    /**
     * Plays a sound if sounds are not null.
     *
     * @param player to play the sound on
     */
    void playSound(Player player);

    /**
     * Updates player vault group after checking that vault options are set up in
     * the config file.
     *
     * @param player to perform group update on
     */
    void updateGroup(Player player);

    /**
     * Perform actions defined in config files such as commands and messages.
     *
     * @param level  level (rank, prestige, or rebirth) holding the components
     * @param player to perform actions on
     */
    void executeComponents(Level level, Player player);

    /**
     * Promote player to next level after checking requirements
     *
     * @param player to promote
     */
    void promote(Player player);

    /**
     * Silently fail when requirements of promotion are not met. Used by /autorankup,prestige,rebirth.
     *
     * @param player to silently promote
     */
    void silentPromote(Player player);

    /**
     * Task responsible for automatic silent promotions.
     * @return auto promotion task
     */
    DistributedTask<Player> getAutoTask();

    /**
     * Task responsible for promoting players to max rank, prestige, or rebirth.
     * @return max promotion task
     */
    ConcurrentTask<Player> getMaxTask();

    /**
     * Stops both auto and max promotion tasks if they are running. (for server reloads)
     */
    void stopTasks();
}
