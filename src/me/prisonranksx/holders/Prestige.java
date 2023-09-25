package me.prisonranksx.holders;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import me.prisonranksx.components.*;
import me.prisonranksx.data.PrestigeStorage;

public class Prestige implements Level {

	private String name;
	private String displayName;
	private String nextPrestigeName;
	private double cost;
	private double costIncrease;
	private List<String> broadcastMessages;
	private List<String> messages;
	private List<String> requirementsMessages;
	private CommandsComponent commandsComponent;
	private RequirementsComponent requirementsComponent;
	private ActionBarComponent actionBarComponent;
	private PermissionsComponent permissionsComponent;
	private FireworkComponent fireworkComponent;
	private RandomCommandsComponent randomCommandsComponent;
	private long number;

	public Prestige(String name, String displayName, String nextPrestigeName, double cost) {
		this(name, displayName, nextPrestigeName, cost, null, null, null, null, null, null, null, null, null, 0.0);
	}

	public Prestige(String name, String displayName, String nextPrestigeName, double cost,
			@Nullable List<String> broadcastMessages, @Nullable List<String> messages,
			@Nullable CommandsComponent commandsComponent, @Nullable RequirementsComponent requirementsComponent,
			@Nullable ActionBarComponent actionBarComponent, @Nullable PermissionsComponent permissionsComponent,
			@Nullable FireworkComponent fireworkComponent, @Nullable RandomCommandsComponent randomCommandsComponent,
			List<String> requirementsMessages, double costIncrease) {
		this.name = name;
		this.displayName = displayName == null ? name : displayName;
		this.nextPrestigeName = nextPrestigeName == null || nextPrestigeName.equals("LASTPRESTIGE") ? null
				: nextPrestigeName;
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
		this.costIncrease = costIncrease;
		this.number = PrestigeStorage.getHandler().isInfinite() ? Long.parseLong(name)
				: PrestigeStorage.getPrestiges().size() + 1;
	}

	public String getName() {
		return name;
	}

	public Prestige setName(String name) {
		this.name = name;
		return this;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public @Nullable String getNextName() {
		return getNextPrestigeName();
	}

	@Override
	public void setNextName(String nextName) {
		setNextPrestigeName(nextName);
	}

	public String getNextPrestigeName() {
		return nextPrestigeName;
	}

	public void setNextPrestigeName(String nextPrestigeName) {
		this.nextPrestigeName = nextPrestigeName;
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

	public void setCommandsComponent(CommandsComponent commandsComponent) {
		this.commandsComponent = commandsComponent;
	}

	public void useCommandsComponent(Consumer<CommandsComponent> action) {
		if (commandsComponent == null) return;
		action.accept(commandsComponent);
	}

	public RequirementsComponent getRequirementsComponent() {
		return requirementsComponent;
	}

	public void setRequirementsComponent(RequirementsComponent requirementsComponent) {
		this.requirementsComponent = requirementsComponent;
	}

	public void useRequirementsComponent(Consumer<RequirementsComponent> action) {
		if (requirementsComponent == null) return;
		action.accept(requirementsComponent);
	}

	public ActionBarComponent getActionBarComponent() {
		return actionBarComponent;
	}

	public void setActionBarComponent(ActionBarComponent actionBarComponent) {
		this.actionBarComponent = actionBarComponent;
	}

	public void useActionBarComponent(Consumer<ActionBarComponent> action) {
		if (actionBarComponent == null) return;
		action.accept(actionBarComponent);
	}

	public PermissionsComponent getPermissionsComponent() {
		return permissionsComponent;
	}

	public void setPermissionsComponent(PermissionsComponent permissionsComponent) {
		this.permissionsComponent = permissionsComponent;
	}

	public void usePermissionsComponent(Consumer<PermissionsComponent> action) {
		if (permissionsComponent == null) return;
		action.accept(permissionsComponent);
	}

	public FireworkComponent getFireworkComponent() {
		return fireworkComponent;
	}

	public void setFireworkComponent(FireworkComponent fireworkComponent) {
		this.fireworkComponent = fireworkComponent;
	}

	public void useFireworkComponent(Consumer<FireworkComponent> action) {
		if (fireworkComponent == null) return;
		action.accept(fireworkComponent);
	}

	public RandomCommandsComponent getRandomCommandsComponent() {
		return randomCommandsComponent;
	}

	public void setRandomCommandsComponent(RandomCommandsComponent randomCommandsComponent) {
		this.randomCommandsComponent = randomCommandsComponent;
	}

	public void useRandomCommandsComponent(Consumer<RandomCommandsComponent> action) {
		if (randomCommandsComponent == null) return;
		action.accept(randomCommandsComponent);
	}

	public List<String> getRequirementsMessages() {
		return requirementsMessages;
	}

	public void setRequirementsMessages(List<String> requirementsMessages) {
		this.requirementsMessages = requirementsMessages;
	}

	@Override
	public long getIndex() {
		return number;
	}

	@Override
	public void setIndex(long index) {
		this.number = index;
	}

	public double getCostIncrease() {
		return costIncrease;
	}

	public void setCostIncrease(double costIncrease) {
		this.costIncrease = costIncrease;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

}
