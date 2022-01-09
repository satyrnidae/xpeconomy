package dev.satyrn.xpeconomy;

import dev.satyrn.papermc.api.commands.v1.CommandHandler;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.commands.*;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.economy.ExperienceEconomy;
import dev.satyrn.xpeconomy.economy.MySQLAccountManager;
import dev.satyrn.xpeconomy.economy.YamlAccountManager;
import dev.satyrn.xpeconomy.lang.I18n;
import dev.satyrn.xpeconomy.listeners.ExperienceBottleEventListener;
import dev.satyrn.xpeconomy.listeners.InventoryEventListener;
import dev.satyrn.xpeconomy.listeners.PlayerEventListener;
import dev.satyrn.xpeconomy.listeners.WorldEventListener;
import dev.satyrn.xpeconomy.storage.MySQLConnectionManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

/**
 * The main plugin class for Experience Economy
 */
@SuppressWarnings("unused")
public final class ExperienceEconomyPlugin extends JavaPlugin {
    /**
     * The internationalization handler instance.
     */
    private transient I18n i18n;
    /**
     * The account manager instance.
     */
    private transient AccountManager accountManager;

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

        // Save the default config and accounts.yml
        this.saveDefaultConfig();
        if (!new File(this.getDataFolder().getPath() + "/accounts.yml").exists()) {
            this.saveResource("accounts.yml", false);
        }

        // Initialize configuration handler.
        final Configuration configuration = new Configuration(this);

        // Setup logging level
        if (configuration.debug.value()) {
            this.getLogger().setLevel(Level.ALL);
        }

        // Initialize internationalization backend.
        this.i18n = this.initializeI18n(configuration);

        // Setup and register the economy classes.
        this.accountManager = this.initializeEconomy(configuration);

        // Setup and register the permission handler.
        final Permission permissionProvider = this.initializePermissionsProvider();

        this.registerEvents(this.accountManager, permissionProvider, configuration);
        this.registerCommands(this.accountManager, permissionProvider, configuration);
    }

    /**
     * Occurs when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        if (this.getServer().isStopping()) {
            this.getLogger()
                    .info(String.format("[%s] Server shutting down. Thanks for using %sv%s!", this.getName(), this.getName(), this.getDescription()
                            .getVersion()));
        } else {
            this.getLogger()
                    .info(String.format("[%s] Plugin disabled. This may be due to a missing dependency. Do you have Vault installed?", this.getName()));
        }

        this.accountManager.save();

        this.i18n.disable();
    }

    /**
     * Registers commands to their handlers.
     *
     * @param accountManager     The account manager instance.
     * @param permissionProvider The permission manager instance.
     */
    private void registerCommands(final AccountManager accountManager, final Permission permissionProvider, final Configuration configuration) {
        final CommandHandler aboutCommandHandler = new AboutCommandHandler(this, permissionProvider);
        final CommandHandler addCommandHandler = new AddCommandHandler(this, permissionProvider, accountManager, configuration).setupCommand(this, "add");
        final CommandHandler balanceCommandHandler = new BalanceCommandHandler(this, permissionProvider, accountManager, configuration).setupCommand(this, "balance");
        final CommandHandler deductCommandHandler = new DeductCommandHandler(this, permissionProvider, accountManager, configuration).setupCommand(this, "deduct");
        final CommandHandler experienceCommandHandler = new ExperienceCommandHandler(this, permissionProvider).setupCommand(this, "experience");
        final CommandHandler helpCommandHandler = new HelpCommandHandler(this, permissionProvider);
        final CommandHandler payCommandHandler = new PayCommandHandler(this, permissionProvider, accountManager, configuration).setupCommand(this, "pay");
        final CommandHandler reloadCommandHandler = new ReloadCommandHandler(this, permissionProvider);
        final CommandHandler setCommandHandler = new SetCommandHandler(this, permissionProvider, accountManager, configuration).setupCommand(this, "setbalance");
        final CommandHandler syncCommandHandler = new SyncCommandHandler(this, permissionProvider, accountManager, configuration).setupCommand(this, "syncxp");
        final CommandHandler transferCommandHandler = new TransferCommandHandler(this, permissionProvider, accountManager, configuration).setupCommand(this, "transfer");

        // TODO: Move subcommands into a resource file of some sort.
        new XPEconomyCommandHandler(this, permissionProvider).registerSubcommand("about", aboutCommandHandler)
                .registerSubcommand("add", "xpeconomy.balance.add", addCommandHandler, "addbal", "addbalance")
                .registerSubcommand("balance", "xpeconomy.balance", balanceCommandHandler, "bal")
                .registerSubcommand("deduct", "xpeconomy.balance.deduct", deductCommandHandler, "deductbal", "deductbalance", "remove", "removebal", "removebalance")
                .registerSubcommand("experience", experienceCommandHandler, "xpeconomy.experience", "exp", "xp")
                .registerSubcommand("help", helpCommandHandler)
                .registerSubcommand("pay", "xpeconomy.pay", payCommandHandler, true, false)
                .registerSubcommand("reload", "xpeconomy.reload", reloadCommandHandler)
                .registerSubcommand("set", "xpeconomy.balance.set", setCommandHandler, "setbal", "setbalance")
                .registerSubcommand("sync", "xpeconomy.balance.sync", syncCommandHandler, "syncxp")
                .registerSubcommand("transfer", "xpeconomy.balance.transfer", transferCommandHandler, "xfer")
                .setupCommand(this, "xpeconomy");
    }

    private Permission initializePermissionsProvider() {
        final RegisteredServiceProvider<Permission> permissionServiceProvider = this.getServer()
                .getServicesManager()
                .getRegistration(Permission.class);
        return Objects.requireNonNull(Objects.requireNonNull(permissionServiceProvider).getProvider());
    }

    /**
     * Initializes the internationalization handler.
     *
     * @param configuration The configuration instance.
     */
    private I18n initializeI18n(final Configuration configuration) {
        // Initialize internationalization handler.
        final I18n i18n = new I18n(this, configuration);
        i18n.enable();

        return i18n;
    }

    /**
     * Initializes the vault economy.
     *
     * @param configuration The configuration instance.
     * @return The account manager instance.
     */
    private AccountManager initializeEconomy(Configuration configuration) {
        final AccountManager accountManager;
        if (configuration.mysql.enabled.value()) {
            final MySQLConnectionManager connection = new MySQLConnectionManager(this, configuration);
            accountManager = new MySQLAccountManager(configuration, this, connection);
        } else {
            accountManager = new YamlAccountManager(configuration, this);
        }
        accountManager.load();

        final ExperienceEconomy economy = new ExperienceEconomy(this, accountManager, configuration);
        this.getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.High);

        return accountManager;
    }

    /**
     * Registers the various event listeners.
     *
     * @param accountManager The account manager instance.
     */
    private void registerEvents(final AccountManager accountManager, final Permission permission, final Configuration configuration) {
        this.getServer().getPluginManager().registerEvents(new InventoryEventListener(this, accountManager), this);
        this.getServer().getPluginManager().registerEvents(new PlayerEventListener(this, accountManager), this);
        this.getServer().getPluginManager().registerEvents(new WorldEventListener(this, accountManager), this);
        this.getServer()
                .getPluginManager()
                .registerEvents(new ExperienceBottleEventListener(this, accountManager, permission, configuration), this);
    }
}
