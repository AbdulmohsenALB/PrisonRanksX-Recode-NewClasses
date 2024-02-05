package me.prisonranksx.holders;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import me.prisonranksx.executors.RebirthExecutor;

/**
 * Used to track player max rebirth data during max rebirth process.
 */
public class TemporaryMaxRebirth {

	private UUID uniqueId;
	private double takenBalance;

	private long rebirths;

	private CompletableFuture<RebirthExecutor.RebirthResult> finalRebirthResult;

	private RebirthExecutor.RebirthResult currentRebirthResult;

	private String firstRebirthName;

	private String firstRebirthDisplayName;

	public TemporaryMaxRebirth(UUID uniqueId) {
		this.uniqueId = uniqueId;
	}

	public static TemporaryMaxRebirth hold(UUID uniqueId) {
		return new TemporaryMaxRebirth(uniqueId);
	}

	public double getTakenBalance() {
		return takenBalance;
	}

	public TemporaryMaxRebirth setTakenBalance(double takenBalance) {
		this.takenBalance = takenBalance;
		return this;
	}

	public long getRebirths() {
		return rebirths;
	}

	public TemporaryMaxRebirth setRebirths(long rebirths) {
		this.rebirths = rebirths;
		return this;
	}

	@NotNull
	public CompletableFuture<RebirthExecutor.RebirthResult> getFinalRebirthResult() {
		return finalRebirthResult == null ? finalRebirthResult = new CompletableFuture<>() : finalRebirthResult;
	}

	public void setFinalRebirthResult(CompletableFuture<RebirthExecutor.RebirthResult> finalRebirthResult) {
		this.finalRebirthResult = finalRebirthResult;
	}

	public String getFirstRebirthName() {
		return firstRebirthName;
	}

	public TemporaryMaxRebirth setFirstRebirthName(String firstRebirthName) {
		this.firstRebirthName = firstRebirthName;
		return this;
	}

	public String getFirstRebirthDisplayName() {
		return firstRebirthDisplayName;
	}

	public TemporaryMaxRebirth setFirstRebirthDisplayName(String firstRebirthDisplayName) {
		this.firstRebirthDisplayName = firstRebirthDisplayName;
		return this;
	}

	public RebirthExecutor.RebirthResult getCurrentRebirthResult() {
		return currentRebirthResult;
	}

	public TemporaryMaxRebirth setCurrentRebirthResult(RebirthExecutor.RebirthResult currentRebirthResult) {
		this.currentRebirthResult = currentRebirthResult;
		return this;
	}

}
