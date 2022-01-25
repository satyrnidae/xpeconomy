package dev.satyrn.xpeconomy.economy;

import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

/**
 * Represents a player account. Handles all XP operations.
 */
public final class PlayerAccount implements Account {
    // The configuration.
    private final @NotNull Configuration configuration;
    /**
     * The account balance.
     */
    private BigInteger balance;
    /**
     * The UUID on the account.
     */
    private UUID uuid;
    /**
     * The account owner's name.
     */
    private @NotNull String name = "";

    /**
     * Creates a new account with no data.
     *
     * @param configuration The configuration instance.
     */
    PlayerAccount(final @NotNull Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Creates an account with a name and UUID.
     *
     * @param configuration The configuration instance.
     * @param uuid          The UUID on the account.
     */
    public PlayerAccount(final Configuration configuration, final UUID uuid) {
        this(configuration);
        this.uuid = uuid;
    }

    // Gets the current economy method.
    private EconomyMethod getEconomyMethod() {
        return this.configuration.economyMethod.value();
    }

    /**
     * Gets the name of the account owner.
     *
     * @return The account owner's name.
     */
    @Contract("-> !null")
    @Override
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * Sets the account owner's name.
     *
     * @return The modified account.
     */
    @Contract(value = "_ -> this", mutates = "this")
    @Override
    public @NotNull Account setName(final @NotNull String name) {
        this.name = name;
        return this;
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
        return this.getEconomyMethod().fromRawBalance(this.getBalanceRaw());
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

    public @NotNull BigInteger getBalanceRaw() {
        return this.balance;
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
        return this.setBalanceRaw(this.getEconomyMethod().toRawBalance(value, this.getBalanceRaw()), updateXPValue);
    }

    /**
     * Sets the raw balance value.
     *
     * @param value The experience point balance.
     * @return The account instance.
     */
    public @NotNull PlayerAccount setBalanceRaw(final @NotNull BigInteger value, final boolean updateXPValue) {
        this.balance = value;

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
    public boolean has(final @NotNull BigDecimal value) {
        final BigInteger hasBalance = this.getEconomyMethod().toRawBalance(value, BigInteger.ZERO);

        return this.balance.compareTo(hasBalance) >= 0;
    }

    /**
     * Withdraws a given amount from the account.
     *
     * @param value The account to withdraw.
     */
    @Override
    public boolean withdraw(final @NotNull BigDecimal value) {
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
    public boolean deposit(final @NotNull BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        this.addBalance(value, true);
        return true;
    }

    /**
     * Adds to the total value of the account.
     *
     * @param value         The value to add
     * @param updateXPValue If true, also updates the player's XP value to match.
     */
    private void addBalance(@NotNull BigDecimal value, @SuppressWarnings("SameParameterValue") boolean updateXPValue) {
        final BigInteger addValue = this.getEconomyMethod().toRawBalance(value, BigInteger.ZERO);

        this.setBalanceRaw(this.balance.add(addValue), updateXPValue);
    }
}
