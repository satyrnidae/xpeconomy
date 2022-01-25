package dev.satyrn.xpeconomy.listeners;

import dev.satyrn.papermc.api.lang.v1.I18n;
import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Handles experience bottle events.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
public final class ExperienceBottleEventListener implements Listener {
    private final @NotNull Plugin plugin;
    private final @NotNull AccountManager accountManager;
    private final @NotNull Permission permission;
    private final @NotNull Configuration.BottleOptionsContainer bottleOptions;

    public ExperienceBottleEventListener(final @NotNull Plugin plugin, final @NotNull AccountManager accountManager, final @NotNull Permission permission, final @NotNull Configuration configuration) {
        this.plugin = plugin;
        this.accountManager = accountManager;
        this.permission = permission;
        this.bottleOptions = configuration.bottleOptions;
    }

    private boolean isDisabled() {
        return !this.bottleOptions.enabled.value();
    }

    private boolean getThrowBottles() {
        return this.bottleOptions.throwBottles.value();
    }

    private int getPointsPerBottle() {
        return this.bottleOptions.pointsPerBottle.value();
    }

    private boolean getRefundThrownBottles() {
        return this.bottleOptions.refundThrownBottles.value();
    }

    private Material getFillInteractBlock() {
        return this.bottleOptions.fillInteractBlock.value();
    }

    @EventHandler
    public void onExperienceBottleUsed(final @NotNull PlayerInteractEvent event) {
        if (this.isDisabled()) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!event.hasItem() || event.getMaterial() != Material.EXPERIENCE_BOTTLE) {
            return;
        }
        final @NotNull Player player = event.getPlayer();
        final @Nullable ItemStack usedItem = event.getItem();
        if (usedItem == null) {
            return;
        }
        if (this.getThrowBottles() && !player.isSneaking()) {
            return;
        }
        if (!this.permission.has(player, "xpeconomy.bottle.use")) {
            return;
        }
        this.plugin.getLogger()
                .log(Level.FINER, String.format("[Event] Player %s used an experience bottle for %s experience points.", player.getName(), this.getPointsPerBottle()));
        event.setCancelled(true);

        // Decrement inventory
        if (player.getGameMode() != GameMode.CREATIVE) {
            usedItem.setAmount(usedItem.getAmount() - 1);
            final @NotNull ItemStack bottleStack = new ItemStack(Material.GLASS_BOTTLE, 1);
            final @NotNull HashMap<Integer, ItemStack> droppedItems = player.getInventory().addItem(bottleStack);
            if (!droppedItems.isEmpty()) {
                for (int key : droppedItems.keySet()) {
                    player.getWorld()
                            .dropItemNaturally(player.getLocation(), droppedItems.get(key), item -> item.setThrower(player.getUniqueId()));
                }
            }
        }

        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_EMPTY, 1.0F, 1.0F);

        final Account account = this.accountManager.getAccount(player.getUniqueId());
        if (account != null) {
            final BigInteger newBalance = account.getBalanceRaw().add(BigInteger.valueOf(this.getPointsPerBottle()));
            account.setBalanceRaw(newBalance, true);
        } else {
            final BigInteger newXpTotal = PlayerXPUtils.getPlayerXPTotal(player)
                    .add(BigInteger.valueOf(this.getPointsPerBottle()));
            PlayerXPUtils.setPlayerXPTotal(player, newXpTotal);
        }
    }

    @EventHandler
    public void onExperienceBottleBreak(final @NotNull ExpBottleEvent event) {
        if (this.isDisabled() || event.isCancelled()) {
            return;
        }
        this.plugin.getLogger().log(Level.FINEST, "[Event] Setting thrown XP bottle experience points.");
        event.setExperience(this.getPointsPerBottle());

        if (this.getRefundThrownBottles()) {
            final @NotNull ThrownExpBottle bottleEntity = event.getEntity();
            final @Nullable ProjectileSource source = bottleEntity.getShooter();
            if (source instanceof final Player player && this.permission.has(player, "xpeconomy.bottle.refund") && player.getGameMode() != GameMode.CREATIVE) {
                bottleEntity.getWorld().dropItem(bottleEntity.getLocation(), new ItemStack(Material.GLASS_BOTTLE, 1));
                this.plugin.getLogger().log(Level.FINER, "[Event] Refunded a thrown bottle.");
            }
        }
    }

    @EventHandler
    public void onBottleUsed(final @NotNull PlayerInteractEvent event) {
        if (this.isDisabled() || this.getFillInteractBlock() == Material.AIR) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final @Nullable ItemStack usedItem = event.getItem();
        if (!event.hasItem() || usedItem == null || event.getMaterial() != Material.GLASS_BOTTLE) {
            return;
        }
        final @Nullable Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != this.getFillInteractBlock()) {
            return;
        }
        final @NotNull Player player = event.getPlayer();
        if (player.isSneaking() || !this.permission.has(player, "xpeconomy.bottle.fill")) {
            return;
        }
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);

        final @Nullable Account account = this.accountManager.getAccount(player.getUniqueId());
        if (account != null) {
            final BigInteger currentBalance = account.getBalanceRaw();
            if (currentBalance.compareTo(BigInteger.valueOf(this.getPointsPerBottle())) < 0) {
                player.sendMessage(I18n.tr("bottle.fill.lowBalance", EconomyMethod.POINTS.toString(new BigDecimal(currentBalance), true), EconomyMethod.POINTS.toString(BigDecimal.valueOf(this.getPointsPerBottle()), true)));
                return;
            }
            account.setBalanceRaw(currentBalance.subtract(BigInteger.valueOf(this.getPointsPerBottle())), true);
        } else {
            final BigInteger currentXpTotal = PlayerXPUtils.getPlayerXPTotal(player);
            if (currentXpTotal.compareTo(BigInteger.valueOf(this.getPointsPerBottle())) < 0) {
                player.sendMessage(I18n.tr("bottle.fill.lowBalance", EconomyMethod.POINTS.toString(new BigDecimal(currentXpTotal), true), EconomyMethod.POINTS.toString(BigDecimal.valueOf(this.getPointsPerBottle()), true)));
                return;
            }
            PlayerXPUtils.setPlayerXPTotal(player, currentXpTotal.subtract(BigInteger.valueOf(this.getPointsPerBottle())));
        }

        this.plugin.getLogger()
                .log(Level.FINEST, String.format("[Event] Player %s filled an experience bottle at %s with %s experience points.", player.getName(), this.getFillInteractBlock(), this.getPointsPerBottle()));

        if (player.getGameMode() != GameMode.CREATIVE) {
            usedItem.setAmount(usedItem.getAmount() - 1);
        }

        final @NotNull ItemStack expBottleStack = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        final HashMap<Integer, ItemStack> droppedItems = player.getInventory().addItem(expBottleStack);
        if (!droppedItems.isEmpty()) {
            for (int key : droppedItems.keySet()) {
                player.getWorld().dropItemNaturally(player.getLocation(), droppedItems.get(key));
            }
        }

        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0F, 1.0F);
    }
}
