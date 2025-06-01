package me.prisonranksx.managers;

import me.hsgamer.unihologram.common.api.Hologram;
import me.hsgamer.unihologram.common.api.HologramProvider;
import me.hsgamer.unihologram.spigot.cmi.provider.CMIHologramProvider;
import me.hsgamer.unihologram.spigot.decentholograms.provider.DHHologramProvider;
import me.hsgamer.unihologram.spigot.holographicdisplays.provider.HDHologramProvider;
import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.common.StaticCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

/**
 * For creating and removing holograms.
 */
public class HologramManager extends StaticCache {

	private static final PrisonRanksX PLUGIN = (PrisonRanksX) JavaPlugin.getProvidingPlugin(HologramManager.class);
	private static final boolean HOLOGRAM_PLUGIN;
	private static final HologramProvider<Location> HOLOGRAM_PROVIDER;
	public static final boolean SUPPORTS_ASYNC;

	static {
		HologramProvider<Location> hologramProvider = null;
		boolean threadSafe = true;
		if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
			hologramProvider = new DHHologramProvider();
		} else if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			hologramProvider = new HDHologramProvider(PLUGIN);
			threadSafe = false;
		} else if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
			hologramProvider = new CMIHologramProvider();
		}
		SUPPORTS_ASYNC = threadSafe;
		HOLOGRAM_PROVIDER = hologramProvider;
		HOLOGRAM_PLUGIN = hologramProvider != null;
	}

	public static boolean hasHologramPlugin() {
		return HOLOGRAM_PLUGIN;
	}

	public static String getProviderName() {
		return HOLOGRAM_PROVIDER.getClass().getSimpleName();
	}

	public static CompletableFuture<Hologram<Location>> createHologram(String hologramName, Location location) {
		if (HOLOGRAM_PROVIDER == null)
			throw new RuntimeException("Cannot create hologram without supported Hologram plugin. " +
					"Supported are: DecentHolograms, HolographicDisplays and CMI.");
		if (!SUPPORTS_ASYNC)
			return CompletableFuture.completedFuture(HOLOGRAM_PROVIDER.createHologram(hologramName, location));
		return CompletableFuture.supplyAsync(() -> HOLOGRAM_PROVIDER.createHologram(hologramName, location));
	}

}
