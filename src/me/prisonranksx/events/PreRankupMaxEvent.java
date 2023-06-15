package me.prisonranksx.events;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.prisonranksx.holders.Rank;

public class PreRankupMaxEvent extends Event implements Cancellable {

	private Player player;
	private Rank rankupFrom;
	private Set<Rank> ranksToBePassed;
	private boolean cancelled;

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public PreRankupMaxEvent(Player player, Rank rankupFrom, Set<Rank> ranksToBePassed) {
		super(false);
		this.player = player;
		this.rankupFrom = rankupFrom;
		this.ranksToBePassed = ranksToBePassed;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	public Player getPlayer() {
		return player;
	}

	public Rank getRankupFrom() {
		return rankupFrom;
	}

	public Set<Rank> getRanksToBePassed() {
		return ranksToBePassed;
	}

}
