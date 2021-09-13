package dev.satyrn.xpeconomy.tasks;

import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginBase;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.logging.Level;

public class PlayerBalanceSynchronizationTask extends BukkitRunnable {
    private final PluginBase plugin;
    private final Player player;
    private final Account account;

    public PlayerBalanceSynchronizationTask(final PluginBase plugin, final Player player, final Account account) {
        this.plugin = plugin;
        this.player = player;
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
        final int level = player.getLevel();
        final float progress = player.getExp();

        final BigDecimal currentXPBalance = PlayerXPUtils.getTotalXPValue(level, progress);
        this.plugin.getLogger().log(Level.FINE,
                String.format("[Tasks] Setting player account balance to \"%s\"", currentXPBalance.doubleValue()));
        this.account.setBalance(currentXPBalance);
    }
}
