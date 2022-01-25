package dev.satyrn.xpeconomy.economy;

import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages player accounts.
 */
public abstract class PlayerAccountManagerBase implements AccountManager {
    // The list of player accounts.
    protected final transient List<PlayerAccount> accounts = new ArrayList<>();

    // The configuration.
    protected transient final @NotNull Configuration configuration;

    /**
     * Creates a new instance of an account manager.
     *
     * @param configuration The configuration manager.
     */
    protected PlayerAccountManagerBase(final @NotNull Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Checks if an account exists for a given player UUID.
     *
     * @param uuid The player UUID.
     * @return Whether the account exists.
     */
    @Override
    public boolean hasAccount(final @NotNull UUID uuid) {
        return this.getAccount(uuid) != null;
    }

    /**
     * Creates an account for a player.
     *
     * @param player The player instance.
     * @return The new account.
     */
    @Contract("null -> fail")
    @Override
    public @NotNull Account createAccount(final @Nullable OfflinePlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }
        final PlayerAccount account = new PlayerAccount(this.configuration, player.getUniqueId());
        if (player.getName() != null) {
            account.setName(player.getName());
        }
        final Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            BigInteger rawStartingBalance = this.getEconomyMethod()
                    .toRawBalance(this.getStartingBalance(), BigInteger.ZERO);
            BigInteger playerBalance = PlayerXPUtils.getPlayerXPTotal(player.getUniqueId());
            if (playerBalance.compareTo(rawStartingBalance) > 0) {
                account.setBalanceRaw(playerBalance, false);
            } else {
                account.setBalanceRaw(rawStartingBalance, true);
            }
        }

        this.accounts.add(account);
        return account;
    }

    /**
     * Gets an account with a specific player UUID.
     *
     * @param uuid The player UUID
     * @return The account instance.
     */
    @Override
    public @Nullable Account getAccount(final @NotNull UUID uuid) {
        for (PlayerAccount account : this.accounts) {
            if (account.getUUID().equals(uuid)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Gets the current economy method
     *
     * @return The current economy method.
     */
    protected EconomyMethod getEconomyMethod() {
        return this.configuration.economyMethod.value();
    }

    /**
     * Gets the starting balance for the account.
     *
     * @return The starting balance.
     */
    protected BigDecimal getStartingBalance() {
        return this.configuration.startingBalance.value();
    }
}
