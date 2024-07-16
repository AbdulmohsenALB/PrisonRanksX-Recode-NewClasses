package me.prisonranksx.components;

import org.bukkit.entity.Player;

public abstract class Component {

	abstract boolean use(Player player);

	abstract ComponentType getType();

}
