package me.prisonranksx.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.prisonranksx.executors.RankupExecutor.RankupResult;

public class AsyncRankupMaxEvent extends Event {

	private Player player;
	private String finalRankupName;
	private String rankupFromName;
	private int totalRankups;
	private double takenBalance;
	private RankupResult rankupResult;
	private boolean limited;

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public AsyncRankupMaxEvent(Player player, RankupResult rankupResult, String rankupFromName, String finalRankupName,
			int totalRankups, double takenBalance, boolean limited) {
		super(true);
		this.player = player;
		this.rankupFromName = rankupFromName;
		this.finalRankupName = finalRankupName;
		this.totalRankups = totalRankups;
		this.limited = limited;
		this.rankupResult = rankupResult;
		this.takenBalance = takenBalance;
	}

	/**
	 * 
	 * @return player that used the rankup max
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * 
	 * @return true if player used: /rankupmax (rankupFromName)
	 */
	public boolean isLimited() {
		return this.limited;
	}

	/**
	 * get the player rank when he started the rankupmax process
	 * 
	 * @return rank name
	 */
	public String getRankupFromName() {
		return this.rankupFromName;
	}

	/**
	 * counts how many ranks the player leveled up from the beginning of the
	 * max rankup process to final rankup
	 * 
	 * @return rankupmax total rankups
	 */
	public int getTotalRankups() {
		return this.totalRankups;
	}

	/**
	 * get the latest rankup player ranked up to.
	 * 
	 * @return final rankup name
	 */
	public String getFinalRankupName() {
		return this.finalRankupName;
	}

	/**
	 * 
	 * @return Money that was taken during the rankup max process.
	 */
	public double getTakenBalance() {
		return takenBalance;
	}

	/**
	 * 
	 * @return Last rankup result
	 */
	public RankupResult getRankupResult() {
		return rankupResult;
	}
}
