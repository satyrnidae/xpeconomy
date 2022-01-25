package dev.satyrn.xpeconomy.listeners;

import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.tasks.PlayerBalanceSynchronizationTask;
import dev.satyrn.xpeconomy.tasks.PlayerExperienceSynchronizationTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Listener class for player events.
 */
public record PlayerEventListener(Plugin plugin, AccountManager accountManager) implements Listener {
    /**
     * Initializes the player event listener.
     *
     * @param accountManager The account manager instance.
     */
    public PlayerEventListener {
    }

    /**
     * Handles player join events.
     *
     * @param e The event arguments.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        this.plugin.getLogger()
                .log(Level.FINER, "[Event] Player joined world, scheduling attempt to apply offline balance changes.");

        final UUID uuid = e.getPlayer().getUniqueId();
        @Nullable Account account = this.accountManager.getAccount(uuid);
        if (account == null) {
            account = this.accountManager.createAccount(e.getPlayer());
        }
        account.setName(e.getPlayer().getName());
        new PlayerExperienceSynchronizationTask(this.plugin, uuid, this.accountManager.getAccount(uuid)).runTaskLater(this.plugin, 1L);
    }

    /**
     * Handles player experience value changes.
     *
     * @param e The event arguments.
     */
    @EventHandler
    public void onExpChange(PlayerExpChangeEvent e) {
        this.plugin.getLogger()
                .log(Level.FINER, "[Event] Player Experience Update scheduled account balance synchronization.");

        final UUID uuid = e.getPlayer().getUniqueId();
        new PlayerBalanceSynchronizationTask(this.plugin, e.getPlayer(), this.accountManager.getAccount(uuid)).runTaskLater(this.plugin, 1L);
    }
}
