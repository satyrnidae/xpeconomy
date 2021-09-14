package dev.satyrn.xpeconomy.api.economy;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a player account. Handles all XP operations.
 */
public interface Account {
    /**
     * Gets the account owner's player UUID.
     *
     * @return The player UUID of the account owner.
     */
    UUID getUUID();

    /**
     * Sets the account owner's player UUID.
     *
     * @param value The account owner's UUID.
     * @return The account instance.
     */
    Account setUUID(UUID value);

    /**
     * Gets the balance on the account.
     *
     * @return The account balance.
     */
    BigDecimal getBalance();


    /**
     * Gets the raw experience point value on the account.
     * @return The raw experience point balance.
     */
    BigDecimal getBalanceRaw();

    /**
     * Sets the balance on the account.
     *
     * @param value The new account balance.
     * @return The account instance.
     */
    Account setBalance(final BigDecimal value);

    /**
     * Sets the balance on the account and optionally updates the player's XP value.
     *
     * @param value         The new account balance.
     * @param updateXPValue If true, also updates the player's XP value to match.
     * @return The account instance.
     */
    Account setBalance(final BigDecimal value, final boolean updateXPValue);

    /**
     * Sets the raw balance value.
     * @param value The experience point balance.
     * @param updateXPValue If true, also updates the player's XP value to match.
     * @return The account instance.
     */
    Account setBalanceRaw(final BigDecimal value, final boolean updateXPValue);

    /**
     * Checks whether the account can withdraw a given amount.
     *
     * @param value The amount to withdraw.
     * @return Whether the account can withdraw a given amount.
     */
    boolean has(final BigDecimal value);

    /**
     * Withdraws a given amount from the account.
     *
     * @param value The account to withdraw.
     * @return Whether the withdrawal was successful.
     */
    boolean withdraw(final BigDecimal value);

    /**
     * Deposits a given amount into the account.
     *
     * @param value The amount to deposit.
     */
    boolean deposit(final BigDecimal value);
}
