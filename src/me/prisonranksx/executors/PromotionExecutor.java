package me.prisonranksx.executors;

import org.bukkit.entity.Player;

import me.prisonranksx.holders.Level;

public interface PromotionExecutor {

	void spawnHologram(Level level, Player player, boolean async);

	void updateGroup(Player player);

	void executeComponents(Level level, Player player);

	void promote(Player player);

	void silentPromote(Player player);

}
