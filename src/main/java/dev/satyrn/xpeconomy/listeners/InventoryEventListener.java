package dev.satyrn.xpeconomy.listeners;

import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.tasks.PlayerBalanceSynchronizationTask;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginBase;

import java.util.UUID;
import java.util.logging.Level;

public final class InventoryEventListener implements Listener {
    /**
     * The account manager instance.
     */
    private final AccountManager accountManager;
    private final PluginBase plugin;

    /**
     * Initializes the player event listener.
     *
     * @param accountManager The account manager instance.
     */
    public InventoryEventListener(final PluginBase plugin,
                                  final AccountManager accountManager) {
        this.plugin = plugin;
        this.accountManager = accountManager;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent e) {
        if (e.isCancelled()) return;

        this.plugin.getLogger().log(Level.FINE,
                "[Events] Enchanting table usage scheduled account balance synchronization.");
        final UUID uuid = e.getEnchanter().getUniqueId();
        new PlayerBalanceSynchronizationTask(this.plugin, e.getEnchanter(), this.accountManager.getAccount(uuid))
                .runTaskLater(this.plugin, 1L);
    }

    @EventHandler
    public void onAnvilUsed(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        final HumanEntity whoClicked = e.getWhoClicked();

        if (!(whoClicked instanceof Player)) return;

        final Player player = (Player) whoClicked;
        final Inventory inventory = e.getInventory();

        if (!(inventory instanceof AnvilInventory)) return;

        final AnvilInventory anvilInventory = (AnvilInventory) inventory;
        final InventoryView view = e.getView();
        final int rawSlot = e.getRawSlot();

        if (rawSlot == view.convertSlot(rawSlot) && rawSlot == 2) {
            final ItemStack repairedItem = anvilInventory.getItem(rawSlot);
            if (repairedItem != null) {
                this.plugin.getLogger().log(Level.FINE,
                        "[Events] Anvil usage scheduled account balance synchronization.");

                new PlayerBalanceSynchronizationTask(this.plugin, player,
                        this.accountManager.getAccount(player.getUniqueId())).runTaskLater(this.plugin, 1L);
            }
        }
    }
}
