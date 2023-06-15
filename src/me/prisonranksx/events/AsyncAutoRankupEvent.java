package me.prisonranksx.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.prisonranksx.executors.RankupExecutor.RankupResult;

public class AsyncAutoRankupEvent extends Event implements Cancellable {

	private Player player;
	private String newRankName;
	private boolean isCancelled;
	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public AsyncAutoRankupEvent(Player player, RankupResult rankupResult, String newRankName) {
		super(true);
		this.player = player;
		this.isCancelled = false;
		this.newRankName = newRankName;
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}

	public Player getPlayer() {
		return this.player;

	}

	public String getNewRankName() {
		return newRankName;
	}

}
