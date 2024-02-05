package me.prisonranksx.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.prisonranksx.executors.PrestigeExecutor.PrestigeResult;

public class AsyncAutoPrestigeEvent extends Event implements Cancellable {

	private Player player;
	private PrestigeResult prestigeResult;
	private boolean isCancelled;
	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public AsyncAutoPrestigeEvent(Player player, PrestigeResult prestigeResult) {
		super(true);
		this.player = player;
		this.isCancelled = false;
		this.prestigeResult = prestigeResult;
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

	public PrestigeResult getPrestigeResult() {
		return prestigeResult;
	}

	public void setPrestigeResult(PrestigeResult prestigeResult) {
		this.prestigeResult = prestigeResult;
	}
}
