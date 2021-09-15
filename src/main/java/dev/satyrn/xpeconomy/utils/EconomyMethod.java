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

    /**
     * The decimal scale of the economy method.
     */
    private final transient int scale;
    /**
     * The rounding mode to use when converting the economy method balance.
     */
    private final transient RoundingMode roundingMode;

    /**
     * Creates a new EconomyMethod enum value.
     *
     * @param scale The decimal scale of the economy method.
     * @param roundingMode The rounding mode to use when converting the economy method balance.
     */
    EconomyMethod(final int scale, final RoundingMode roundingMode) {
        this.scale = scale;
        this.roundingMode = roundingMode;
    }

    /**
     * Returns the decimal scale of the economy method.
     *
     * @return The decimal scale of the economy method.
     */
    public int getScale() {
        return this.scale;
    }

    /**
     * Returns the rounding mode to use when converting the economy method balance.
     *
     * @return The rounding mode to use when converting the economy method balance.
     */
    public RoundingMode getRoundingMode() {
        return this.roundingMode;
    }

    /**
     * Returns the default economy method value.
     * @return POINTS
     */
    public static EconomyMethod getDefault() { return POINTS; }
}
