package dev.satyrn.xpeconomy.economy;

import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.configuration.ExperienceEconomyConfiguration;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages player accounts.
 */
public abstract class AccountManagerBase implements AccountManager {
    /**
     * The list of player accounts.
     */
    protected final transient List<PlayerAccount> accounts = new ArrayList<>();
    /**
     * The economy method to use.
     */
    protected final transient EconomyMethod economyMethod;
    /**
     * The starting balance of new accounts.
     */
    protected final transient double startingBalance;

    /**
     * Creates a new instance of an account manager.
     *
     * @param configuration The configuration manager.
     */
    protected AccountManagerBase(final ExperienceEconomyConfiguration configuration) {
        this.economyMethod = configuration.economyMethod.value();
        this.startingBalance = configuration.startingValue.value();
    }

    /**
     * Checks if an account exists for a given player UUID.
     *
     * @param uuid The player UUID.
     * @return Whether the account exists.
     */
    @Override
    public boolean hasAccount(final UUID uuid) {
        return this.getAccount(uuid) != null;
    }

    /**
     * Creates an account for a player.
     *
     * @param player The player instance.
     * @return The new account.
     */
    @Override
    public Account createAccount(final OfflinePlayer player) {
        if (player != null) {
            final PlayerAccount account = new PlayerAccount(this.economyMethod, player.getUniqueId());
            final Player onlinePlayer = player.getPlayer();
            if (onlinePlayer != null) {
                BigDecimal playerBalance = PlayerXPUtils.getPlayerXPTotal(player.getUniqueId());
                BigDecimal startingBalance = this.getStartingBalance();
                if (playerBalance.compareTo(startingBalance) > 0) {
                    account.setBalanceRaw(playerBalance, false);
                } else {
                    account.setBalanceRaw(startingBalance, true);
                }
            }

            this.accounts.add(account);
        }
        return null;
    }

    /**
     * Gets an account with a specific player UUID.
     *
     * @param uuid The player UUID
     * @return The account instance.
     */
    @Override
    public Account getAccount(final UUID uuid) {
        for (PlayerAccount account : this.accounts) {
            if (account.getUUID().equals(uuid)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Gets the starting value for the account.
     *
     * @return The starting value for the account.
     */
    private BigDecimal getStartingBalance() {
        return BigDecimal.valueOf(this.startingBalance)
                .setScale(this.economyMethod.getScale(), this.economyMethod.getRoundingMode());
    }
}
