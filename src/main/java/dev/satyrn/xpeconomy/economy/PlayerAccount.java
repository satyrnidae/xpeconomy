package dev.satyrn.xpeconomy.economy;

import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import org.javatuples.Pair;

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
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Sets the account owner's player UUID.
     *
     * @param value The account owner's UUID.
     * @return The account instance.
     */
    @Override
    public Account setUUID(UUID value) {
        this.uuid = value;
        return this;
    }

    /**
     * Gets the balance on the account.
     *
     * @return The account balance.
     */
    @Override
    public BigDecimal getBalance() {
        BigDecimal balance = this.balance;

        if (this.economyMethod == EconomyMethod.LEVELS) {
            final Pair<Integer, Float> progress = PlayerXPUtils.toLevelProgress(balance);
            balance = BigDecimal.valueOf(progress.getValue0())
                    .add(BigDecimal.valueOf(progress.getValue1()))
                    .setScale(EconomyMethod.LEVELS.getScale(), EconomyMethod.LEVELS.getRoundingMode());
        }

        return balance;
    }

    public BigDecimal getBalanceRaw() {
        return this.balance;
    }

    /**
     * Sets the balance on the account.
     *
     * @param value The new account balance.
     * @return The account instance.
     */
    @Override
    public PlayerAccount setBalance(final BigDecimal value) {
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
    public PlayerAccount setBalance(BigDecimal value, final boolean updateXPValue) {
        if (this.economyMethod == EconomyMethod.LEVELS) {
            final int level = value.intValue();
            final float progress = value.remainder(BigDecimal.ONE)
                    .setScale(EconomyMethod.LEVELS.getScale(), EconomyMethod.LEVELS.getRoundingMode()).floatValue();

            value = PlayerXPUtils.getTotalXPValue(level, progress);
        }

        return this.setBalanceRaw(value, updateXPValue);
    }

    /**
     * Sets the raw balance value.
     * @param value The experience point balance.
     * @return The account instance.
     */
    public PlayerAccount setBalanceRaw(final BigDecimal value, final boolean updateXPValue) {
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
    public boolean has(BigDecimal value) {
        if (this.economyMethod == EconomyMethod.LEVELS) {
            final int level = value.intValue();
            final float progress = value.remainder(BigDecimal.ONE)
                    .setScale(EconomyMethod.LEVELS.getScale(), EconomyMethod.LEVELS.getRoundingMode()).floatValue();
            value = PlayerXPUtils.getTotalXPValue(level, progress);
        }

        return this.balance.compareTo(value) > -1;
    }

    /**
     * Withdraws a given amount from the account.
     *
     * @param value The account to withdraw.
     * @return Whether the withdrawal was successful.
     */
    @Override
    public boolean withdraw(final BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0 || !this.has(value)) {
            return false;
        }

        this.addBalance(value.negate(), true);

        return true;
    }

    /**
     * Deposits a given amount into the account.
     *
     * @param value The amount to deposit.
     */
    @Override
    public boolean deposit(final BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        this.addBalance(value, true);

        return true;
    }

    /**
     * Adds to the total value of the account.
     * @param value The value to add
     * @param updateXPValue If true, also updates the player's XP value to match.
     */
    private void addBalance(BigDecimal value, boolean updateXPValue) {
        if (this.economyMethod == EconomyMethod.LEVELS) {
            final int level = value.intValue();
            final float progress = value.remainder(BigDecimal.ONE).floatValue();

            value = PlayerXPUtils.getTotalXPValue(level, progress);
        }

        this.setBalanceRaw(this.balance.add(value), updateXPValue);
    }
}
