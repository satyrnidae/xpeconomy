package dev.satyrn.xpeconomy.tasks;

import dev.satyrn.xpeconomy.api.economy.AccountManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public final class SaveAccountDataTask extends BukkitRunnable {
    private final transient Plugin plugin;
    private final transient AccountManager accountManager;

    public SaveAccountDataTask(final Plugin plugin, final AccountManager accountManager) {
        this.plugin = plugin;
        this.accountManager = accountManager;
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
        this.plugin.getLogger().log(Level.FINER, "[Scheduled Task] Saving account data.");
        this.accountManager.save();
    }
}
