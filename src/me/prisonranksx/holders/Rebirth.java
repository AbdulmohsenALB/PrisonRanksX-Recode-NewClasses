package me.prisonranksx.holders;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import me.prisonranksx.components.*;

public class Rebirth extends Rank implements Level {

	private String name;
	private String displayName;
	private String nextRebirthName;
	private double cost;
	private double costIncrease;
	private long requiredPrestiges;
	private List<String> broadcastMessages;
	private List<String> messages;
	private List<String> requirementsMessages;
	private CommandsComponent commandsComponent;
	private RequirementsComponent requirementsComponent;
	private ActionBarComponent actionBarComponent;
	private PermissionsComponent permissionsComponent;
	private FireworkComponent fireworkComponent;
	private RandomCommandsComponent randomCommandsComponent;

	public Rebirth(String name, String displayName, String nextRebirthName, double cost) {
		this(name, displayName, nextRebirthName, cost, null, null, null, null, null, null, null, null, null, 0.0, -1);
	}

	public Rebirth(String name, String displayName, String nextRebirthName, double cost,
			@Nullable List<String> broadcastMessages, @Nullable List<String> messages,
			@Nullable CommandsComponent commandsComponent, @Nullable RequirementsComponent requirementsComponent,
			@Nullable ActionBarComponent actionBarComponent, @Nullable PermissionsComponent permissionsComponent,
			@Nullable FireworkComponent fireworkComponent, @Nullable RandomCommandsComponent randomCommandsComponent,
			List<String> requirementsMessages, double costIncrease, long requiredPrestiges) {
		super(name, displayName, nextRebirthName, cost);
		this.name = name;
		this.displayName = displayName == null ? name : displayName;
		this.nextRebirthName = nextRebirthName == null || nextRebirthName.equals("LASTREBIRTH") ? null
				: nextRebirthName;
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
		this.requiredPrestiges = requiredPrestiges;
	}

	public String getName() {
		return name;
	}

	public Rebirth setName(String name) {
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
		return nextRebirthName;
	}

	@Override
	public void setNextName(String nextName) {
		setNextRebirthName(nextName);
	}

	public String getNextRebirthName() {
		return nextRebirthName;
	}

	public void setNextRebirthName(String nextRebirthName) {
		this.nextRebirthName = nextRebirthName;
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

	@Override
	public void useCommandsComponent(Consumer<CommandsComponent> action) {
		super.useCommandsComponent(action);
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

	@Override
	public void useActionBarComponent(Consumer<ActionBarComponent> action) {
		super.useActionBarComponent(action);
	}

	public void setActionBarComponent(ActionBarComponent actionBarComponent) {
		this.actionBarComponent = actionBarComponent;
	}

	public PermissionsComponent getPermissionsComponent() {
		return permissionsComponent;
	}

	@Override
	public void usePermissionsComponent(Consumer<PermissionsComponent> action) {
		super.usePermissionsComponent(action);
	}

	public void setPermissionsComponent(PermissionsComponent permissionsComponent) {
		this.permissionsComponent = permissionsComponent;
	}

	public FireworkComponent getFireworkComponent() {
		return fireworkComponent;
	}

	@Override
	public void useFireworkComponent(Consumer<FireworkComponent> action) {
		super.useFireworkComponent(action);
	}

	public void setFireworkComponent(FireworkComponent fireworkComponent) {
		this.fireworkComponent = fireworkComponent;
	}

	public RandomCommandsComponent getRandomCommandsComponent() {
		return randomCommandsComponent;
	}

	@Override
	public void useRandomCommandsComponent(Consumer<RandomCommandsComponent> action) {
		super.useRandomCommandsComponent(action);
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

	@Override
	public long getIndex() {
		return super.getIndex();
	}

	@Override
	public void setIndex(long index) {
		super.setIndex(index);
	}

	public double getCostIncrease() {
		return costIncrease;
	}

	public void setCostIncrease(double costIncrease) {
		this.costIncrease = costIncrease;
	}

	public long getRequiredPrestiges() {
		return requiredPrestiges;
	}

	public void setRequiredPrestiges(long requiredPrestiges) {
		this.requiredPrestiges = requiredPrestiges;
	}

}
