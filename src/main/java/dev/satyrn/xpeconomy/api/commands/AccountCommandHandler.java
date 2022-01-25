package dev.satyrn.xpeconomy.api.commands;

import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;


/**
 * Represents a command handler which manages accounts.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
public abstract class AccountCommandHandler extends VaultCommandHandler {
    // The account manager instance.
    private final @NotNull AccountManager accountManager;
    // The configuration.
    private final @NotNull Configuration configuration;

    /**
     * Represents a command which manages accounts or account balances.
     *
     * @param plugin         The plugin instance.
     * @param permission     The permission instance.
     * @param accountManager The account manager.
     * @param configuration  The configuration instance.
     */
    protected AccountCommandHandler(final @NotNull Plugin plugin, final @NotNull Permission permission, final @NotNull AccountManager accountManager, final @NotNull Configuration configuration) {
        super(plugin, permission);
        this.accountManager = accountManager;
        this.configuration = configuration;
    }

    /**
     * Gets the account manager instance.
     *
     * @return The account manager instance.
     */
    public @NotNull AccountManager getAccountManager() {
        return accountManager;
    }

    /**
     * Gets the economy method.
     *
     * @return The economy method.
     */
    public @NotNull EconomyMethod getEconomyMethod() {
        return this.configuration.economyMethod.value();
    }

    /**
     * Gets the configuration.
     *
     * @return The configuration.
     */
    public @NotNull Configuration getConfiguration() {
        return configuration;
    }
}
