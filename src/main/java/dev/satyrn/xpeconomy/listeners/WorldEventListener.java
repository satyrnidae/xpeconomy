package dev.satyrn.xpeconomy.listeners;

import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.tasks.SaveAccountDataTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.PluginBase;

import java.util.logging.Level;

public final class WorldEventListener implements Listener {
    private final PluginBase plugin;
    private final AccountManager accountManager;

    public WorldEventListener(final PluginBase plugin, final AccountManager accountManager) {
        this.plugin = plugin;
        this.accountManager = accountManager;
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent e) {
        this.plugin.getLogger().log(Level.FINE,
                "[Events] World save triggered account data write to disk.");
        new SaveAccountDataTask(this.plugin, this.accountManager).runTaskAsynchronously(this.plugin);
    }
}
