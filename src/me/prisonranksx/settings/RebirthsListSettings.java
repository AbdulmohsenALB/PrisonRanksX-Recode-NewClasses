package me.prisonranksx.settings;

import java.util.List;

public class RebirthsListSettings extends Settings {

	private String rebirthCurrentFormat, rebirthCompletedFormat, rebirthOtherFormat;
	private boolean enablePages;
	private int rebirthPerPage;
	private List<String> rebirthWithPagesListFormat, rebirthListFormat;

	public RebirthsListSettings() {
		super("Rebirths-List-Options");
		setup();
	}

	@Override
	public void setup() {
		rebirthCurrentFormat = getString("rebirth-current-format", true);
		rebirthCompletedFormat = getString("rebirth-completed-format", true);
		rebirthOtherFormat = getString("rebirth-other-format", true);
		enablePages = getBoolean("enable-pages");
		rebirthPerPage = getInt("rebirth-per-page");
		rebirthWithPagesListFormat = getStringList("rebirth-with-pages-list-format", true);
		rebirthListFormat = getStringList("rebirth-list-format", true);
	}

	public String getRebirthCurrentFormat() {
		return rebirthCurrentFormat;
	}

	public String getRebirthCompletedFormat() {
		return rebirthCompletedFormat;
	}

	public String getRebirthOtherFormat() {
		return rebirthOtherFormat;
	}

	public boolean isEnablePages() {
		return enablePages;
	}

	public int getRebirthPerPage() {
		return rebirthPerPage;
	}

	public List<String> getRebirthWithPagesListFormat() {
		return rebirthWithPagesListFormat;
	}

	public List<String> getRebirthListFormat() {
		return rebirthListFormat;
	}

}
