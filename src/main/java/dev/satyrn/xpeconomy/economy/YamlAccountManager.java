package dev.satyrn.xpeconomy.economy;

import dev.satyrn.xpeconomy.configuration.ExperienceEconomyConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

/**
 * An account manager with a YAML file backend.
 */
public final class YamlAccountManager extends AccountManagerBase {
    /**
     * The plugin instance.
     */
    private final transient Plugin plugin;
    /**
     * The config file.
     */
    private final transient YamlConfiguration ymlConfigFile = new YamlConfiguration();

    /**
     * Creates a new instance of an account manager with a YAML backend.
     *
     * @param plugin The plugin instance.
     */
    public YamlAccountManager(final ExperienceEconomyConfiguration configuration, final Plugin plugin) {
        super(configuration);
        this.plugin = plugin;
    }

    /**
     * Loads account data from a YAML file.
     *
     */
    @Override
    public void load() {
        final File configPath = new File(plugin.getDataFolder().getPath() + "/accounts.yml");
        if (!configPath.exists()) {
            return;
        }

        try {
            this.ymlConfigFile.load(configPath);
        } catch (InvalidConfigurationException | IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE,
                    String.format("[Accounts] Failed to load accounts.yml: %s", ex.getMessage()), ex);
            return;
        }

        final List<Map<?, ?>> accountsSection = this.ymlConfigFile.getMapList("accounts");
        for (final Map<?, ?> map : accountsSection) {
            final PlayerAccount account = new PlayerAccount(this.economyMethod);
            try {
                for (final Map.Entry<?, ?> savedAccount : map.entrySet()) {
                    if (savedAccount.getKey().toString().equals("uuid")) {
                        account.setUUID(UUID.fromString(savedAccount.getValue().toString()));
                    } else if (savedAccount.getKey().toString().equals("balance")) {
                        final double balance = Double.parseDouble(savedAccount.getValue().toString());
                        account.setBalanceRaw(BigDecimal.valueOf(balance), false);
                    }
                }

                this.plugin.getLogger().log(Level.FINE,
                        String.format("[Accounts] Loaded an account for player %s with balance %s",
                                account.getUUID(), account.getBalance().doubleValue()));
                this.accounts.add(account);
            } catch (IllegalArgumentException ex) {
                this.plugin.getLogger().log(Level.WARNING,
                        String.format("[Accounts] Failed to load an account from accounts.yml: %s", ex.getMessage()),
                        ex);
            }
        }
    }

    /**
     * Saves account data to a YAML file.
     *
     */
    @Override
    public void save() {
        final File configPath = new File(plugin.getDataFolder().getPath() + "/accounts.yml");
        final List<Map<?, ?>> mapList = new ArrayList<>();

        for (final PlayerAccount account : this.accounts) {
            final Map<String, String> map = new HashMap<>();
            map.put("uuid", account.getUUID().toString());
            map.put("balance", Double.toString(account.getBalanceRaw().doubleValue()));
            mapList.add(map);
        }

        this.ymlConfigFile.set("accounts", mapList);

        try {
            this.ymlConfigFile.save(configPath);
        } catch (final IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE,
                    String.format("[Accounts] Failed to save accounts.yml: %s", ex.getMessage()), ex);
        }
    }
}
