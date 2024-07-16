package me.prisonranksx.utils;

import java.util.zip.Adler32;

/**
 * For fast access using {@linkplain java.util.Map#get(Object)} ->
 * {@code // num being one of the numbers that can lie in between one of the ranges in the map.}
 * <br>
 * {@code int num = 50;}
 * {@code Map.get(HashedLongRange.matchingHash(num));}
 */
public class HashedLongRange {

	private static final int HASH_CODE;
	private long min, max, value;

	static {
		Adler32 adl = new Adler32();
		adl.update("HashedLongRange".getBytes());
		HASH_CODE = (int) adl.getValue();
	}

	public HashedLongRange(long min, long max, long value) {
		this.min = min;
		this.max = max;
		this.value = value;
	}

	public static HashedLongRange newRange(long min, long max) {
		return new HashedLongRange(min, max, 0);
	}

	public static HashedLongRange matchingHash(long num) {
		return new HashedLongRange(0, 0, num);
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public void setMin(long min) {
		this.min = min;
	}

	public void setMax(long max) {
		this.max = max;
	}

	@Override
	public boolean equals(Object object) {
		HashedLongRange hashedLongRange = ((HashedLongRange) object);
		return hashedLongRange.isWithinRange(value);
	}

	public boolean isWithinRange(long num) {
		return num >= min && num <= max;
	}

	@Override
	public String toString() {
		return "HashedLongRange: min=" + min + ", max=" + max;
	}

	@Override
	public int hashCode() {
		return HASH_CODE;
	}

}
