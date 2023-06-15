package me.prisonranksx.common;

import java.util.HashMap;
import java.util.Map;

import me.prisonranksx.utils.ModuloLongRange;

public class MainTest {

	private static Map<ModuloLongRange, String> m = new HashMap<>();

	public static void main(String[] args) {
		m.put(ModuloLongRange.newRange(100), "100");
		m.put(ModuloLongRange.newRange(200), "200");
		m.put(ModuloLongRange.newRange(500), "500");
		m.put(ModuloLongRange.newRange(750), "750");
		ModuloLongRange.forEachMatchingHash(m, 600, v -> System.out.println(v));
	}

}
