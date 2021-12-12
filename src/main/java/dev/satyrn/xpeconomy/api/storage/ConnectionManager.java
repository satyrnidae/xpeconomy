package dev.satyrn.xpeconomy.api.storage;

import org.jetbrains.annotations.Nullable;

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
    @Nullable Connection connect();
}
