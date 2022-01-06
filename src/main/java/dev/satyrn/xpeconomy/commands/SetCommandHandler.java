package dev.satyrn.xpeconomy.commands;

import dev.satyrn.xpeconomy.api.commands.CommandHandler;
import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.lang.I18n;
import dev.satyrn.xpeconomy.utils.Commands;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implements a command which allows a user to set another's balance.
 * @author Isabel Maskrey (saturniidae)
 */
public class SetCommandHandler extends CommandHandler {
    /** The account manager instance. */
    private final transient @NotNull AccountManager accountManager;
    /** The current economy method. */
    private final transient @NotNull EconomyMethod economyMethod;

    public SetCommandHandler(@NotNull Permission permission, @NotNull AccountManager accountManager, @NotNull EconomyMethod economyMethod) {
        super(permission);
        this.accountManager = accountManager;
        this.economyMethod = economyMethod;
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

        if (sender instanceof Player
                && !permission.has(sender, "xpeconomy.balance.set")) {
            sender.sendMessage(I18n.tr("command.balance.set.permission"));
            return true;
        }

        if (args.length > playerArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.generic.usage", this.getUsage(sender, command)));
            return true;
        }

        if (args.length < amountArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.balance.set.parameter.amount.missing"));
            return true;
        }

        final BigDecimal amount;
        try {
            if (this.economyMethod.getScale() > 0) {
                final double amountArg = Double.parseDouble(args[amountArgIndex]);
                amount = this.economyMethod.scale(BigDecimal.valueOf(amountArg));
            } else {
                final int amountArg = Integer.parseInt(args[amountArgIndex]);
                amount = this.economyMethod.scale(BigDecimal.valueOf(amountArg));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(I18n.tr("command.balance.set.parameter.amount.invalid"));
            return true;
        }

        final OfflinePlayer target;
        if (args.length < playerArgIndex + 1) {
            if (sender instanceof Player) {
                target = (Player)sender;
            } else {
                sender.sendMessage(I18n.tr("command.balance.set.parameter.player.missing"));
                return true;
            }
        } else {
            final String targetName = args[playerArgIndex];
            final Optional<OfflinePlayer> result = Commands.getPlayer(targetName);
            if (result.isEmpty()) {
                sender.sendMessage(I18n.tr("command.generic.invalid_target", targetName));
                return true;
            }
            target = result.get();
            if (sender instanceof final Player player
                    && player.getUniqueId() != target.getUniqueId()) {
                if (!permission.has(player, "xpeconomy.balance.set.others")) {
                    sender.sendMessage(I18n.tr("command.balance.set.permission.others"));
                    return true;
                }
                if (permission.has(target.getPlayer(), "xpeconomy.balance.set.exempt")
                        && !permission.has(player, "xpeconomy.balance.set.exempt.bypass")) {
                    sender.sendMessage(I18n.tr("xpeconomy.balance.set.permission.exempt", target.getName()));
                    return true;
                }
            }
        }

        final Account account = this.accountManager.getAccount(target.getUniqueId());
        if (account == null) {
            if (sender instanceof final Player player
                    && player.getUniqueId() == target.getUniqueId()) {
                sender.sendMessage(I18n.tr("command.generic.invalid_sender.no_account"));
            } else {
                sender.sendMessage(I18n.tr("command.generic.invalid_target.no_account", target.getName()));
            }
            return true;
        }

        account.setBalance(amount, true);
        if (sender instanceof final Player player
                && player.getUniqueId() == target.getUniqueId()) {
            sender.sendMessage(I18n.tr("command.balance.set.result",
                    this.economyMethod.toString(account.getBalance(), true)));
        } else {
            sender.sendMessage(I18n.tr("command.balance.set.result.others",
                    target.getName(),
                    this.economyMethod.toString(account.getBalance(), true)));
        }
        return true;
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside of a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param alias   The alias used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed and command label
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final boolean isSubCommand = "xpeconomy".equalsIgnoreCase(command.getName());
        final int amountArgIndex = isSubCommand ? 1 : 0;
        final int playerArgIndex = isSubCommand ? 2 : 1;
        final List<String> completionOptions = new ArrayList<>();

        if (!(sender instanceof Player) || this.getPermission().has(sender, "xpeconomy.balance.set")) {
            if (args.length == amountArgIndex + 1) { // Are we currently editing the amount argument?
                completionOptions.add(this.economyMethod.toString(BigDecimal.ZERO));
                completionOptions.add(this.economyMethod.toString(BigDecimal.ONE));
                completionOptions.add(this.economyMethod.toString(BigDecimal.TEN));
            } else if (args.length == playerArgIndex + 1) { // Are we currently editing the player argument?
                if (!(sender instanceof Player) || this.getPermission().has(sender, "xpeconomy.balance.set.others")) {
                    completionOptions.addAll(Commands.getPlayerNames());
                }
            }
        }

        return completionOptions;
    }

    /**
     * Gets the command usage hint.
     *
     * @param sender The player sender
     * @param command The command to default to if the usage is not set on the handler.
     * @return The command usage hint.
     */
    @Override
    protected @NotNull String getUsage(@NotNull CommandSender sender, @NotNull Command command) {
        if ("xpeconomy".equalsIgnoreCase(command.getName())) {
            if (sender instanceof Player) {
                return I18n.tr("command.balance.set.usage.subcommand");
            }
            return I18n.tr("command.balance.set.usage.subcommand.console");
        }
        if (sender instanceof Player) {
            return I18n.tr("command.balance.set.usage");
        }
        return I18n.tr("command.balance.set.usage.console");
    }
}
