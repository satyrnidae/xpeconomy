package dev.satyrn.xpeconomy.api.commands;

import dev.satyrn.xpeconomy.api.configuration.ConfigurationConsumer;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.utils.ConfigurationConsumerRegistry;
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
public abstract class AccountCommandHandler extends VaultCommandHandler implements ConfigurationConsumer {
    // The account manager instance.
    private final @NotNull AccountManager accountManager;
    // The economy method instance.
    private @NotNull EconomyMethod economyMethod = EconomyMethod.getDefault();

    /**
     * Represents a command which manages accounts or account balances.
     *
     * @param plugin The plugin instance.
     * @param permission The permission instance.
     * @param accountManager The account manager.
     * @param configuration The configuration instance.
     */
    protected AccountCommandHandler(final @NotNull Plugin plugin,
                                    final @NotNull Permission permission,
                                    final @NotNull AccountManager accountManager,
                                    final @NotNull Configuration configuration) {
        super(plugin, permission);
        this.accountManager = accountManager;
        this.reloadConfiguration(configuration);
        ConfigurationConsumerRegistry.register(this);
    }

    /**
     * Updates the state of the command based on the current configuration.
     *
     * @param configuration The configuration.
     */
    @Override
    public void reloadConfiguration(@NotNull Configuration configuration) {
        this.economyMethod = configuration.economyMethod.value();
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
        return this.economyMethod;
    }

}
