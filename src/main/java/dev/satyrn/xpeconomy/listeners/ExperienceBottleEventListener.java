package dev.satyrn.xpeconomy.listeners;

import dev.satyrn.xpeconomy.api.configuration.ConfigurationConsumer;
import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.lang.I18n;
import dev.satyrn.xpeconomy.utils.ConfigurationConsumerRegistry;
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
public final class ExperienceBottleEventListener implements Listener, ConfigurationConsumer {
    private final @NotNull Plugin plugin;
    private final @NotNull AccountManager accountManager;
    private final @NotNull Permission permission;
    private boolean enabled;
    private boolean throwBottles;
    private int pointsPerBottle;
    private Material fillInteractBlock;
    private boolean refundThrownBottles;

    public ExperienceBottleEventListener(final @NotNull Plugin plugin,
                                         final @NotNull AccountManager accountManager,
                                         final @NotNull Permission permission,
                                         final @NotNull Configuration configuration) {
        this.plugin = plugin;
        this.accountManager = accountManager;
        this.permission = permission;
        this.reloadConfiguration(configuration);
        ConfigurationConsumerRegistry.register(this);
    }

    @Override
    public void reloadConfiguration(final @NotNull Configuration configuration) {
        this.enabled = configuration.bottleOptions.enabled.value();
        this.throwBottles = configuration.bottleOptions.throwBottles.value();
        this.pointsPerBottle = configuration.bottleOptions.pointsPerBottle.value();
        this.fillInteractBlock = configuration.bottleOptions.fillInteractBlock.value();
        this.refundThrownBottles = configuration.bottleOptions.refundThrownBottles.value();
        if (this.pointsPerBottle < 1 && this.enabled) {
            this.plugin.getLogger().log(Level.INFO, "[Configuration] Points per bottle was less than 1, setting to default value 7 instead.");
            this.pointsPerBottle = 7;
        }
    }

    @EventHandler
    public void onExperienceBottleUsed(final @NotNull PlayerInteractEvent event) {
        if (!this.enabled) {
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
        if (this.throwBottles && !player.isSneaking()) {
            return;
        }
        if (!this.permission.has(player, "xpeconomy.bottle.use")) {
            return;
        }
        this.plugin.getLogger().log(Level.FINER, String.format("[Event] Player %s used an experience bottle for %s experience points.", player.getName(), this.pointsPerBottle));
        event.setCancelled(true);

        // Decrement inventory
        if (player.getGameMode() != GameMode.CREATIVE) {
            usedItem.setAmount(usedItem.getAmount() - 1);
            final @NotNull ItemStack bottleStack = new ItemStack(Material.GLASS_BOTTLE, 1);
            final @NotNull HashMap<Integer, ItemStack> droppedItems = player.getInventory().addItem(bottleStack);
            if (!droppedItems.isEmpty()) {
                for (int key : droppedItems.keySet()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), droppedItems.get(key), item -> {
                        item.setThrower(player.getUniqueId());
                    });
                }
            }
        }

        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_EMPTY, 1.0F, 1.0F);

        final Account account = this.accountManager.getAccount(player.getUniqueId());
        if (account != null) {
            final BigInteger newBalance = account.getBalanceRaw().add(BigInteger.valueOf(this.pointsPerBottle));
            account.setBalanceRaw(newBalance, true);
        } else {
            final BigInteger newXpTotal = PlayerXPUtils.getPlayerXPTotal(player).add(BigInteger.valueOf(this.pointsPerBottle));
            PlayerXPUtils.setPlayerXPTotal(player, newXpTotal);
        }
    }

    @EventHandler
    public void onExperienceBottleBreak(final @NotNull ExpBottleEvent event) {
        if (!this.enabled || event.isCancelled()) {
            return;
        }
        this.plugin.getLogger().log(Level.FINER, "[Event] Setting thrown XP bottle experience points.");
        event.setExperience(this.pointsPerBottle);

        if (this.refundThrownBottles) {
            final @NotNull ThrownExpBottle bottleEntity = event.getEntity();
            final @Nullable ProjectileSource source = bottleEntity.getShooter();
            if (source instanceof final Player player && this.permission.has(player, "xpeconomy.bottle.refund")) {
                bottleEntity.getWorld().dropItem(bottleEntity.getLocation(), new ItemStack(Material.GLASS_BOTTLE, 1));
                this.plugin.getLogger().log(Level.FINER, "[Event] Refunded a thrown bottle.");
            }
        }
    }

    @EventHandler
    public void onBottleUsed(final @NotNull PlayerInteractEvent event) {
        if (!this.enabled || this.fillInteractBlock == Material.AIR) {
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
        if (clickedBlock == null || clickedBlock.getType() != this.fillInteractBlock) {
            return;
        }
        final @NotNull Player player = event.getPlayer();
        if (!this.permission.has(player, "xpeconomy.bottle.fill")) {
            return;
        }
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);

        final @Nullable Account account = this.accountManager.getAccount(player.getUniqueId());
        if (account != null) {
            final BigInteger currentBalance = account.getBalanceRaw();
            if (currentBalance.compareTo(BigInteger.valueOf(this.pointsPerBottle)) < 0) {
                player.sendMessage(I18n.tr("bottle.fill.lowBalance",
                        EconomyMethod.POINTS.toString(new BigDecimal(currentBalance), true),
                        EconomyMethod.POINTS.toString(BigDecimal.valueOf(this.pointsPerBottle), true)));
                return;
            }
            account.setBalanceRaw(currentBalance.subtract(BigInteger.valueOf(this.pointsPerBottle)), true);
        } else {
            final BigInteger currentXpTotal = PlayerXPUtils.getPlayerXPTotal(player);
            if (currentXpTotal.compareTo(BigInteger.valueOf(this.pointsPerBottle)) < 0) {
                player.sendMessage(I18n.tr("bottle.fill.lowBalance",
                        EconomyMethod.POINTS.toString(new BigDecimal(currentXpTotal), true),
                        EconomyMethod.POINTS.toString(BigDecimal.valueOf(this.pointsPerBottle), true)));
                return;
            }
            PlayerXPUtils.setPlayerXPTotal(player, currentXpTotal.subtract(BigInteger.valueOf(this.pointsPerBottle)));
        }

        this.plugin.getLogger().log(Level.FINER, String.format("[Event] Player %s filled an experience bottle at %s with %s experience points.", player.getName(), this.fillInteractBlock, this.pointsPerBottle));

        if (player.getGameMode() != GameMode.CREATIVE) {
            usedItem.setAmount(usedItem.getAmount() - 1);

            final @NotNull ItemStack expBottleStack = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
            final HashMap<Integer, ItemStack> droppedItems = player.getInventory().addItem(expBottleStack);
            if (!droppedItems.isEmpty()) {
                for (int key : droppedItems.keySet()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), droppedItems.get(key));
                }
            }
        }

        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0F, 1.0F);
    }
}
