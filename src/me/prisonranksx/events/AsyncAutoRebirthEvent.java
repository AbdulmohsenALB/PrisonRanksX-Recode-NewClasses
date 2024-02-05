package me.prisonranksx.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.prisonranksx.executors.RebirthExecutor.RebirthResult;

public class AsyncAutoRebirthEvent extends Event implements Cancellable {

	private Player player;
	private RebirthResult rebirthResult;
	private boolean isCancelled;
	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public AsyncAutoRebirthEvent(Player player, RebirthResult rebirthResult) {
		super(true);
		this.player = player;
		this.isCancelled = false;
		this.rebirthResult = rebirthResult;
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

	public RebirthResult getRebirthResult() {
		return rebirthResult;
	}

	public void setRebirthResult(RebirthResult rebirthResult) {
		this.rebirthResult = rebirthResult;
	}
}
