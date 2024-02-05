package me.prisonranksx.holders;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import me.prisonranksx.executors.RankupExecutor;

/**
 * Used to track player max rankup data during max rankup process.
 */
public class TemporaryMaxRankup {

	private UUID uniqueId;
	private double takenBalance;

	private long rankups;

	private CompletableFuture<RankupExecutor.RankupResult> finalRankupResult;

	private RankupExecutor.RankupResult currentRankupResult;

	private String firstRankName;

	private String firstRankDisplayName;

	private String lastAllowedRankName;

	public TemporaryMaxRankup(UUID uniqueId) {
		this.uniqueId = uniqueId;
	}

	public static TemporaryMaxRankup hold(UUID uniqueId) {
		return new TemporaryMaxRankup(uniqueId);
	}

	public double getTakenBalance() {
		return takenBalance;
	}

	public TemporaryMaxRankup setTakenBalance(double takenBalance) {
		this.takenBalance = takenBalance;
		return this;
	}

	public long getRankups() {
		return rankups;
	}

	public TemporaryMaxRankup setRankups(long rankups) {
		this.rankups = rankups;
		return this;
	}

	@NotNull
	public CompletableFuture<RankupExecutor.RankupResult> getFinalRankupResult() {
		return finalRankupResult == null ? finalRankupResult = new CompletableFuture<>() : finalRankupResult;
	}

	public void setFinalRankupResult(CompletableFuture<RankupExecutor.RankupResult> finalRankupResult) {
		this.finalRankupResult = finalRankupResult;
	}

	public String getFirstRankName() {
		return firstRankName;
	}

	public TemporaryMaxRankup setFirstRankName(String firstRankName) {
		this.firstRankName = firstRankName;
		return this;
	}

	public String getFirstRankDisplayName() {
		return firstRankDisplayName;
	}

	public TemporaryMaxRankup setFirstRankDisplayName(String firstRankDisplayName) {
		this.firstRankDisplayName = firstRankDisplayName;
		return this;
	}

	public RankupExecutor.RankupResult getCurrentRankupResult() {
		return currentRankupResult;
	}

	public TemporaryMaxRankup setCurrentRankupResult(RankupExecutor.RankupResult currentRankupResult) {
		this.currentRankupResult = currentRankupResult;
		return this;
	}

	public String getLastAllowedRankName() {
		return lastAllowedRankName;
	}

	public void setLastAllowedRankName(String lastAllowedRankName) {
		this.lastAllowedRankName = lastAllowedRankName;
	}

}
