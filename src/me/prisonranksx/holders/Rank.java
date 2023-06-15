package me.prisonranksx.holders;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import me.prisonranksx.components.ActionBarComponent;
import me.prisonranksx.components.CommandsComponent;
import me.prisonranksx.components.FireworkComponent;
import me.prisonranksx.components.PermissionsComponent;
import me.prisonranksx.components.RandomCommandsComponent;
import me.prisonranksx.components.RequirementsComponent;

public class Rank {

	private String name;
	private String displayName;
	private String nextRankName;
	private double cost;
	private List<String> broadcastMessages;
	private List<String> messages;
	private List<String> requirementsMessages;
	private boolean allowPrestige;
	private CommandsComponent commandsComponent;
	private RequirementsComponent requirementsComponent;
	private ActionBarComponent actionBarComponent;
	private PermissionsComponent permissionsComponent;
	private FireworkComponent fireworkComponent;
	private RandomCommandsComponent randomCommandsComponent;
	private int index;

	public Rank(String name, String displayName, String nextRankName, double cost) {
		this(name, displayName, nextRankName, cost, null, null, null, null, null, null, null, null, null, false);
	}

	public Rank(String name, String displayName, String nextRankName, double cost,
			@Nullable List<String> broadcastMessages, @Nullable List<String> messages,
			@Nullable CommandsComponent commandsComponent, @Nullable RequirementsComponent requirementsComponent,
			@Nullable ActionBarComponent actionBarComponent, @Nullable PermissionsComponent permissionsComponent,
			@Nullable FireworkComponent fireworkComponent, @Nullable RandomCommandsComponent randomCommandsComponent,
			List<String> requirementsMessages, boolean allowPrestige) {
		this.name = name;
		this.displayName = displayName == null ? name : displayName;
		this.nextRankName = nextRankName == null || nextRankName.equalsIgnoreCase("LASTRANK") ? null : nextRankName;
		this.cost = cost;
		this.broadcastMessages = broadcastMessages == null || broadcastMessages.isEmpty() ? null : broadcastMessages;
		this.messages = messages == null || messages.isEmpty() ? null : messages;
		this.commandsComponent = commandsComponent;
		this.requirementsComponent = requirementsComponent;
		this.actionBarComponent = actionBarComponent;
		this.permissionsComponent = permissionsComponent;
		this.fireworkComponent = fireworkComponent;
		this.randomCommandsComponent = randomCommandsComponent;
		this.requirementsMessages = requirementsMessages == null || requirementsMessages.isEmpty()
				|| randomCommandsComponent == null ? null : requirementsMessages;
		this.allowPrestige = allowPrestige;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * 
	 * @return null when rank is last rank or its nextrank is set to "LASTRANK"
	 */
	@Nullable
	public String getNextRankName() {
		return nextRankName;
	}

	public void setNextRankName(String nextRankName) {
		this.nextRankName = nextRankName;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public List<String> getBroadcastMessages() {
		return broadcastMessages;
	}

	public void setBroadcastMessages(List<String> broadcastMessages) {
		this.broadcastMessages = broadcastMessages;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	public CommandsComponent getCommandsComponent() {
		return commandsComponent;
	}

	public void useCommandsComponent(Consumer<CommandsComponent> action) {
		if (commandsComponent == null) return;
		action.accept(commandsComponent);
	}

	public void setCommandsComponent(CommandsComponent commandsComponent) {
		this.commandsComponent = commandsComponent;
	}

	public RequirementsComponent getRequirementsComponent() {
		return requirementsComponent;
	}

	public void setRequirementsComponent(RequirementsComponent requirementsComponent) {
		this.requirementsComponent = requirementsComponent;
	}

	public ActionBarComponent getActionBarComponent() {
		return actionBarComponent;
	}

	public void useActionBarComponent(Consumer<ActionBarComponent> action) {
		if (actionBarComponent == null) return;
		action.accept(actionBarComponent);
	}

	public void setActionBarComponent(ActionBarComponent actionBarComponent) {
		this.actionBarComponent = actionBarComponent;
	}

	public PermissionsComponent getPermissionsComponent() {
		return permissionsComponent;
	}

	public void usePermissionsComponent(Consumer<PermissionsComponent> action) {
		if (permissionsComponent == null) return;
		action.accept(permissionsComponent);
	}

	public void setPermissionsComponent(PermissionsComponent permissionsComponent) {
		this.permissionsComponent = permissionsComponent;
	}

	public FireworkComponent getFireworkComponent() {
		return fireworkComponent;
	}

	public void useFireworkComponent(Consumer<FireworkComponent> action) {
		if (fireworkComponent == null) return;
		action.accept(fireworkComponent);
	}

	public void setFireworkComponent(FireworkComponent fireworkComponent) {
		this.fireworkComponent = fireworkComponent;
	}

	public RandomCommandsComponent getRandomCommandsComponent() {
		return randomCommandsComponent;
	}

	public void useRandomCommandsComponent(Consumer<RandomCommandsComponent> action) {
		if (randomCommandsComponent == null) return;
		action.accept(randomCommandsComponent);
	}

	public void setRandomCommandsComponent(RandomCommandsComponent randomCommandsComponent) {
		this.randomCommandsComponent = randomCommandsComponent;
	}

	public List<String> getRequirementsMessages() {
		return requirementsMessages;
	}

	public void setRequirementsMessages(List<String> requirementsMessages) {
		this.requirementsMessages = requirementsMessages;
	}

	public boolean isAllowPrestige() {
		return allowPrestige;
	}

	public void setAllowPrestige(boolean allowPrestige) {
		this.allowPrestige = allowPrestige;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
