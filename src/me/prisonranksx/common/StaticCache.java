package me.prisonranksx.common;

/**
 * Just for readability purposes
 * <br>
 * Calling any method will cache the fields of the static class
 * <br>
 * and performs all methods within static blocks if there is any.
 */
public abstract class StaticCache {

	private static boolean cached = false;

	public static boolean cache() {
		return cached = true;
	}

	public static boolean isCached() {
		return cached;
	}

	public static void setCached(boolean cached) {
		StaticCache.cached = cached;
	}

}
