package dev.satyrn.xpeconomy.utils;

import dev.satyrn.xpeconomy.lang.I18n;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Economy processing method.
 */
public enum EconomyMethod {
    /**
     * Economy method for individual points.
     */
    POINTS(0, RoundingMode.HALF_UP, "currency.points"),
    /**
     * Economy method for levels.
     */
    LEVELS(0, RoundingMode.DOWN, "currency.levels"),
    /**
     * Economy method for per-hundred XP points.
     */
    PER_HUNDRED(2, RoundingMode.DOWN, "currency.perhundred");

    /**
     * The decimal scale of the economy method.
     */
    private final transient int scale;
    /**
     * The rounding mode to use when converting the economy method balance.
     */
    private final transient RoundingMode roundingMode;
    private final transient String economyNameKey;

    /**
     * Creates a new EconomyMethod enum value.
     *  @param scale The decimal scale of the economy method.
     * @param roundingMode The rounding mode to use when converting the economy method balance.
     * @param economyNameKey
     */
    EconomyMethod(final int scale, final RoundingMode roundingMode, String economyNameKey) {
        this.scale = scale;
        this.roundingMode = roundingMode;
        this.economyNameKey = economyNameKey;
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

    /**
     * Scales the value to the economy mode.
     * @param value The value to scale.
     * @return The scaled value.
     */
    public @NotNull BigDecimal scale(final @NotNull BigDecimal value) {
        return value.setScale(this.getScale(), this.getRoundingMode());
    }

    /**
     * Transforms
     * @param value
     * @return
     */
    public @NotNull String toString(@NotNull BigDecimal value) {
        return this.toString(value, false);
    }

    public @NotNull String toString(@NotNull BigDecimal value, boolean includeCurrencyName) {
        value = this.scale(value);

        StringBuilder pattern = new StringBuilder("#,##0");

        if (this.scale != 0) {
            pattern.append(".").append("0".repeat(Math.max(0, this.scale)));
        }
        final DecimalFormat formatter = new DecimalFormat(pattern.toString());

        final StringBuilder stringValue = new StringBuilder(formatter.format(value));
        if (includeCurrencyName) {
            stringValue.append(" ");
            if (value.compareTo(BigDecimal.ONE) == 0) {
                stringValue.append(this.getCurrencyName());
            } else {
                stringValue.append(this.getCurrencyNamePlural());
            }
        }
        return stringValue.toString();
    }

    public @NotNull String getCurrencyName() {
        return I18n.tr(this.economyNameKey + ".name");
    }

    public @NotNull String getCurrencyNamePlural() {
        return I18n.tr(this.economyNameKey + ".name.plural");
    }
}
