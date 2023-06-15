package me.prisonranksx.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.prisonranksx.executors.PrestigeExecutor.PrestigeResult;

public class PrestigeUpdateEvent extends Event implements Cancellable {

	private Player player;
	private String newPrestigeName;
	private boolean isCancelled;
	private PrestigeUpdateCause prestigeUpdateCause;
	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public PrestigeUpdateEvent(Player player, PrestigeUpdateCause prestigeUpdateCause, PrestigeResult prestigeResult,
			String newPrestigeName) {
		this.player = player;
		this.prestigeUpdateCause = prestigeUpdateCause;
		this.isCancelled = false;
		this.newPrestigeName = newPrestigeName;
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

	public PrestigeUpdateCause getCause() {
		return this.prestigeUpdateCause;
	}

	public void setCause(PrestigeUpdateCause prestigeUpdateCause) {
		this.prestigeUpdateCause = prestigeUpdateCause;
	}

	public String getNewPrestigeName() {
		return newPrestigeName;
	}

}
