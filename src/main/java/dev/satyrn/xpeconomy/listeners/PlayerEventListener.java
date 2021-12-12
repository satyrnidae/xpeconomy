package dev.satyrn.xpeconomy.listeners;

import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.tasks.PlayerBalanceSynchronizationTask;
import dev.satyrn.xpeconomy.tasks.PlayerExperienceSynchronizationTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Listener class for player events.
 */
public final class PlayerEventListener implements Listener {
    /**
     * The account manager instance.
     */
    private final transient AccountManager accountManager;
    /**
     * The plugin instance.
     */
    private final transient Plugin plugin;

    /**
     * Initializes the player event listener.
     *
     * @param accountManager The account manager instance.
     */
    public PlayerEventListener(final Plugin plugin,
                               final AccountManager accountManager) {
        this.plugin = plugin;
        this.accountManager = accountManager;
    }

    /**
     * Handles player join events.
     *
     * @param e The event arguments.
     */
    @EventHandler
    public final void onJoin(PlayerJoinEvent e) {
        this.plugin.getLogger().log(Level.INFO,
                "[Events] Player joined world, scheduleing attempt to apply offline balance changes.");

        final UUID uuid = e.getPlayer().getUniqueId();
        if (!this.accountManager.hasAccount(uuid)) {
            this.accountManager.createAccount(e.getPlayer());
        }
        new PlayerExperienceSynchronizationTask(this.plugin, uuid, this.accountManager.getAccount(uuid))
                .runTaskLater(this.plugin, 1L);
    }

    /**
     * Handles player experience value changes.
     *
     * @param e The event arguments.
     */
    @EventHandler
    public final void onExpChange(PlayerExpChangeEvent e) {
        this.plugin.getLogger().log(Level.INFO,
                "[Events] Player Experience Update scheduled account balance synchronization.");

        final UUID uuid = e.getPlayer().getUniqueId();
        new PlayerBalanceSynchronizationTask(this.plugin, e.getPlayer(), this.accountManager.getAccount(uuid))
                .runTaskLater(this.plugin, 1L);
    }
}
