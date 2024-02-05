package me.prisonranksx.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.prisonranksx.executors.RebirthExecutor.RebirthResult;

public class AsyncRebirthMaxEvent extends Event {

	private Player player;
	private String finalRebirthName;
	private String rebirthFromName;
	private long totalRebirths;
	private double takenBalance;
	private RebirthResult rebirthResult;
	private boolean limited;

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public AsyncRebirthMaxEvent(Player player, RebirthResult rebirthResult, String rebirthFromName,
			String finalRebirthName, long totalRebirths, double takenBalance, boolean limited) {
		super(true);
		this.player = player;
		this.rebirthFromName = rebirthFromName;
		this.finalRebirthName = finalRebirthName;
		this.totalRebirths = totalRebirths;
		this.limited = limited;
		this.rebirthResult = rebirthResult;
		this.takenBalance = takenBalance;
	}

	/**
	 * 
	 * @return Player that used rebirth max
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * 
	 * @return true if player used: /rebirthmax (maxAmountOfRebirthsToRebirth)
	 */
	public boolean isLimited() {
		return this.limited;
	}

	/**
	 * Gets first rebirth player leveled up from
	 * 
	 * @return rebirth name
	 */
	public String getRebirthFromName() {
		return this.rebirthFromName;
	}

	/**
	 * Counts how many rebirths player has passed from the beginning of the
	 * max rebirth process to final rebirth
	 * 
	 * @return rebirthmax total rebirths
	 */
	public long getTotalRebirths() {
		return this.totalRebirths;
	}

	/**
	 * Gets the latest rebirth player leveled up to.
	 * 
	 * @return final rebirth name
	 */
	public String getFinalRebirthName() {
		return this.finalRebirthName;
	}

	/**
	 * 
	 * @return Money that was taken during the rebirth max process.
	 */
	public double getTakenBalance() {
		return takenBalance;
	}

	/**
	 * 
	 * @return Last rebirth result
	 */
	public RebirthResult getRebirthResult() {
		return rebirthResult;
	}
}
