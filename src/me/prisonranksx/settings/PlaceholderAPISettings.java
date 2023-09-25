package me.prisonranksx.settings;

import me.prisonranksx.bukkitutils.MCProgressBar;

public class PlaceholderAPISettings extends Settings {

    private String nextRankProgressBarStyle, nextRankProgressBarFilled, nextRankProgressBarNeeded,
            nextRankProgressBarFull, nextRankProgressBarLastRank, nextRankPercentageLastRank, nextRankCostLastRank,
            nextRankLastRank, currentRankLastRank, prestigeLastPrestige, prestigeNoPrestige, nextPrestigeNoPrestige,
            currencySymbol, percentSign, rebirthNoRebirth, nextRebirthNoRebirth, rebirthLastRebirth,
            nextProgressBarStyleRankup, nextProgressBarStylePrestige, nextProgressBarStyleRebirth,
            nextProgressBarFilledRankup, nextProgressBarFilledPrestige, nextProgressBarFilledRebirth,
            nextProgressBarNeededRankup, nextProgressBarNeededPrestige, nextProgressBarNeededRebirth,
            nextProgressBarFullIsRankup, nextProgressBarFullIsPrestige, nextProgressBarFullIsRebirth,
            nextProgressBarFullIsLast, leaderboardNameRankNull, leaderboardValueRankNull, leaderboardNamePrestigeNull,
            leaderboardValuePrestigeNull, leaderboardNameRebirthNull, leaderboardValueRebirthNull;

    private boolean nextRankProgressBarFullEnabled, currentRankLastRankEnabled, currencySymbolBehind, percentSignBehind,
            nextProgressBarFullIsRankupEnabled, nextProgressBarFullIsPrestigeEnabled,
            nextProgressBarFullIsRebirthEnabled, nextProgressBarFullIsLastEnabled;

    private MCProgressBar nextRankProgressBar, nextRankProgressBarExtra, nextProgressBarRankup,
            nextProgressBarRankupExtra, nextProgressBarPrestige, nextProgressBarPrestigeExtra, nextProgressBarRebirth,
            nextProgressBarRebirthExtra;

    public PlaceholderAPISettings() {
        super("PlaceholderAPI-Options");
        setup();
    }

    @Override
    public void setup() {

        refreshParentSection();

        nextRankProgressBarStyle = getString("next-rank-progress-bar-style", true);
        nextRankProgressBarFilled = getString("next-rank-progress-bar-filled", true);
        nextRankProgressBarNeeded = getString("next-rank-progress-bar-needed", true);
        nextRankProgressBarFull = getString("next-rank-progress-bar-full", true);
        nextRankProgressBarLastRank = getString("next-rank-progress-bar-last-rank", true);
        nextRankPercentageLastRank = getString("next-rank-percentage-last-rank", true);
        nextRankCostLastRank = getString("next-rank-cost-last-rank", true);
        nextRankLastRank = getString("next-rank-last-rank", true);
        currentRankLastRank = getString("current-rank-last-rank", true);
        prestigeLastPrestige = getString("prestige-last-prestige", true);
        prestigeNoPrestige = getString("prestige-no-prestige", true);
        nextPrestigeNoPrestige = getString("next-prestige-no-prestige", true);
        currencySymbol = getString("currency-symbol", true);
        percentSign = getString("percent-sign", true);
        rebirthNoRebirth = getString("rebirth-no-rebirth", true);
        nextRebirthNoRebirth = getString("next-rebirth-no-rebirth", true);
        rebirthLastRebirth = getString("rebirth-last-rebirth", true);
        nextProgressBarStyleRankup = getString("next-progress-bar-style.rankup", true);
        nextProgressBarStylePrestige = getString("next-progress-bar-style.prestige", true);
        nextProgressBarStyleRebirth = getString("next-progress-bar-style.rebirth", true);
        nextProgressBarFilledRankup = getString("next-progress-bar-filled.rankup", true);
        nextProgressBarFilledPrestige = getString("next-progress-bar-filled.prestige", true);
        nextProgressBarFilledRebirth = getString("next-progress-bar-filled.rebirth", true);
        nextProgressBarNeededRankup = getString("next-progress-bar-needed.rankup", true);
        nextProgressBarNeededPrestige = getString("next-progress-bar-needed.prestige", true);
        nextProgressBarNeededRebirth = getString("next-progress-bar-needed.rebirth", true);
        nextProgressBarFullIsRankup = getString("next-progress-bar-full-is-rankup", true);
        nextProgressBarFullIsPrestige = getString("next-progress-bar-full-is-prestige", true);
        nextProgressBarFullIsRebirth = getString("next-progress-bar-full-is-rebirth", true);
        nextProgressBarFullIsLast = getString("next-progress-bar-full-is-last", true);
        leaderboardNameRankNull = getString("leaderboard-name-rank-null", true);
        leaderboardValueRankNull = getString("leaderboard-value-rank-null", true);
        leaderboardNamePrestigeNull = getString("leaderboard-name-prestige-null", true);
        leaderboardValuePrestigeNull = getString("leaderboard-value-prestige-null", true);
        leaderboardNameRebirthNull = getString("leaderboard-name-rebirth-null", true);
        leaderboardValueRebirthNull = getString("leaderboard-value-rebirth-null", true);

        nextRankProgressBarFullEnabled = getBoolean("next-rank-progress-bar-full-enabled");
        currentRankLastRankEnabled = getBoolean("current-rank-last-rank-enabled");
        currencySymbolBehind = getBoolean("currency-symbol-behind");
        percentSignBehind = getBoolean("percent-sign-behind");
        nextProgressBarFullIsRankupEnabled = getBoolean("next-progress-bar-full-is-rankup-enabled");
        nextProgressBarFullIsPrestigeEnabled = getBoolean("next-progress-bar-full-is-prestige-enabled");
        nextProgressBarFullIsRebirthEnabled = getBoolean("next-progress-bar-full-is-rebirth-enabled");
        nextProgressBarFullIsLastEnabled = getBoolean("next-progress-bar-full-is-last-enabled");

        nextRankProgressBar = new MCProgressBar(getInt("next-rank-progress-bar-size"), nextRankProgressBarStyle,
                nextRankProgressBarFilled, nextRankProgressBarNeeded);
        nextRankProgressBarExtra = new MCProgressBar(getInt("next-rank-progress-bar-extra-size"),
                nextRankProgressBarStyle, nextRankProgressBarFilled, nextRankProgressBarNeeded);
        nextProgressBarRankup = new MCProgressBar(getInt("next-progress-bar-size"), nextProgressBarStyleRankup,
                nextProgressBarFilledRankup, nextProgressBarNeededRankup);
        nextProgressBarRankupExtra = new MCProgressBar(getInt("next-progress-bar-extra-size"),
                nextProgressBarStyleRankup, nextProgressBarFilledRankup, nextProgressBarNeededRankup);
        nextProgressBarPrestige = new MCProgressBar(getInt("next-progress-bar-size"), nextProgressBarStylePrestige,
                nextProgressBarFilledPrestige, nextProgressBarNeededPrestige);
        nextProgressBarPrestigeExtra = new MCProgressBar(getInt("next-progress-bar-extra-size"),
                nextProgressBarStylePrestige, nextProgressBarFilledPrestige, nextProgressBarNeededPrestige);
        nextProgressBarRebirth = new MCProgressBar(getInt("next-progress-bar-size"), nextProgressBarStyleRebirth,
                nextProgressBarFilledRebirth, nextProgressBarNeededRebirth);
        nextProgressBarRebirthExtra = new MCProgressBar(getInt("next-progress-bar-extra-size"),
                nextProgressBarStyleRebirth, nextProgressBarFilledRebirth, nextProgressBarNeededRebirth);
    }

    public String getNextRankProgressBarStyle() {
        return nextRankProgressBarStyle;
    }

    public void setNextRankProgressBarStyle(String nextRankProgressBarStyle) {
        this.nextRankProgressBarStyle = nextRankProgressBarStyle;
    }

    public String getNextRankProgressBarFilled() {
        return nextRankProgressBarFilled;
    }

    public void setNextRankProgressBarFilled(String nextRankProgressBarFilled) {
        this.nextRankProgressBarFilled = nextRankProgressBarFilled;
    }

    public String getNextRankProgressBarNeeded() {
        return nextRankProgressBarNeeded;
    }

    public void setNextRankProgressBarNeeded(String nextRankProgressBarNeeded) {
        this.nextRankProgressBarNeeded = nextRankProgressBarNeeded;
    }

    public String getNextRankProgressBarFull() {
        return nextRankProgressBarFull;
    }

    public void setNextRankProgressBarFull(String nextRankProgressBarFull) {
        this.nextRankProgressBarFull = nextRankProgressBarFull;
    }

    public String getNextRankProgressBarLastRank() {
        return nextRankProgressBarLastRank;
    }

    public void setNextRankProgressBarLastRank(String nextRankProgressBarLastRank) {
        this.nextRankProgressBarLastRank = nextRankProgressBarLastRank;
    }

    public String getNextRankPercentageLastRank() {
        return nextRankPercentageLastRank;
    }

    public void setNextRankPercentageLastRank(String nextRankPercentageLastRank) {
        this.nextRankPercentageLastRank = nextRankPercentageLastRank;
    }

    public String getNextRankCostLastRank() {
        return nextRankCostLastRank;
    }

    public void setNextRankCostLastRank(String nextRankCostLastRank) {
        this.nextRankCostLastRank = nextRankCostLastRank;
    }

    public String getNextRankLastRank() {
        return nextRankLastRank;
    }

    public void setNextRankLastRank(String nextRankLastRank) {
        this.nextRankLastRank = nextRankLastRank;
    }

    public String getCurrentRankLastRank() {
        return currentRankLastRank;
    }

    public void setCurrentRankLastRank(String currentRankLastRank) {
        this.currentRankLastRank = currentRankLastRank;
    }

    public String getPrestigeLastPrestige() {
        return prestigeLastPrestige;
    }

    public void setPrestigeLastPrestige(String prestigeLastPrestige) {
        this.prestigeLastPrestige = prestigeLastPrestige;
    }

    public String getPrestigeNoPrestige() {
        return prestigeNoPrestige;
    }

    public void setPrestigeNoPrestige(String prestigeNoPrestige) {
        this.prestigeNoPrestige = prestigeNoPrestige;
    }

    public String getNextPrestigeNoPrestige() {
        return nextPrestigeNoPrestige;
    }

    public void setNextPrestigeNoPrestige(String nextPrestigeNoPrestige) {
        this.nextPrestigeNoPrestige = nextPrestigeNoPrestige;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getPercentSign() {
        return percentSign;
    }

    public void setPercentSign(String percentSign) {
        this.percentSign = percentSign;
    }

    public String getRebirthNoRebirth() {
        return rebirthNoRebirth;
    }

    public void setRebirthNoRebirth(String rebirthNoRebirth) {
        this.rebirthNoRebirth = rebirthNoRebirth;
    }

    public String getNextRebirthNoRebirth() {
        return nextRebirthNoRebirth;
    }

    public void setNextRebirthNoRebirth(String nextRebirthNoRebirth) {
        this.nextRebirthNoRebirth = nextRebirthNoRebirth;
    }

    public String getRebirthLastRebirth() {
        return rebirthLastRebirth;
    }

    public void setRebirthLastRebirth(String rebirthLastRebirth) {
        this.rebirthLastRebirth = rebirthLastRebirth;
    }

    public String getNextProgressBarStyleRankup() {
        return nextProgressBarStyleRankup;
    }

    public void setNextProgressBarStyleRankup(String nextProgressBarStyleRankup) {
        this.nextProgressBarStyleRankup = nextProgressBarStyleRankup;
    }

    public String getNextProgressBarStylePrestige() {
        return nextProgressBarStylePrestige;
    }

    public void setNextProgressBarStylePrestige(String nextProgressBarStylePrestige) {
        this.nextProgressBarStylePrestige = nextProgressBarStylePrestige;
    }

    public String getNextProgressBarStyleRebirth() {
        return nextProgressBarStyleRebirth;
    }

    public void setNextProgressBarStyleRebirth(String nextProgressBarStyleRebirth) {
        this.nextProgressBarStyleRebirth = nextProgressBarStyleRebirth;
    }

    public String getNextProgressBarFilledRankup() {
        return nextProgressBarFilledRankup;
    }

    public void setNextProgressBarFilledRankup(String nextProgressBarFilledRankup) {
        this.nextProgressBarFilledRankup = nextProgressBarFilledRankup;
    }

    public String getNextProgressBarFilledPrestige() {
        return nextProgressBarFilledPrestige;
    }

    public void setNextProgressBarFilledPrestige(String nextProgressBarFilledPrestige) {
        this.nextProgressBarFilledPrestige = nextProgressBarFilledPrestige;
    }

    public String getNextProgressBarFilledRebirth() {
        return nextProgressBarFilledRebirth;
    }

    public void setNextProgressBarFilledRebirth(String nextProgressBarFilledRebirth) {
        this.nextProgressBarFilledRebirth = nextProgressBarFilledRebirth;
    }

    public String getNextProgressBarNeededRankup() {
        return nextProgressBarNeededRankup;
    }

    public void setNextProgressBarNeededRankup(String nextProgressBarNeededRankup) {
        this.nextProgressBarNeededRankup = nextProgressBarNeededRankup;
    }

    public String getNextProgressBarNeededPrestige() {
        return nextProgressBarNeededPrestige;
    }

    public void setNextProgressBarNeededPrestige(String nextProgressBarNeededPrestige) {
        this.nextProgressBarNeededPrestige = nextProgressBarNeededPrestige;
    }

    public String getNextProgressBarNeededRebirth() {
        return nextProgressBarNeededRebirth;
    }

    public void setNextProgressBarNeededRebirth(String nextProgressBarNeededRebirth) {
        this.nextProgressBarNeededRebirth = nextProgressBarNeededRebirth;
    }

    public String getNextProgressBarFullIsRankup() {
        return nextProgressBarFullIsRankup;
    }

    public void setNextProgressBarFullIsRankup(String nextProgressBarFullIsRankup) {
        this.nextProgressBarFullIsRankup = nextProgressBarFullIsRankup;
    }

    public String getNextProgressBarFullIsPrestige() {
        return nextProgressBarFullIsPrestige;
    }

    public void setNextProgressBarFullIsPrestige(String nextProgressBarFullIsPrestige) {
        this.nextProgressBarFullIsPrestige = nextProgressBarFullIsPrestige;
    }

    public String getNextProgressBarFullIsRebirth() {
        return nextProgressBarFullIsRebirth;
    }

    public void setNextProgressBarFullIsRebirth(String nextProgressBarFullIsRebirth) {
        this.nextProgressBarFullIsRebirth = nextProgressBarFullIsRebirth;
    }

    public String getNextProgressBarFullIsLast() {
        return nextProgressBarFullIsLast;
    }

    public void setNextProgressBarFullIsLast(String nextProgressBarFullIsLast) {
        this.nextProgressBarFullIsLast = nextProgressBarFullIsLast;
    }

    public String getLeaderboardNameRankNull() {
        return leaderboardNameRankNull;
    }

    public void setLeaderboardNameRankNull(String leaderboardNameRankNull) {
        this.leaderboardNameRankNull = leaderboardNameRankNull;
    }

    public String getLeaderboardValueRankNull() {
        return leaderboardValueRankNull;
    }

    public void setLeaderboardValueRankNull(String leaderboardValueRankNull) {
        this.leaderboardValueRankNull = leaderboardValueRankNull;
    }

    public String getLeaderboardNamePrestigeNull() {
        return leaderboardNamePrestigeNull;
    }

    public void setLeaderboardNamePrestigeNull(String leaderboardNamePrestigeNull) {
        this.leaderboardNamePrestigeNull = leaderboardNamePrestigeNull;
    }

    public String getLeaderboardValuePrestigeNull() {
        return leaderboardValuePrestigeNull;
    }

    public void setLeaderboardValuePrestigeNull(String leaderboardValuePrestigeNull) {
        this.leaderboardValuePrestigeNull = leaderboardValuePrestigeNull;
    }

    public String getLeaderboardNameRebirthNull() {
        return leaderboardNameRebirthNull;
    }

    public void setLeaderboardNameRebirthNull(String leaderboardNameRebirthNull) {
        this.leaderboardNameRebirthNull = leaderboardNameRebirthNull;
    }

    public String getLeaderboardValueRebirthNull() {
        return leaderboardValueRebirthNull;
    }

    public void setLeaderboardValueRebirthNull(String leaderboardValueRebirthNull) {
        this.leaderboardValueRebirthNull = leaderboardValueRebirthNull;
    }

    public boolean isNextRankProgressBarFullEnabled() {
        return nextRankProgressBarFullEnabled;
    }

    public void setNextRankProgressBarFullEnabled(boolean nextRankProgressBarFullEnabled) {
        this.nextRankProgressBarFullEnabled = nextRankProgressBarFullEnabled;
    }

    public boolean isCurrentRankLastRankEnabled() {
        return currentRankLastRankEnabled;
    }

    public void setCurrentRankLastRankEnabled(boolean currentRankLastRankEnabled) {
        this.currentRankLastRankEnabled = currentRankLastRankEnabled;
    }

    public boolean isCurrencySymbolBehind() {
        return currencySymbolBehind;
    }

    public void setCurrencySymbolBehind(boolean currencySymbolBehind) {
        this.currencySymbolBehind = currencySymbolBehind;
    }

    public boolean isPercentSignBehind() {
        return percentSignBehind;
    }

    public void setPercentSignBehind(boolean percentSignBehind) {
        this.percentSignBehind = percentSignBehind;
    }

    public boolean isNextProgressBarFullIsRankupEnabled() {
        return nextProgressBarFullIsRankupEnabled;
    }

    public void setNextProgressBarFullIsRankupEnabled(boolean nextProgressBarFullIsRankupEnabled) {
        this.nextProgressBarFullIsRankupEnabled = nextProgressBarFullIsRankupEnabled;
    }

    public boolean isNextProgressBarFullIsPrestigeEnabled() {
        return nextProgressBarFullIsPrestigeEnabled;
    }

    public void setNextProgressBarFullIsPrestigeEnabled(boolean nextProgressBarFullIsPrestigeEnabled) {
        this.nextProgressBarFullIsPrestigeEnabled = nextProgressBarFullIsPrestigeEnabled;
    }

    public boolean isNextProgressBarFullIsRebirthEnabled() {
        return nextProgressBarFullIsRebirthEnabled;
    }

    public void setNextProgressBarFullIsRebirthEnabled(boolean nextProgressBarFullIsRebirthEnabled) {
        this.nextProgressBarFullIsRebirthEnabled = nextProgressBarFullIsRebirthEnabled;
    }

    public boolean isNextProgressBarFullIsLastEnabled() {
        return nextProgressBarFullIsLastEnabled;
    }

    public void setNextProgressBarFullIsLastEnabled(boolean nextProgressBarFullIsLastEnabled) {
        this.nextProgressBarFullIsLastEnabled = nextProgressBarFullIsLastEnabled;
    }

    public MCProgressBar getNextRankProgressBar() {
        return nextRankProgressBar;
    }

    public void setNextRankProgressBar(MCProgressBar nextRankProgressBar) {
        this.nextRankProgressBar = nextRankProgressBar;
    }

    public MCProgressBar getNextRankProgressBarExtra() {
        return nextRankProgressBarExtra;
    }

    public void setNextRankProgressBarExtra(MCProgressBar nextRankProgressBarExtra) {
        this.nextRankProgressBarExtra = nextRankProgressBarExtra;
    }

    public MCProgressBar getNextProgressBarRankup() {
        return nextProgressBarRankup;
    }

    public void setNextProgressBarRankup(MCProgressBar nextProgressBarRankup) {
        this.nextProgressBarRankup = nextProgressBarRankup;
    }

    public MCProgressBar getNextProgressBarRankupExtra() {
        return nextProgressBarRankupExtra;
    }

    public void setNextProgressBarRankupExtra(MCProgressBar nextProgressBarRankupExtra) {
        this.nextProgressBarRankupExtra = nextProgressBarRankupExtra;
    }

    public MCProgressBar getNextProgressBarPrestige() {
        return nextProgressBarPrestige;
    }

    public void setNextProgressBarPrestige(MCProgressBar nextProgressBarPrestige) {
        this.nextProgressBarPrestige = nextProgressBarPrestige;
    }

    public MCProgressBar getNextProgressBarPrestigeExtra() {
        return nextProgressBarPrestigeExtra;
    }

    public void setNextProgressBarPrestigeExtra(MCProgressBar nextProgressBarPrestigeExtra) {
        this.nextProgressBarPrestigeExtra = nextProgressBarPrestigeExtra;
    }

    public MCProgressBar getNextProgressBarRebirth() {
        return nextProgressBarRebirth;
    }

    public void setNextProgressBarRebirth(MCProgressBar nextProgressBarRebirth) {
        this.nextProgressBarRebirth = nextProgressBarRebirth;
    }

    public MCProgressBar getNextProgressBarRebirthExtra() {
        return nextProgressBarRebirthExtra;
    }

    public void setNextProgressBarRebirthExtra(MCProgressBar nextProgressBarRebirthExtra) {
        this.nextProgressBarRebirthExtra = nextProgressBarRebirthExtra;
    }

}
