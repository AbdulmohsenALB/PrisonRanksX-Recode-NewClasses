package me.prisonranksx.hooks;

import org.bukkit.Location;

public interface IHologramManager {

	public IHologram createHologram(String hologramName, Location location, boolean threadSafe);
	
	public void deleteHologram(IHologram hologram);
	
	public void deleteHologram(IHologram hologram, int removeTime);
	
	public void addHologramLine(IHologram hologram, String line, boolean threadSafe);
	
}
