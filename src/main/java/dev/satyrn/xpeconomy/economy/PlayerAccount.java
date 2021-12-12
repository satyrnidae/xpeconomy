package dev.satyrn.xpeconomy.economy;

import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Represents a player account. Handles all XP operations.
 */
public final class PlayerAccount implements Account {
    /**
     * The economy method to use.
     */
    private final transient EconomyMethod economyMethod;
    /**
     * The account balance.
     */
    private BigDecimal balance;
    /**
     * The UUID on the account.
     */
    private UUID uuid;

    /**
     * Creates a new account with no data.
     */
    PlayerAccount(final EconomyMethod economyMethod) {
        this.economyMethod = economyMethod;
    }

    /**
     * Creates an account with a name and UUID.
     *
     * @param economyMethod The economy method for the account.
     * @param uuid The UUID on the account.
     */
    public PlayerAccount(final EconomyMethod economyMethod, final UUID uuid) {
        this(economyMethod);
        this.uuid = uuid;
    }

    /**
     * Gets the account owner's player UUID.
     *
     * @return The player UUID of the account owner.
     */
    @Override
    public @NotNull UUID getUUID() {
        return this.uuid;
    }

    /**
     * Sets the account owner's player UUID.
     *
     * @param value The account owner's UUID.
     * @return The account instance.
     */
    @Override
    public @NotNull Account setUUID(@NotNull UUID value) {
        this.uuid = value;
        return this;
    }

    /**
     * Gets the balance on the account.
     *
     * @return The account balance.
     */
    @Override
    public @NotNull BigDecimal getBalance() {
        final BigDecimal balance;

        switch (this.economyMethod) {
            case LEVELS -> balance = PlayerXPUtils.toLevelProgress(this.getBalanceRaw()).getValue0();
            case PER_HUNDRED -> balance = this.getBalanceRaw().divide(BigDecimal.valueOf(100),
                                                                        this.economyMethod.getScale(),
                                                                        this.economyMethod.getRoundingMode());
            default -> balance = this.getBalanceRaw();
        }

        return balance;
    }

    public @NotNull BigDecimal getBalanceRaw() {
        return this.balance;
    }

    /**
     * Sets the balance on the account.
     *
     * @param value The new account balance.
     * @return The account instance.
     */
    @Override
    public @NotNull PlayerAccount setBalance(final @NotNull BigDecimal value) {
        return this.setBalance(value, false);
    }

    /**
     * Sets the balance on the account and optionally updates the player's XP value.
     *
     * @param value         The new account balance.
     * @param updateXPValue If true, also updates the player's XP value to match.
     * @return The account instance.
     */
    @Override
    public @NotNull PlayerAccount setBalance(final @NotNull BigDecimal value, final boolean updateXPValue) {
        final BigDecimal newBalance;
        switch (this.economyMethod) {
            case LEVELS -> newBalance = PlayerXPUtils.getXPForLevel(value);
            case PER_HUNDRED -> newBalance = value.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.DOWN);
            default -> newBalance = value;
        }

        return this.setBalanceRaw(newBalance, updateXPValue);
    }

    /**
     * Sets the raw balance value.
     * @param value The experience point balance.
     * @return The account instance.
     */
    public @NotNull PlayerAccount setBalanceRaw(final @NotNull BigDecimal value, final boolean updateXPValue) {
        this.balance = value.setScale(0, RoundingMode.HALF_UP);

        if (updateXPValue) {
            PlayerXPUtils.setPlayerXPTotal(this.uuid, this.balance);
        }

        this.balance = value;
        return this;
    }

    /**
     * Checks whether the account can withdraw a given amount.
     *
     * @param value The amount to withdraw.
     * @return Whether the account can withdraw a given amount.
     */
    @Override
    public boolean has(final @NotNull BigDecimal value) {
        final BigDecimal hasBalance;

        switch (this.economyMethod) {
            case LEVELS -> hasBalance = PlayerXPUtils.getXPForLevel(value);
            case PER_HUNDRED -> hasBalance = value.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.DOWN);
            default -> hasBalance = value;
        }

        return this.balance.compareTo(hasBalance) > -1;
    }

    /**
     * Withdraws a given amount from the account.
     *
     * @param value The account to withdraw.
     */
    @Override
    public void withdraw(final @NotNull BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0 || !this.has(value)) {
            return;
        }

        this.addBalance(value.negate(), true);

    }

    /**
     * Deposits a given amount into the account.
     *
     * @param value The amount to deposit.
     */
    @Override
    public void deposit(final @NotNull BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return;
        }

        this.addBalance(value, true);

    }

    /**
     * Adds to the total value of the account.
     * @param value The value to add
     * @param updateXPValue If true, also updates the player's XP value to match.
     */
    private void addBalance(@NotNull BigDecimal value, boolean updateXPValue) {
        final BigDecimal newValue;
        switch (this.economyMethod) {
            case LEVELS -> {
                // Extract the player's current level / progress from their current balance
                final Pair<BigDecimal, BigDecimal> levelProgress = PlayerXPUtils.toLevelProgress(this.balance);

                // Get XP points for current level progress.
                final BigDecimal currentLevelXp = PlayerXPUtils.getCurrentLevelProgress(levelProgress.getValue0(),
                        levelProgress.getValue1());

                // Add the current level with the new value
                final BigDecimal nextLevel = levelProgress.getValue0().add(value);

                // Get the XP points for the new level value
                final BigDecimal xpForNextLevel = PlayerXPUtils.getXPForLevel(nextLevel);

                // Add next level and progress XP for the new balance.
                newValue = xpForNextLevel.add(currentLevelXp);
            } case PER_HUNDRED -> {
                // For a per hundred points based economy, we need to multiply
                // the value by 100 and then add it to the balance.
                final BigDecimal rawXpValue = value.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.DOWN);

                newValue = this.getBalanceRaw().add(rawXpValue);
            }
            default -> newValue = this.getBalanceRaw().add(value);
        }

        this.setBalanceRaw(newValue, updateXPValue);
    }
}
