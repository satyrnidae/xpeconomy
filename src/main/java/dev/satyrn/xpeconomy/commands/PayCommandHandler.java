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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Handles the Pay command and subcommand.
 */
public final class PayCommandHandler extends CommandHandler {
    /**
     * The account manager instance.
     */
    private final transient @NotNull AccountManager accountManager;
    private final transient @NotNull EconomyMethod economyMethod;

    private static final @NotNull Pattern DOUBLE_PATTERN = Pattern.compile("\\d*(\\.\\d+)?");

    /**
     * Creates a new pay command handler.
     * @param accountManager The account manager instance.
     * @param permission The permission manager instance.
     */
    public PayCommandHandler(@NotNull final AccountManager accountManager, @NotNull final Permission permission,
                             @NotNull final EconomyMethod economyMethod) {
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

        if (sender instanceof Player) {
            if (args.length != (isSubCommand ? 3 : 2)) {
                sender.sendMessage(I18n.tr("command.generic.usage", this.getUsage(sender, command)));
            } else if (!this.getPermission().has(sender, "xpeconomy.pay")) {
                sender.sendMessage(I18n.tr("command.pay.permission"));
            } else if (!DOUBLE_PATTERN.matcher(args[isSubCommand ? 2 : 1]).matches()) {
                sender.sendMessage(I18n.tr("command.pay.invalid_amount", args[isSubCommand ? 2 : 1]));
            } else {
                final BigDecimal payment = this.economyMethod.scale(BigDecimal.valueOf(
                        Double.parseDouble(args[isSubCommand ? 2 : 1])));
                final String targetName = args[isSubCommand ? 1 : 0];
                final Optional<OfflinePlayer> result = Commands.getPlayer(targetName);
                if (result.isPresent()) {
                    final OfflinePlayer target = result.get();
                    final UUID senderId = ((Player)sender).getUniqueId();
                    if (target.getUniqueId() != senderId) {
                        Account senderAccount = this.accountManager.getAccount(senderId);
                        if (senderAccount != null) {
                            if (senderAccount.has(payment)) {
                                Account targetAccount = this.accountManager.getAccount(target.getUniqueId());
                                if (targetAccount != null) {
                                    senderAccount.withdraw(payment);
                                    targetAccount.deposit(payment);
                                    sender.sendMessage(I18n.tr("command.pay.result", target.getName(),
                                            this.economyMethod.toString(payment, true)));
                                } else {
                                    sender.sendMessage(I18n.tr("command.generic.invalid_target.no_account",
                                            target.getName()));
                                }
                            } else {
                                sender.sendMessage(I18n.tr("command.pay.insufficient_balance"));
                            }
                        } else {
                            sender.sendMessage(I18n.tr("command.generic.invalid_sender.no_account"));
                        }
                    } else {
                        sender.sendMessage(I18n.tr("command.pay.invalid_target"));
                    }
                } else {
                    sender.sendMessage(I18n.tr("command.generic.invalid_target", targetName));
                }
            }
        } else {
            sender.sendMessage(I18n.tr("command.pay.invalid_sender"));
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
     * @return A List of possible completions for the final argument, or null to default to the command executor.
     */
    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> completionOptions = new ArrayList<>();
        final boolean isSubCommand = "xpeconomy".equalsIgnoreCase(command.getName());

        if (args.length == (isSubCommand ? 2 : 1)) {
            // First argument: player name.
            if (!(sender instanceof Player) || this.getPermission().has(sender, "xpeconomy.pay")) {
                completionOptions.addAll(Commands.getPlayerNames());
            }
        } else if (args.length == (isSubCommand ? 3 : 2)) {
            completionOptions.add(this.economyMethod.toString(BigDecimal.ZERO));
        }

        return completionOptions;
    }

    /**
     * Gets the value of the target argument as a string.
     * @param command The command which was executed.
     * @param args The command arguments.
     * @return The string value of the first argument of the command or subcommand.
     */
    private @Nullable String getTarget(@NotNull Command command, @NotNull String[] args) {
        final int argumentIndex = "xpeconomy".equalsIgnoreCase(command.getName()) ? 2 : 1;

        if (args.length > argumentIndex) {
            return args[argumentIndex];
        }
        return null;
    }

    /**
     * Gets the value of the target argument as a string.
     * @param command The command which was executed.
     * @param args The command arguments.
     * @return The string value of the second argument of the command or subcommand.
     */
    private @Nullable String getAmount(@NotNull Command command, @NotNull String[] args) {
        final int argumentIndex = "xpeconomy".equalsIgnoreCase(command.getName()) ? 3 : 2;

        if (args.length > argumentIndex) {
            return args[argumentIndex];
        }
        return null;
    }

    @Override
    protected final @NotNull String getUsage(final @NotNull CommandSender sender, final @NotNull Command command) {
        if ("xpeconomy".equalsIgnoreCase(command.getName())) {
            return "/xpeconomy pay player amount";
        }
        return "/pay player amount";
    }
}
