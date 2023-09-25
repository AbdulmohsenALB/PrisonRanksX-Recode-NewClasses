package me.prisonranksx.hooks;

import java.util.List;

import me.filoghost.holographicdisplays.api.hologram.PlaceholderSetting;
import org.bukkit.Location;

import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;

import me.prisonranksx.PrisonRanksX;

/**
 * Includes and manages a <b>constructor class</b> named HDHologram which implements IHologram
 * <br>HDHologram is a wrapper of HolographicDisplays API
 */
@SuppressWarnings("deprecation")
public class HDHologramManager implements IHologramManager {

	private PrisonRanksX plugin;
	private static HolographicDisplaysAPI api;
	
	public HDHologramManager(PrisonRanksX plugin) {
		this.plugin = plugin;
		api = HolographicDisplaysAPI.get(plugin);
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
				plugin.doSyncLater(() -> hologramHD.getLines().appendText(line), 1);
			else 
				hologramHD.getLines().appendText(line);
		}

		@Override
		public void addLine(List<String> lines, boolean threadSafe) {
			if(threadSafe) 
				plugin.doSyncLater(() -> lines.forEach(hologramHD.getLines()::appendText), 1);
			else 
				lines.forEach(hologramHD.getLines()::appendText);
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
			hologramHD = api.createHologram(location);
			hologramHD.setPlaceholderSetting(PlaceholderSetting.ENABLE_ALL);
		}

		private void createThreadSafe() {
			plugin.doSync(() -> {
				hologramHD = api.createHologram(location);
				hologramHD.setPlaceholderSetting(PlaceholderSetting.ENABLE_ALL);
			});
		}

	}
	
}
