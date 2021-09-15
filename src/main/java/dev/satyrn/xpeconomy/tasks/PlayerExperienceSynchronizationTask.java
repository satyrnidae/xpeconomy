package dev.satyrn.xpeconomy.tasks;

import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Task used to synchronize player experience with a value in an account.
 */
public final class PlayerExperienceSynchronizationTask extends BukkitRunnable {
    /**
     * The plugin instance.
     */
    private final transient Plugin plugin;
    /**
     * The player ID.
     */
    private final transient UUID playerID;
    /**
     * The player's economy account.
     */
    private final transient Account account;

    /**
     * Creates a new player experience synchronization task.
     * @param plugin The plugin instance.
     * @param playerID The player ID.
     * @param account The player's economy account
     */
    public PlayerExperienceSynchronizationTask(final Plugin plugin, final UUID playerID, final Account account) {
        this.plugin = plugin;
        this.playerID = playerID;
        this.account = account;
    }

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        if (this.account != null) {
            this.plugin.getLogger().log(Level.FINE,
                    String.format("[Tasks] Setting player experience to \"%s\"",
                            this.account.getBalance().doubleValue()));
            PlayerXPUtils.setPlayerXPTotal(this.playerID, this.account.getBalance());
        }
    }
}
