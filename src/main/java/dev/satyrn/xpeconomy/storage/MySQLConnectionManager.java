package dev.satyrn.xpeconomy.storage;

import dev.satyrn.xpeconomy.api.storage.ConnectionManager;
import dev.satyrn.xpeconomy.configuration.ExperienceEconomyConfiguration;
import org.bukkit.plugin.PluginBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Represents a MySQL connection to a data source.
 */
public class MySQLConnectionManager implements ConnectionManager {
    /**
     * The plugin instance.
     */
    private final PluginBase plugin;
    /**
     * The configuration instance.
     */
    private final ExperienceEconomyConfiguration configuration;

    /**
     * Initializes the connection class.
     *
     * @param plugin        The plugin instance.
     * @param configuration The configuration instance.
     */
    public MySQLConnectionManager(final ExperienceEconomyConfiguration configuration, final PluginBase plugin) {
        this.plugin = plugin;
        this.configuration = configuration;
    }

    /**
     * Connect to the data source.
     *
     * @return The connection to the data source.
     */
    public Connection connect() {
        StringBuilder connectionURLBuilder = new StringBuilder("jdbc:mysql://")
                .append(this.configuration.mysql.hostname).append(':').append(this.configuration.mysql.port)
                .append('/').append(this.configuration.mysql.database);
        final List<Map<?, ?>> flagsList = this.configuration.mysql.flags.value();
        if (!flagsList.isEmpty()) {
            final Map<?, ?> flags = flagsList.get(0);
            int i = 0;
            for (final Map.Entry<?, ?> entry : flags.entrySet()) {
                connectionURLBuilder.append(i++ == 0 ? "?" : "&")
                        .append(entry.getKey().toString())
                        .append(entry.getValue().toString());
            }
        }

        final String connectionURL = connectionURLBuilder.toString();
        this.plugin.getLogger().log(Level.INFO,
                String.format("[Storage] Attempting connection to database at %s", connectionURL));

        final Connection connection;
        try {
            connection = DriverManager.getConnection(connectionURL, this.configuration.mysql.userID.value(),
                    this.configuration.mysql.password.value());
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE,
                    String.format("[Storage] Failed to connect to the database: %s", ex.getMessage()),
                    ex);
            return null;
        }
        return connection;
    }
}
