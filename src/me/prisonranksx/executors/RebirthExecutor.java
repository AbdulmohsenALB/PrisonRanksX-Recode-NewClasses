package me.prisonranksx.executors;

import com.google.common.collect.Sets;
import me.prisonranksx.components.RequirementsComponent.RequirementEvaluationResult;
import me.prisonranksx.events.AsyncAutoRebirthEvent;
import me.prisonranksx.events.RebirthUpdateCause;
import me.prisonranksx.events.RebirthUpdateEvent;
import me.prisonranksx.holders.Rebirth;
import me.prisonranksx.holders.User;
import me.prisonranksx.reflections.UniqueId;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RebirthExecutor extends PromotionExecutor {

	public static final Set<UUID> AUTO_REBIRTH_PLAYERS = new HashSet<>();

	public static final Set<UUID> MAX_REBIRTH_PLAYERS = Sets.newConcurrentHashSet();

	public final Set<UUID> MAX_REBIRTH_BREAKER = Sets.newConcurrentHashSet();

	public static Set<UUID> getAutoRebirthPlayers() {
		return AUTO_REBIRTH_PLAYERS;
	}

	public static boolean isAutoRebirth(Player player) {
		return AUTO_REBIRTH_PLAYERS.contains(UniqueId.getUUID(player));
	}

	public static boolean isAutoRebirth(UUID uniqueId) {
		return AUTO_REBIRTH_PLAYERS.contains(uniqueId);
	}

	public static boolean isMaxPrestiging(Player player) {
		return MAX_REBIRTH_PLAYERS.contains(UniqueId.getUUID(player));
	}

	public static boolean isMaxPrestiging(UUID uniqueId) {
		return MAX_REBIRTH_PLAYERS.contains(uniqueId);
	}

	public static void addMaxRebirthPlayer(Player player) {
		MAX_REBIRTH_PLAYERS.add(UniqueId.getUUID(player));
	}

	public static void addMaxRebirthPlayer(UUID uniqueId) {
		MAX_REBIRTH_PLAYERS.add(uniqueId);
	}

	public static void removeMaxRebirthPlayer(Player player) {
		MAX_REBIRTH_PLAYERS.remove(UniqueId.getUUID(player));
	}

	public static void removeMaxRebirthPlayer(UUID uniqueId) {
		MAX_REBIRTH_PLAYERS.remove(uniqueId);
	}

	public default void breakMaxRebirth(UUID uniqueId) {
		MAX_REBIRTH_BREAKER.add(uniqueId);
		MAX_REBIRTH_PLAYERS.remove(uniqueId);
	}

	public default boolean finishBreakMaxRebirth(UUID uniqueId) {
		return MAX_REBIRTH_BREAKER.remove(uniqueId);
	}

	public default boolean finishBreakMaxRebirth(Player player) {
		return MAX_REBIRTH_BREAKER.remove(UniqueId.getUUID(player));
	}

	public enum RebirthResult {

		FAIL_NO_PERMISSION(false),
		FAIL_LAST_REBIRTH(false),
		FAIL_NOT_LAST_RANK(false),
		FAIL_NOT_LAST_PRESTIGE(false),
		FAIL_NOT_ENOUGH_BALANCE(false),
		FAIL_REQUIREMENTS_NOT_MET(false),
		FAIL_EVENT_CANCEL(false),
		FAIL_OTHER(false),
		SUCCESS(true);

		private boolean success;
		private RequirementEvaluationResult requirementEvaluationResult;
		private User userResult;
		private double doubleResult = -1;
		@Nullable
		private String stringResult;
		@Nullable
		private Rebirth rebirthResult;

		RebirthResult(boolean success) {
			this.success = success;
		}

		public boolean isSuccessful() {
			return success;
		}

		public RebirthResult getResult() {
			return this;
		}

		@Nullable
		public RequirementEvaluationResult getRequirementEvaluationResult() {
			return requirementEvaluationResult;
		}

		public RebirthResult withRequirementEvaluation(
				@Nullable RequirementEvaluationResult requirementEvaluationResult) {
			this.requirementEvaluationResult = requirementEvaluationResult;
			return this;
		}

		@Nullable
		public String getStringResult() {
			return stringResult;
		}

		public RebirthResult withString(@Nullable String stringResult) {
			this.stringResult = stringResult;
			return this;
		}

		public double getDoubleResult() {
			return doubleResult;
		}

		public RebirthResult withDouble(double doubleResult) {
			this.doubleResult = doubleResult;
			return this;
		}

		@Nullable
		public Rebirth getRebirthResult() {
			return rebirthResult;
		}

		public RebirthResult withRebirth(@Nullable Rebirth rebirthResult) {
			this.rebirthResult = rebirthResult;
			return this;
		}

		public User getUserResult() {
			return userResult;
		}

		public RebirthResult withUser(User userResult) {
			this.userResult = userResult;
			return this;
		}

	}

	/**
	 * Performs a check of all the neccessary requirements on a player to rebirth
	 * <ul>
	 * Conditions include:
	 * <li>Permissions
	 * <li>Last Rebirth
	 * <li>Last Rank, see {@linkplain #canRebirth(Player, boolean)} to skip this
	 * condition
	 * <li>Balance
	 * <li>PlaceholderAPI string and number requirements
	 * </ul>
	 *
	 * @param player check if specified player can rebirth or not
	 * @return RebirthResult with the reason of rebirth failure or success
	 */
	RebirthResult canRebirth(Player player);

	/**
	 * Performs a check of all the neccessary requirements on a player to rebirth
	 * <ul>
	 * Conditions include:
	 * <li>Permissions
	 * <li>Last Rebirth
	 * <li>Last Rank if {@code skipLastRankCheck} is false
	 * <li>Balance
	 * <li>PlaceholderAPI string and number requirements
	 * </ul>
	 *
	 * @param player            check if specified player can rebirth or not
	 * @param skipLastRankCheck whether last rank should be checked or not, this is
	 *                          normally used with rebirth max
	 * @return RebirthResult with the reason of rebirth failure or success
	 */
	RebirthResult canRebirth(Player player, boolean skipLastRankCheck);

	/**
	 * Performs a check of all the neccessary requirements on a player to rebirth
	 * <ul>
	 * Conditions include:
	 * <li>Permissions
	 * <li>Last Rebirth
	 * <li>Last Rank if {@code skipLastRankCheck} is false
	 * <li>Balance
	 * <li>PlaceholderAPI string and number requirements
	 * </ul>
	 *
	 * @param player            check if specified player can rebirth or not
	 * @param balance           balance to check, if balance is set to -1, then all
	 *                          checks will be skipped except for last rebirth
	 *                          check. This was made for force rebirth.
	 * @param skipLastRankCheck whether last rank should be checked or not, this is
	 *                          normally used with rebirth max
	 * @return RebirthResult with the reason of rebirth failure or success
	 */
	RebirthResult canRebirth(Player player, double balance, boolean skipLastRankCheck);

	/**
	 * @param player player to toggle auto rebirth for
	 * @return new state
	 */
	boolean toggleAutoRebirth(Player player);

	/**
	 * @param player player to toggle auto rebirth for
	 * @param enable forcefully enable / disable regardless of player auto rebirth
	 *               state
	 * @return new state
	 */
	boolean toggleAutoRebirth(Player player, boolean enable);

	/**
	 * @param player player to check auto rebirth state for
	 * @return whether auto rebirth is enabled or not
	 */
	boolean isAutoRebirthEnabled(Player player);

	/**
	 * @param player player to promote to the next rebirth
	 * @return RebirthResult that notifies you of the outcome of the promotion,
	 * whether it failed or succeeded
	 */
	RebirthResult rebirth(Player player);

	/**
	 * @param player player to promote to the next rebirth
	 * @param silent prevents messages from being sent on promotion failure. This
	 *               method is used by auto rebirth
	 * @return RebirthResult that notifies you of the outcome of the promotion,
	 * whether it failed or succeeded
	 */
	RebirthResult rebirth(Player player, boolean silent);

	/**
	 * Forcefully rebirths a player to the next rebirth
	 *
	 * @param player player to promote to the next rebirth without checks
	 * @return RebirthResult only FAIL_LAST_REBIRTH and SUCCESS
	 */
	RebirthResult forceRebirth(Player player);

	/**
	 * @param player to perform max rebirth for
	 * @return RebirthResult that notifies you of the last outcome of the
	 * promotion, whether it failed or succeeded
	 */
	CompletableFuture<RebirthResult> maxRebirth(Player player);

	/**
	 * @param player player to include in the event
	 * @param cause  the cause of the rebirth
	 * @param result rebirth outcome
	 * @return event that got called
	 */
	RebirthUpdateEvent callRebirthUpdateEvent(Player player, RebirthUpdateCause cause, RebirthResult result);

	/**
	 * @param player player to include in the event
	 * @param result rebirth outcome
	 * @return event that got called
	 */
	AsyncAutoRebirthEvent callAsyncAutoRebirthEvent(Player player, RebirthResult result);

	/**
	 * @param player that's about to max rebirth
	 * @return false if event is cancelled, true otherwise.
	 */
	boolean callPreRebirthMaxEvent(Player player);

	void callAsyncRebirthMaxEvent(Player player, RebirthResult lastResult, String fromRebirth, String toRebirth,
								  long totalRebirths, double takenBalance, boolean limited);

}
