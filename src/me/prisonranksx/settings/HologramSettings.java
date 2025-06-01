package me.prisonranksx.settings;

import java.util.List;

/**
 * Settings for promotion holograms from config.yml
 */
public class HologramSettings extends Settings {

	private boolean rankupEnabled, prestigeEnabled, rebirthEnabled;
	private int rankupRemoveDelay, prestigeRemoveDelay, rebirthRemoveDelay;
	private double rankupHeight, prestigeHeight, rebirthHeight;
	private List<String> rankupFormat, prestigeFormat, rebirthFormat;

	public HologramSettings() {
		super("Holograms");
		setup();
	}

	@Override
	public void setup() {

		refreshParentSection();

		rankupEnabled = getBoolean("rankup.enable");
		prestigeEnabled = getBoolean("prestige.enable");
		rebirthEnabled = getBoolean("rebirth.enable");
		rankupRemoveDelay = getInt("rankup.remove-delay");
		prestigeRemoveDelay = getInt("prestige.remove-delay");
		rebirthRemoveDelay = getInt("rebirth.remove-delay");
		rankupHeight = getDouble("rankup.height");
		prestigeHeight = getDouble("prestige.height");
		rebirthHeight = getDouble("rebirth.height");
		rankupFormat = getStringList("rankup.format", true);
		prestigeFormat = getStringList("prestige.format", true);
		rebirthFormat = getStringList("rebirth.format", true);

	}

	public boolean isHologramsEnabled() {
		return isRankupEnabled() || isPrestigeEnabled() || isRebirthEnabled();
	}

	public boolean isRankupEnabled() {
		return rankupEnabled;
	}

	public boolean isPrestigeEnabled() {
		return prestigeEnabled;
	}

	public boolean isRebirthEnabled() {
		return rebirthEnabled;
	}

	public int getRankupRemoveDelay() {
		return rankupRemoveDelay;
	}

	public int getPrestigeRemoveDelay() {
		return prestigeRemoveDelay;
	}

	public int getRebirthRemoveDelay() {
		return rebirthRemoveDelay;
	}

	public double getRankupHeight() {
		return rankupHeight;
	}

	public double getPrestigeHeight() {
		return prestigeHeight;
	}

	public double getRebirthHeight() {
		return rebirthHeight;
	}

	public List<String> getRankupFormat() {
		return rankupFormat;
	}

	public List<String> getPrestigeFormat() {
		return prestigeFormat;
	}

	public List<String> getRebirthFormat() {
		return rebirthFormat;
	}

}
