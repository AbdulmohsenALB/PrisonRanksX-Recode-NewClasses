package me.prisonranksx.executors;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import me.prisonranksx.components.RequirementsComponent.RequirementEvaluationResult;
import me.prisonranksx.events.PrestigeUpdateCause;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.User;
import me.prisonranksx.reflections.UniqueId;

public interface PrestigeExecutor {

	static final Set<UUID> AUTO_PRESTIGE_PLAYERS = new HashSet<>();

	public static Set<UUID> getAutoPrestigePlayers() {
		return AUTO_PRESTIGE_PLAYERS;
	}

	public static boolean switchAutoPrestige(Player player) {
		UUID uniqueId = UniqueId.getUUID(player);
		return AUTO_PRESTIGE_PLAYERS.contains(uniqueId) ? AUTO_PRESTIGE_PLAYERS.remove(uniqueId)
				: AUTO_PRESTIGE_PLAYERS.add(uniqueId);
	}

	public static boolean switchAutoPrestige(Player player, boolean enable) {
		UUID uniqueId = UniqueId.getUUID(player);
		return enable ? AUTO_PRESTIGE_PLAYERS.add(uniqueId) : AUTO_PRESTIGE_PLAYERS.remove(uniqueId);
	}

	public static boolean isAutoPrestige(Player player) {
		return AUTO_PRESTIGE_PLAYERS.contains(UniqueId.getUUID(player));
	}

	enum PrestigeResult {

		FAIL_NO_PERMISSION(false),
		FAIL_LAST_PRESTIGE(false),
		FAIL_NOT_LAST_RANK(false),
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
		private Prestige prestigeResult;

		PrestigeResult(boolean success) {
			this.success = success;
		}

		public boolean isSuccessful() {
			return success;
		}

		public PrestigeResult getResult() {
			return this;
		}

		@Nullable
		public RequirementEvaluationResult getRequirementEvaluationResult() {
			return requirementEvaluationResult;
		}

		public PrestigeResult withRequirementEvaluation(
				@Nullable RequirementEvaluationResult requirementEvaluationResult) {
			this.requirementEvaluationResult = requirementEvaluationResult;
			return this;
		}

		@Nullable
		public String getStringResult() {
			return stringResult;
		}

		public PrestigeResult withString(@Nullable String stringResult) {
			this.stringResult = stringResult;
			return this;
		}

		public double getDoubleResult() {
			return doubleResult;
		}

		public PrestigeResult withDouble(double doubleResult) {
			this.doubleResult = doubleResult;
			return this;
		}

		@Nullable
		public Prestige getPrestigeResult() {
			return prestigeResult;
		}

		public PrestigeResult withPrestige(@Nullable Prestige prestigeResult) {
			this.prestigeResult = prestigeResult;
			return this;
		}

		public User getUserResult() {
			return userResult;
		}

		public PrestigeResult withUser(User userResult) {
			this.userResult = userResult;
			return this;
		}

	}

	/**
	 * Performs a check of all the neccessary requirements on a player to prestige
	 * <ul>
	 * Conditions include:
	 * <li>Permissions
	 * <li>Last Prestige
	 * <li>Last Rank, see {@linkplain #canPrestige(Player, boolean)} to skip this
	 * condition
	 * <li>Balance
	 * <li>PlaceholderAPI string and number requirements
	 * </ul>
	 * 
	 * @param player check if specified player can prestige or not
	 * @return PrestigeResult with the reason of prestige failure or success
	 */
	PrestigeResult canPrestige(Player player);

	/**
	 * Performs a check of all the neccessary requirements on a player to prestige
	 * <ul>
	 * Conditions include:
	 * <li>Permissions
	 * <li>Last Prestige
	 * <li>Last Rank if {@code skipLastRankCheck} is false
	 * <li>Balance
	 * <li>PlaceholderAPI string and number requirements
	 * </ul>
	 * 
	 * @param player            check if specified player can prestige or not
	 * @param skipLastRankCheck whether last rank should be checked or not, this is
	 *                          normally used with prestige max
	 * @return PrestigeResult with the reason of prestige failure or success
	 */
	PrestigeResult canPrestige(Player player, boolean skipLastRankCheck);

	/**
	 * Performs a check of all the neccessary requirements on a player to prestige
	 * <ul>
	 * Conditions include:
	 * <li>Permissions
	 * <li>Last Prestige
	 * <li>Last Rank if {@code skipLastRankCheck} is false
	 * <li>Balance
	 * <li>PlaceholderAPI string and number requirements
	 * </ul>
	 * 
	 * @param player            check if specified player can prestige or not
	 * @param balance           balance to check other than player's actual balance
	 * @param skipLastRankCheck whether last rank should be checked or not, this is
	 *                          normally used with prestige max
	 * @return PrestigeResult with the reason of prestige failure or success
	 */
	PrestigeResult canPrestige(Player player, double balance, boolean skipLastRankCheck);

	/**
	 * 
	 * @param player player to toggle auto prestige for
	 * @return new state
	 */
	boolean toggleAutoPrestige(Player player);

	/**
	 * 
	 * @param player player to toggle auto prestige for
	 * @param enable forcefully enable / disable regardless of player auto prestige
	 *               state
	 * @return new state
	 */
	boolean toggleAutoPrestige(Player player, boolean enable);

	/**
	 * 
	 * @param player player to check auto prestige state for
	 * @return whether auto prestige is enabled or not
	 */
	boolean isAutoPrestigeEnabled(Player player);

	/**
	 * 
	 * @param player player to promote to the next prestige
	 * @return PrestigeResult that notifies you of the outcome of the promotion,
	 *         whether it failed or succeeded
	 */
	PrestigeResult prestige(Player player);

	/**
	 * 
	 * @param player player to promote to the next prestige
	 * @param silent prevents messages from being sent on promotion failure. This
	 *               method is used by auto prestige
	 * @return PrestigeResult that notifies you of the outcome of the promotion,
	 *         whether it failed or succeeded
	 */
	PrestigeResult prestige(Player player, boolean silent);

	/**
	 * Forcefully prestiges a player to the next prestige
	 * 
	 * @param player player to promote to the next prestige without checks
	 * @return PrestigeResult only FAIL_LAST_PRESTIGE and SUCCESS
	 */
	PrestigeResult forcePrestige(Player player);

	/**
	 * 
	 * @param player to perform max prestige for
	 * @return PrestigeResult that notifies you of the last outcome of the
	 *         promotion, whether it failed or succeeded
	 */
	PrestigeResult maxPrestige(Player player);

	/**
	 * 
	 * @param player          player to include in the event
	 * @param cause           the cause of the prestige
	 * @param result          prestige outcome
	 * @param updatedPrestige prestige that player will get prestiged to
	 * @param currentPrestige prestige that player will prestige from
	 * @param successful      whether player gonna successfully prestige or not
	 * @return false if event is cancelled, true otherwise
	 */
	boolean callPrestigeUpdateEvent(Player player, PrestigeUpdateCause cause, PrestigeResult result,
			@Nullable String updatedPrestige, boolean successful);

	/**
	 * 
	 * @param player          player to include in the event
	 * @param cause           the cause of the prestige
	 * @param result          prestige outcome
	 * @param updatedPrestige prestige that player will get prestiged to
	 * @param currentPrestige prestige that player will prestige from
	 * @param successful      whether player gonna successfully prestige or not
	 * @return false if event is cancelled, true otherwise
	 */
	boolean callAsyncAutoPrestigeEvent(Player player, PrestigeUpdateCause cause, PrestigeResult result,
			@Nullable String updatedPrestige, @Nullable String currentPrestige, boolean successful);

	boolean callPrePrestigeMaxEvent(Player player);

	void callAsyncPrestigeMaxEvent(Player player, PrestigeResult lastResult, String fromPrestige, String toPrestige,
			int totalPrestiges, double takenBalance);

}
