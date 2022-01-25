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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implements a command which can be used to transfer balance from one player to another.
 * @author Isabel Maskrey (saturniidae)
 */
public final class TransferCommandHandler extends AccountCommandHandler {
    /**
     * Creates a new instance of the remove command handler.
     *
     * @param plugin The plugin instance
     * @param permission The permission manager object
     * @param accountManager The account manager instance
     * @param configuration The current economy method
     */
    public TransferCommandHandler(final @NotNull Plugin plugin,
                                  final @NotNull Permission permission,
                                  final @NotNull AccountManager accountManager,
                                  final @NotNull Configuration configuration) {
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
        final int recipientArgIndex = isSubCommand ? 3 : 2;

        final Permission permission = this.getPermission();

        if (sender instanceof Player
                && !permission.has(sender, "xpeconomy.balance.transfer")) {
            sender.sendMessage(I18n.tr("command.balance.transfer.permission"));
            return true;
        }

        if (args.length > recipientArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.generic.usage", this.getUsage(sender, command)));
            return true;
        }

        if (args.length < amountArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.balance.transfer.parameter.amount.missing"));
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
            sender.sendMessage(I18n.tr("command.balance.transfer.parameter.amount.invalid", args[amountArgIndex]));
            return true;
        }

        final OfflinePlayer target;
        if (args.length < playerArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.balance.transfer.parameter.player.missing"));
            return true;
        } else {
            final String targetName = args[playerArgIndex];
            final Optional<OfflinePlayer> result = Commands.getPlayer(targetName);
            if (result.isEmpty()) {
                sender.sendMessage(I18n.tr("command.generic.invalidTarget", targetName));
                return true;
            }
            target = result.get();
            if (sender instanceof final Player player
                    && player.getUniqueId() != target.getUniqueId()) {
                if (permission.has(target.getPlayer(), "xpeconomy.balance.transfer.exempt")
                        && !permission.has(player, "xpeconomy.balance.transfer.exempt.bypass")) {
                    sender.sendMessage(I18n.tr("xpeconomy.balance.transfer.permission.exempt", target.getName() == null ? target.getUniqueId() : target.getName()));
                    return true;
                }
            }
        }

        final OfflinePlayer recipient;
        if (args.length < recipientArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.balance.transfer.parameter.recipient.missing"));
            return true;
        } else {
            final String targetName = args[playerArgIndex];
            final Optional<OfflinePlayer> result = Commands.getPlayer(targetName);
            if (result.isEmpty()) {
                sender.sendMessage(I18n.tr("command.generic.invalidTarget", targetName));
                return true;
            }
            recipient = result.get();
        }

        if (recipient.getUniqueId() == target.getUniqueId()) {
            sender.sendMessage(I18n.tr("command.balance.transfer.self"));
            return true;
        }

        final Account account = this.getAccountManager().getAccount(target.getUniqueId());
        if (account == null) {
            if (sender instanceof final Player player
                    && player.getUniqueId() == target.getUniqueId()) {
                sender.sendMessage(I18n.tr("command.generic.invalidSender.noAccount"));
            } else {
                sender.sendMessage(I18n.tr("command.generic.invalidTarget.noAccount", target.getName() == null ? target.getUniqueId() : target.getName()));
            }
            return true;
        }

        final Account recipientAccount = this.getAccountManager().getAccount(recipient.getUniqueId());
        if (recipientAccount == null) {
            if (sender instanceof final Player player
                    && player.getUniqueId() == target.getUniqueId()) {
                sender.sendMessage(I18n.tr("command.generic.invalidSender.noAccount"));
            } else {
                sender.sendMessage(I18n.tr("command.generic.invalidTarget.noAccount", recipient.getName() == null ? recipient.getUniqueId() : recipient.getName()));
            }
            return true;
        }

        final BigInteger rawAccountBalance = account.getBalanceRaw();

        if (amount.compareTo(account.getBalance()) > 0) {
            sender.sendMessage(I18n.tr("command.balance.transfer.failure.lowBalance",
                    target.getName() == null ? target.getUniqueId() : target.getName(),
                    this.getEconomyMethod().toString(account.getBalance(), true),
                    this.getEconomyMethod().toString(amount, true)));
            return true;
        }

        if (!account.withdraw(amount)) {
            sender.sendMessage(I18n.tr("command.balance.transfer.failure.withdraw",
                    target.getName() == null ? account.getName() : target.getName()));
            return true;
        }
        if (!recipientAccount.deposit(amount)) {
            account.setBalanceRaw(rawAccountBalance, true);
            sender.sendMessage(I18n.tr("command.balance.transfer.failure.deposit",
                    recipient.getName() == null ? recipientAccount.getName() : recipient.getName(),
                    target.getName() == null ? account.getName() : target.getName()));
            return true;
        }

        sender.sendMessage(I18n.tr("command.balance.transfer.result",
                target.getName() == null ? account.getName() : target.getName(),
                recipient.getName() == null ? recipientAccount.getName() : recipient.getName()));

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
        final int recipientArgIndex = isSubCommand ? 3 : 2;
        final List<String> completionOptions = new ArrayList<>();

        if (!(sender instanceof Player) || this.getPermission().has(sender, "xpeconomy.balance.transfer")) {
            if (args.length == amountArgIndex + 1) { // Are we currently editing the amount argument?
                completionOptions.add(this.getEconomyMethod().toString(BigDecimal.ZERO));
                completionOptions.add(this.getEconomyMethod().toString(BigDecimal.ONE));
                completionOptions.add(this.getEconomyMethod().toString(BigDecimal.TEN));
            } else if (args.length == playerArgIndex + 1 || args.length == recipientArgIndex + 1) { // Are we currently editing the player argument?
                completionOptions.addAll(Commands.getPlayerNames());
            }
        }

        return completionOptions;
    }

    /**
     * Gets the command usage hint.
     *
     * @param sender Source of the command.
     * @param command The command to default to if the usage is not set on the handler.
     * @return The command usage hint.
     */
    @Override
    protected @NotNull String getUsage(@NotNull CommandSender sender, @NotNull Command command) {
        if ("xpeconomy".equalsIgnoreCase(command.getName())) {
            return I18n.tr("command.balance.transfer.usage.subcommand");
        }
        return I18n.tr("command.balance.transfer.usage");
    }
}
