package dev.satyrn.xpeconomy.economy;

import dev.satyrn.xpeconomy.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Level;

/**
 * An account manager with a YAML file backend.
 */
public final class YamlAccountManager extends PlayerAccountManagerBase {
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
    public YamlAccountManager(final Configuration configuration, final Plugin plugin) {
        super(configuration);
        this.plugin = plugin;
    }

    /**
     * Loads account data from a YAML file.
     */
    @Override
    public void load() {
        final File configPath = new File(plugin.getDataFolder().getPath() + File.separator + "accounts.yml");
        if (!configPath.exists()) {
            return;
        }

        try {
            this.ymlConfigFile.load(configPath);
        } catch (InvalidConfigurationException | IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "[Storage] Failed to load accounts.yml.", ex);
            return;
        }

        String loadingVersion = this.ymlConfigFile.getString("version");
        if (!Objects.equals(loadingVersion, this.plugin.getDescription().getVersion())) {
            this.plugin.getLogger()
                    .log(Level.WARNING, "[Storage] Account storage file {0} is version {1} (expected {2})... Loading will continue but data loss may occur!", new Object[]{configPath.getPath(), loadingVersion == null ? "unknown" : loadingVersion, this.plugin.getDescription().getVersion()});
        }

        final List<Map<?, ?>> accountsSection = this.ymlConfigFile.getMapList("accounts");
        for (final Map<?, ?> savedAccount : accountsSection) {
            final PlayerAccount account = new PlayerAccount(this.configuration);
            try {
                for (final Map.Entry<?, ?> accountDetails : savedAccount.entrySet()) {
                    if (accountDetails.getKey().toString().equals("uuid")) {
                        account.setUUID(UUID.fromString(accountDetails.getValue().toString()));
                    } else if (accountDetails.getKey().toString().equals("balance")) {
                        final double balance = Double.parseDouble(accountDetails.getValue().toString());
                        account.setBalanceRaw(BigDecimal.valueOf(balance)
                                .setScale(0, RoundingMode.HALF_UP)
                                .toBigInteger(), false);
                    } else if (accountDetails.getKey().toString().equals("name")) {
                        final String name = accountDetails.getValue().toString();
                        account.setName(name);
                    }
                }

                this.plugin.getLogger()
                        .log(Level.FINE, "[Storage] Loaded an account for player {0} with balance {1}", new Object[]{account.getUUID(), account.getBalance().doubleValue()});
                this.accounts.add(account);
            } catch (IllegalArgumentException ex) {
                this.plugin.getLogger()
                        .log(Level.WARNING, "[Storage] Failed to load an account from accounts.yml.", ex);
            }
        }
    }

    /**
     * Saves account data to a YAML file.
     */
    @Override
    public synchronized void save() {
        final File configPath = new File(plugin.getDataFolder().getPath() + File.separator + "accounts.yml");
        final List<Map<?, ?>> mapList = new ArrayList<>();

        this.ymlConfigFile.set("version", this.plugin.getDescription().getVersion());

        for (final PlayerAccount account : this.accounts) {
            final Map<String, String> map = new HashMap<>();
            map.put("uuid", account.getUUID().toString());
            map.put("balance", Double.toString(account.getBalanceRaw().doubleValue()));
            map.put("name", account.getName());
            mapList.add(map);
        }

        this.ymlConfigFile.set("accounts", mapList);

        try {
            this.ymlConfigFile.save(configPath);
        } catch (final IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "[Storage] Failed to save accounts.yml.", ex);
        }
    }
}
