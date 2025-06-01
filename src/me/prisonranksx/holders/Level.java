package me.prisonranksx.holders;

import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.components.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A level represents either a rank, prestige, or a rebirth. (This is the generic term for them). Sometimes they are referred to as stages.
 */
public interface Level {

	/**
	 * @return name (referred to as configuration section in config yaml file) of level. 'A', 'B', 'P1'... without the quotes.
	 */
	@NotNull
	String getName();

	/**
	 * Changes level name to a new one (memory change only).
	 * AdminExecutor should be used for file changes.
	 *
	 * @param name new name to change to
	 * @return level object for further editing.
	 */
	Level setName(String name);

	/**
	 * @return display name with parsed colors and symbols.
	 */
	@NotNull
	String getDisplayName();

	/**
	 * Sets a new display name (memory change only).
	 * AdminExecutor should be used for file changes.
	 *
	 * @param displayName new display name.
	 */
	void setDisplayName(String displayName);

	/**
	 * @return next name of level or null if it's the last one. ("LASTRANK" from config files changes to null in code)
	 */
	@Nullable
	String getNextName();

	/**
	 * Change next level name in memory.
	 *
	 * @param nextName to change to
	 */
	void setNextName(String nextName);

	/**
	 * @return Gets cost of level. Increased cost is not included, just the plain
	 * cost
	 * specified in config file. Use {@linkplain PRXAPI#getRankFinalCost(Rank, UUID)} for that.
	 */
	double getCost();

	/**
	 * Changes cost to a new one in memory.
	 *
	 * @param cost to change to.
	 */
	void setCost(double cost);


	/**
	 * @return List of broadcast messages or null if none were specified.
	 */
	@Nullable
	List<String> getBroadcastMessages();

	/**
	 * Changes broadcast messages to a new one in memory.
	 *
	 * @param broadcastMessages to change to.
	 */
	void setBroadcastMessages(List<String> broadcastMessages);


	/**
	 * @return List of messages that are only sent to player upon promotion or null if none were specified.
	 */
	@Nullable
	List<String> getMessages();

	/**
	 * Changes messages to a new one in memory.
	 *
	 * @param messages to change to.
	 */
	void setMessages(List<String> messages);


	/**
	 * @return Component that caches console and player commands into their respective lists without the prefix,
	 * so they are ready for dispatching.
	 */
	@Nullable
	CommandsComponent getCommandsComponent();

	/**
	 * Uses and executes action on component only if it's not null, otherwise does
	 * nothing.
	 *
	 * @param action to perform on component.
	 */
	void useCommandsComponent(Consumer<CommandsComponent> action);

	/**
	 * Changes component to a new one in memory.
	 *
	 * @param commandsComponent to change to.
	 */
	void setCommandsComponent(CommandsComponent commandsComponent);

	/**
	 * @return Component that is responsible for handling requirements conditions and evaluating them.
	 */
	@Nullable
	RequirementsComponent getRequirementsComponent();

	void setRequirementsComponent(RequirementsComponent requirementsComponent);

	@Nullable
	ActionBarComponent getActionBarComponent();

	/**
	 * Uses and executes action on component only if it's not null, otherwise does
	 * nothing.
	 *
	 * @param action to perform on component.
	 */
	void useActionBarComponent(Consumer<ActionBarComponent> action);

	void setActionBarComponent(ActionBarComponent actionBarComponent);

	@Nullable
	PermissionsComponent getPermissionsComponent();

	/**
	 * Uses and executes action on component only if it's not null, otherwise does
	 * nothing.
	 *
	 * @param action to perform on component.
	 */
	void usePermissionsComponent(Consumer<PermissionsComponent> action);

	void setPermissionsComponent(PermissionsComponent permissionsComponent);

	@Nullable
	FireworkComponent getFireworkComponent();

	/**
	 * Uses and executes action on component only if it's not null, otherwise does
	 * nothing.
	 *
	 * @param action to perform on component.
	 */
	void useFireworkComponent(Consumer<FireworkComponent> action);

	void setFireworkComponent(FireworkComponent fireworkComponent);

	@Nullable
	RandomCommandsComponent getRandomCommandsComponent();

	/**
	 * Uses and executes action on component only if it's not null, otherwise does
	 * nothing.
	 *
	 * @param action to perform on component.
	 */
	void useRandomCommandsComponent(Consumer<RandomCommandsComponent> action);

	void setRandomCommandsComponent(RandomCommandsComponent randomCommandsComponent);

	@Nullable
	List<String> getRequirementsMessages();

	void setRequirementsMessages(List<String> requirementsMessages);

	/**
	 * Index used for organizing lists.
	 *
	 * @return level number in the ladder of the same type of levels.
	 */
	long getIndex();

	/**
	 * Changes index that is purpose is to organize lists.
	 *
	 * @param index to change to.
	 */
	void setIndex(long index);

	/**
	 * Index used for organizing lists.
	 *
	 * @return level number in the ladder of the same type of levels.
	 */
	default long getNumber() {
		return getIndex();
	}

	/**
	 * Changes index that is purpose is to organize lists.
	 *
	 * @param number to change to.
	 */
	default void setNumber(long number) {
		setIndex((int) number);
	}

	default LevelType getLevelType() {
		return null;
	}

	default boolean isMultiAccess() {
		return false;
	}

}
