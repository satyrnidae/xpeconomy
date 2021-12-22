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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AddCommandHandler extends CommandHandler {
    private final @NotNull AccountManager accountManager;
    private final @NotNull EconomyMethod economyMethod;

    /**
     * Initializes a new command handler with the permissions manager instance.
     *
     * @param permission The permission manager instance.
     * @param accountManager The account manager instance.
     * @param economyMethod The economy being used.
     */
    public AddCommandHandler(@NotNull Permission permission,
                             @NotNull AccountManager accountManager,
                             @NotNull EconomyMethod economyMethod) {
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

        if (sender instanceof final Player player
                && !permission.has(player, "xpeconomy.balance.add")) {
            sender.sendMessage(I18n.tr("command.balance.add.permission"));
            return true;
        }

        if (args.length > playerArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.generic.usage", this.getUsage(sender, command)));
            return true;
        }

        if (args.length < amountArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.balance.add.parameter.amount.missing"));
            return true;
        }

        final BigDecimal amount;
        try {
            final double amountArg = Double.parseDouble(args[amountArgIndex]);
            amount = BigDecimal.valueOf(amountArg).setScale(this.economyMethod.getScale(), RoundingMode.DOWN);
        } catch (NumberFormatException ex) {
            sender.sendMessage(I18n.tr("command.balance.add.parameter.amount.invalid", args[amountArgIndex]));
            return true;
        }

        final OfflinePlayer target;

        if (args.length < playerArgIndex + 1) {
            if (sender instanceof Player player) {
                target = player;
            } else {
                sender.sendMessage(I18n.tr("command.balance.add.parameter.player.missing"));
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
                if (!permission.has(player, "xpeconomy.balance.add.others")) {
                    sender.sendMessage(I18n.tr("command.balance.add.permission.others"));
                    return true;
                }
                if (permission.has(target.getPlayer(), "xpeconomy.balance.add.exempt")
                        && !permission.has(player, "xpeconomy.balance.add.exempt.bypass")) {
                    sender.sendMessage(I18n.tr("command.balance.add.permission.exempt", target.getName()));
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

        account.deposit(amount);

        if ((sender instanceof final Player player)
                && player.getUniqueId() == target.getUniqueId()) {
            sender.sendMessage(I18n.tr("command.balance.add.result",
                    this.economyMethod.toString(amount, true),
                    this.economyMethod.toString(account.getBalance(), true)));
        } else {
            sender.sendMessage(I18n.tr("command.balance.add.result.others",
                    this.economyMethod.toString(amount, true),
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
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final boolean isSubCommand = "xpeconomy".equalsIgnoreCase(command.getName());
        final int amountArgIndex = isSubCommand ? 1 : 0;
        final int playerArgIndex = isSubCommand ? 2 : 1;
        final List<String> completionOptions = new ArrayList<>();

        if (!(sender instanceof Player) || this.getPermission().has(sender, "xpeconomy.balance.add")) {
            if (args.length == amountArgIndex + 1) {
                completionOptions.add(this.economyMethod.toString(BigDecimal.ZERO));
                completionOptions.add(this.economyMethod.toString(BigDecimal.ONE));
                completionOptions.add(this.economyMethod.toString(BigDecimal.TEN));
            } else if (args.length == playerArgIndex + 1) {
                if (!(sender instanceof Player) || this.getPermission().has(sender, "xpeconomy.balance.add.others")) {
                    completionOptions.addAll(Commands.getPlayerNames());
                }
            }
        }

        return completionOptions;
    }

    @Override
    protected final @NotNull String getUsage(final @NotNull CommandSender sender, final @NotNull Command command) {
        if ("xpeconomy".equalsIgnoreCase(command.getName())) {
            if (sender instanceof Player) {
                return "/xpeconomy add §o§dAMOUNT§r§e [§o§dPLAYER§r§e]";
            }
            return "/xpeconomy add §o§dAMOUNT§r§e §o§dPLAYER§r§e";
        }
        if (sender instanceof Player) {
            return "/addbalance §o§dAMOUNT§r§e [§o§dPLAYER§r§e]";
        }
        return "/addbalance §o§dAMOUNT§r§e §o§dPLAYER§r§e";
    }
}
