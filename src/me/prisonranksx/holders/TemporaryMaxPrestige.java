package me.prisonranksx.holders;

import me.prisonranksx.executors.PrestigeExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TemporaryMaxPrestige {

    private UUID uniqueId;
    private double takenBalance;

    private long prestiges;

    private CompletableFuture<PrestigeExecutor.PrestigeResult> finalPrestigeResult;

    private PrestigeExecutor.PrestigeResult currentPrestigeResult;

    private String firstPrestigeName;

    private String firstPrestigeDisplayName;

    public TemporaryMaxPrestige(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public static TemporaryMaxPrestige hold(UUID uniqueId) {
        return new TemporaryMaxPrestige(uniqueId);
    }

    public double getTakenBalance() {
        return takenBalance;
    }

    public TemporaryMaxPrestige setTakenBalance(double takenBalance) {
        this.takenBalance = takenBalance;
        return this;
    }

    public long getPrestiges() {
        return prestiges;
    }

    public TemporaryMaxPrestige setPrestiges(long prestiges) {
        this.prestiges = prestiges;
        return this;
    }

    @NotNull
    public CompletableFuture<PrestigeExecutor.PrestigeResult> getFinalPrestigeResult() {
        return finalPrestigeResult == null ? finalPrestigeResult = new CompletableFuture<>() : finalPrestigeResult;
    }

    public void setFinalPrestigeResult(CompletableFuture<PrestigeExecutor.PrestigeResult> finalPrestigeResult) {
        this.finalPrestigeResult = finalPrestigeResult;
    }

    public String getFirstPrestigeName() {
        return firstPrestigeName;
    }

    public TemporaryMaxPrestige setFirstPrestigeName(String firstPrestigeName) {
        this.firstPrestigeName = firstPrestigeName;
        return this;
    }

    public String getFirstPrestigeDisplayName() {
        return firstPrestigeDisplayName;
    }

    public TemporaryMaxPrestige setFirstPrestigeDisplayName(String firstPrestigeDisplayName) {
        this.firstPrestigeDisplayName = firstPrestigeDisplayName;
        return this;
    }

    public PrestigeExecutor.PrestigeResult getCurrentPrestigeResult() {
        return currentPrestigeResult;
    }

    public TemporaryMaxPrestige setCurrentPrestigeResult(PrestigeExecutor.PrestigeResult currentPrestigeResult) {
        this.currentPrestigeResult = currentPrestigeResult;
        return this;
    }

}
