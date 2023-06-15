package me.prisonranksx.settings;

import me.prisonranksx.components.CommandsComponent;

public class PrestigeSettings extends AbstractSettings {

	private boolean resetMoney, resetRank, deleteRank, removePrestigePermissionsOnPrestigeDeletion,
			removePrestigePermissionsOnPrestigeReset;

	private double rankCostIncreasePercentage;

	private String increaseExpression;

	private CommandsComponent prestigeCommands, prestigeDeleteCommands, prestigeResetCommands;

	public PrestigeSettings() {
		super("Prestige-Options");
		setup();
	}

	@Override
	public void setup() {

		refreshParentSection();

		resetMoney = getBoolean("reset-money");
		resetRank = getBoolean("reset-rank");
		deleteRank = getBoolean("delete-rank");
		removePrestigePermissionsOnPrestigeDeletion = getBoolean("remove-prestige-permissions-on-prestige-deletion");
		removePrestigePermissionsOnPrestigeReset = getBoolean("remove-prestige-permissions-on-prestige-reset");

		rankCostIncreasePercentage = getDouble("rank-cost-increase-percentage");

		increaseExpression = getString("increase-expression");

		prestigeCommands = CommandsComponent.parseCommands(getStringList("prestige-commands"));
		prestigeDeleteCommands = CommandsComponent.parseCommands(getStringList("prestige-delete-commands"));
		prestigeResetCommands = CommandsComponent.parseCommands(getStringList("prestige-reset-commands"));

	}

	public boolean isResetMoney() {
		return resetMoney;
	}

	public void setResetMoney(boolean resetMoney) {
		this.resetMoney = resetMoney;
	}

	public boolean isResetRank() {
		return resetRank;
	}

	public void setResetRank(boolean resetRank) {
		this.resetRank = resetRank;
	}

	public boolean isDeleteRank() {
		return deleteRank;
	}

	public void setDeleteRank(boolean deleteRank) {
		this.deleteRank = deleteRank;
	}

	public boolean isRemovePrestigePermissionsOnPrestigeDeletion() {
		return removePrestigePermissionsOnPrestigeDeletion;
	}

	public void setRemovePrestigePermissionsOnPrestigeDeletion(boolean removePrestigePermissionsOnPrestigeDeletion) {
		this.removePrestigePermissionsOnPrestigeDeletion = removePrestigePermissionsOnPrestigeDeletion;
	}

	public boolean isRemovePrestigePermissionsOnPrestigeReset() {
		return removePrestigePermissionsOnPrestigeReset;
	}

	public void setRemovePrestigePermissionsOnPrestigeReset(boolean removePrestigePermissionsOnPrestigeReset) {
		this.removePrestigePermissionsOnPrestigeReset = removePrestigePermissionsOnPrestigeReset;
	}

	public double getRankCostIncreasePercentage() {
		return rankCostIncreasePercentage;
	}

	public void setRankCostIncreasePercentage(double rankCostIncreasePercentage) {
		this.rankCostIncreasePercentage = rankCostIncreasePercentage;
	}

	public String getIncreaseExpression() {
		return increaseExpression;
	}

	public void setIncreaseExpression(String increaseExpression) {
		this.increaseExpression = increaseExpression;
	}

	public CommandsComponent getPrestigeCommands() {
		return prestigeCommands;
	}

	public void setPrestigeCommands(CommandsComponent prestigeCommands) {
		this.prestigeCommands = prestigeCommands;
	}

	public CommandsComponent getPrestigeDeleteCommands() {
		return prestigeDeleteCommands;
	}

	public void setPrestigeDeleteCommands(CommandsComponent prestigeDeleteCommands) {
		this.prestigeDeleteCommands = prestigeDeleteCommands;
	}

	public CommandsComponent getPrestigeResetCommands() {
		return prestigeResetCommands;
	}

	public void setPrestigeResetCommands(CommandsComponent prestigeResetCommands) {
		this.prestigeResetCommands = prestigeResetCommands;
	}

}
