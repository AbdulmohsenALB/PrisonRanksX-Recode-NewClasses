package me.prisonranksx.managers;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.UserData;
import com.earth2me.essentials.economy.EconomyLayer;
import com.earth2me.essentials.economy.EconomyLayers;
import me.prisonranksx.common.StaticCache;
import net.ess3.api.MaxMoneyException;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


@SuppressWarnings("deprecation")
public class EconomyManager extends StaticCache {

    public static final Economy ECONOMY = setupEconomy();
    private static final BalanceFormatter BALANCE_FORMATTER = new BalanceFormatter();
    private static final DirectEconomyProvider ECONOMY_PROVIDER;

    static {
        DirectEconomyProvider economyProvider;
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            economyProvider = new EssEconomyProvider();
        } else {
            economyProvider = new VaultEconomyProvider();
        }
        ECONOMY_PROVIDER = economyProvider;
    }

    private abstract static class DirectEconomyProvider {

        public void takeMoney(OfflinePlayer player, double amount) {
            ECONOMY.withdrawPlayer(player, amount);
        }

        public void setMoney(OfflinePlayer player, double amount) {
            ECONOMY.withdrawPlayer(player, ECONOMY.getBalance(player));
            ECONOMY.depositPlayer(player, amount);
        }

        public void giveMoney(OfflinePlayer player, double amount) {
            ECONOMY.depositPlayer(player, amount);
        }

    }

    public static class VaultEconomyProvider extends DirectEconomyProvider {
        public VaultEconomyProvider() {
            super();
        }
    }

    public static class EssEconomyProvider extends DirectEconomyProvider {

        private IEssentials essentials;

        public EssEconomyProvider() {
            essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        }

        public void takeMoney(OfflinePlayer player, double amount) {
            UserData userData = essentials.getUser(player);
            BigDecimal amountBigDecimal = BigDecimal.valueOf(amount);
            EconomyLayer layer = EconomyLayers.getSelectedLayer();
            if (layer != null && (layer.hasAccount(player) || layer.createPlayerAccount(player))) {
                layer.set(player, amountBigDecimal);
            }
            try {

                userData.setMoney(userData.getMoney().subtract(amountBigDecimal), true);
            } catch (MaxMoneyException e) {
                throw new RuntimeException(e);
            }
        }

        public void setMoney(OfflinePlayer player, double amount) {
            UserData userData = essentials.getUser(player);
            BigDecimal newBalance = BigDecimal.valueOf(amount);
            EconomyLayer layer = EconomyLayers.getSelectedLayer();
            if (layer != null && (layer.hasAccount(player) || layer.createPlayerAccount(player))) {
                layer.set(player, newBalance);
            }
            try {
                userData.setMoney(newBalance, true);
            } catch (MaxMoneyException e) {
                throw new RuntimeException(e);
            }
        }

        public void giveMoney(OfflinePlayer player, double amount) {
            UserData userData = essentials.getUser(player);
            BigDecimal amountBigDecimal = BigDecimal.valueOf(amount);
            EconomyLayer layer = EconomyLayers.getSelectedLayer();
            if (layer != null && (layer.hasAccount(player) || layer.createPlayerAccount(player))) {
                layer.set(player, amountBigDecimal);
            }
            try {
                userData.setMoney(userData.getMoney().add(amountBigDecimal), true);
            } catch (MaxMoneyException e) {
                throw new RuntimeException(e);
            }
        }

    }

    static {
        BALANCE_FORMATTER.setup();
    }

    public static void reload() {
        BALANCE_FORMATTER.setup();
    }

    public EconomyManager() {
        setupEconomy();
    }

    public static Economy setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return null;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        return rsp == null ? null : rsp.getProvider();
    }

    @Nullable
    public static Economy getEconomyService() {
        return ECONOMY;
    }

    public static void takeBalance(OfflinePlayer offlinePlayer, double amount) {
        ECONOMY_PROVIDER.takeMoney(offlinePlayer, amount);
    }

    public static void giveBalance(OfflinePlayer offlinePlayer, double amount) {
        ECONOMY_PROVIDER.giveMoney(offlinePlayer, amount);
    }

    public static void setBalance(OfflinePlayer offlinePlayer, double amount) {
        ECONOMY_PROVIDER.setMoney(offlinePlayer, amount);
    }

    public static double getBalance(OfflinePlayer offlinePlayer) {
        return ECONOMY.getBalance(offlinePlayer);
    }

    /**
     * @param amount to format
     * @return (example) formats amount to: 1.0k, 5.3k, 15.3m.
     */
    public static String shortcutFormat(double amount) {
        return BALANCE_FORMATTER.shortcutFormat(amount);
    }

    /**
     * @param amount to format
     * @return (example) formats amount to: 1,250.
     */
    public static String commaFormat(double amount) {
        return BALANCE_FORMATTER.commaFormat(amount);
    }

    /**
     * @param amount to format
     * @return (example) formats amount to: 1,250.53
     */
    public static String commaFormatWithDecimals(double amount) {
        return BALANCE_FORMATTER.commaFormat(amount, true);
    }

    public static class BalanceFormatter {

        private String thousand, million, billion, trillion, quadrillion, quintillion, sextillion, septillion,
                octillion, nonillion, decillion, undecillion, duoDecillion, zillion;
        private String[] abbreviations;
        private final DecimalFormat abb = new DecimalFormat("0.##");
        private final DecimalFormat df = new DecimalFormat("###,###.##");
        private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

        public void setup() {
            FileConfiguration config = ConfigManager.getConfig();
            this.thousand = config.getString("Balance-Formatter.thousand", "k");
            this.million = config.getString("Balance-Formatter.million", "m");
            this.billion = config.getString("Balance-Formatter.billion", "b");
            this.trillion = config.getString("Balance-Formatter.trillion", "t");
            this.quadrillion = config.getString("Balance-Formatter.quadrillion", "q");
            this.quintillion = config.getString("Balance-Formatter.quintillion", "qt");
            this.sextillion = config.getString("Balance-Formatter.sextillion", "sxt");
            this.septillion = config.getString("Balance-Formatter.septillion", "spt");
            this.octillion = config.getString("Balance-Formatter.octillion", "o");
            this.nonillion = config.getString("Balance-Formatter.nonillion", "n");
            this.decillion = config.getString("Balance-Formatter.decillion", "d");
            this.undecillion = config.getString("Balance-Formatter.undecillion", "ud");
            this.duoDecillion = config.getString("Balance-Formatter.duo-decillion", "dd");
            this.zillion = config.getString("Balance-Formatter.zillion", "z");
            this.abbreviations = new String[]{"", thousand, million, billion, trillion, quadrillion, quintillion, sextillion,
                    septillion, octillion, nonillion, decillion, undecillion, duoDecillion, zillion};
        }

        public String shortcutFormat(double amount) {
            if (amount >= 1000) {
                double floor = Math.floor(Math.log10(amount) / 3);
                double x = amount / Math.pow(10, floor * 3);
                int abbreviationIndex = (int) floor;
                if (abbreviationIndex >= abbreviations.length) {
                    int zillionPart = abbreviationIndex / abbreviations.length;
                    int remainingIndex = abbreviationIndex % abbreviations.length;

                    StringBuilder abbreviationBuilder = new StringBuilder(abbreviations[abbreviations.length - 1]);
                    abbreviationBuilder.append(zillionPart);

                    if (remainingIndex > 0) {
                        abbreviationBuilder.append(abbreviations[remainingIndex]);
                    }

                    return abb.format(x) + abbreviationBuilder;
                } else {
                    return abb.format(x) + abbreviations[abbreviationIndex];
                }
            }
            return String.valueOf(amount);
        }

        public String commaFormat(double amount) {
            return nf.format(amount);
        }

        public String commaFormat(double amount, boolean withDecimal) {
            return withDecimal ? df.format(amount) : nf.format(amount);
        }

    }

}
