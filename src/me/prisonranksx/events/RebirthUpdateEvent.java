package me.prisonranksx.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.prisonranksx.executors.RebirthExecutor.RebirthResult;

public class RebirthUpdateEvent extends Event implements Cancellable {

	private Player player;
	private boolean isCancelled;
	private RebirthResult rebirthResult;
	private RebirthUpdateCause rebirthUpdateCause;
	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public RebirthUpdateEvent(Player player, RebirthUpdateCause rebirthUpdateCause, RebirthResult rebirthResult) {
		this.player = player;
		this.rebirthUpdateCause = rebirthUpdateCause;
		this.rebirthResult = rebirthResult;
		this.isCancelled = false;
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

	public RebirthUpdateCause getCause() {
		return this.rebirthUpdateCause;
	}

	public RebirthResult getRebirthResult() {
		return rebirthResult;
	}

	public void setRebirthResult(RebirthResult rebirthResult) {
		this.rebirthResult = rebirthResult;
	}
}
