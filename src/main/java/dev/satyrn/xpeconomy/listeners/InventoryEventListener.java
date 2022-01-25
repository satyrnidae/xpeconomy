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
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.logging.Level;

public record InventoryEventListener(Plugin plugin, AccountManager accountManager) implements Listener {
    /**
     * Initializes the player event listener.
     *
     * @param accountManager The account manager instance.
     */
    public InventoryEventListener {
    }

    /**
     * Handles the event that is called when a user enchants an item.
     *
     * @param e The event arguments.
     */
    @EventHandler
    public void onEnchantItem(EnchantItemEvent e) {
        if (e.isCancelled()) {
            return;
        }

        this.plugin.getLogger()
                .log(Level.FINER, "[Event] Enchanting table usage scheduled account balance synchronization.");
        final UUID uuid = e.getEnchanter().getUniqueId();
        new PlayerBalanceSynchronizationTask(this.plugin, e.getEnchanter(), this.accountManager.getAccount(uuid)).runTaskLater(this.plugin, 1L);
    }

    /**
     * Handles the event that is called when a user takes or attempts to take an item from the anvil's third slot.
     *
     * @param e The event arguments.
     */
    @EventHandler
    public void onAnvilUsed(InventoryClickEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final HumanEntity whoClicked = e.getWhoClicked();

        if (!(whoClicked instanceof final Player player)) {
            return;
        }

        final Inventory inventory = e.getInventory();

        if (!(inventory instanceof final AnvilInventory anvilInventory)) {
            return;
        }

        final InventoryView view = e.getView();
        final int rawSlot = e.getRawSlot();

        if (rawSlot == view.convertSlot(rawSlot) && rawSlot == 2) {
            final ItemStack result = anvilInventory.getItem(rawSlot);
            if (result != null) {
                this.plugin.getLogger()
                        .log(Level.FINER, "[Event] Anvil usage scheduled account balance synchronization.");

                new PlayerBalanceSynchronizationTask(this.plugin, player, this.accountManager.getAccount(player.getUniqueId())).runTaskLater(this.plugin, 1L);
            }
        }
    }
}
