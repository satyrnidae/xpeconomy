package dev.satyrn.xpeconomy.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
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
    private static final BigInteger FUNC_1_MAX = BigInteger.valueOf(16);
    /**
     * The maximum level count at which the second function should be used.
     */
    private static final BigInteger FUNC_2_MAX = BigInteger.valueOf(31);

    /**
     * Sets a player's experience values to a specific balance.
     *
     * @param uuid  The player UUID.
     * @param total The player balance.
     */
    @SuppressWarnings("unused")
    public static void setPlayerXPTotal(final @NotNull UUID uuid, final @NotNull BigInteger total) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        final Player player = offlinePlayer.getPlayer();
        if (!offlinePlayer.isOnline() || player == null) {
            return;
        }

        setPlayerXPTotal(player, total);
    }

    /**
     * Sets a player's experience values to a specific total.
     *
     * @param player The player.
     * @param total  The player's XP total.
     */
    public static void setPlayerXPTotal(final @NotNull Player player, final @NotNull BigInteger total) {
        Pair<BigInteger, BigDecimal> pair = toLevelProgress(total);

        int currentLevel = player.getLevel();
        float currentProgress = player.getExp();
        int newLevel = pair.getValue0().intValue();
        float newProgress = pair.getValue1().floatValue();

        if (currentLevel < newLevel) {
            float f = currentLevel > 30 ? 1F : (float) currentLevel / 30F;
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, f * 0.75F, 1.0F);
        }
        float pitch = (float) (Math.random() - Math.random()) * 0.35F + 0.9F;
        if (currentLevel > newLevel || (currentLevel == newLevel && currentProgress > newProgress)) {
            pitch -= 0.25F;
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1F, pitch);

        player.setLevel(newLevel);
        player.setExp(newProgress);
    }

    /**
     * Gets the total amount of XP a player has.
     *
     * @param uuid The player's UUID.
     * @return The total amount of XP that the player has, rounded to the nearest whole number.
     */
    public static BigInteger getPlayerXPTotal(final UUID uuid) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        final Player player = offlinePlayer.getPlayer();
        if (!offlinePlayer.isOnline() || player == null) {
            return BigInteger.ZERO;
        }
        return getPlayerXPTotal(player);
    }

    /**
     * Gets the total amount of XP a player has.
     *
     * @param player The player.
     * @return The total amount of XP that the player has, rounded to the nearest whole number.
     */
    public static BigInteger getPlayerXPTotal(final @NotNull Player player) {
        return getTotalXPValue(player.getLevel(), player.getExp());
    }

    /**
     * Calculates the XP level and progress values from the total XP value.
     *
     * @param total A pair of a level and a progress percentage value.
     * @return The levels and progress for the given total.
     */
    public static Pair<BigInteger, BigDecimal> toLevelProgress(final BigInteger total) {
        BigDecimal levelProgress;

        // For levels <= 16, use "sqrt(x + 9) - 3".
        if (total.compareTo(getXPForLevel(FUNC_1_MAX)) < 1) {
            levelProgress = new BigDecimal(total).add(BigDecimal.valueOf(9))
                    .sqrt(MATH_CONTEXT)
                    .subtract(BigDecimal.valueOf(3));
        }
        // For levels > 16 and levels <= 31, use "81/10 + sqrt((2/5) × (x - 7839/40))".
        else if (total.compareTo(getXPForLevel(FUNC_2_MAX)) < 1) {
            levelProgress = new BigDecimal(total).subtract(BigDecimal.valueOf(7839)
                            .divide(BigDecimal.valueOf(40), MATH_CONTEXT))
                    .multiply(BigDecimal.valueOf(2).divide(BigDecimal.valueOf(5), MATH_CONTEXT))
                    .sqrt(MATH_CONTEXT)
                    .add(BigDecimal.valueOf(81).divide(BigDecimal.valueOf(10), MATH_CONTEXT));
        }
        // For levels > 31, use "325/18 + sqrt((2/9) × (x - 54215/72))".
        else {
            levelProgress = new BigDecimal(total).subtract(BigDecimal.valueOf(54215)
                            .divide(BigDecimal.valueOf(72), MATH_CONTEXT))
                    .multiply(BigDecimal.valueOf(2).divide(BigDecimal.valueOf(9), MATH_CONTEXT))
                    .sqrt(MATH_CONTEXT)
                    .add(BigDecimal.valueOf(325).divide(BigDecimal.valueOf(18), MATH_CONTEXT));
        }
        final BigInteger levels = levelProgress.setScale(0, RoundingMode.FLOOR).toBigInteger();
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
    public static BigInteger getTotalXPValue(final int level, final float progress) {
        return getXPForLevel(BigInteger.valueOf(level)).add(getCurrentLevelProgress(BigInteger.valueOf(level), BigDecimal.valueOf(progress)));
    }

    /**
     * Calculates the total XP value for a given level. h/t to Minecraft Wiki.
     * If this equation changes in-game, it will have to change here as well.
     *
     * @param level The player's level.
     * @return The total XP value for the player's current level, rounded to a whole number.
     */
    public static BigInteger getXPForLevel(final BigInteger level) {
        BigDecimal totalXPForLevel;
        final BigDecimal decimalLevel = new BigDecimal(level);

        // For levels <= 16, use "x^2 + 6x".
        if (level.compareTo(FUNC_1_MAX) <= 0) {
            // x^2 + 6x
            totalXPForLevel = decimalLevel                                                                              // x
                    .pow(2)                                                                                             // x^2
                    .add(BigDecimal.valueOf(6)
                            .multiply(decimalLevel));                                                 // x^2 + 6x
        }
        // For levels > 16 and levels <= 31, use "(5/2)x^2 - (81/2)x + 360".
        else if (level.compareTo(FUNC_2_MAX) <= 0) {
            totalXPForLevel = BigDecimal.valueOf(5)                                                                     // 5
                    .divide(BigDecimal.valueOf(2), MATH_CONTEXT)                                                        // 5/2
                    .multiply(decimalLevel.pow(2))                                                                      // (5/2)x^2
                    .subtract(BigDecimal.valueOf(81)
                            .divide(BigDecimal.valueOf(2), MATH_CONTEXT)                        // (5/2)x^2 - (81/2...
                            .multiply(decimalLevel))                                                                    // (5/2)x^2 - (81/2)x
                    .add(BigDecimal.valueOf(360));                                                                      // (5/2)x^2 - (18/2)x + 360
        }
        // For levels > 31, use "(9/2)x^2 - (352/2)x + 2220".
        else {
            totalXPForLevel = BigDecimal.valueOf(9)                                                                     // 9
                    .divide(BigDecimal.valueOf(2), MATH_CONTEXT)                                                        // 9/2
                    .multiply(decimalLevel.pow(2))                                                                      // (9/2)x^2
                    .subtract(BigDecimal.valueOf(325)
                            .divide(BigDecimal.valueOf(2), MATH_CONTEXT)
                            .multiply(decimalLevel))                                                                    // (9/2)x^2 - (325/2)x
                    .add(BigDecimal.valueOf(2220));                                                                     // (9/2)x^2 - (325/2)x + 2220;
        }
        return totalXPForLevel.setScale(0, RoundingMode.HALF_UP).toBigInteger();
    }

    /**
     * Gets the current XP amount for the player's current progress towards leveling. h/t to Minecraft Wiki.
     * If this equation changes in-game, it will have to change here as well.
     *
     * @param level   The player's current level.
     * @param percent The current progress towards the next level.
     * @return The total XP the player currently has within the current level, rounded to a whole number.
     */
    public static BigInteger getCurrentLevelProgress(final BigInteger level, final BigDecimal percent) {
        BigDecimal toNextLevel;

        // For levels < 16, use "2x + 7"
        if (level.compareTo(FUNC_1_MAX) < 0) {
            toNextLevel = new BigDecimal(level).multiply(BigDecimal.valueOf(2)).add(BigDecimal.valueOf(7));
        }
        // For levels >= 16 and levels < 31, use "5x - 38"
        else if (level.compareTo(FUNC_2_MAX) < 0) {
            toNextLevel = new BigDecimal(level).multiply(BigDecimal.valueOf(5)).subtract(BigDecimal.valueOf(38));
        }
        // For levels >= 31, use "9x - 158"
        else {
            toNextLevel = new BigDecimal(level).multiply(BigDecimal.valueOf(9)).subtract(BigDecimal.valueOf(158));
        }
        return toNextLevel.multiply(percent).setScale(0, RoundingMode.HALF_UP).toBigInteger();
    }
}
