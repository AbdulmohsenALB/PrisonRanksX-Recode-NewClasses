package me.prisonranksx.executors;

import org.bukkit.entity.Player;

import me.prisonranksx.holders.Level;

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
	 * Silently fail when requirements of promotion are not met.
	 * 
	 * @param player to silently promote
	 */
	void silentPromote(Player player);

}
