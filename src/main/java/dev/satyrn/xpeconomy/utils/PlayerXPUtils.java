package dev.satyrn.xpeconomy.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.javatuples.Pair;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Performs XP conversion, calculation, and updating.
 */
public final class PlayerXPUtils {
    /**
     * The math context to use during calculations.
     */
    private static final MathContext MATH_CONTEXT = new MathContext(34, RoundingMode.HALF_UP);
    /**
     * The maximum level count at which the first function should be used.
     */
    private static final BigDecimal FUNC_1_MAX = BigDecimal.valueOf(16);
    /**
     * The maximum level count at which the second function should be used.
     */
    private static final BigDecimal FUNC_2_MAX = BigDecimal.valueOf(31);

    /**
     * Sets a player's experience values to a specific balance.
     *
     * @param uuid    The player UUID.
     * @param balance The player balance.
     */
    public static void setPlayerXPTotal(final UUID uuid, BigDecimal balance) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        final Player player = offlinePlayer.getPlayer();
        if (!offlinePlayer.isOnline() || player == null) {
            return;
        }

        Pair<BigDecimal, BigDecimal> pair = toLevelProgress(balance);
        player.setLevel(pair.getValue0().intValue());
        player.setExp(pair.getValue1().floatValue());
    }

    /**
     * Gets the total amount of XP a player has.
     *
     * @param uuid The player's UUID.
     * @return The total amount of XP that the player has, rounded to the nearest whole number.
     */
    public static BigDecimal getPlayerXPTotal(final UUID uuid) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (!offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null) {
            return BigDecimal.ZERO;
        }
        final Player player = offlinePlayer.getPlayer();
        return getTotalXPValue(player.getLevel(), player.getExp());
    }

    /**
     * Calculates the XP level and progress values from the total XP value.
     *
     * @param total A pair of a level and a progress percentage value.
     * @return The levels and progress for the given total.
     */
    public static Pair<BigDecimal, BigDecimal> toLevelProgress(final BigDecimal total) {
        BigDecimal levelProgress;

        // For levels <= 16, use "sqrt(x + 9) - 3".
        if (total.compareTo(getXPForLevel(FUNC_1_MAX)) < 1) {
            levelProgress = total                                                                                       // x
                    .add(BigDecimal.valueOf(9))                                                                         // x + 9
                    .sqrt(MATH_CONTEXT)                                                                                 // sqrt(x + 9)
                    .subtract(BigDecimal.valueOf(3));                                                                   // sqrt(x + 9) - 3
        }
        // For levels > 16 and levels <= 31, use "81/10 + sqrt((2/5) × (x - 7839/40))".
        else if (total.compareTo(getXPForLevel(FUNC_2_MAX)) < 1) {
            levelProgress = total                                                                                       // x
                    .subtract(BigDecimal.valueOf(7839).divide(BigDecimal.valueOf(40), MATH_CONTEXT))                    // x - 7839/40
                    .multiply(BigDecimal.valueOf(2).divide(BigDecimal.valueOf(5), MATH_CONTEXT))                        // (x - 7839/40) × 2/5
                    .sqrt(MATH_CONTEXT)                                                                                 // sqrt((x - 7839/40) × 2/5)
                    .add(BigDecimal.valueOf(81).divide(BigDecimal.valueOf(10), MATH_CONTEXT));                          // sqrt((x - 7839/40) x 2/5) + 81/10
        }
        // For levels > 31, use "325/18 + sqrt((2/9) × (x - 54215/72))".
        else {
            levelProgress = total                                                                                       // x
                    .subtract(BigDecimal.valueOf(54215).divide(BigDecimal.valueOf(72), MATH_CONTEXT))                   // x - 54215/72
                    .multiply(BigDecimal.valueOf(2).divide(BigDecimal.valueOf(9), MATH_CONTEXT))                        // (x - 54215/72) × 2/9
                    .sqrt(MATH_CONTEXT)                                                                                 // sqrt((x - 54215/72) × 2/9)
                    .add(BigDecimal.valueOf(325).divide(BigDecimal.valueOf(18), MATH_CONTEXT));                         // sqrt((x - 54215/72) × 2/9) + 325/18
        }
        final BigDecimal levels = levelProgress.setScale(0, RoundingMode.FLOOR);
        final BigDecimal progress = levelProgress.remainder(BigDecimal.ONE);
        return new Pair<>(levels, progress);
    }

    /**
     * Gets the total amount of XP a player with the given level and level-up progress has.
     *
     * @param level    The player's current level count.
     * @param progress The player's current progress to the next XP level.
     * @return The total amount of XP that the player has, rounded to the nearest whole number.
     */
    public static BigDecimal getTotalXPValue(final int level, final float progress) {
        return getXPForLevel(BigDecimal.valueOf(level))
                .add(getCurrentLevelProgress(BigDecimal.valueOf(level), BigDecimal.valueOf(progress)));
    }

    /**
     * Calculates the total XP value for a given level. h/t to Minecraft Wiki.
     *
     * @param level The player's level.
     * @return The total XP value for the player's current level, rounded to a whole number.
     * @implNote If this equation changes in-game, it will have to change here as well.
     */
    public static BigDecimal getXPForLevel(final BigDecimal level) {
        BigDecimal totalXPForLevel;
        final BigDecimal decimalLevel = level.setScale(0, RoundingMode.FLOOR);

        // For levels <= 16, use "x^2 + 6x".
        if (level.compareTo(FUNC_1_MAX) <= 0){
            // x^2 + 6x
            totalXPForLevel = decimalLevel                                                                              // x
                    .pow(2)                                                                                             // x^2
                    .add(BigDecimal.valueOf(6).multiply(decimalLevel));                                                 // x^2 + 6x
        }
        // For levels > 16 and levels <= 31, use "(5/2)x^2 - (81/2)x + 360".
        else if (level.compareTo(FUNC_2_MAX) <= 0) {
            totalXPForLevel = BigDecimal.valueOf(5)                                                                     // 5
                    .divide(BigDecimal.valueOf(2), MATH_CONTEXT)                                                        // 5/2
                    .multiply(decimalLevel.pow(2))                                                                      // (5/2)x^2
                    .subtract(BigDecimal.valueOf(81).divide(BigDecimal.valueOf(2), MATH_CONTEXT)                        // (5/2)x^2 - (81/2...
                            .multiply(decimalLevel))                                                                    // (5/2)x^2 - (81/2)x
                    .add(BigDecimal.valueOf(360));                                                                      // (5/2)x^2 - (18/2)x + 360
        }
        // For levels > 31, use "(9/2)x^2 - (352/2)x + 2220".
        else {
            totalXPForLevel = BigDecimal.valueOf(9)                                                                     // 9
                    .divide(BigDecimal.valueOf(2), MATH_CONTEXT)                                                        // 9/2
                    .multiply(decimalLevel.pow(2))                                                                      // (9/2)x^2
                    .subtract(BigDecimal.valueOf(325).divide(BigDecimal.valueOf(2), MATH_CONTEXT)
                            .multiply(decimalLevel))                                                                    // (9/2)x^2 - (325/2)x
                    .add(BigDecimal.valueOf(2220));                                                                     // (9/2)x^2 - (325/2)x + 2220;
        }
        return totalXPForLevel.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Gets the current XP amount for the player's current progress towards leveling. h/t to Minecraft Wiki.
     *
     * @param level   The player's current level.
     * @param percent The current progress towards the next level.
     * @return The total XP the player currently has within the current level, rounded to a whole number.
     * @implNote If this equation changes in-game, it will have to change here as well.
     */
    public static BigDecimal getCurrentLevelProgress(final BigDecimal level, final BigDecimal percent) {
        BigDecimal toNextLevel;
        final BigDecimal levelFloor = level.setScale(0, RoundingMode.FLOOR);

        // For levels < 16, use "2x + 7"
        if (level.compareTo(FUNC_1_MAX) < 0) {
            toNextLevel = levelFloor                                                                                  // x
                    .multiply(BigDecimal.valueOf(2))                                                                    // 2x
                    .add(BigDecimal.valueOf(7));                                                                        // 2x + 7
        }
        // For levels >= 16 and levels < 31, use "5x - 38"
        else if (level.compareTo(FUNC_2_MAX) < 0) {
            toNextLevel = levelFloor                                                                                  // x
                    .multiply(BigDecimal.valueOf(5))                                                                    // 5x
                    .subtract(BigDecimal.valueOf(38));                                                                  // 5x - 38
        }
        // For levels >= 31, use "9x - 158"
        else {
            toNextLevel = levelFloor                                                                                  // x
                    .multiply(BigDecimal.valueOf(9))                                                                    // 9x
                    .subtract(BigDecimal.valueOf(158));                                                                 // 9x - 158
        }
        return toNextLevel.multiply(percent).setScale(0, RoundingMode.HALF_UP);
    }
}
