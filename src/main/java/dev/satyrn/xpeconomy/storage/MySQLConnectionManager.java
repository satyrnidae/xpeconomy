package dev.satyrn.xpeconomy.storage;

import dev.satyrn.xpeconomy.api.configuration.ConfigurationConsumer;
import dev.satyrn.xpeconomy.api.storage.ConnectionManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.utils.ConfigurationConsumerRegistry;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Represents a MySQL connection to a data source.
 */
public final class MySQLConnectionManager implements ConnectionManager, ConfigurationConsumer {
    /**
     * The plugin instance.
     */
    private final transient @NotNull Plugin plugin;
    private transient @Nullable String hostname;
    private transient int port;
    private transient @Nullable String database;
    private transient @NotNull List<Map<?, ?>> flags = new ArrayList<>();
    private transient @Nullable String userID;
    private transient @Nullable String password;

    /**
     * Initializes the connection class.
     *
     * @param plugin        The plugin instance.
     * @param configuration The configuration instance.
     */
    public MySQLConnectionManager(final @NotNull Plugin plugin, final @NotNull Configuration configuration) {
        this.plugin = plugin;
        this.reloadConfiguration(configuration);
        ConfigurationConsumerRegistry.register(this);
    }

    /**
     * Reloads the configuration of this consumer.
     *
     * @param configuration The configuration instance.
     */
    @Override
    public void reloadConfiguration(final @NotNull Configuration configuration) {
        this.hostname = configuration.mysql.hostname.value();
        this.port = configuration.mysql.port.value();
        this.database = configuration.mysql.database.value();
        this.flags = configuration.mysql.flags.value();
        this.userID = configuration.mysql.userID.value();
        this.password = configuration.mysql.password.value();
    }

    /**
     * Connect to the data source.
     *
     * @return The connection to the data source.
     */
    public synchronized Connection connect() {
        StringBuilder connectionURLBuilder = new StringBuilder("jdbc:mysql://")
                .append(this.hostname)
                .append(':')
                .append(this.port)
                .append('/')
                .append(this.database);

        if (!this.flags.isEmpty()) {
            int i = 0;
            for (final Map<?, ?> flags : this.flags) {
                for (final Map.Entry<?, ?> entry : flags.entrySet()) {
                    connectionURLBuilder.append(i++ == 0 ? "?" : "&")
                            .append(entry.getKey().toString())
                            .append("=")
                            .append(entry.getValue().toString());
                }
            }
        }

        final String connectionURL = connectionURLBuilder.toString();
        final Connection connection;
        try {
            this.plugin.getLogger().log(Level.FINE, String.format("[Storage] Connecting to MySQL database at %s with user %s", connectionURL, this.userID));
            connection = DriverManager.getConnection(connectionURL, this.userID, this.password);
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, String.format("[Storage] Failed to connect to the database: %s", ex.getMessage()), ex);
            return null;
        }
        return connection;
    }
}
