package me.prisonranksx.utils;

import java.util.zip.Adler32;

public class HashedStartsWith {

    private static final int HASH_CODE;
    private String startsWith;

    static {
        Adler32 adl = new Adler32();
        adl.update("HashedStartsWith".getBytes());
        HASH_CODE = (int) adl.getValue();
    }

    public HashedStartsWith(String startsWith) {
        this.startsWith = startsWith;
    }

    @Override
    public boolean equals(Object object) {
        HashedStartsWith hashedStartsWith = ((HashedStartsWith) object);
        return hashedStartsWith.startsWith.equals(startsWith);
    }

    @Override
    public int hashCode() {
        return HASH_CODE;
    }

    public static HashedStartsWith matchingHash(String startsWith) {
        return new HashedStartsWith(startsWith);
    }

}
