package dev.satyrn.xpeconomy.listeners;

import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.tasks.SaveAccountDataTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

/**
 * Handles world events.
 */
public final class WorldEventListener implements Listener {
    /**
     * The plugin instance.
     */
    private final transient Plugin plugin;
    /**
     * The account manager instance.
     */
    private final transient AccountManager accountManager;

    /**
     * Creates a new world event listener.
     *
     * @param plugin The plugin instance.
     * @param accountManager The account manager instance.
     */
    public WorldEventListener(final Plugin plugin, final AccountManager accountManager) {
        this.plugin = plugin;
        this.accountManager = accountManager;
    }

    /**
     * Saves the account data on the manager each time the world is saved.
     *
     * @param e The world save event.
     */
    @EventHandler
    public void onWorldSave(WorldSaveEvent e) {
        this.plugin.getLogger().log(Level.FINE,
                "[Events] World save triggered account data write to disk.");
        new SaveAccountDataTask(this.plugin, this.accountManager).runTask(this.plugin);
    }
}
