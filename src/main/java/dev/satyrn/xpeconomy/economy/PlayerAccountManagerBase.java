package dev.satyrn.xpeconomy.economy;

import dev.satyrn.xpeconomy.api.configuration.ConfigurationConsumer;
import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.utils.ConfigurationConsumerRegistry;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
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
public abstract class PlayerAccountManagerBase implements AccountManager, ConfigurationConsumer {
    // The list of player accounts.
    protected final transient List<PlayerAccount> accounts = new ArrayList<>();
    // The economy method to use.
    protected transient EconomyMethod economyMethod;
    // The starting balance of new accounts.
    protected transient BigDecimal startingBalance;
    // The configuration instance.
    protected transient Configuration configuration;

    /**
     * Creates a new instance of an account manager.
     *
     * @param configuration The configuration manager.
     */
    protected PlayerAccountManagerBase(final @NotNull Configuration configuration) {
        this.reloadConfiguration(configuration);
        ConfigurationConsumerRegistry.register(this);
    }

    /**
     * Called when the configuration is reloaded. Sets the state of the consumer based on the new configuration.
     *
     * @param configuration The configuration.
     */
    @Override
    public void reloadConfiguration(@NotNull Configuration configuration) {
        this.configuration = configuration;
        this.economyMethod = configuration.economyMethod.value();
        this.startingBalance = configuration.startingBalance.value();
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
    @Override
    public @Nullable Account createAccount(final @Nullable OfflinePlayer player) {
        PlayerAccount account = null;
        if (player != null) {
            account = new PlayerAccount(this.configuration, player.getUniqueId());
            final Player onlinePlayer = player.getPlayer();
            if (onlinePlayer != null) {
                BigInteger rawStartingBalance = this.economyMethod.toRawBalance(this.startingBalance, BigInteger.ZERO);
                BigInteger playerBalance = PlayerXPUtils.getPlayerXPTotal(player.getUniqueId());
                if (playerBalance.compareTo(rawStartingBalance) > 0) {
                    account.setBalanceRaw(playerBalance, false);
                } else {
                    account.setBalanceRaw(rawStartingBalance, true);
                }
            }

            this.accounts.add(account);
        }
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
}
