package me.prisonranksx.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.prisonranksx.executors.PrestigeExecutor.PrestigeResult;

public class AsyncPrestigeMaxEvent extends Event {

	private Player player;
	private String finalPrestigeName;
	private String prestigeFromName;
	private long totalPrestiges;
	private double takenBalance;
	private PrestigeResult prestigeResult;
	private boolean limited;

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public AsyncPrestigeMaxEvent(Player player, PrestigeResult prestigeResult, String prestigeFromName,
			String finalPrestigeName, long totalPrestiges, double takenBalance, boolean limited) {
		super(true);
		this.player = player;
		this.prestigeFromName = prestigeFromName;
		this.finalPrestigeName = finalPrestigeName;
		this.totalPrestiges = totalPrestiges;
		this.limited = limited;
		this.prestigeResult = prestigeResult;
		this.takenBalance = takenBalance;
	}

	/**
	 * 
	 * @return Player that used prestige max
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * 
	 * @return true if player used: /prestigemax (maxAmountOfPrestigesToPrestige)
	 */
	public boolean isLimited() {
		return this.limited;
	}

	/**
	 * Gets first prestige player leveled up from
	 * 
	 * @return prestige name
	 */
	public String getPrestigeFromName() {
		return this.prestigeFromName;
	}

	/**
	 * Counts how many prestiges player has passed from the beginning of the
	 * max prestige process to final prestige
	 * 
	 * @return prestigemax total prestiges
	 */
	public long getTotalPrestiges() {
		return this.totalPrestiges;
	}

	/**
	 * Gets the latest prestige player leveled up to.
	 * 
	 * @return final prestige name
	 */
	public String getFinalPrestigeName() {
		return this.finalPrestigeName;
	}

	/**
	 * 
	 * @return Money that was taken during the prestige max process.
	 */
	public double getTakenBalance() {
		return takenBalance;
	}

	/**
	 * 
	 * @return Last prestige result
	 */
	public PrestigeResult getPrestigeResult() {
		return prestigeResult;
	}
}
