package me.prisonranksx.api;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.executors.RankupExecutor;
import me.prisonranksx.executors.RankupExecutor.RankupResult;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.User;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.utils.Rina;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PRXAPI {

    private static PrisonRanksX plugin = PrisonRanksX.getInstance();

    public static PrisonRanksX getInstance() {
        return plugin = PrisonRanksX.getInstance();
    }

    public static RankupResult rankup(Player player) {
        return plugin.getRankupExecutor().rankup(player);
    }

    public static RankupResult rankup(Player player, boolean silent) {
        return plugin.getRankupExecutor().rankup(player, silent);
    }

    public static RankupResult rankup(Player promoter, Player target) {
        return plugin.getRankupExecutor().rankup(promoter, target);
    }

    public static RankupResult forceRankup(Player player) {
        return plugin.getRankupExecutor().forceRankup(player);
    }

    public static CompletableFuture<RankupResult> maxRankup(Player player) {
        return plugin.getRankupExecutor().maxRankup(player);
    }

    public static CompletableFuture<RankupResult> maxRankup(Player player, String lastRank) {
        return plugin.getRankupExecutor().maxRankup(player, lastRank);
    }

    public static boolean toggleAutoRankup(Player player) {
        return plugin.getRankupExecutor().toggleAutoRankup(player);
    }

    public static boolean toggleAutoRankup(Player player, boolean enable) {
        return plugin.getRankupExecutor().toggleAutoRankup(player, enable);
    }

    public static boolean isAutoRankup(Player player) {
        return plugin.getRankupExecutor().isAutoRankupEnabled(player);
    }

    public static RankupResult canRankup(Player player) {
        return plugin.getRankupExecutor().canRankup(player);
    }

    public static Set<UUID> getAutoRankupPlayers() {
        return RankupExecutor.AUTO_RANKUP_PLAYERS;
    }

    public static Rank getPlayerRank(UUID uniqueId) {
        User user = getUser(uniqueId);
        return RankStorage.getRank(user.getRankName(), user.getPathName());
    }

    public static Rank getPlayerRank(Player player) {
        return getPlayerRank(UniqueId.getUUID(player));
    }

    @Nullable
    public static Prestige getPlayerPrestige(UUID uniqueId) {
        return PrestigeStorage.getPrestige(getUser(uniqueId).getPrestigeName());
    }

    @Nullable
    public static Prestige getPlayerPrestige(Player player) {
        return getPlayerPrestige(UniqueId.getUUID(player));
    }

    public static boolean hasPrestige(UUID uniqueId) {
        return getUser(uniqueId).getPrestigeName() != null;
    }

    public static boolean hasPrestige(Player player) {
        return hasPrestige(UniqueId.getUUID(player));
    }

    public static long getPlayerPrestigeNumber(UUID uniqueId) {
        return PrestigeStorage.getHandler().getPrestigeNumber(getUser(uniqueId).getPrestigeName());
    }

    public static long getPlayerPrestigeNumber(Player player) {
        return getPlayerPrestigeNumber(UniqueId.getUUID(player));
    }

    public static double getRankFinalCost(Rank rank, UUID uniqueId) {
        Prestige prestige = getPlayerPrestige(uniqueId);
        if (!hasPrestige(uniqueId)) return rank.getCost();
        return Double.parseDouble(Rina.evaluateMathExpression(plugin.getPrestigeSettings()
                .getIncreaseExpression()
                .replace("{increase_percentage}", String.valueOf(prestige.getCostIncrease()))
                .replace("{rank_cost}", String.valueOf(rank.getCost()))
                .replace("{prestige_number}", String.valueOf(getPlayerPrestigeNumber(uniqueId)))));
    }

    public static double getRankFinalCost(Rank rank, Player player) {
        Prestige prestige = getPlayerPrestige(player);
        if (!hasPrestige(player)) return rank.getCost();
        return Double.parseDouble(Rina.evaluateMathExpression(plugin.getPrestigeSettings()
                .getIncreaseExpression()
                .replace("{increase_percentage}", String.valueOf(prestige.getCostIncrease()))
                .replace("{rank_cost}", String.valueOf(rank.getCost()))
                .replace("{prestige_number}", String.valueOf(getPlayerPrestigeNumber(player)))));
    }

    public static String getPlayerPathOrDefault(Player player) {
        UUID uuid = UniqueId.getUUID(player);
        if (plugin.getUserController().isLoaded(uuid)) return plugin.getUserController().getUser(uuid).getPathName();
        return RankStorage.getDefaultPath();
    }

    public static User getUser(Player player) {
        return getUser(UniqueId.getUUID(player));
    }

    public static User getUser(UUID uniqueId) {
        return plugin.getUserController().getUser(uniqueId);
    }

}
