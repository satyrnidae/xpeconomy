package dev.satyrn.xpeconomy.economy;

import com.google.common.collect.ImmutableList;
import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * The XP Economy handler.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class ExperienceEconomy implements Economy {
    /**
     * Response for unimplemented methods
     */
    private static final EconomyResponse NOT_IMPLEMENTED = new EconomyResponse(0D, 0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    /**
     * The parent plugin instance.
     */
    private final transient Plugin plugin;
    /**
     * The account manager instance.
     */
    private final transient AccountManager accountManager;
    // The economy method to use.
    private final transient @NotNull Configuration configuration;

    /**
     * Creates a new instance of the Economy class.
     *
     * @param plugin         The parent plugin instance.
     * @param accountManager The account manager instance.
     */
    public ExperienceEconomy(final Plugin plugin, final AccountManager accountManager, final @NotNull Configuration configuration) {
        this.plugin = plugin;
        this.accountManager = accountManager;
        this.configuration = configuration;
    }

    // Gets the current economy method
    private EconomyMethod getEconomyMethod() {
        return this.configuration.economyMethod.value();
    }

    /**
     * Checks if economy method is enabled.
     *
     * @return Success or Failure
     */
    @Override
    public boolean isEnabled() {
        return this.plugin.isEnabled();
    }

    /**
     * Gets name of economy method
     *
     * @return Name of Economy Method
     */
    @Override
    public String getName() {
        return this.plugin.getName();
    }

    /**
     * Returns true if the given implementation supports banks.
     *
     * @return true if the implementation supports banks
     */
    @Override
    public boolean hasBankSupport() {
        return false;
    }

    /**
     * Some economy plugins round off after a certain number of digits.
     * This function returns the number of digits the plugin keeps
     * or -1 if no rounding occurs.
     *
     * @return number of digits after the decimal point kept
     */
    @Override
    public int fractionalDigits() {
        return this.getEconomyMethod().getScale();
    }

    /**
     * Format amount into a human-readable String This provides translation into
     * economy specific formatting to improve consistency between plugins.
     *
     * @param amount to format
     * @return Human-readable string describing amount
     */
    @Override
    public String format(final double amount) {
        return this.getEconomyMethod().toString(BigDecimal.valueOf(amount));
    }

    /**
     * Returns the name of the currency in plural form.
     * If the economy being used does not support currency names then an empty string will be returned.
     *
     * @return name of the currency (plural)
     */
    @Override
    public String currencyNamePlural() {
        return this.getEconomyMethod().getCurrencyNamePlural();
    }

    /**
     * Returns the name of the currency in singular form.
     * If the economy being used does not support currency names then an empty string will be returned.
     *
     * @return name of the currency (singular)
     */
    @Override
    public String currencyNameSingular() {
        return this.getEconomyMethod().getCurrencyName();
    }

    /**
     * Checks if this player has an account on the server yet
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param playerName The name of the player.
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer)} instead.
     */
    @Override
    @Deprecated
    public boolean hasAccount(final String playerName) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return this.hasAccount(player);
    }

    /**
     * Checks if this player has an account on the server yet
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param player to check
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(final OfflinePlayer player) {
        return this.accountManager.hasAccount(player.getUniqueId());
    }

    /**
     * Checks if this player has an account on the server yet
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param playerName to check in the world
     * @param worldName  world-specific account
     * @return if the player has an account
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer, String)} instead.
     */
    @Override
    @Deprecated
    public boolean hasAccount(final String playerName, final String worldName) {
        return this.hasAccount(playerName);
    }

    /**
     * Checks if this player has an account on the server yet on the given world
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param player    to check in the world
     * @param worldName world-specific account
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(final OfflinePlayer player, final String worldName) {
        return this.hasAccount(player);
    }

    /**
     * Gets balance of a player
     *
     * @param playerName The player name.
     * @return Amount currently held in players account
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer)} instead.
     */
    @Override
    @Deprecated
    public double getBalance(final String playerName) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return getBalance(player);
    }

    /**
     * Gets balance of a player
     *
     * @param player of the player
     * @return Amount currently held in players account
     */
    @Override
    public double getBalance(final OfflinePlayer player) {
        final Account account = this.accountManager.getAccount(player.getUniqueId());
        return account == null ? 0.0D : account.getBalance().doubleValue();
    }

    /**
     * @param playerName The player name
     * @param world      The name of the world.
     * @return Amount currently held in players account
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer, String)} instead.
     */
    @Override
    @Deprecated
    public double getBalance(final String playerName, final String world) {
        return this.getBalance(playerName);
    }

    /**
     * Gets balance of a player on the specified world.
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player to check
     * @param world  name of the world
     * @return Amount currently held in players account
     */
    @Override
    public double getBalance(final OfflinePlayer player, final String world) {
        return this.getBalance(player);
    }

    /**
     * @param playerName to check
     * @param amount     to check for
     * @deprecated As of VaultAPI 1.4 use {@link #has(OfflinePlayer, double)} instead.
     */
    @Override
    @Deprecated
    public boolean has(final String playerName, final double amount) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return this.has(player, amount);
    }

    /**
     * Checks if the player account has the amount - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to check
     * @param amount to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    @Override
    public boolean has(final OfflinePlayer player, final double amount) {
        final Account account = this.accountManager.getAccount(player.getUniqueId());
        return account != null && account.has(BigDecimal.valueOf(amount));
    }

    /**
     * @param playerName to check
     * @param worldName  to check with
     * @param amount     to check for
     * @deprecated As of VaultAPI 1.4 use {@link #has(OfflinePlayer, String, double)} instead.
     */
    @Override
    @Deprecated
    public boolean has(final String playerName, final String worldName, final double amount) {
        return this.has(playerName, amount);
    }

    /**
     * Checks if the player account has the amount in a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to check
     * @param worldName to check with
     * @param amount    to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    @Override
    public boolean has(final OfflinePlayer player, final String worldName, final double amount) {
        return this.has(player, amount);
    }

    /**
     * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param playerName to check
     * @param amount     to check for
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, double)} instead.
     */
    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(final String playerName, final double amount) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }

    /**
     * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to withdraw from
     * @param amount Amount to withdraw
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse withdrawPlayer(final OfflinePlayer player, final double amount) {
        final Account account = this.accountManager.getAccount(player.getUniqueId());
        if (account == null) {
            return new EconomyResponse(0D, 0D, EconomyResponse.ResponseType.FAILURE, "");
        }
        if (amount < 0.0D) {
            return new EconomyResponse(0D, account.getBalance()
                    .doubleValue(), EconomyResponse.ResponseType.FAILURE, "");
        }
        final BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        if (account.has(decimalAmount)) {
            account.withdraw(decimalAmount);
            return new EconomyResponse(amount, account.getBalance()
                    .doubleValue(), EconomyResponse.ResponseType.SUCCESS, "");
        }
        return new EconomyResponse(0D, account.getBalance().doubleValue(), EconomyResponse.ResponseType.FAILURE, "");
    }

    /**
     * Withdraw an amount from a player on a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param playerName to withdraw from
     * @param worldName  name of the world
     * @param amount     amount to withdraw
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, String, double)} instead.
     */
    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(final String playerName, final String worldName, final double amount) {
        return this.withdrawPlayer(playerName, amount);
    }

    /**
     * Withdraw an amount from a player on a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to withdraw from
     * @param worldName - name of the world
     * @param amount    Amount to withdraw
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse withdrawPlayer(final OfflinePlayer player, final String worldName, final double amount) {
        return this.withdrawPlayer(player, amount);
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param playerName to deposit to
     * @param amount     amount ot deposit
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, double)} instead.
     */
    @Override
    @Deprecated
    public EconomyResponse depositPlayer(final String playerName, final double amount) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to deposit to
     * @param amount Amount to deposit
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse depositPlayer(final OfflinePlayer player, final double amount) {
        final Account account = this.accountManager.getAccount(player.getUniqueId());
        if (account == null) {
            return new EconomyResponse(0D, 0D, EconomyResponse.ResponseType.FAILURE, "");
        }
        if (amount < 0.0D) {
            return new EconomyResponse(0D, account.getBalance()
                    .doubleValue(), EconomyResponse.ResponseType.FAILURE, "");
        }
        account.deposit(BigDecimal.valueOf(amount));
        return new EconomyResponse(amount, account.getBalance()
                .doubleValue(), EconomyResponse.ResponseType.SUCCESS, "");
    }

    /**
     * @param playerName to check
     * @param worldName  to use
     * @param amount     to deposit
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, String, double)} instead.
     */
    @Override
    @Deprecated
    public EconomyResponse depositPlayer(final String playerName, final String worldName, final double amount) {
        return depositPlayer(playerName, amount);
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to deposit to
     * @param worldName name of the world
     * @param amount    Amount to deposit
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse depositPlayer(final OfflinePlayer player, final String worldName, final double amount) {
        return depositPlayer(player, amount);
    }

    /**
     * Creates a bank account with the specified name and the player as the owner
     *
     * @param name   to use
     * @param player to use
     * @deprecated As of VaultAPI 1.4 use {{@link #createBank(String, OfflinePlayer)} instead.
     */
    @Override
    @Deprecated
    public EconomyResponse createBank(final String name, final String player) {
        return NOT_IMPLEMENTED;
    }

    /**
     * Creates a bank account with the specified name and the player as the owner
     *
     * @param name   of account
     * @param player the account should be linked to
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse createBank(final String name, final OfflinePlayer player) {
        return NOT_IMPLEMENTED;
    }

    /**
     * Deletes a bank account with the specified name.
     *
     * @param name of the back to delete
     * @return if the operation completed successfully
     */
    @Override
    public EconomyResponse deleteBank(final String name) {
        return NOT_IMPLEMENTED;
    }

    /**
     * Returns the amount the bank has
     *
     * @param name of the account
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankBalance(final String name) {
        return NOT_IMPLEMENTED;
    }

    /**
     * Returns true or false whether the bank has the amount specified - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to check for
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankHas(final String name, final double amount) {
        return NOT_IMPLEMENTED;
    }

    /**
     * Withdraw an amount from a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to withdraw
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankWithdraw(final String name, final double amount) {
        return NOT_IMPLEMENTED;
    }

    /**
     * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to deposit
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankDeposit(final String name, final double amount) {
        return NOT_IMPLEMENTED;
    }

    /**
     * Check if a player is the owner of a bank account
     *
     * @param name       of the account
     * @param playerName to check for ownership
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankOwner(String, OfflinePlayer)} instead.
     */
    @Override
    @Deprecated
    public EconomyResponse isBankOwner(final String name, final String playerName) {
        return NOT_IMPLEMENTED;
    }

    /**
     * Check if a player is the owner of a bank account
     *
     * @param name   of the account
     * @param player to check for ownership
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse isBankOwner(final String name, final OfflinePlayer player) {
        return NOT_IMPLEMENTED;
    }

    /**
     * Check if the player is a member of the bank account
     *
     * @param name       of the account
     * @param playerName to check membership
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankMember(String, OfflinePlayer)} instead.
     */
    @Override
    @Deprecated
    public EconomyResponse isBankMember(final String name, final String playerName) {
        return NOT_IMPLEMENTED;
    }

    /**
     * Check if the player is a member of the bank account
     *
     * @param name   of the account
     * @param player to check membership
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse isBankMember(final String name, final OfflinePlayer player) {
        return NOT_IMPLEMENTED;
    }

    /**
     * Gets the list of banks
     *
     * @return the List of Banks
     */
    @Override
    public List<String> getBanks() {
        return ImmutableList.of();
    }

    /**
     * Attempts to create a player account for the given player
     *
     * @param playerName The player name
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer)} instead.
     */
    @Override
    @Deprecated
    public boolean createPlayerAccount(final String playerName) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return createPlayerAccount(player);
    }

    /**
     * Attempts to create a player account for the given player
     *
     * @param player OfflinePlayer
     * @return if the account creation was successful
     */
    @Override
    public boolean createPlayerAccount(final OfflinePlayer player) {
        accountManager.createAccount(player);
        return true;
    }

    /**
     * Attempts to create a player account for the given player on the specified world
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this then false will always be returned.
     *
     * @param playerName player name
     * @param worldName  name of the world
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer, String)} instead.
     */
    @Override
    @Deprecated
    public boolean createPlayerAccount(final String playerName, final String worldName) {
        return this.createPlayerAccount(playerName);
    }

    /**
     * Attempts to create a player account for the given player on the specified world
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this then false will always be returned.
     *
     * @param player    OfflinePlayer
     * @param worldName String name of the world
     * @return if the account creation was successful
     */
    @Override
    public boolean createPlayerAccount(final OfflinePlayer player, final String worldName) {
        return this.createPlayerAccount(player);
    }
}
