package me.prisonranksx.bukkitutils.bukkittickbalancer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public abstract class SplittableTask implements Runnable {

	BukkitTask bukkitTask = null;

	public BukkitTask init() {
		return bukkitTask = init(JavaPlugin.getProvidingPlugin(SplittableTask.class));
	}

	public BukkitTask initAsync() {
		return bukkitTask = initAsync(JavaPlugin.getProvidingPlugin(SplittableTask.class));
	}

	public BukkitTask init(JavaPlugin plugin) {
		return bukkitTask = init(plugin, 0, 0);
	}

	public BukkitTask init(JavaPlugin plugin, int delay, int period) {
		return bukkitTask = Bukkit.getServer().getScheduler().runTaskTimer(plugin, this, delay, period);
	}

	public BukkitTask initAsync(JavaPlugin plugin) {
		return bukkitTask = initAsync(plugin, 0, 0);
	}

	public BukkitTask initAsync(JavaPlugin plugin, int delay, int period) {
		return bukkitTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this, delay, period);
	}

	public BukkitTask getBukkitTask() {
		return bukkitTask;
	}

}
