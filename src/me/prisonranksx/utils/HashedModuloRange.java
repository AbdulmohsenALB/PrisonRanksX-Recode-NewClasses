package me.prisonranksx.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.Adler32;

public class HashedModuloRange {

    private static final int HASH_CODE;
    private long range, value;

    static {
        Adler32 adl = new Adler32();
        adl.update("HashedModuloRange".getBytes());
        HASH_CODE = (int) adl.getValue();
    }

    public HashedModuloRange(long range, long value) {
        this.range = range;
        this.value = value;
    }

    public static HashedModuloRange newRange(long range) {
        return new HashedModuloRange(range, -1);
    }

    public static HashedModuloRange matchingHash(long value) {
        return new HashedModuloRange(-1, value);
    }

    public static Set<HashedModuloRange> matchingHash(Set<HashedModuloRange> ranges, long value) {
        Set<HashedModuloRange> s = new HashSet<>();
        for (HashedModuloRange mlr : ranges) {
            if (mlr.isWithinRange(value)) s.add(mlr);
        }
        return s;
    }

    public static <T> void forEachMatchingHash(Map<HashedModuloRange, T> ranges, long value, Consumer<T> action) {
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
        HashedModuloRange hashedModuloRange = ((HashedModuloRange) object);
        return hashedModuloRange.isWithinRange(value);
    }

    public boolean isWithinRange(long num) {
        return num % range == 0;
    }

    @Override
    public String toString() {
        return "HashedModuloRange: range=" + range;
    }

    @Override
    public int hashCode() {
        return HASH_CODE;
    }

}
