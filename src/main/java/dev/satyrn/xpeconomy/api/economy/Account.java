package dev.satyrn.xpeconomy.api.economy;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

/**
 * Represents an economy account. Handles all XP operations.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
public interface Account {
    /**
     * Gets the account owner's name
     *
     * @return The name of the account owner.
     */
    @NotNull String getName();

    /**
     * Sets the account owner's name.
     *
     * @param value The new account owner name.
     * @return The modified account.
     */
    @NotNull Account setName(final @NotNull String value);

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
    @NotNull BigInteger getBalanceRaw();

    /**
     * Sets the balance on the account and optionally updates the player's XP value.
     *
     * @param value         The new account balance.
     * @param updateXPValue If true, also updates the player's XP to match.
     * @return The account instance.
     */
    @NotNull Account setBalance(final @NotNull BigDecimal value, final boolean updateXPValue);

    /**
     * Sets the raw balance value.
     *
     * @param value         The experience point balance.
     * @param updateXPValue If true, also updates the player's XP to match.
     * @return The account instance.
     */
    @NotNull Account setBalanceRaw(final @NotNull BigInteger value, final boolean updateXPValue);

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
    boolean withdraw(final @NotNull BigDecimal value);

    /**
     * Deposits a given amount into the account.
     *
     * @param value The amount to deposit.
     */
    boolean deposit(final @NotNull BigDecimal value);
}
