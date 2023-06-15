package me.prisonranksx.hooks;

import java.util.List;

import org.bukkit.Location;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import me.prisonranksx.PrisonRanksX;

/**
 * Includes and manages a <b>constructor class</b> named HDHologram which implements IHologram
 * <br>HDHologram is a wrapper of HolographicDisplays API
 */
@SuppressWarnings("deprecation")
public class HDHologramManager implements IHologramManager {

	private PrisonRanksX plugin;
	
	public HDHologramManager(PrisonRanksX plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public IHologram createHologram(String hologramName, Location location, boolean threadSafe) {
		return (new HDHologram()).create(plugin, hologramName, location, threadSafe);
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

	public static class HDHologram implements IHologram {

		private Hologram hologramHD;
		private PrisonRanksX plugin;
		private Location location;
		@SuppressWarnings("unused")
		private boolean threadSafe;
		@SuppressWarnings("unused")
		private String hologramName;

		public HDHologram() {}

		@Override
		public IHologram create(PrisonRanksX plugin, String hologramName, Location location, boolean threadSafe) {
			HDHologram holo = new HDHologram();
			holo.plugin = plugin;
			holo.location = location;
			holo.threadSafe = threadSafe;
			holo.hologramName = hologramName;
			if(threadSafe)
				holo.createThreadSafe();
			else
				holo.createNonSafe();
			return holo;
		}

		@Override
		public void addLine(String line, boolean threadSafe) {
			if(threadSafe)
				plugin.doSyncLater(() -> hologramHD.appendTextLine(line), 1);
			else 
				hologramHD.appendTextLine(line);
		}

		@Override
		public void addLine(List<String> lines, boolean threadSafe) {
			if(threadSafe) 
				plugin.doSyncLater(() -> lines.forEach(hologramHD::appendTextLine), 1);
			else 
				lines.forEach(hologramHD::appendTextLine);
		}

		@Override
		public void delete() {
			plugin.doSync(() -> hologramHD.delete());
		}

		@Override
		public void delete(int delay) {
			plugin.doSyncLater(() -> hologramHD.delete(), 20 * delay);
		}

		private void createNonSafe() {
			hologramHD = HologramsAPI.createHologram(plugin, location);
			hologramHD.setAllowPlaceholders(true);
		}

		private void createThreadSafe() {
			plugin.doSync(() -> {
				hologramHD = HologramsAPI.createHologram(plugin, location);
				hologramHD.setAllowPlaceholders(true);  
			});
		}

	}
	
}
