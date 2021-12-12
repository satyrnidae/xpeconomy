package dev.satyrn.xpeconomy.utils;

import java.util.Optional;

public final class Cast {
    /**
     * Don't instantiate Objects
     */
    private Cast() {}

    public static <T> Optional<T> as(Class<T> asClass, Object object) {
        T cast = null;
        if (asClass.isAssignableFrom(object.getClass())) {
            cast = asClass.cast(object);
        }
        return Optional.ofNullable(cast);
    }
}
