package me.prisonranksx.components;

import org.bukkit.entity.Player;

public abstract class Component {

	enum ComponentType {
		TITLE,
		ACTION_BAR,
		COMMANDS,
		FIREWORK,
		PERMISSIONS,
		RANDOM_COMMANDS,
		REQUIREMENTS;
	}

	abstract boolean use(Player player);

	abstract ComponentType getType();

}
