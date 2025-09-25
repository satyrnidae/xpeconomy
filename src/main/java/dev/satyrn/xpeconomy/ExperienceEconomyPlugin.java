package dev.satyrn.xpeconomy;

import dev.satyrn.papermc.api.commands.v1.CommandHandler;
import dev.satyrn.papermc.api.lang.v1.I18n;
import dev.satyrn.papermc.api.storage.v1.MySQLConnectionManager;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.commands.*;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.economy.ExperienceEconomy;
import dev.satyrn.xpeconomy.economy.MySQLAccountManager;
import dev.satyrn.xpeconomy.economy.YamlAccountManager;
import dev.satyrn.xpeconomy.listeners.ExperienceBottleEventListener;
import dev.satyrn.xpeconomy.listeners.InventoryEventListener;
import dev.satyrn.xpeconomy.listeners.PlayerEventListener;
import dev.satyrn.xpeconomy.listeners.WorldEventListener;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

/**
 * The main plugin class for Experience Economy
 */
@SuppressWarnings("unused")
public final class ExperienceEconomyPlugin extends JavaPlugin {
    // The internationalization handler instance.
    private I18n i18n;
    // The account manager instance.
    private AccountManager accountManager;
    // The telemetry instance.
    private Metrics metrics;
    // The configuration instance.
    private Configuration configuration;

    /**
     * Occurs when the plugin configuration is reloaded.
     */
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (this.configuration == null) {
            this.configuration = new Configuration(this);
        }
        if (this.configuration.debug.value()) {
            this.getLogger().setLevel(Level.ALL);
        } else {
            this.getLogger().setLevel(Level.INFO);
        }

        if (this.i18n == null && I18n.getInstance() != null) {
            this.i18n = I18n.getInstance();
            this.i18n.setLocale(this.configuration.locale.value());
        }
    }

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
        this.configuration = new Configuration(this);

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

        // bStats metrics
        if (configuration.metrics.value() && this.metrics == null) {
            final Metrics metrics = new Metrics(this, 14015);
            metrics.addCustomChart(new SimplePie("account_storage", () -> configuration.mysql.enabled.value() ? "MySQL" : "YAML"));
            metrics.addCustomChart(new SimplePie("bottle_options_enabled", () -> configuration.bottleOptions.enabled.value() ? "Yes" : "No"));
            metrics.addCustomChart(new SimplePie("economy_method", () -> configuration.economyMethod.value()
                    .toString()
                    .toLowerCase(Locale.ROOT)));
            if (configuration.bottleOptions.enabled.value()) {
                metrics.addCustomChart(new SimplePie("block_to_fill_xp_bottles", () -> configuration.bottleOptions.fillInteractBlock.value().toString().toLowerCase(Locale.ROOT)));
                metrics.addCustomChart(new SimplePie("throw_bottles", () -> configuration.bottleOptions.throwBottles.value() ? "Yes" : "No"));
                metrics.addCustomChart(new SimplePie("refund_thrown_bottles", () -> configuration.bottleOptions.refundThrownBottles.value() ? "Yes" : "No"));
                metrics.addCustomChart(new SimplePie("points_per_bottle", () -> {
                    int pointsPerBottle = configuration.bottleOptions.pointsPerBottle.value();
                    if (pointsPerBottle <= 10) {
                        return "1-10";
                    }
                    if (pointsPerBottle <= 25) {
                        return "11-25";
                    }
                    if (pointsPerBottle <= 50) {
                        return "26-50";
                    }
                    if (pointsPerBottle <= 75) {
                        return "51-75";
                    }
                    if (pointsPerBottle <= 100) {
                        return "76-100";
                    }
                    return ">100 :O";
                }));
            }
            metrics.addCustomChart(new SimplePie("starting_balance_in_levels", () -> {
                final @NotNull EconomyMethod economyMethod = configuration.economyMethod.value();
                final @NotNull BigDecimal startingBalance = configuration.startingBalance.value();
                Pair<BigInteger, BigDecimal> startingLevelProgress = PlayerXPUtils.toLevelProgress(economyMethod.toRawBalance(startingBalance, BigInteger.ZERO));
                int levels = startingLevelProgress.getValue0().intValue();
                if (levels == 0) {
                    if (startingLevelProgress.getValue1().floatValue() <= 0F) {
                        return "None :(";
                    }
                    return "<1";
                }
                if (levels <= 2) {
                    return "1-2";
                }
                if (levels <= 5) {
                    return "3-5";
                }
                if (levels <= 10) {
                    return "6-10";
                }
                if (levels <= 15) {
                    return "11-15";
                }
                if (levels <= 20) {
                    return "16-20";
                } if (levels <= 25) {
                    return "21-25";
                }
                if (levels <= 30) {
                    return "26-30";
                }
                return ">30 :O";
            }));
        }
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
        final CommandHandler reloadCommandHandler = new ReloadCommandHandler(this, permissionProvider, configuration);
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
        final I18n i18n = new I18n(this, "lang");
        i18n.setLocale(configuration.locale.value());
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
            final MySQLConnectionManager connection = new MySQLConnectionManager(this, configuration.mysql);
            accountManager = new MySQLAccountManager(configuration, this, connection);
        } else {
            accountManager = new YamlAccountManager(configuration, this);
        }
        accountManager.load();

        final ExperienceEconomy economy = new ExperienceEconomy(this, accountManager, configuration);
        this.getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Highest);

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
