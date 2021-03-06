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
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Handles the Pay command and subcommand.
 */
public final class PayCommandHandler extends AccountCommandHandler {
    // The pattern to match a double.
    private static final @NotNull Pattern DOUBLE_PATTERN = Pattern.compile("\\d*(\\.\\d+)?");

    /**
     * Creates a new pay command handler.
     *
     * @param plugin         The plugin instance
     * @param permission     The permission manager instance
     * @param accountManager The account manager
     * @param configuration  The configuration instance
     */
    public PayCommandHandler(final @NotNull Plugin plugin, final @NotNull Permission permission, final @NotNull AccountManager accountManager, final @NotNull Configuration configuration) {
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

        if (sender instanceof Player) {
            if (args.length != (isSubCommand ? 3 : 2)) {
                sender.sendMessage(I18n.tr("command.generic.usage", this.getUsage(sender, command)));
            } else if (!this.getPermission().has(sender, "xpeconomy.pay")) {
                sender.sendMessage(I18n.tr("command.pay.permission"));
            } else if (!DOUBLE_PATTERN.matcher(args[isSubCommand ? 2 : 1]).matches()) {
                sender.sendMessage(I18n.tr("command.pay.invalidAmount", args[isSubCommand ? 2 : 1]));
            } else {
                final BigDecimal payment = this.getEconomyMethod()
                        .scale(BigDecimal.valueOf(Double.parseDouble(args[isSubCommand ? 2 : 1])));
                final String targetName = args[isSubCommand ? 1 : 0];
                final Optional<OfflinePlayer> result = Commands.getPlayer(targetName);
                if (result.isPresent()) {
                    final OfflinePlayer target = result.get();
                    final UUID senderId = ((Player) sender).getUniqueId();
                    if (target.getUniqueId() != senderId) {
                        Account senderAccount = this.getAccountManager().getAccount(senderId);
                        if (senderAccount != null) {
                            if (senderAccount.has(payment)) {
                                Account targetAccount = this.getAccountManager().getAccount(target.getUniqueId());
                                if (targetAccount != null) {
                                    senderAccount.withdraw(payment);
                                    targetAccount.deposit(payment);
                                    sender.sendMessage(I18n.tr("command.pay.result", target.getName() == null ? targetAccount.getName() : target.getName(), this.getEconomyMethod()
                                            .toString(payment, true)));
                                } else {
                                    sender.sendMessage(I18n.tr("command.generic.invalidTarget.noAccount", target.getName() == null ? target.getUniqueId() : target.getName()));
                                }
                            } else {
                                sender.sendMessage(I18n.tr("command.pay.insufficientBalance"));
                            }
                        } else {
                            sender.sendMessage(I18n.tr("command.generic.invalidSender.noAccount"));
                        }
                    } else {
                        sender.sendMessage(I18n.tr("command.pay.invalidTarget"));
                    }
                } else {
                    sender.sendMessage(I18n.tr("command.generic.invalidTarget", targetName));
                }
            }
        } else {
            sender.sendMessage(I18n.tr("command.pay.invalidSender"));
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
            completionOptions.add(this.getEconomyMethod().toString(BigDecimal.ZERO));
        }

        return completionOptions;
    }

    @Override
    protected @NotNull String getUsage(final @NotNull CommandSender sender, final @NotNull Command command) {
        if ("xpeconomy".equalsIgnoreCase(command.getName())) {
            return I18n.tr("command.pay.usage.subcommand");
        }
        return I18n.tr("command.pay.usage");
    }
}
