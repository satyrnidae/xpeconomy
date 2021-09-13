package dev.satyrn.xpeconomy;

import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.configuration.ExperienceEconomyConfiguration;
import dev.satyrn.xpeconomy.economy.ExperienceEconomy;
import dev.satyrn.xpeconomy.economy.MySQLAccountManager;
import dev.satyrn.xpeconomy.economy.YamlAccountManager;
import dev.satyrn.xpeconomy.lang.I18n;
import dev.satyrn.xpeconomy.listeners.InventoryEventListener;
import dev.satyrn.xpeconomy.listeners.PlayerEventListener;
import dev.satyrn.xpeconomy.listeners.WorldEventListener;
import dev.satyrn.xpeconomy.storage.MySQLConnectionManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin class for Experience Economy
 */
public final class ExperienceEconomyPlugin extends JavaPlugin {
    /**
     * The internationalization handler instance.
     */
    private I18n i18n;
    /**
     * The account manager instance.
     */
    private AccountManager accountManager;

    /**
     * Occurs when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        // Plugin will not function without Vault.
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.getLogger().severe("Failed to acquire Vault plugin. This plugin will be disabled.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.saveDefaultConfig();
        this.saveResource("accounts.yml", false);

        // Initialize configuration handler.
        final ExperienceEconomyConfiguration configuration = new ExperienceEconomyConfiguration(this);

        // Initialize internationalization backend.
        this.initializeI18n(configuration);

        // Setup and register the economy classes.
        this.initializeEconomy(configuration);

        this.getServer().getPluginManager().registerEvents(new PlayerEventListener(this, this.accountManager), this);
        this.getServer().getPluginManager().registerEvents(new WorldEventListener(this, this.accountManager), this);
        this.getServer().getPluginManager().registerEvents(new InventoryEventListener(this, this.accountManager), this);
    }

    /**
     * Occurs when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        if (this.getServer().isStopping()) {
            this.getLogger().info(String.format("[%s] Server shutting down. Thanks for using %sv%s!",
                    this.getName(), this.getName(), this.getDescription().getVersion()));
        } else {
            this.getLogger().info(String.format("[%s] Plugin disabled. This may be due to a missing dependency. Do you have Vault installed?",
                    this.getName()));
        }

        this.accountManager.save();

        i18n.disable();
    }

    /**
     * Initializes the internationalization handler.
     * @param configuration The configuration instance.
     */
    private void initializeI18n(final ExperienceEconomyConfiguration configuration) {
        // Initialize internationalization handler.
        i18n = new I18n(this);
        i18n.setLocale(configuration.locale.value());
        i18n.enable();
    }

    /**
     * Initializes the vault economy.
     * @param configuration The configuration instance.
     */
    private void initializeEconomy(ExperienceEconomyConfiguration configuration) {
        if (configuration.mysql.enabled.value()) {
            final MySQLConnectionManager connection = new MySQLConnectionManager(configuration, this);
            accountManager = new MySQLAccountManager(configuration, this, connection);
        } else {
            accountManager = new YamlAccountManager(configuration, this);
        }
        accountManager.load();

        final ExperienceEconomy economy = new ExperienceEconomy(this, accountManager);
        this.getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.High);
    }
}
