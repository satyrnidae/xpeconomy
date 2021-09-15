package dev.satyrn.xpeconomy;

import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.commands.EconomyCommandExecutor;
import dev.satyrn.xpeconomy.commands.XPEconomyCommandTabCompleter;
import dev.satyrn.xpeconomy.configuration.ExperienceEconomyConfiguration;
import dev.satyrn.xpeconomy.economy.ExperienceEconomy;
import dev.satyrn.xpeconomy.economy.MySQLAccountManager;
import dev.satyrn.xpeconomy.economy.YamlAccountManager;
import dev.satyrn.xpeconomy.lang.I18n;
import dev.satyrn.xpeconomy.storage.MySQLConnectionManager;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * The main plugin class for Experience Economy
 */
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
        this.saveResource("accounts.yml", false);

        // Initialize configuration handler.
        final ExperienceEconomyConfiguration configuration = new ExperienceEconomyConfiguration(this);

        // Initialize internationalization backend.
        this.i18n = this.initializeI18n(configuration);

        // Setup and register the economy classes.
        this.accountManager = this.initializeEconomy(configuration);

        // Setup and register the permission handler.
        final Permission permissionProvider = this.initializePermissionsProvider();

        this.registerEvents(this.accountManager);
        this.registerCommands(this.accountManager, permissionProvider);
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

        this.i18n.disable();
    }

    private void registerCommands (final AccountManager accountManager, final Permission permissionProvider) {
        final PluginCommand xpEconomyCommand = Objects.requireNonNull(this.getCommand("xpeconomy"));
        final CommandExecutor commandExecutor = new EconomyCommandExecutor(this, accountManager, permissionProvider);

        xpEconomyCommand.setExecutor(commandExecutor);
        xpEconomyCommand.setTabCompleter(new XPEconomyCommandTabCompleter(permissionProvider));
    }

    private Permission initializePermissionsProvider() {
        final RegisteredServiceProvider<Permission> permissionServiceProvider = this.getServer().getServicesManager()
                .getRegistration(Permission.class);
        return Objects.requireNonNull(Objects.requireNonNull(permissionServiceProvider).getProvider());
    }

    /**
     * Initializes the internationalization handler.
     *
     * @param configuration The configuration instance.
     */
    private I18n initializeI18n(final ExperienceEconomyConfiguration configuration) {
        // Initialize internationalization handler.
        final I18n i18n = new I18n(this);
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
    private AccountManager initializeEconomy(ExperienceEconomyConfiguration configuration) {
        final AccountManager accountManager;
        if (configuration.mysql.enabled.value()) {
            final MySQLConnectionManager connection = new MySQLConnectionManager(configuration, this);
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
    private void registerEvents(final AccountManager accountManager) {
        try (final ScanResult scanResult =
                     new ClassGraph().enableClassInfo().acceptPackages("dev.satyrn.xpeconomy.listeners").scan()) {
            final ClassInfoList listenerClasses = scanResult.getClassesImplementing(Listener.class)
                    .filter(classInfo -> classInfo.isStandardClass() && !classInfo.isAbstract());
            for (final ClassInfo classInfo : listenerClasses) {
                try {
                    Constructor<?> ctor = classInfo.loadClass().getConstructor(Plugin.class, AccountManager.class);
                    Listener listener = (Listener) ctor.newInstance(this, accountManager);
                    this.getServer().getPluginManager().registerEvents(listener, this);
                    this.getLogger().log(Level.INFO, String.format("Loaded event listener for %s", classInfo.getName()));
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                    this.getLogger().log(Level.SEVERE, String.format("Failed to load event listener for class %s: %s",
                            classInfo.getName(), ex.getMessage()));
                }
            }
        }
    }
}
