package dev.satyrn.xpeconomy.commands;

import dev.satyrn.papermc.api.lang.v1.I18n;
import dev.satyrn.xpeconomy.api.commands.AccountCommandHandler;
import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.utils.Commands;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implements a command which can be used to deduct a specific amount from a player's account.
 *
 * @author Isabel Maskrey (saturniidae)
 */
public final class DeductCommandHandler extends AccountCommandHandler {
    /**
     * Creates a new instance of the remove command handler.
     *
     * @param plugin         The plugin instance
     * @param permission     The permission manager object.
     * @param accountManager The account manager instance.
     * @param configuration  The plugin configuration.
     */
    public DeductCommandHandler(final @NotNull Plugin plugin, final @NotNull Permission permission, final @NotNull AccountManager accountManager, final @NotNull Configuration configuration) {
        super(plugin, permission, accountManager, configuration);
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final boolean isSubCommand = "xpeconomy".equalsIgnoreCase(command.getName());
        final int amountArgIndex = isSubCommand ? 1 : 0;
        final int playerArgIndex = isSubCommand ? 2 : 1;

        final Permission permission = this.getPermission();

        if (sender instanceof Player && !permission.has(sender, "xpeconomy.balance.deduct")) {
            sender.sendMessage(I18n.tr("command.balance.deduct.permission"));
            return true;
        }

        if (args.length > playerArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.generic.usage", this.getUsage(sender, command)));
            return true;
        }

        if (args.length < amountArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.balance.deduct.parameter.amount.missing"));
            return true;
        }

        final BigDecimal amount;
        try {
            if (this.getEconomyMethod().getScale() > 0) {
                final double amountArg = Double.parseDouble(args[amountArgIndex]);
                amount = this.getEconomyMethod().scale(BigDecimal.valueOf(amountArg));
            } else {
                final int amountArg = Integer.parseInt(args[amountArgIndex]);
                amount = this.getEconomyMethod().scale(BigDecimal.valueOf(amountArg));
            }
        } catch (NumberFormatException nfe) {
            sender.sendMessage(I18n.tr("command.balance.deduct.parameter.amount.invalid", args[amountArgIndex]));
            return true;
        }

        final OfflinePlayer target;
        if (args.length < playerArgIndex + 1) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(I18n.tr("command.balance.deduct.parameter.player.missing"));
                return true;
            }
        } else {
            final String targetName = args[playerArgIndex];
            final Optional<OfflinePlayer> result = Commands.getPlayer(targetName);
            if (result.isEmpty()) {
                sender.sendMessage(I18n.tr("command.generic.invalidTarget", targetName));
                return true;
            }
            target = result.get();
            if (sender instanceof final Player player && player.getUniqueId() != target.getUniqueId()) {
                if (!permission.has(player, "xpeconomy.balance.deduct.others")) {
                    sender.sendMessage(I18n.tr("command.balance.deduct.permission.others"));
                    return true;
                }
                if (permission.has(target.getPlayer(), "xpeconomy.balance.deduct.exempt") && !permission.has(player, "xpeconomy.balance.deduct.exempt.bypass")) {
                    sender.sendMessage(I18n.tr("xpeconomy.balance.deduct.permission.exempt", target.getName() == null ? target.getUniqueId() : target.getName()));
                    return true;
                }
            }
        }

        final Account account = this.getAccountManager().getAccount(target.getUniqueId());
        if (account == null) {
            if (sender instanceof final Player player && player.getUniqueId() == target.getUniqueId()) {
                sender.sendMessage(I18n.tr("command.generic.invalidSender.noAccount"));
            } else {
                sender.sendMessage(I18n.tr("command.generic.invalidTarget.noAccount", target.getName() == null ? target.getUniqueId() : target.getName()));
            }
            return true;
        }

        if (amount.compareTo(account.getBalance()) > 0) {
            if (sender instanceof final Player player && player.getUniqueId() == target.getUniqueId()) {
                sender.sendMessage(I18n.tr("command.balance.deduct.failure.lowBalance", this.getEconomyMethod()
                        .toString(account.getBalance(), true), this.getEconomyMethod().toString(amount, true)));
            } else {
                sender.sendMessage(I18n.tr("command.balance.deduct.failure.lowBalance.others", target.getName() == null ? target.getUniqueId() : target.getName(), this.getEconomyMethod()
                        .toString(account.getBalance(), true), this.getEconomyMethod().toString(amount, true)));
            }
            return true;
        }

        if (!account.withdraw(amount)) {
            sender.sendMessage(I18n.tr("command.balance.deduct.failure"));
            return true;
        }

        if (sender instanceof final Player player && player.getUniqueId() == target.getUniqueId()) {
            sender.sendMessage(I18n.tr("command.balance.deduct.result", this.getEconomyMethod()
                    .toString(amount, true), this.getEconomyMethod().toString(account.getBalance(), true)));
        } else {
            sender.sendMessage(I18n.tr("command.balance.deduct.result.others", this.getEconomyMethod()
                    .toString(amount, true), target.getName() == null ? account.getName() : target.getName(), this.getEconomyMethod()
                    .toString(account.getBalance(), true)));
        }

        return true;
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param alias   The alias used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed and command label
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final boolean isSubCommand = "xpeconomy".equalsIgnoreCase(command.getName());
        final int amountArgIndex = isSubCommand ? 1 : 0;
        final int playerArgIndex = isSubCommand ? 2 : 1;
        final List<String> completionOptions = new ArrayList<>();

        if (!(sender instanceof Player) || this.getPermission().has(sender, "xpeconomy.balance.deduct")) {
            if (args.length == amountArgIndex + 1) { // Are we currently editing the amount argument?
                completionOptions.add(this.getEconomyMethod().toString(BigDecimal.ZERO));
                completionOptions.add(this.getEconomyMethod().toString(BigDecimal.ONE));
                completionOptions.add(this.getEconomyMethod().toString(BigDecimal.TEN));
            } else if (args.length == playerArgIndex + 1) { // Are we currently editing the player argument?
                if (!(sender instanceof Player) || this.getPermission()
                        .has(sender, "xpeconomy.balance.deduct.others")) {
                    completionOptions.addAll(Commands.getPlayerNames());
                }
            }
        }

        return completionOptions;
    }

    /**
     * Gets the command usage hint.
     *
     * @param sender  Source of the command.
     * @param command The command to default to if the usage is not set on the handler.
     * @return The command usage hint.
     */
    @Override
    protected @NotNull String getUsage(@NotNull CommandSender sender, @NotNull Command command) {
        if ("xpeconomy".equalsIgnoreCase(command.getName())) {
            if (sender instanceof Player) {
                return I18n.tr("command.balance.deduct.usage.subcommand");
            }
            return I18n.tr("command.balance.deduct.usage.subcommand.console");
        }
        if (sender instanceof Player) {
            return I18n.tr("command.balance.deduct.usage");
        }
        return I18n.tr("command.balance.deduct.usage.console");
    }
}
