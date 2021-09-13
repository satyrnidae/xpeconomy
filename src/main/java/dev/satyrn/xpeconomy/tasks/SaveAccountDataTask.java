package dev.satyrn.xpeconomy.tasks;

import dev.satyrn.xpeconomy.api.economy.AccountManager;
import org.bukkit.plugin.PluginBase;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class SaveAccountDataTask extends BukkitRunnable {
    private final transient PluginBase plugin;
    private final transient AccountManager accountManager;

    public SaveAccountDataTask(final PluginBase plugin, final AccountManager accountManager) {
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
        this.plugin.getLogger().log(Level.FINE, "[Tasks] Writing account data to disk.");
        this.accountManager.save();
    }
}
