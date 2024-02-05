package me.prisonranksx.settings;

import java.util.List;

public class PrestigesListSettings extends Settings {

	private String prestigeCurrentFormat, prestigeCompletedFormat, prestigeOtherFormat;
	private boolean enablePages;
	private int prestigePerPage;
	private List<String> prestigeWithPagesListFormat, prestigeListFormat;

	public PrestigesListSettings() {
		super("Prestiges-List-Options");
		setup();
	}

	@Override
	public void setup() {
		prestigeCurrentFormat = getString("prestige-current-format", true);
		prestigeCompletedFormat = getString("prestige-completed-format", true);
		prestigeOtherFormat = getString("prestige-other-format", true);
		enablePages = getBoolean("enable-pages");
		prestigePerPage = getInt("prestige-per-page");
		prestigeWithPagesListFormat = getStringList("prestige-with-pages-list-format", true);
		prestigeListFormat = getStringList("prestige-list-format", true);
	}

	public String getPrestigeCurrentFormat() {
		return prestigeCurrentFormat;
	}

	public String getPrestigeCompletedFormat() {
		return prestigeCompletedFormat;
	}

	public String getPrestigeOtherFormat() {
		return prestigeOtherFormat;
	}

	public boolean isEnablePages() {
		return enablePages;
	}

	public int getPrestigePerPage() {
		return prestigePerPage;
	}

	public List<String> getPrestigeWithPagesListFormat() {
		return prestigeWithPagesListFormat;
	}

	public List<String> getPrestigeListFormat() {
		return prestigeListFormat;
	}

}
