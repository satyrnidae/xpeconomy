package dev.satyrn.xpeconomy.economy;

import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Represents a player account. Handles all XP operations.
 */
public final class PlayerAccount implements Account {
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
    PlayerAccount() {
    }

    /**
     * Creates an account with a name and UUID.
     *
     * @param uuid The UUID on the account.
     */
    public PlayerAccount(final UUID uuid) {
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
    public PlayerAccount setBalance(final BigDecimal value, final boolean updateXPValue) {
        this.balance = value.setScale(0, RoundingMode.HALF_UP);

        if (updateXPValue) {
            PlayerXPUtils.setPlayerXPTotal(this.uuid, this.balance);
        }

        return this;
    }

    /**
     * Checks whether the account can withdraw a given amount.
     *
     * @param value The amount to withdraw.
     * @return Whether the account can withdraw a given amount.
     */
    @Override
    public boolean has(final BigDecimal value) {
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
        if (value.compareTo(BigDecimal.ZERO) < 0 || !this.has(value)) return false;

        final BigDecimal newBalance = this.balance.subtract(value);

        this.setBalance(newBalance.max(BigDecimal.ZERO), true);

        return true;
    }

    /**
     * Deposits a given amount into the account.
     *
     * @param value The amount to deposit.
     */
    @Override
    public boolean deposit(final BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) return false;

        final BigDecimal newBalance = this.balance.add(value);

        this.setBalance(newBalance, true);

        return true;
    }
}
