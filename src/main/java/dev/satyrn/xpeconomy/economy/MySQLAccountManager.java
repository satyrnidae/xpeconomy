package dev.satyrn.xpeconomy.economy;

import dev.satyrn.xpeconomy.api.storage.ConnectionManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

/**
 * An account manager with a MySQL backend.
 */
public final class MySQLAccountManager extends PlayerAccountManagerBase {
    /**
     * The plugin instance.
     */
    private final transient Plugin plugin;
    /**
     * The connection manager.
     */
    private final transient ConnectionManager connectionManager;

    private @Nullable String tablePrefix;

    /**
     * Creates a new account manager with a MySQL backend.
     *
     * @param plugin            The plugin instance.
     * @param configuration     The configuration instance.
     * @param connectionManager The connection manager.
     */
    public MySQLAccountManager(final Configuration configuration, final Plugin plugin,
                               final ConnectionManager connectionManager) {
        super(configuration);
        this.plugin = plugin;
        this.connectionManager = connectionManager;
    }

    /**
     * Called when the configuration is reloaded. Sets the state of the consumer based on the new configuration.
     *
     * @param configuration The configuration.
     */
    @Override
    public void reloadConfiguration(@NotNull Configuration configuration) {
        super.reloadConfiguration(configuration);
        this.tablePrefix = configuration.mysql.tablePrefix.value();
    }

    /**
     * Loads account details from the database.
     *
     */
    @Override
    public void load() {
        this.plugin.getLogger().log(Level.FINER, "[Storage] Loading accounts from MySQL data source...");
        try (final Connection connection = this.connectionManager.connect()) {
            if (connection == null) return;
            this.createTable(connection);

            final Statement statement = connection.createStatement();
            final String selectQuery = String.format("SELECT BIN_TO_UUID(uuid) AS uuid, balance FROM %s", this.getTableName());
            try (final ResultSet results = statement.executeQuery(selectQuery)) {
                while (results.next()) {
                    final String uuid = results.getString("uuid");
                    final BigDecimal balance = results.getBigDecimal("balance");
                    final PlayerAccount account = new PlayerAccount(this.configuration, UUID.fromString(uuid))
                            .setBalanceRaw(balance.setScale(0, RoundingMode.DOWN).toBigInteger(), false);
                    this.accounts.add(account);
                }
            }
        } catch (final SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE,
                    String.format("[Storage] Failed to load account information from the database: %s", ex.getMessage()), ex);
        }
    }

    /**
     * Saves the accounts to the database in a new thread.
     *
     */
    @Override
    public synchronized void save() {
        this.plugin.getLogger().log(Level.FINER, "[Storage] Saving account data to the MySQL database.");
        try (final Connection connection = this.connectionManager.connect()) {
            if (connection == null) return;
            this.createTable(connection);

            final String insertSQLStatement = String.format(
                    "INSERT INTO %s (uuid, balance, create_date, update_date) VALUES (UUID_TO_BIN(?), ?, ?, ?) ON DUPLICATE KEY UPDATE balance = ?, update_date = ?",
                    this.getTableName());
            try (final PreparedStatement statement = connection.prepareStatement(insertSQLStatement)) {
                int i = 0;
                for (final PlayerAccount account : this.accounts) {
                    final Date currentTime = Calendar.getInstance().getTime();
                    statement.setString(1, account.getUUID().toString());
                    statement.setBigDecimal(2, new BigDecimal(account.getBalanceRaw()));
                    statement.setTimestamp(3, new Timestamp(currentTime.getTime()));
                    statement.setTimestamp(4, new Timestamp(currentTime.getTime()));
                    statement.setBigDecimal(5, new BigDecimal(account.getBalanceRaw()));
                    statement.setTimestamp(6, new Timestamp(currentTime.getTime()));
                    statement.addBatch();
                    if (i++ % 1000 == 0 || i == this.accounts.size()) {
                        statement.executeBatch();
                    }
                }
            }
        } catch (final SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE,
                    String.format("[Storage] Failed to save account information to the database: %s", ex.getMessage()), ex);
        }
    }

    /**
     * Creates the account table if it is not yet present.
     *
     * @param connection The connection instance
     * @throws SQLException An error occurs when either getting
     */
    private void createTable(final @NotNull Connection connection) throws SQLException {
        this.plugin.getLogger().log(Level.FINER, "[Storage] Ensuring data source integrity.");
        final DatabaseMetaData metaData = connection.getMetaData();
        final String tableName = this.getTableName();
        try (final ResultSet resultSet = metaData.getTables(null, null, tableName,
                new String[]{"TABLE"})) {
            if (!resultSet.next()) {
                this.plugin.getLogger().log(Level.FINER, "[Storage] Account table was not present in the MySQL data source. Creating a new table...");
                final String createTable = String.format(
                        "CREATE TABLE %s (uuid VARBINARY(16) NOT NULL, balance NUMERIC NOT NULL, update_date TIMESTAMP NOT NULL PRIMARY KEY (uuid))",
                        tableName);
                try (final Statement statement = connection.createStatement()) {
                    statement.execute(createTable);
                }
            } else {
                // Update table to use new date and audit columns.
                try (final ResultSet createDateColumn = metaData.getColumns(null, null, tableName, "create_date")) {
                    if (!createDateColumn.next()) {
                        this.plugin.getLogger().log(Level.FINER, "[Storage] Account table was present but create date column was missing. Adding new column...");
                        final String createColumn = String.format("ALTER TABLE %s ADD create_date TIMESTAMP NOT NULL", tableName);
                        try (final Statement statement = connection.createStatement()) {
                            statement.execute(createColumn);
                        }
                    }
                }
                try (final ResultSet createDateColumn = metaData.getColumns(null, null, tableName, "update_date")) {
                    if (!createDateColumn.next()) {
                        this.plugin.getLogger().log(Level.FINER, "[Storage] Account table was present but update date column was missing. Adding new column...");
                        final String createColumn = String.format("ALTER TABLE %s ADD update_date TIMESTAMP NOT NULL", tableName);
                        try (final Statement statement = connection.createStatement()) {
                            statement.execute(createColumn);
                        }
                    }
                }
            }
        }
        this.plugin.getLogger().log(Level.FINEST, "[Storage] Data source integrity verified.");
    }

    /**
     * Gets the name of the table.
     *
     * @return The name of the table.
     */
    private String getTableName() {
        final StringBuilder tableNameBuilder = new StringBuilder("accounts");
        if (this.tablePrefix != null && !this.tablePrefix.isEmpty()) {
            tableNameBuilder.insert(0, '_').insert(0, this.tablePrefix);
        }

        return tableNameBuilder.toString();
    }
}
