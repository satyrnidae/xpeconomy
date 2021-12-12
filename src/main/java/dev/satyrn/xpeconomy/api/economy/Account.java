package dev.satyrn.xpeconomy.api.economy;

import org.jetbrains.annotations.NotNull;

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
    @NotNull UUID getUUID();

    /**
     * Sets the account owner's player UUID.
     *
     * @param value The account owner's UUID.
     * @return The account instance.
     */
    @NotNull Account setUUID(final @NotNull UUID value);

    /**
     * Gets the balance on the account.
     *
     * @return The account balance.
     */
    @NotNull BigDecimal getBalance();

    /**
     * Sets the balance on the account.
     *
     * @param value The new account balance.
     * @return The account instance.
     */
    @NotNull Account setBalance(final @NotNull BigDecimal value);

    /**
     * Gets the raw experience point value on the account.
     *
     * @return The raw experience point balance.
     */
    @NotNull BigDecimal getBalanceRaw();

    /**
     * Sets the balance on the account and optionally updates the player's XP value.
     *
     * @param value         The new account balance.
     * @param updateXPValue If true, also updates the player's XP value to match.
     * @return The account instance.
     */
    @NotNull Account setBalance(final @NotNull BigDecimal value, final boolean updateXPValue);

    /**
     * Sets the raw balance value.
     *
     * @param value         The experience point balance.
     * @param updateXPValue If true, also updates the player's XP value to match.
     * @return The account instance.
     */
    @NotNull Account setBalanceRaw(final @NotNull BigDecimal value, final boolean updateXPValue);

    /**
     * Checks whether the account can withdraw a given amount.
     *
     * @param value The amount to withdraw.
     * @return Whether the account can withdraw a given amount.
     */
    boolean has(final @NotNull BigDecimal value);

    /**
     * Withdraws a given amount from the account.
     *
     * @param value The account to withdraw.
     */
    void withdraw(final @NotNull BigDecimal value);

    /**
     * Deposits a given amount into the account.
     *
     * @param value The amount to deposit.
     */
    void deposit(final @NotNull BigDecimal value);
}
