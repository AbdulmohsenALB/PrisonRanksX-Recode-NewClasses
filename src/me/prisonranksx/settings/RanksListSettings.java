package me.prisonranksx.settings;

import java.util.List;

public class RanksListSettings extends Settings {

    private String rankCurrentFormat, rankCompletedFormat, rankOtherFormat;
    private boolean enablePages;
    private int rankPerPage;
    private List<String> rankWithPagesListFormat, rankListFormat;

    public RanksListSettings() {
        super("Ranks-List-Options");
        setup();
    }

    @Override
    public void setup() {
        rankCurrentFormat = getString("rank-current-format", true);
        rankCompletedFormat = getString("rank-completed-format", true);
        rankOtherFormat = getString("rank-other-format", true);
        enablePages = getBoolean("enable-pages");
        rankPerPage = getInt("rank-per-page");
        rankWithPagesListFormat = getStringList("rank-with-pages-list-format", true);
        rankListFormat = getStringList("rank-list-format", true);
    }

    public String getRankCurrentFormat() {
        return rankCurrentFormat;
    }

    public String getRankCompletedFormat() {
        return rankCompletedFormat;
    }

    public String getRankOtherFormat() {
        return rankOtherFormat;
    }

    public boolean isEnablePages() {
        return enablePages;
    }

    public int getRankPerPage() {
        return rankPerPage;
    }

    public List<String> getRankWithPagesListFormat() {
        return rankWithPagesListFormat;
    }

    public List<String> getRankListFormat() {
        return rankListFormat;
    }

}
