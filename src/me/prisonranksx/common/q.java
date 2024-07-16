package me.prisonranksx.common;

import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.prisonranksx.managers.StringManager;

public class q {

	public static void msg(String msg) {
		Bukkit.broadcastMessage(msg);
	}

	public static void sendToOps(String s) {
		for (OfflinePlayer offlinePlayer : Bukkit.getOperators()) {
			if (offlinePlayer.isOnline()) offlinePlayer.getPlayer().sendMessage(StringManager.parseColors("&c" + s));
		}
	}

	public static void bcn(String s, Object obj) {
		Bukkit.broadcastMessage(obj == null ? s + " is null" : s + " is not null");
	}

	public static void bc(String msg) {
		Bukkit.broadcastMessage("[test] " + msg);
	}

	public static void bc(int msg) {
		Bukkit.broadcastMessage("[test-int] " + msg);
	}

	public static <T> void bc(T msg) {
		Bukkit.broadcastMessage("[test-" + msg.getClass().getTypeName() + "] " + msg);
	}

	public static <T> void bc(T msg, Function<T, String> func) {
		Bukkit.broadcastMessage("[test-" + msg.getClass().getTypeName() + "] " + func.apply(msg));
	}

	public static <T> void bc(Iterable<T> msg) {
		Bukkit.broadcastMessage("[test-iterable] start");
		for (T t : msg) Bukkit.broadcastMessage(t.toString());
		Bukkit.broadcastMessage("[test-iterable] end");
	}

	public static <T> void bc(Iterable<T> msg, String infoMsg) {
		Bukkit.broadcastMessage("[test-iterable] start: " + infoMsg);
		for (T t : msg) Bukkit.broadcastMessage(t.toString());
		Bukkit.broadcastMessage("[test-iterable] end");
	}

	public static <T> void bc(Iterable<T> msg, Function<T, String> func) {
		Bukkit.broadcastMessage("[test-iterable] start");
		for (T t : msg) Bukkit.broadcastMessage(func.apply(t));
		Bukkit.broadcastMessage("[test-iterable] end");
	}

	public static <T> void bc(Iterable<T> msg, Function<T, String> func, String infoMsg) {
		Bukkit.broadcastMessage("[test-iterable] start: " + infoMsg);
		for (T t : msg) Bukkit.broadcastMessage(func.apply(t));
		Bukkit.broadcastMessage("[test-iterable] end");
	}

}
