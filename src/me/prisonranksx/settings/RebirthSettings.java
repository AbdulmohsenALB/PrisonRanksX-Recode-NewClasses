package me.prisonranksx.settings;

import me.prisonranksx.components.CommandsComponent;

public class RebirthSettings extends AbstractSettings {

	private boolean resetMoney, resetRank, resetPrestige, deletePrestige, removeRankPermissionsOnRebirth,
			removePrestigePermissionsOnRebirth, removeRebirthPermissionsOnRebirthDeletion,
			removeRebirthPermissionsOnRebirthReset;

	private double prestigeCostIncreasePercentage;

	private String increaseExpression;

	private CommandsComponent rebirthCommands, rebirthDeleteCommands;

	public RebirthSettings() {
		super("Rebirth-Options");
		setup();
	}

	@Override
	public void setup() {

		refreshParentSection();

		resetMoney = getBoolean("reset-money");
		resetRank = getBoolean("reset-rank");
		resetPrestige = getBoolean("reset-prestige");
		deletePrestige = getBoolean("delete-prestige");
		removeRankPermissionsOnRebirth = getBoolean("remove-rank-permissions-on-rebirth");
		removePrestigePermissionsOnRebirth = getBoolean("remove-prestige-permissions-on-rebirth");
		removeRebirthPermissionsOnRebirthDeletion = getBoolean("remove-rebirth-permissions-on-rebirth-deletion");
		removeRebirthPermissionsOnRebirthReset = getBoolean("remove-rebirth-permissions-on-rebirth-reset");

		prestigeCostIncreasePercentage = getDouble("prestige-cost-increase-percentage");

		increaseExpression = getString("increase-expression");

		rebirthCommands = CommandsComponent.parseCommands(getStringList("rebirth-commands"));
		rebirthDeleteCommands = CommandsComponent.parseCommands(getStringList("rebirth-delete-commands"));

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

	public boolean isResetPrestige() {
		return resetPrestige;
	}

	public void setResetPrestige(boolean resetPrestige) {
		this.resetPrestige = resetPrestige;
	}

	public boolean isDeletePrestige() {
		return deletePrestige;
	}

	public void setDeletePrestige(boolean deletePrestige) {
		this.deletePrestige = deletePrestige;
	}

	public boolean isRemoveRankPermissionsOnRebirth() {
		return removeRankPermissionsOnRebirth;
	}

	public void setRemoveRankPermissionsOnRebirth(boolean removeRankPermissionsOnRebirth) {
		this.removeRankPermissionsOnRebirth = removeRankPermissionsOnRebirth;
	}

	public boolean isRemovePrestigePermissionsOnRebirth() {
		return removePrestigePermissionsOnRebirth;
	}

	public void setRemovePrestigePermissionsOnRebirth(boolean removePrestigePermissionsOnRebirth) {
		this.removePrestigePermissionsOnRebirth = removePrestigePermissionsOnRebirth;
	}

	public boolean isRemoveRebirthPermissionsOnRebirthDeletion() {
		return removeRebirthPermissionsOnRebirthDeletion;
	}

	public void setRemoveRebirthPermissionsOnRebirthDeletion(boolean removeRebirthPermissionsOnRebirthDeletion) {
		this.removeRebirthPermissionsOnRebirthDeletion = removeRebirthPermissionsOnRebirthDeletion;
	}

	public boolean isRemoveRebirthPermissionsOnRebirthReset() {
		return removeRebirthPermissionsOnRebirthReset;
	}

	public void setRemoveRebirthPermissionsOnRebirthReset(boolean removeRebirthPermissionsOnRebirthReset) {
		this.removeRebirthPermissionsOnRebirthReset = removeRebirthPermissionsOnRebirthReset;
	}

	public double getPrestigeCostIncreasePercentage() {
		return prestigeCostIncreasePercentage;
	}

	public void setPrestigeCostIncreasePercentage(double prestigeCostIncreasePercentage) {
		this.prestigeCostIncreasePercentage = prestigeCostIncreasePercentage;
	}

	public String getIncreaseExpression() {
		return increaseExpression;
	}

	public void setIncreaseExpression(String increaseExpression) {
		this.increaseExpression = increaseExpression;
	}

	public CommandsComponent getRebirthCommands() {
		return rebirthCommands;
	}

	public void setRebirthCommands(CommandsComponent rebirthCommands) {
		this.rebirthCommands = rebirthCommands;
	}

	public CommandsComponent getRebirthDeleteCommands() {
		return rebirthDeleteCommands;
	}

	public void setRebirthDeleteCommands(CommandsComponent rebirthDeleteCommands) {
		this.rebirthDeleteCommands = rebirthDeleteCommands;
	}

}
