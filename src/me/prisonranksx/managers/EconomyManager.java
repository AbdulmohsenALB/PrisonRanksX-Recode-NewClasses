package me.prisonranksx.managers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.prisonranksx.common.StaticCache;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

@SuppressWarnings("deprecation")
public class EconomyManager extends StaticCache {

	private static final Economy ECONOMY = setupEconomy();
	private static final BalanceFormatter BALANCE_FORMATTER = new BalanceFormatter();

	static {
		BALANCE_FORMATTER.setup();
	}

	public static void reload() {
		BALANCE_FORMATTER.setup();
	}

	private static Economy setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) return null;
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
		return rsp == null ? null : rsp.getProvider();
	}

	@Nullable
	public static Economy getEconomyService() {
		return ECONOMY;
	}

	public static EconomyResponse takeBalance(OfflinePlayer offlinePlayer, double amount) {
		return ECONOMY.withdrawPlayer(offlinePlayer, amount);
	}

	public static EconomyResponse giveBalance(OfflinePlayer offlinePlayer, double amount) {
		return ECONOMY.depositPlayer(offlinePlayer, amount);
	}

	public static EconomyResponse setBalance(OfflinePlayer offlinePlayer, double amount) {
		takeBalance(offlinePlayer, getBalance(offlinePlayer));
		return ECONOMY.depositPlayer(offlinePlayer, amount);
	}

	public static double getBalance(OfflinePlayer offlinePlayer) {
		return ECONOMY.getBalance(offlinePlayer);
	}

	public static EconomyResponse takeBalance(String name, double amount) {
		return ECONOMY.withdrawPlayer(name, amount);
	}

	public static EconomyResponse giveBalance(String name, double amount) {
		return ECONOMY.depositPlayer(name, amount);
	}

	public static EconomyResponse setBalance(String name, double amount) {
		ECONOMY.withdrawPlayer(name, getBalance(name));
		return ECONOMY.depositPlayer(name, amount);
	}

	public static double getBalance(String name) {
		return ECONOMY.getBalance(name);
	}

	/**
	 * 
	 * @param amount to format
	 * @return (example) formats amount to: 1.0k, 5.3k, 15.3m.
	 */
	public static String shortcutFormat(double amount) {
		return BALANCE_FORMATTER.shortcutFormat(amount);
	}

	/**
	 * 
	 * @param amount to format
	 * @return (example) formats amount to: 1,250.
	 */
	public static String commaFormat(double amount) {
		return BALANCE_FORMATTER.commaFormat(amount);
	}

	/**
	 * 
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
			String[] abbreviations = { "", thousand, million, billion, trillion, quadrillion, quintillion, sextillion,
					septillion, octillion, nonillion, decillion, undecillion, duoDecillion, zillion, "+" + zillion,
					"++", "*", "*+", "**", "?", "??", "!", "!!", "~", "~~" };
			this.abbreviations = abbreviations;
		}

		public String shortcutFormat(double amount) {
			if (amount > 999) {
				double x = amount / Math.pow(10, Math.floor(Math.log10(amount) / 3) * 3);
				return abb.format(x) + abbreviations[((int) Math.floor(Math.log10(amount) / 3))];
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
