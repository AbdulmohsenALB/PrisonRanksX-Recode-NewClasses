package me.prisonranksx.settings;

import com.google.common.collect.Sets;
import me.prisonranksx.bukkitutils.XSound;
import me.prisonranksx.bukkitutils.XSound.Record;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.utils.ToString;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds values of the main configuration file (config.yml) as well as general
 * settings that are not necessarily found in the config into variables
 */
public class GlobalSettings extends Settings {

    @NonConfig
    private boolean mvdwPlaceholderAPILoaded;
    @NonConfig
    private boolean actionUtilLoaded;
    @NonConfig
    private boolean debug;
    @NonConfig
    private boolean terminateMode;
    @NonConfig
    private boolean legacy;
    @NonConfig
    private boolean luckPermsLoaded;
    @NonConfig
    private boolean groupManagerLoaded;
    @NonConfig
    private boolean permissionsEXLoaded;
    @NonConfig
    private boolean vaultLoaded;
    @NonConfig
    private boolean holographicDisplaysLoaded;
    @NonConfig
    private boolean decentHologramsLoaded;
    @NonConfig
    private boolean RGBSupported;
    private boolean rankEnabled;
    private boolean prestigeEnabled;
    private boolean rebirthEnabled;
    private boolean forceSave;
    private boolean actionBarProgress;
    private boolean rankupMaxWarpFilter;
    private boolean vaultGroups;
    @NonConfig
    private boolean hologramsPlugin;
    @NonConfig
    private boolean placeholderAPILoaded;
    private boolean saveOnLeave;
    private boolean checkVault;
    private boolean saveNotification;
    private boolean allowEasterEggs;
    private boolean enabledInsteadOfDisabled;
    private boolean infinitePrestige;
    private boolean forceRankDisplay;
    private boolean forcePrestigeDisplay;
    private boolean forceRebirthDisplay;
    private boolean formatChat;
    private boolean allWorldsBroadcast;
    private boolean sendRankupMsg;
    private boolean sendPrestigeMsg;
    private boolean sendRebirthMsg;
    private boolean sendRankupMaxMsg;
    private boolean guiRankList;
    private boolean guiPrestigeList;
    private boolean guiRebirthList;
    private boolean perRankPermission;
    private boolean rankupMaxBroadcastLastRankOnly;
    private boolean rankupMaxMsgLastRankOnly;
    private boolean rankupMaxRankupMsgLastRankOnly;
    private boolean rankupMaxWithPrestige;
    private boolean actionBarProgressOnlyPickaxe;
    private boolean expBarProgress;
    private boolean autoSave;
    private boolean enableLeaderboard;
    private boolean prestigeMaxPrestigeMsgLastPrestigeOnly;
    private boolean prestigeConfirm, rebirthConfirm;

    private boolean autoRankupAlwaysEnabled;

    private String forceDisplayOrder, noPrestigeDisplay, noRebirthDisplay, vaultGroupsPlugin, actionBarProgressFormat,
            chatEventHandlingPriority, loginEventHandlingPriority, dataStorageType;

    private Record rankupSound, prestigeSound, rebirthSound;

    private int autoRankupDelay, autoPrestigeDelay, autoRebirthDelay, actionBarProgressUpdater, expBarProgressUpdater,
            autoSaveTime, prestigeConfirmTimeOut, rebirthConfirmTimeOut;

    private Set<String> worlds = new HashSet<>();

    /**
     * MC Versions that don't support RGB colors (1.4-1.15)
     */
    @NonConfig
    public static final Set<String> NON_RGB_VERSIONS = Sets.newHashSet("1.15", "1.14", "1.13", "1.12", "1.11", "1.10",
            "1.9", "1.8", "1.7", "1.6", "1.5", "1.4");
    /**
     * MC Versions that don't support UUIDs (1.4-1.6)
     */
    @NonConfig
    public static final Set<String> LEGACY_VERSIONS = Sets.newHashSet("1.6", "1.5", "1.4");

    @NonConfig
    public static final boolean SUPPORTS_ACTION_BAR;

    static {
        boolean supportsActionBar = true;
        for (String version : LEGACY_VERSIONS)
            if (Bukkit.getVersion().contains(version) || Bukkit.getVersion().contains("1.7")) supportsActionBar = false;
        SUPPORTS_ACTION_BAR = supportsActionBar;
    }

    private boolean cmiLoaded;

    /**
     * Loads settings from config.yml upon instance creation.
     */
    public GlobalSettings() {
        super("Options");
        setup();
    }

    public boolean hasPlugin(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    @Override
    public void setup() {

        refreshParentSection();

        // Dependencies
        vaultLoaded = hasPlugin("Vault");
        mvdwPlaceholderAPILoaded = hasPlugin("MVdWPlaceholderAPI");
        placeholderAPILoaded = hasPlugin("PlaceholderAPI");
        luckPermsLoaded = hasPlugin("LuckPerms");
        groupManagerLoaded = hasPlugin("GroupManager");
        permissionsEXLoaded = hasPlugin("PermissionsEX");
        actionUtilLoaded = hasPlugin("ActionUtil");
        holographicDisplaysLoaded = hasPlugin("HolographicDisplays");
        decentHologramsLoaded = hasPlugin("DecentHolograms");
        cmiLoaded = hasPlugin("CMI");

        // Versions
        RGBSupported = NON_RGB_VERSIONS.stream()
                .noneMatch(nonRGBVersion -> Bukkit.getVersion().contains(nonRGBVersion));
        legacy = LEGACY_VERSIONS.stream().anyMatch(legacyVersion -> Bukkit.getVersion().contains(legacyVersion));

        // Outside Options *The Only Exception*
        worlds.clear();
        ConfigManager.getConfig().getStringList("worlds").forEach(worldName -> worlds.add(worldName.toLowerCase()));

        // Booleans
        rankEnabled = getBoolean("rank-enabled");
        prestigeEnabled = getBoolean("prestige-enabled");
        rebirthEnabled = getBoolean("rebirth-enabled");
        forceSave = getBoolean("force-save");
        actionBarProgress = getBoolean("action-bar-progress");
        rankupMaxWarpFilter = getBoolean("rankupmax-warp-filter");
        vaultGroups = getBoolean("rankup-vault-groups");
        hologramsPlugin = holographicDisplaysLoaded || decentHologramsLoaded || cmiLoaded;
        checkVault = getBoolean("rankup-vault-groups-check");
        saveOnLeave = getBoolean("save-on-leave");
        saveNotification = getBoolean("save-notification");
        allowEasterEggs = getBoolean("allow-easter-eggs");
        enabledInsteadOfDisabled = getBoolean("enabled-worlds-instead-of-disabled");
        infinitePrestige = getBoolean("infinite-prestige");
        forceRankDisplay = getBoolean("force-rank-display");
        forcePrestigeDisplay = getBoolean("force-prestige-display");
        forceRebirthDisplay = getBoolean("force-rebirth-display");
        formatChat = getBoolean("format-chat");
        allWorldsBroadcast = getBoolean("all-worlds-broadcast");
        sendRankupMsg = getBoolean("send-rankup-msg");
        sendPrestigeMsg = getBoolean("send-prestige-msg");
        sendRebirthMsg = getBoolean("send-rebirth-msg");
        sendRankupMaxMsg = getBoolean("send-rankup-max-msg");
        guiRankList = getBoolean("gui-ranks-list");
        guiPrestigeList = getBoolean("gui-prestiges-list");
        guiRebirthList = getBoolean("gui-rebirths-list");
        perRankPermission = getBoolean("per-rank-permission");
        rankupMaxBroadcastLastRankOnly = getBoolean("rankupmax-broadcast-last-rank-only");
        rankupMaxMsgLastRankOnly = getBoolean("rankupmax-msg-last-rank-only");
        rankupMaxRankupMsgLastRankOnly = getBoolean("rankupmax-rankup-msg-last-only");
        rankupMaxWithPrestige = getBoolean("rankupmax-with-prestige");
        actionBarProgressOnlyPickaxe = getBoolean("action-bar-progress-only-pickaxe");
        expBarProgress = getBoolean("exp-bar-progress");
        autoSave = getBoolean("auto-save");
        enableLeaderboard = getBoolean("enable-leaderboard");
        prestigeMaxPrestigeMsgLastPrestigeOnly = getBoolean("prestigemax-prestige-msg-last-prestige-only");
        autoRankupAlwaysEnabled = getBoolean("auto-rankup-always-enabled");
        prestigeConfirm = getBoolean("prestige-confirm");
        rebirthConfirm = getBoolean("rebirth-confirm");

        // Strings
        forceDisplayOrder = getString("force-display-order", true);
        noPrestigeDisplay = getString("no-prestige-display", true);
        noRebirthDisplay = getString("no-rebirth-display", true);
        vaultGroupsPlugin = getString("rankup-vault-groups-plugin");
        actionBarProgressFormat = getString("action-bar-progress-format", true);
        chatEventHandlingPriority = getString("chat-event-handling-priority");
        loginEventHandlingPriority = getString("login-event-handling-priority");
        dataStorageType = getString("data-storage-type");

        // Sounds
        String rankupSoundName = getString("rankup-sound-name");
        if (rankupSoundName != null && !rankupSoundName.isEmpty()) {
            rankupSound = new Record(XSound.matchXSound(rankupSoundName).get(), null, null,
                    getFloat("rankup-sound-volume"), getFloat("rankup-sound-pitch"), false);
        }
        String prestigeSoundName = getString("prestige-sound-name");
        if (prestigeSoundName != null && !prestigeSoundName.isEmpty()) {
            prestigeSound = new Record(XSound.matchXSound(prestigeSoundName).get(), null, null,
                    getFloat("prestige-sound-volume"), getFloat("prestige-sound-pitch"), false);
        }
        String rebirthSoundName = getString("rebirth-sound-name");
        if (rebirthSoundName != null && !rebirthSoundName.isEmpty()) {
            rebirthSound = new Record(XSound.matchXSound(rebirthSoundName).get(), null, null,
                    getFloat("rebirth-sound-volume"), getFloat("rebirth-sound-pitch"), false);
        }

        // Integers
        autoRankupDelay = getInt("auto-rankup-delay");
        autoPrestigeDelay = getInt("auto-prestige-delay");
        autoRebirthDelay = getInt("auto-rebirth-delay");
        actionBarProgressUpdater = getInt("action-bar-progress-updater");
        expBarProgressUpdater = getInt("exp-bar-progress-updater");
        autoSaveTime = getInt("auto-save-time");
        prestigeConfirmTimeOut = getInt("prestige-confirm-time-out");
        rebirthConfirmTimeOut = getInt("rebirth-confirm-time-out");
    }

    private @NotNull String recordToString(Record record) {
        if (record == null) return "null";
        return "[XSound.name=" + record.sound.name() + ", Sound.name= + " + record.sound.parseSound().name()
                + ", volume=" + record.volume + ", pitch=" + record.pitch + "]";
    }

    @Override
    public String toString() {
        return ToString.toString(this);
    }

    public boolean isAutoRankupAlwaysEnabled() {
        return autoRankupAlwaysEnabled;
    }

    public void setAutoRankupAlwaysEnabled(boolean autoRankupAlwaysEnabled) {
        this.autoRankupAlwaysEnabled = autoRankupAlwaysEnabled;
    }

    public boolean isLuckPermsLoaded() {
        return luckPermsLoaded;
    }

    public void setLuckPermsLoaded(boolean luckPermsLoaded) {
        this.luckPermsLoaded = luckPermsLoaded;
    }

    public boolean isGroupManagerLoaded() {
        return groupManagerLoaded;
    }

    public void setGroupManagerLoaded(boolean groupManagerLoaded) {
        this.groupManagerLoaded = groupManagerLoaded;
    }

    public boolean isPermissionsEXLoaded() {
        return permissionsEXLoaded;
    }

    public void setPermissionsEXLoaded(boolean permissionsEXLoaded) {
        this.permissionsEXLoaded = permissionsEXLoaded;
    }

    public boolean isVaultLoaded() {
        return vaultLoaded;
    }

    public void setVaultLoaded(boolean vaultLoaded) {
        this.vaultLoaded = vaultLoaded;
    }

    public boolean isHolographicDisplaysLoaded() {
        return holographicDisplaysLoaded;
    }

    public void setHolographicDisplaysLoaded(boolean holographicDisplaysLoaded) {
        this.holographicDisplaysLoaded = holographicDisplaysLoaded;
    }

    public boolean isDecentHologramsLoaded() {
        return decentHologramsLoaded;
    }

    public void setDecentHologramsLoaded(boolean decentHologramsLoaded) {
        this.decentHologramsLoaded = decentHologramsLoaded;
    }

    public void setActionUtilLoaded(boolean actionUtilLoaded) {
        this.actionUtilLoaded = actionUtilLoaded;
    }

    public boolean isMvdwPlaceholderAPILoaded() {
        return mvdwPlaceholderAPILoaded;
    }

    public void setMvdwPlaceholderAPILoaded(boolean mvdwPlaceholderAPILoaded) {
        this.mvdwPlaceholderAPILoaded = mvdwPlaceholderAPILoaded;
    }

    public boolean isActionUtilLoaded() {
        return actionUtilLoaded;
    }

    public void setActionUtil(boolean actionUtil) {
        this.actionUtilLoaded = actionUtil;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isTerminateMode() {
        return terminateMode;
    }

    public void setTerminateMode(boolean terminateMode) {
        this.terminateMode = terminateMode;
    }

    public boolean isLegacy() {
        return legacy;
    }

    public void setLegacy(boolean legacy) {
        this.legacy = legacy;
    }

    public boolean isRankEnabled() {
        return rankEnabled;
    }

    public void setRankEnabled(boolean rankEnabled) {
        this.rankEnabled = rankEnabled;
    }

    public boolean isPrestigeEnabled() {
        return prestigeEnabled;
    }

    public void setPrestigeEnabled(boolean prestigeEnabled) {
        this.prestigeEnabled = prestigeEnabled;
    }

    public boolean isRebirthEnabled() {
        return rebirthEnabled;
    }

    public void setRebirthEnabled(boolean rebirthEnabled) {
        this.rebirthEnabled = rebirthEnabled;
    }

    public boolean isForceSave() {
        return forceSave;
    }

    public void setForceSave(boolean forceSave) {
        this.forceSave = forceSave;
    }

    public boolean isActionBarProgress() {
        return actionBarProgress;
    }

    public void setActionBarProgress(boolean actionBarProgress) {
        this.actionBarProgress = actionBarProgress;
    }

    public boolean isRankupMaxWarpFilter() {
        return rankupMaxWarpFilter;
    }

    public void setRankupMaxWarpFilter(boolean rankupMaxWarpFilter) {
        this.rankupMaxWarpFilter = rankupMaxWarpFilter;
    }

    public boolean isVaultGroups() {
        return vaultGroups;
    }

    public void setVaultGroups(boolean vaultGroups) {
        this.vaultGroups = vaultGroups;
    }

    public boolean isHologramsPlugin() {
        return hologramsPlugin;
    }

    public void setHologramsPlugin(boolean hologramsPlugin) {
        this.hologramsPlugin = hologramsPlugin;
    }

    public boolean isPlaceholderAPILoaded() {
        return placeholderAPILoaded;
    }

    public void setPlaceholderAPILoaded(boolean placeholderAPILoaded) {
        this.placeholderAPILoaded = placeholderAPILoaded;
    }

    public boolean isSaveOnLeave() {
        return saveOnLeave;
    }

    public void setSaveOnLeave(boolean saveOnLeave) {
        this.saveOnLeave = saveOnLeave;
    }

    public boolean isCheckVault() {
        return checkVault;
    }

    public void setCheckVault(boolean checkVault) {
        this.checkVault = checkVault;
    }

    public boolean isSaveNotification() {
        return saveNotification;
    }

    public void setSaveNotification(boolean saveNotification) {
        this.saveNotification = saveNotification;
    }

    public boolean isAllowEasterEggs() {
        return allowEasterEggs;
    }

    public void setAllowEasterEggs(boolean allowEasterEggs) {
        this.allowEasterEggs = allowEasterEggs;
    }

    public boolean isEnabledInsteadOfDisabled() {
        return enabledInsteadOfDisabled;
    }

    public void setEnabledInsteadOfDisabled(boolean enabledInsteadOfDisabled) {
        this.enabledInsteadOfDisabled = enabledInsteadOfDisabled;
    }

    public boolean isInfinitePrestige() {
        return infinitePrestige;
    }

    public void setInfinitePrestige(boolean infinitePrestige) {
        this.infinitePrestige = infinitePrestige;
    }

    public boolean isRankForceDisplay() {
        return forceRankDisplay;
    }

    public void setRankForceDisplay(boolean rankForceDisplay) {
        this.forceRankDisplay = rankForceDisplay;
    }

    public boolean isPrestigeForceDisplay() {
        return forcePrestigeDisplay;
    }

    public void setPrestigeForceDisplay(boolean prestigeForceDisplay) {
        this.forcePrestigeDisplay = prestigeForceDisplay;
    }

    public boolean isRebirthForceDisplay() {
        return forceRebirthDisplay;
    }

    public void setRebirthForceDisplay(boolean rebirthForceDisplay) {
        this.forceRebirthDisplay = rebirthForceDisplay;
    }

    public boolean isFormatChat() {
        return formatChat;
    }

    public void setFormatChat(boolean formatChat) {
        this.formatChat = formatChat;
    }

    public boolean isRGBSupported() {
        return RGBSupported;
    }

    public void setRGBSupported(boolean RGBSupported) {
        this.RGBSupported = RGBSupported;
    }

    public String getForceDisplayOrder() {
        return forceDisplayOrder;
    }

    public void setForceDisplayOrder(String forceDisplayOrder) {
        this.forceDisplayOrder = forceDisplayOrder;
    }

    public String getNoPrestigeDisplay() {
        return noPrestigeDisplay;
    }

    public void setNoPrestigeDisplay(String noPrestigeDisplay) {
        this.noPrestigeDisplay = noPrestigeDisplay;
    }

    public String getNoRebirthDisplay() {
        return noRebirthDisplay;
    }

    public void setNoRebirthDisplay(String noRebirthDisplay) {
        this.noRebirthDisplay = noRebirthDisplay;
    }

    public String getVaultGroupsPlugin() {
        return vaultGroupsPlugin;
    }

    public void setVaultGroupsPlugin(String vaultGroupsPlugin) {
        this.vaultGroupsPlugin = vaultGroupsPlugin;
    }

    public String getActionBarProgressFormat() {
        return actionBarProgressFormat;
    }

    public void setActionBarProgressFormat(String actionBarProgressFormat) {
        this.actionBarProgressFormat = actionBarProgressFormat;
    }

    public String getChatEventHandlingPriority() {
        return chatEventHandlingPriority;
    }

    public void setChatEventHandlingPriority(String chatEventHandlingPriority) {
        this.chatEventHandlingPriority = chatEventHandlingPriority;
    }

    public String getLoginEventHandlingPriority() {
        return loginEventHandlingPriority;
    }

    public void setLoginEventHandlingPriority(String loginEventHandlingPriority) {
        this.loginEventHandlingPriority = loginEventHandlingPriority;
    }

    public Record getPrestigeSound() {
        return prestigeSound;
    }

    public void setPrestigeSound(Record prestigeSound) {
        this.prestigeSound = prestigeSound;
    }

    public Record getRankupSound() {
        return rankupSound;
    }

    public void setRankupSound(Record rankupSound) {
        this.rankupSound = rankupSound;
    }

    public Record getRebirthSound() {
        return rebirthSound;
    }

    public void setRebirthSound(Record rebirthSound) {
        this.rebirthSound = rebirthSound;
    }

    public int getAutoRankupDelay() {
        return autoRankupDelay;
    }

    public void setAutoRankupDelay(int autoRankupDelay) {
        this.autoRankupDelay = autoRankupDelay;
    }

    public int getAutoPrestigeDelay() {
        return autoPrestigeDelay;
    }

    public void setAutoPrestigeDelay(int autoPrestigeDelay) {
        this.autoPrestigeDelay = autoPrestigeDelay;
    }

    public int getAutoRebirthDelay() {
        return autoRebirthDelay;
    }

    public void setAutoRebirthDelay(int autoRebirthDelay) {
        this.autoRebirthDelay = autoRebirthDelay;
    }

    public boolean isForceRankDisplay() {
        return forceRankDisplay;
    }

    public void setForceRankDisplay(boolean forceRankDisplay) {
        this.forceRankDisplay = forceRankDisplay;
    }

    public boolean isForcePrestigeDisplay() {
        return forcePrestigeDisplay;
    }

    public void setForcePrestigeDisplay(boolean forcePrestigeDisplay) {
        this.forcePrestigeDisplay = forcePrestigeDisplay;
    }

    public boolean isForceRebirthDisplay() {
        return forceRebirthDisplay;
    }

    public void setForceRebirthDisplay(boolean forceRebirthDisplay) {
        this.forceRebirthDisplay = forceRebirthDisplay;
    }

    public boolean isAllWorldsBroadcast() {
        return allWorldsBroadcast;
    }

    public void setAllWorldsBroadcast(boolean allWorldsBroadcast) {
        this.allWorldsBroadcast = allWorldsBroadcast;
    }

    public boolean isSendRankupMsg() {
        return sendRankupMsg;
    }

    public void setSendRankupMsg(boolean sendRankupMsg) {
        this.sendRankupMsg = sendRankupMsg;
    }

    public boolean isSendPrestigeMsg() {
        return sendPrestigeMsg;
    }

    public void setSendPrestigeMsg(boolean sendPrestigeMsg) {
        this.sendPrestigeMsg = sendPrestigeMsg;
    }

    public boolean isSendRebirthMsg() {
        return sendRebirthMsg;
    }

    public void setSendRebirthMsg(boolean sendRebirthMsg) {
        this.sendRebirthMsg = sendRebirthMsg;
    }

    public boolean isSendRankupMaxMsg() {
        return sendRankupMaxMsg;
    }

    public void setSendRankupMaxMsg(boolean sendRankupMaxMsg) {
        this.sendRankupMaxMsg = sendRankupMaxMsg;
    }

    public boolean isGuiRankList() {
        return guiRankList;
    }

    public void setGuiRankList(boolean guiRankList) {
        this.guiRankList = guiRankList;
    }

    public boolean isGuiPrestigeList() {
        return guiPrestigeList;
    }

    public void setGuiPrestigeList(boolean guiPrestigeList) {
        this.guiPrestigeList = guiPrestigeList;
    }

    public boolean isGuiRebirthList() {
        return guiRebirthList;
    }

    public void setGuiRebirthList(boolean guiRebirthList) {
        this.guiRebirthList = guiRebirthList;
    }

    public boolean isPerRankPermission() {
        return perRankPermission;
    }

    public void setPerRankPermission(boolean perRankPermission) {
        this.perRankPermission = perRankPermission;
    }

    public boolean isRankupMaxBroadcastLastRankOnly() {
        return rankupMaxBroadcastLastRankOnly;
    }

    public void setRankupMaxBroadcastLastRankOnly(boolean rankupMaxBroadcastLastRankOnly) {
        this.rankupMaxBroadcastLastRankOnly = rankupMaxBroadcastLastRankOnly;
    }

    public boolean isRankupMaxMsgLastRankOnly() {
        return rankupMaxMsgLastRankOnly;
    }

    public void setRankupMaxMsgLastRankOnly(boolean rankupMaxMsgLastRankOnly) {
        this.rankupMaxMsgLastRankOnly = rankupMaxMsgLastRankOnly;
    }

    public boolean isRankupMaxRankupMsgLastRankOnly() {
        return rankupMaxRankupMsgLastRankOnly;
    }

    public void setRankupMaxRankupMsgLastRankOnly(boolean rankupMaxRankupMsgLastRankOnly) {
        this.rankupMaxRankupMsgLastRankOnly = rankupMaxRankupMsgLastRankOnly;
    }

    public boolean isRankupMaxWithPrestige() {
        return rankupMaxWithPrestige;
    }

    public void setRankupMaxWithPrestige(boolean rankupMaxWithPrestige) {
        this.rankupMaxWithPrestige = rankupMaxWithPrestige;
    }

    public boolean isActionBarProgressOnlyPickaxe() {
        return actionBarProgressOnlyPickaxe;
    }

    public void setActionBarProgressOnlyPickaxe(boolean actionBarProgressOnlyPickaxe) {
        this.actionBarProgressOnlyPickaxe = actionBarProgressOnlyPickaxe;
    }

    public boolean isExpBarProgress() {
        return expBarProgress;
    }

    public void setExpBarProgress(boolean expBarProgress) {
        this.expBarProgress = expBarProgress;
    }

    public boolean isAutoSave() {
        return autoSave;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    public boolean isEnableLeaderboard() {
        return enableLeaderboard;
    }

    public void setEnableLeaderboard(boolean enableLeaderboard) {
        this.enableLeaderboard = enableLeaderboard;
    }

    public boolean isPrestigeMaxPrestigeMsgLastPrestigeOnly() {
        return prestigeMaxPrestigeMsgLastPrestigeOnly;
    }

    public void setPrestigeMaxPrestigeMsgLastPrestigeOnly(boolean prestigeMaxPrestigeMsgLastPrestigeOnly) {
        this.prestigeMaxPrestigeMsgLastPrestigeOnly = prestigeMaxPrestigeMsgLastPrestigeOnly;
    }

    public int getActionBarProgressUpdater() {
        return actionBarProgressUpdater;
    }

    public void setActionBarProgressUpdater(int actionBarProgressUpdater) {
        this.actionBarProgressUpdater = actionBarProgressUpdater;
    }

    public int getExpBarProgressUpdater() {
        return expBarProgressUpdater;
    }

    public void setExpBarProgressUpdater(int expBarProgressUpdater) {
        this.expBarProgressUpdater = expBarProgressUpdater;
    }

    public int getAutoSaveTime() {
        return autoSaveTime;
    }

    public void setAutoSaveTime(int autoSaveTime) {
        this.autoSaveTime = autoSaveTime;
    }

    public Set<String> getWorlds() {
        return worlds;
    }

    public void setWorlds(Set<String> worlds) {
        this.worlds = worlds;
    }

    public boolean isWorldIncluded(String worldName) {
        return worlds.contains(worldName.toLowerCase()) != enabledInsteadOfDisabled;
    }

    public boolean isWorldIncluded(World world) {
        return isWorldIncluded(world.getName());
    }

    public String getDataStorageType() {
        return dataStorageType;
    }

    public void setDataStorageType(String dataStorageType) {
        this.dataStorageType = dataStorageType;
    }

    public boolean isPrestigeConfirm() {
        return prestigeConfirm;
    }

    public void setPrestigeConfirm(boolean prestigeConfirm) {
        this.prestigeConfirm = prestigeConfirm;
    }

    public boolean isRebirthConfirm() {
        return rebirthConfirm;
    }

    public void setRebirthConfirm(boolean rebirthConfirm) {
        this.rebirthConfirm = rebirthConfirm;
    }

    public int getPrestigeConfirmTimeOut() {
        return prestigeConfirmTimeOut;
    }

    public void setPrestigeConfirmTimeOut(int prestigeConfirmTimeOut) {
        this.prestigeConfirmTimeOut = prestigeConfirmTimeOut;
    }

    public int getRebirthConfirmTimeOut() {
        return rebirthConfirmTimeOut;
    }

    public void setRebirthConfirmTimeOut(int rebirthConfirmTimeOut) {
        this.rebirthConfirmTimeOut = rebirthConfirmTimeOut;
    }

    public boolean isForceDisplay() {
        return forceRankDisplay || forcePrestigeDisplay || forceRebirthDisplay;
    }

    public boolean isCmiLoaded() {
        return cmiLoaded;
    }

    public void setCmiLoaded(boolean cmiLoaded) {
        this.cmiLoaded = cmiLoaded;
    }

}
