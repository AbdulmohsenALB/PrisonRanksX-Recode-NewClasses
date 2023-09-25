package me.prisonranksx.components;

import java.util.List;
import java.util.function.Consumer;

import me.prisonranksx.utils.ToString;

/**
 * 
 * Holds components in one object, if a rank, prestige, or a rebirth is not
 * needed.
 *
 */
public class ComponentsHolder {

	// private RequirementsComponent requirementsComponent;
	private String identifier;
	private CommandsComponent commandsComponent;
	private ActionBarComponent actionBarComponent;
	private FireworkComponent fireworkComponent;
	private PermissionsComponent permissionsComponent;
	private RandomCommandsComponent randomCommandsComponent;
	private List<String> messages, broadcastMessages;

	public ComponentsHolder() {}

	public static ComponentsHolder hold() {
		return new ComponentsHolder();
	}

	public ComponentsHolder commands(CommandsComponent commandsComponent) {
		this.commandsComponent = commandsComponent;
		return this;
	}

	public ComponentsHolder actionBar(ActionBarComponent actionBarComponent) {
		this.actionBarComponent = actionBarComponent;
		return this;
	}

	public ComponentsHolder firework(FireworkComponent fireworkComponent) {
		this.fireworkComponent = fireworkComponent;
		return this;
	}

	public ComponentsHolder randomCommands(RandomCommandsComponent randomCommandsComponent) {
		this.randomCommandsComponent = randomCommandsComponent;
		return this;
	}

	public void useCommandsComponent(Consumer<CommandsComponent> action) {
		if (commandsComponent == null) return;
		action.accept(commandsComponent);
	}

	public void useRandomCommandsComponent(Consumer<RandomCommandsComponent> action) {
		if (randomCommandsComponent == null) return;
		action.accept(randomCommandsComponent);
	}

	public void useActionBarComponent(Consumer<ActionBarComponent> action) {
		if (actionBarComponent == null) return;
		action.accept(actionBarComponent);
	}

	public void usePermissionsComponent(Consumer<PermissionsComponent> action) {
		if (permissionsComponent == null) return;
		action.accept(permissionsComponent);
	}

	public void useFireworkComponent(Consumer<FireworkComponent> action) {
		if (fireworkComponent == null) return;
		action.accept(fireworkComponent);
	}

	public ComponentsHolder messages(List<String> messages) {
		this.messages = messages;
		return this;
	}

	public ComponentsHolder broadcastMessages(List<String> broadcastMessages) {
		this.broadcastMessages = broadcastMessages;
		return this;
	}

	public List<String> getMessages() {
		return messages;
	}

	public List<String> getBroadcastMessages() {
		return broadcastMessages;
	}

	public String getIdentifier() {
		return identifier;
	}

	public ComponentsHolder identify(String identifier) {
		this.identifier = identifier;
		return this;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}

}
