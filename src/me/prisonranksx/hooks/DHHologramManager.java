package me.prisonranksx.hooks;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.prisonranksx.PrisonRanksX;

/**
 * Includes and manages a <b>constructor class</b> named DHHologram which implements IHologram
 * <br>DHHologram is a wrapper of DecentHolograms API
 */
public class DHHologramManager implements IHologramManager {

	private PrisonRanksX plugin;
	
	public DHHologramManager(PrisonRanksX plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public IHologram createHologram(String hologramName, Location location, boolean threadSafe) {
		return (new DHHologram()).create(plugin, hologramName, location, threadSafe);
	}

	@Override
	public void deleteHologram(IHologram hologram) {
		hologram.delete();
	}

	@Override
	public void deleteHologram(IHologram hologram, int removeTime) {
		hologram.delete(removeTime);
	}

	@Override
	public void addHologramLine(IHologram hologram, String line, boolean threadSafe) {
		try {
			hologram.addLine(line, threadSafe);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	public static class DHHologram implements IHologram {

		private Hologram hologramDH;
		private PrisonRanksX plugin;

		/**
		 * DecentHolograms holograms are async, so holograms are always thread safe.
		 */
		@Override
		public IHologram create(PrisonRanksX plugin, String hologramName, Location location, boolean threadSafe) {
			if(threadSafe) {
				return CompletableFuture.supplyAsync(() -> {
					DHHologram holo = new DHHologram();
					holo.plugin = plugin;
					holo.hologramDH = DHAPI.createHologram(hologramName, location);
					return holo;
				}).join();
			} else {
				DHHologram holo = new DHHologram();
				holo.plugin = plugin;
				holo.hologramDH = DHAPI.createHologram(hologramName, location);
				return holo;
			}
		}

		@Override
		public void addLine(String line, boolean threadSafe) {
			try {
				DHAPI.addHologramLine(hologramDH, line);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} 
		}

		@Override
		public void addLine(List<String> lines, boolean threadSafe) {
			try {
				lines.forEach(line -> DHAPI.addHologramLine(hologramDH, line));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} 
		}
		
		@Override
		public void delete() {
			hologramDH.delete();
		}

		@Override
		public void delete(int delay) {
			plugin.doSyncLater(() -> hologramDH.delete(), delay * 20);
		}

	}

}
