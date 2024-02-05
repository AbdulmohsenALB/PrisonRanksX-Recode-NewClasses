package me.prisonranksx.executors;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import me.prisonranksx.bukkitutils.bukkittickbalancer.ConcurrentTask;
import me.prisonranksx.bukkitutils.bukkittickbalancer.DistributedTask;
import me.prisonranksx.components.RequirementsComponent.RequirementEvaluationResult;
import me.prisonranksx.events.AsyncAutoRankupEvent;
import me.prisonranksx.events.RankUpdateCause;
import me.prisonranksx.events.RankUpdateEvent;
import me.prisonranksx.holders.Level;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.User;
import me.prisonranksx.reflections.UniqueId;

public interface RankupExecutor extends PromotionExecutor {

	static final Set<UUID> AUTO_RANKUP_PLAYERS = new HashSet<>();

	static final Set<UUID> MAX_RANKUP_PLAYERS = Sets.newConcurrentHashSet();

	final Set<UUID> MAX_RANKUP_BREAKER = Sets.newConcurrentHashSet();

	/**
	 * @return players who have their auto rankup enabled
	 */
	public static Set<UUID> getAutoRankupPlayers() {
		return AUTO_RANKUP_PLAYERS;
	}

	public static boolean isAutoRankup(Player player) {
		return AUTO_RANKUP_PLAYERS.contains(UniqueId.getUUID(player));
	}

	public static boolean isMaxRankup(Player player) {
		return MAX_RANKUP_PLAYERS.contains(UniqueId.getUUID(player));
	}

	public static boolean isMaxRankup(UUID uniqueId) {
		return MAX_RANKUP_PLAYERS.contains(uniqueId);
	}

	public static void addMaxRankupPlayer(Player player) {
		MAX_RANKUP_PLAYERS.add(UniqueId.getUUID(player));
	}

	public static void addMaxRankupPlayer(UUID uniqueId) {
		MAX_RANKUP_PLAYERS.add(uniqueId);
	}

	public static void removeMaxRankupPlayer(Player player) {
		MAX_RANKUP_PLAYERS.remove(UniqueId.getUUID(player));
	}

	public static void removeMaxRankupPlayer(UUID uniqueId) {
		MAX_RANKUP_PLAYERS.remove(uniqueId);
	}

	public default void breakMaxRankup(UUID uniqueId) {
		MAX_RANKUP_BREAKER.add(uniqueId);
		MAX_RANKUP_PLAYERS.remove(uniqueId);
	}

	public default boolean finishBreakMaxRankup(UUID uniqueId) {
		return MAX_RANKUP_BREAKER.remove(uniqueId);
	}

	public default boolean finishBreakMaxRankup(Player player) {
		return MAX_RANKUP_BREAKER.remove(UniqueId.getUUID(player));
	}

	/**
	 * Stores variables of a rankup that are needed for components to be executed,
	 * such as: rank object and cost
	 */
	public enum RankupResult {

		FAIL_NO_PERMISSION(false),
		FAIL_LAST_RANK(false),
		FAIL_NOT_ENOUGH_BALANCE(false),
		FAIL_REQUIREMENTS_NOT_MET(false),
		FAIL_OTHER(false),
		SUCCESS(true);

		private boolean success;
		private RequirementEvaluationResult requirementEvaluationResult;
		private User userResult;
		private double doubleResult = -1;
		@Nullable
		private String stringResult;
		@Nullable
		private Rank rankResult;

		RankupResult(boolean success) {
			this.success = success;
		}

		public boolean isSuccessful() {
			return success;
		}

		public RankupResult getResult() {
			return this;
		}

		@Nullable
		public RequirementEvaluationResult getRequirementEvaluationResult() {
			return requirementEvaluationResult;
		}

		public RankupResult withRequirementEvaluation(
				@Nullable RequirementEvaluationResult requirementEvaluationResult) {
			this.requirementEvaluationResult = requirementEvaluationResult;
			return this;
		}

		/**
		 * Usually next rank name.
		 * 
		 * @return
		 */
		@Nullable
		public String getStringResult() {
			return stringResult;
		}

		public RankupResult withString(@Nullable String stringResult) {
			this.stringResult = stringResult;
			return this;
		}

		public double getDoubleResult() {
			return doubleResult;
		}

		public RankupResult withDouble(double doubleResult) {
			this.doubleResult = doubleResult;
			return this;
		}

		@Nullable
		public Rank getRankResult() {
			return rankResult;
		}

		public RankupResult withRank(@Nullable Rank rankResult) {
			this.rankResult = rankResult;
			return this;
		}

		public User getUserResult() {
			return userResult;
		}

		public RankupResult withUser(User userResult) {
			this.userResult = userResult;
			return this;
		}

	}

	/**
	 * Performs a check of all the neccessary requirements on a player to rankup
	 * <ul>
	 * Conditions include:
	 * <li>Permissions
	 * <li>Last Rank
	 * <li>Balance
	 * <li>PlaceholderAPI string and number requirements
	 * </ul>
	 *
	 * @param player check if specified player can rankup or not
	 * @return RankupResult with the reason of rankup failure or success
	 */
	RankupResult canRankup(Player player);

	/**
	 * Performs a check of all the neccessary requirements on a player to rankup
	 * <ul>
	 * Conditions include:
	 * <li>Permissions
	 * <li>Last Rank
	 * <li>Balance
	 * <li>PlaceholderAPI string and number requirements
	 * </ul>
	 *
	 * @param player  check if specified player can rankup or not
	 * @param balance balance to check, if balance is set to -1 then all checks will
	 *                be skipped except for last rank check. This was made for force
	 *                rankup.
	 * @return RankupResult with the reason of rankup failure or success
	 */
	RankupResult canRankup(Player player, double balance);

	/**
	 * @param player player to toggle auto rankup for
	 * @return new state
	 */
	boolean toggleAutoRankup(Player player);

	/**
	 * @param player player to toggle auto rankup for
	 * @param enable forcefully enable / disable regardless of player auto rankup
	 *               state
	 * @return new state
	 */
	boolean toggleAutoRankup(Player player, boolean enable);

	/**
	 * @param player player to check auto rankup state for
	 * @return whether auto rankup is enabled or not
	 */
	boolean isAutoRankupEnabled(Player player);

	/**
	 * @param player player to promote to the next rank
	 * @return RankupResult that notifies you of the outcome of the promotion,
	 *         whether it failed or succeeded
	 */
	RankupResult rankup(Player player);

	/**
	 * Money is taken from player instead of target.
	 * 
	 * @param player player that will promote target to the next rank
	 * @param target target to be promoted
	 * @return RankupResult that notifies you of the outcome of the promotion,
	 *         whether it failed or succeeded
	 */
	RankupResult rankup(Player player, Player target);

	/**
	 * @param player player to promote to the next rank
	 * @param silent prevents messages from being sent on promotion failure. This
	 *               method is used by auto rankup
	 * @return RankupResult that notifies you of the outcome of the promotion,
	 *         whether it failed or succeeded
	 */
	RankupResult rankup(Player player, boolean silent);

	/**
	 * Forcefully promotes a player to the next rank
	 *
	 * @param player player to promote to the next rank without checks
	 * @return RankupResult only results in FAIL_LAST_RANK and SUCCESS
	 */
	RankupResult forceRankup(Player player);

	/**
	 * @param player to perform max rankup for
	 * @return A completable future of RankupResult that notifies you of the last
	 *         outcome of the
	 *         promotion, whether it failed or succeeded
	 */
	CompletableFuture<RankupResult> maxRankup(Player player);

	/**
	 * @param player   to perform max rankup for
	 * @param lastRank last rank player is allowed to reach or null to ignore
	 * @return A completable future of RankupResult that notifies you of the last
	 *         outcome of the
	 *         promotion, whether it failed or succeeded
	 */
	CompletableFuture<RankupResult> maxRankup(Player player, @Nullable String lastRank);

	/**
	 * @param rank   to execute components of
	 * @param player to perform components on
	 */
	void executeComponents(Level rank, Player player);

	/**
	 * @param player player to include in the event
	 * @param cause  the cause of the rankup
	 * @param result rankup outcome
	 * @return RankUpdateEvent that will be called.
	 */
	RankUpdateEvent callRankUpdateEvent(Player player, RankUpdateCause cause, RankupResult result);

	/**
	 * @param player player to include in the event
	 * @param result rankup outcome
	 * @return AsyncAutoRankupEvent that will be called.
	 */
	AsyncAutoRankupEvent callAsyncAutoRankupEvent(Player player, RankupResult result);

	/**
	 * @param player          player to include in the event
	 * @param rankupFrom      rank player is going to rankup from
	 * @param ranksToBePassed ranks that player will go through
	 * @return false if event is cancelled, ture otherwise
	 */
	boolean callPreRankupMaxEvent(Player player, Rank rankupFrom, Set<Rank> ranksToBePassed);

	/**
	 * @param player       player to include in the event
	 * @param lastResult   last rankup result of the max rankup
	 * @param fromRank     rank player started ranking up from
	 * @param toRank       rank player landed at
	 * @param totalRankups amount of ranks player has gone through
	 * @param takenBalance how much balance was taken from player
	 * @param limited      whether max rankup is set to stop at specific rank rather
	 *                     than last rank
	 */
	void callAsyncRankupMaxEvent(Player player, RankupResult lastResult, String fromRank, String toRank,
			int totalRankups, double takenBalance, boolean limited);

	/**
	 * Updates player group in a permission plugin if that was enabled
	 *
	 * @param player whom group will get updated
	 */
	void updateGroup(Player player);

	DistributedTask<Player> getAutoRankupTask();

	ConcurrentTask<Player> getMaxRankupTask();

}
