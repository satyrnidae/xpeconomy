package dev.satyrn.xpeconomy.economy;

import dev.satyrn.xpeconomy.api.storage.ConnectionManager;
import dev.satyrn.xpeconomy.configuration.ExperienceEconomyConfiguration;
import org.bukkit.plugin.PluginBase;

import java.math.BigDecimal;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

/**
 * An account manager with a MySQL backend.
 */
public final class MySQLAccountManager extends AccountManagerBase {
    /**
     * The plugin instance.
     */
    private final transient PluginBase plugin;
    /**
     * The connection manager.
     */
    private final transient ConnectionManager connectionManager;
    private final transient ExperienceEconomyConfiguration configuration;

    /**
     * Creates a new account manager with a MySQL backend.
     *
     * @param plugin            The plugin instance.
     * @param configuration     The configuration instance.
     * @param connectionManager The connection manager.
     */
    public MySQLAccountManager(final ExperienceEconomyConfiguration configuration, final PluginBase plugin,
                               final ConnectionManager connectionManager) {
        super(configuration);
        this.plugin = plugin;
        this.connectionManager = connectionManager;
        this.configuration = configuration;
    }

    /**
     * Loads account details from the database.
     *
     * @return Whether the load succeeded.
     */
    @Override
    public boolean load() {
        try (final Connection connection = this.connectionManager.connect()) {
            if (connection == null) return false;

            this.createTable(connection);

            final Statement statement = connection.createStatement();
            final String selectQuery = "SELECT BIN_TO_UUID(uuid) AS uuid, balance FROM accounts";
            try (final ResultSet results = statement.executeQuery(selectQuery)) {
                while (results.next()) {
                    final String uuid = results.getString("uuid");
                    final BigDecimal balance = results.getBigDecimal("balance");
                    final PlayerAccount account = new PlayerAccount(this.economyMethod, UUID.fromString(uuid))
                            .setBalanceRaw(balance, false);
                    this.accounts.add(account);
                }
            }
        } catch (final SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE,
                    String.format("Failed to load account information from the database: %s", ex.getMessage()), ex);
            return false;
        }
        return true;
    }

    /**
     * Saves the accounts to the database.
     *
     * @return Whether or not the save succeeded
     */
    @Override
    public boolean save() {
        try (final Connection connection = this.connectionManager.connect()) {
            if (connection == null) return false;

            this.createTable(connection);

            final String insertSQLStatement = String.format(
                    "INSERT INTO %s (uuid, balance) VALUES (UUID_TO_BIN(?), ?) ON DUPLICATE KEY UPDATE balance = ?",
                    this.getTableName());
            try (final PreparedStatement statement = connection.prepareStatement(insertSQLStatement)) {
                int i = 0;
                for (final PlayerAccount account : this.accounts) {
                    statement.setString(1, account.getUUID().toString());
                    statement.setBigDecimal(2, account.getBalanceRaw());
                    statement.setBigDecimal(3, account.getBalanceRaw());
                    statement.addBatch();
                    if (i++ % 1000 == 0 || i == this.accounts.size()) {
                        statement.executeBatch();
                    }
                }
            }
        } catch (final SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE,
                    String.format("Failed to save account information to the database: %s", ex.getMessage()), ex);
            return false;
        }
        return true;
    }

    /**
     * Creates the account table if it is not yet present.
     *
     * @param connection The connection instance
     * @throws SQLException An error occurs when either getting
     */
    private void createTable(final Connection connection) throws SQLException {
        final DatabaseMetaData metaData = connection.getMetaData();
        final String tableName = this.getTableName();
        try (final ResultSet resultSet = metaData.getTables(null, null, tableName,
                new String[]{"TABLE"})) {
            if (!resultSet.next()) {
                final String createTable = String.format(
                        "CREATE TABLE %s (uuid VARBINARY(16) NOT NULL, balance NUMERIC NOT NULL, PRIMARY KEY (uuid))",
                        tableName);
                try (final Statement statement = connection.createStatement()) {
                    statement.execute(createTable);
                }
            }
        }
    }

    /**
     * Gets the name of the table.
     *
     * @return The name of the table.
     */
    private String getTableName() {
        final StringBuilder tableNameBuilder = new StringBuilder("accounts");
        final String tablePrefix = this.configuration.mysql.tablePrefix.value();
        if (tablePrefix != null && !tablePrefix.isEmpty()) {
            tableNameBuilder.insert(0, '_').insert(0, tablePrefix);
        }

        return tableNameBuilder.toString();
    }
}
