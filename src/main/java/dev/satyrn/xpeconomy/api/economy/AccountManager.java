package dev.satyrn.xpeconomy.api.economy;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Manages, creates, saves, and loads player accounts.
 */
public interface AccountManager {
    /**
     * Loads player account data from storage.
     *
     * @return Whether the load completed successfully.
     */
    boolean load();

    /**
     * Saves player account data to storage.
     *
     * @return Whether the save completed successfully.
     */
    boolean save();

    /**
     * Checks if an account exists for a given player UUID.
     *
     * @param uuid The player UUID.
     * @return Whether the account exists.
     */
    boolean hasAccount(final UUID uuid);

    /**
     * Creates an account for a player.
     *
     * @param player The player instance.
     * @return The new account.
     */
    Account createAccount(final OfflinePlayer player);

    /**
     * Gets an account with a specific player UUID.
     *
     * @param uuid The player UUID
     * @return The account instance.
     */
    Account getAccount(final UUID uuid);
}
