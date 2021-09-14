package dev.satyrn.xpeconomy.utils;

import java.math.RoundingMode;

/**
 * Economy processing method.
 */
public enum EconomyMethod {
    /**
     * Economy method for individual points.
     */
    POINTS(0, RoundingMode.HALF_UP),
    /**
     * Economy method for levels.
     */
    LEVELS(2, RoundingMode.DOWN);

    private final transient int scale;
    private final transient RoundingMode roundingMode;

    EconomyMethod(final int scale, final RoundingMode roundingMode) {
        this.scale = scale;
        this.roundingMode = roundingMode;
    }

    public int getScale() {
        return this.scale;
    }

    public RoundingMode getRoundingMode() {
        return this.roundingMode;
    }
}
