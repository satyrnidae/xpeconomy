package dev.satyrn.xpeconomy.economy;

import dev.satyrn.papermc.api.storage.v1.ConnectionManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

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
    /**
     * Whether the MySQL server supports UUID functions (8.0+).
     */
    private transient boolean supportsUuidFunctions = false;

    /**
     * Creates a new account manager with a MySQL backend.
     *
     * @param plugin            The plugin instance.
     * @param configuration     The configuration instance.
     * @param connectionManager The connection manager.
     */
    public MySQLAccountManager(final Configuration configuration, final Plugin plugin, final ConnectionManager connectionManager) {
        super(configuration);
        this.plugin = plugin;
        this.connectionManager = connectionManager;
    }

    /**
     * Returns the table prefix.
     *
     * @return The table prefix.
     */
    private String getTablePrefix() {
        return this.configuration.mysql.tablePrefix.value();
    }

    /**
     * Loads account details from the database.
     */
    @Override
    public void load() {
        this.plugin.getLogger().log(Level.FINER, "[Storage] Loading accounts from MySQL data source...");
        try (final Connection connection = this.connectionManager.connect()) {
            if (connection == null) {
                return;
            }
            this.detectMySQLVersion(connection);
            this.createTable(connection);

            final Statement statement = connection.createStatement();
            final String selectQuery;
            if (this.supportsUuidFunctions) {
                selectQuery = String.format("SELECT BIN_TO_UUID(uuid) AS uuid, balance, name FROM %s", this.getTableName());
            } else {
                selectQuery = String.format("SELECT uuid, balance, name FROM %s", this.getTableName());
            }

            try (final ResultSet results = statement.executeQuery(selectQuery)) {
                while (results.next()) {
                    final UUID uuid;
                    if (this.supportsUuidFunctions) {
                        uuid = UUID.fromString(results.getString("uuid"));
                    } else {
                        final byte[] uuidBytes = results.getBytes("uuid");
                        uuid = bytesToUUID(uuidBytes);
                    }
                    final BigDecimal balance = results.getBigDecimal("balance");
                    final String name = results.getString("name");
                    final PlayerAccount account = new PlayerAccount(this.configuration, uuid).setBalanceRaw(balance.setScale(0, RoundingMode.DOWN)
                            .toBigInteger(), false);
                    if (name != null) {
                        account.setName(name);
                    }
                    this.accounts.add(account);
                }
            }
        } catch (final SQLException ex) {
            this.plugin.getLogger()
                    .log(Level.SEVERE, "[Storage] Failed to load account information from the database.", ex);
        }
    }

    /**
     * Saves the accounts to the database in a new thread.
     */
    @Override
    public synchronized void save() {
        this.plugin.getLogger().log(Level.FINER, "[Storage] Saving account data to the MySQL database.");
        try (final Connection connection = this.connectionManager.connect()) {
            if (connection == null) {
                return;
            }
            this.detectMySQLVersion(connection);
            this.createTable(connection);

            final String insertSQLStatement;
            if (this.supportsUuidFunctions) {
                insertSQLStatement = String.format("INSERT INTO %s (uuid, balance, name, create_date, update_date) VALUES (UUID_TO_BIN(?), ?, ?, ?, ?) ON DUPLICATE KEY UPDATE balance = ?, name = ?, update_date = ?", this.getTableName());
            } else {
                insertSQLStatement = String.format("INSERT INTO %s (uuid, balance, name, create_date, update_date) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE balance = ?, name = ?, update_date = ?", this.getTableName());
            }

            try (final PreparedStatement statement = connection.prepareStatement(insertSQLStatement)) {
                int i = 0;
                for (final PlayerAccount account : this.accounts) {
                    final Date currentTime = Calendar.getInstance().getTime();
                    if (this.supportsUuidFunctions) {
                        statement.setString(1, account.getUUID().toString());
                    } else {
                        statement.setBytes(1, uuidToBytes(account.getUUID()));
                    }
                    statement.setBigDecimal(2, new BigDecimal(account.getBalanceRaw()));
                    statement.setString(3, account.getName());
                    statement.setTimestamp(4, new Timestamp(currentTime.getTime()));
                    statement.setTimestamp(5, new Timestamp(currentTime.getTime()));
                    statement.setBigDecimal(6, new BigDecimal(account.getBalanceRaw()));
                    statement.setString(7, account.getName());
                    statement.setTimestamp(8, new Timestamp(currentTime.getTime()));
                    statement.addBatch();
                    if (i++ % 1000 == 0 || i == this.accounts.size()) {
                        statement.executeBatch();
                    }
                }
            }
        } catch (final SQLException ex) {
            this.plugin.getLogger()
                    .log(Level.SEVERE, "[Storage] Failed to save account information to the database.", ex);
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
        try (final ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            if (!resultSet.next()) {
                this.plugin.getLogger()
                        .log(Level.FINER, "[Storage] Account table was not present in the MySQL data source. Creating a new table...");
                final String createTable = String.format("CREATE TABLE %s (uuid VARBINARY(16) NOT NULL, balance NUMERIC NOT NULL, name VARCHAR(16), create_date TIMESTAMP NOT NULL, update_date TIMESTAMP NOT NULL, PRIMARY KEY (uuid))", tableName);
                try (final Statement statement = connection.createStatement()) {
                    statement.execute(createTable);
                }
            } else {
                // Update table to use new date and audit columns.
                try (final ResultSet createDateColumn = metaData.getColumns(null, null, tableName, "create_date")) {
                    if (!createDateColumn.next()) {
                        this.plugin.getLogger()
                                .log(Level.FINER, "[Storage] Account table was present but create date column was missing. Adding new column...");
                        final String createColumn = String.format("ALTER TABLE %s ADD create_date TIMESTAMP NOT NULL", tableName);
                        try (final Statement statement = connection.createStatement()) {
                            statement.execute(createColumn);
                        }
                    }
                }
                try (final ResultSet createDateColumn = metaData.getColumns(null, null, tableName, "update_date")) {
                    if (!createDateColumn.next()) {
                        this.plugin.getLogger()
                                .log(Level.FINER, "[Storage] Account table was present but update date column was missing. Adding new column...");
                        final String createColumn = String.format("ALTER TABLE %s ADD update_date TIMESTAMP NOT NULL", tableName);
                        try (final Statement statement = connection.createStatement()) {
                            statement.execute(createColumn);
                        }
                    }
                }
                try (final ResultSet createDateColumn = metaData.getColumns(null, null, tableName, "name")) {
                    if (!createDateColumn.next()) {
                        this.plugin.getLogger()
                                .log(Level.FINER, "[Storage] Account table was present but name column was missing. Adding new column...");
                        final String createColumn = String.format("ALTER TABLE %s ADD name VARCHAR(16)", tableName);
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
     * Detects the MySQL version and sets the supportsUuidFunctions flag.
     *
     * @param connection The database connection.
     */
    private void detectMySQLVersion(final @NotNull Connection connection) {
        try {
            final DatabaseMetaData metaData = connection.getMetaData();
            final int majorVersion = metaData.getDatabaseMajorVersion();
            this.supportsUuidFunctions = majorVersion >= 8;
            this.plugin.getLogger().log(Level.FINE, String.format("[Storage] Detected MySQL version %d.%d. UUID functions %s.",
                    majorVersion, metaData.getDatabaseMinorVersion(),
                    this.supportsUuidFunctions ? "supported" : "not supported (using polyfill)"));
        } catch (final SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "[Storage] Failed to detect MySQL version. Defaulting to polyfill UUID functions.", ex);
            this.supportsUuidFunctions = false;
        }
    }

    /**
     * Gets the name of the table.
     *
     * @return The name of the table.
     */
    private String getTableName() {
        final StringBuilder tableNameBuilder = new StringBuilder("accounts");
        if (this.getTablePrefix() != null && !this.getTablePrefix().isEmpty()) {
            tableNameBuilder.insert(0, '_').insert(0, this.getTablePrefix());
        }

        return tableNameBuilder.toString();
    }

    /**
     * Converts a UUID to a byte array for storage in MySQL.
     *
     * @param uuid The UUID to convert.
     * @return The byte array representation.
     */
    private static byte[] uuidToBytes(final UUID uuid) {
        final byte[] bytes = new byte[16];
        final long mostSigBits = uuid.getMostSignificantBits();
        final long leastSigBits = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (mostSigBits >>> (8 * (7 - i)));
            bytes[8 + i] = (byte) (leastSigBits >>> (8 * (7 - i)));
        }
        return bytes;
    }

    /**
     * Converts a byte array from MySQL to a UUID.
     *
     * @param bytes The byte array to convert.
     * @return The UUID.
     */
    private static UUID bytesToUUID(final byte[] bytes) {
        long mostSigBits = 0;
        long leastSigBits = 0;
        for (int i = 0; i < 8; i++) {
            mostSigBits = (mostSigBits << 8) | (bytes[i] & 0xff);
            leastSigBits = (leastSigBits << 8) | (bytes[8 + i] & 0xff);
        }
        return new UUID(mostSigBits, leastSigBits);
    }
}
