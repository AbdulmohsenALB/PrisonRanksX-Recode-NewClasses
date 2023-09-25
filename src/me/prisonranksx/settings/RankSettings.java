package me.prisonranksx.settings;

import me.prisonranksx.components.CommandsComponent;

public class RankSettings extends Settings {

    private boolean removeRankPermissionsOnRankDeletion, removeRankPermissionsOnRankReset;

    private CommandsComponent rankDeleteCommands, rankResetCommands;

    public RankSettings() {
        super("Rank-Options");
        setup();
    }

    @Override
    public void setup() {

        refreshParentSection();

        removeRankPermissionsOnRankDeletion = getBoolean("remove-rank-permissions-on-rank-deletion");
        removeRankPermissionsOnRankReset = getBoolean("remove-rank-permissions-on-rank-reset");

        rankDeleteCommands = CommandsComponent.parseCommands(getStringList("rank-delete-commands"));
        rankResetCommands = CommandsComponent.parseCommands(getStringList("rank-reset-commands"));

    }

    public boolean isRemoveRankPermissionsOnRankDeletion() {
        return removeRankPermissionsOnRankDeletion;
    }

    public void setRemoveRankPermissionsOnRankDeletion(boolean removeRankPermissionsOnRankDeletion) {
        this.removeRankPermissionsOnRankDeletion = removeRankPermissionsOnRankDeletion;
    }

    public boolean isRemoveRankPermissionsOnRankReset() {
        return removeRankPermissionsOnRankReset;
    }

    public void setRemoveRankPermissionsOnRankReset(boolean removeRankPermissionsOnRankReset) {
        this.removeRankPermissionsOnRankReset = removeRankPermissionsOnRankReset;
    }

    public CommandsComponent getRankDeleteCommands() {
        return rankDeleteCommands;
    }

    public void setRankDeleteCommands(CommandsComponent rankDeleteCommands) {
        this.rankDeleteCommands = rankDeleteCommands;
    }

    public CommandsComponent getRankResetCommands() {
        return rankResetCommands;
    }

    public void setRankResetCommands(CommandsComponent rankResetCommands) {
        this.rankResetCommands = rankResetCommands;
    }

}
