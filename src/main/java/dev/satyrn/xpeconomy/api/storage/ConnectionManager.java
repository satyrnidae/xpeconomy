package dev.satyrn.xpeconomy.api.storage;

import java.sql.Connection;

/**
 * Manages connections to a data source.
 */
public interface ConnectionManager {
    /**
     * Connect to the data source.
     *
     * @return The connection to the data source.
     */
    Connection connect();
}
