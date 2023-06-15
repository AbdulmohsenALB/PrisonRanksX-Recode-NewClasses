package me.prisonranksx.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.Adler32;

public class ModuloLongRange {

	private static final int HASH_CODE;
	private long range, value;

	static {
		Adler32 adl = new Adler32();
		adl.update(String.valueOf("ModuloLongRange").getBytes());
		HASH_CODE = (int) adl.getValue();
	}

	public ModuloLongRange(long range, long value) {
		this.range = range;
		this.value = value;
	}

	public static ModuloLongRange newRange(long range) {
		return new ModuloLongRange(range, -1);
	}

	public static ModuloLongRange matchingHash(long value) {
		return new ModuloLongRange(-1, value);
	}

	public static Set<ModuloLongRange> matchingHash(Set<ModuloLongRange> ranges, long value) {
		Set<ModuloLongRange> s = new HashSet<>();
		for (ModuloLongRange mlr : ranges) {
			if (mlr.isWithinRange(value)) s.add(mlr);
		}
		return s;
	}

	public static <T> void forEachMatchingHash(Map<ModuloLongRange, T> ranges, long value, Consumer<T> action) {
		ranges.forEach((mlr, obj) -> {
			if (mlr.isWithinRange(value)) action.accept(obj);
		});
	}

	public long getRange() {
		return range;
	}

	public void setRange(long range) {
		this.range = range;
	}

	@Override
	public boolean equals(Object object) {
		ModuloLongRange moduloLongRange = ((ModuloLongRange) object);
		return moduloLongRange.isWithinRange(value);
	}

	public boolean isWithinRange(long num) {
		return num % range == 0;
	}

	@Override
	public String toString() {
		return "ModuloLongRange: range=" + range;
	}

	@Override
	public int hashCode() {
		return HASH_CODE;
	}

}
