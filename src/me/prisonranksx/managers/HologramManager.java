package me.prisonranksx.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.common.StaticCache;
import me.prisonranksx.hooks.DHHologramManager;
import me.prisonranksx.hooks.HDHologramManager;
import me.prisonranksx.hooks.IHologram;
import me.prisonranksx.hooks.IHologramManager;

public class HologramManager extends StaticCache {

	private static final IHologramManager HOLOGRAM_MANAGER;
	private static final PrisonRanksX PLUGIN = (PrisonRanksX) JavaPlugin.getProvidingPlugin(HologramManager.class);
	private static final boolean HOLOGRAM_PLUGIN;

	static {
		IHologramManager hologramManager = null;
		if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
			hologramManager = new HDHologramManager(PLUGIN);
		} else if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			hologramManager = new DHHologramManager(PLUGIN);
		}
		HOLOGRAM_MANAGER = hologramManager;
		HOLOGRAM_PLUGIN = HOLOGRAM_MANAGER != null;
	}

	public static boolean hasHologramPlugin() {
		return HOLOGRAM_PLUGIN;
	}

	public static IHologramManager getImplementedManager() {
		return HOLOGRAM_MANAGER;
	}

	public static IHologram createHologram(String hologramName, Location location, boolean threadSafe) {
		return HOLOGRAM_MANAGER.createHologram(hologramName, location, HOLOGRAM_PLUGIN);
	}

}
