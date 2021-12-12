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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles the balance command and subcommand.
 */
public final class BalanceCommandHandler extends CommandHandler {
    /**
     * The account manager instance.
     */
    private final transient @NotNull AccountManager accountManager;
    /**
     * The economy method.
     */
    private final transient @NotNull EconomyMethod economyMethod;

    /**
     * Creates a new command executor.
     *
     * @param accountManager The account manager instance.
     * @param permission     The permission manager instance.
     */
    public BalanceCommandHandler(final @NotNull AccountManager accountManager, final @NotNull Permission permission,
                                 final @NotNull EconomyMethod economyMethod) {
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
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command,
                             final @NotNull String label, final @NotNull String[] args) {
        // args is either [balance, player] or [player]
        final boolean isSubCommand = "xpeconomy".equalsIgnoreCase(command.getName());
        final int playerArgument = isSubCommand ? 1 : 0;

        // args length must be between 0 and 1 for the command / subcommand.
        if (args.length < (isSubCommand ? 1 : 0)
                || args.length > (isSubCommand ? 2 : 1)) {
            sender.sendMessage(I18n.tr("command.generic.usage", this.getUsage(sender, command)));
        } else if (sender instanceof final Player player) {
            // Sender is a player
            if (this.getPermission().has(sender, "xpeconomy.balance")) {
                // Sender specified a target
                if (args.length == playerArgument + 1) {
                    final Optional<OfflinePlayer> result = Commands.getPlayer(args[playerArgument]);
                    if (result.isPresent()) {
                        final OfflinePlayer target = result.get();
                        if (target.getUniqueId() == player.getUniqueId()) {
                            // Player targeted themselves.
                            final Account playerAccount = this.accountManager.getAccount(player.getUniqueId());
                            if (playerAccount == null) {
                                sender.sendMessage(I18n.tr("command.generic.invalid_sender.no_account"));
                            } else {
                                sender.sendMessage(I18n.tr("command.balance.result",
                                        this.economyMethod.toString(playerAccount.getBalance(), true)));
                            }
                        } else {
                            // Player target found, check permissions
                            if (!this.getPermission().has(player, "xpeconomy.balance.others")) {
                                // Player cannot check others' account balance.
                                sender.sendMessage(I18n.tr("command.balance.permission.others"));
                            } else if (this.getPermission().has(target.getPlayer(), "xpeconomy.balance.exempt") &&
                                    !this.getPermission().has(player, "xpeconomy.balance.exempt.bypass")) {
                                // Player cannot bypass exempt status
                                sender.sendMessage(I18n.tr("command.balance.permission.exempt", target.getName()));
                            } else {
                                final Account targetAccount = this.accountManager.getAccount(target.getUniqueId());
                                if (targetAccount == null) {
                                    // Target does not have an active account.
                                    sender.sendMessage(I18n.tr("command.generic.invalid_target.no_account",
                                            target.getName()));
                                } else {
                                    sender.sendMessage(I18n.tr("command.balance.result.others", target.getName(),
                                            this.economyMethod.toString(targetAccount.getBalance(), true)));
                                }
                            }
                        }
                    } else {
                        sender.sendMessage(I18n.tr("command.generic.invalid_target", args[playerArgument]));
                    }
                } else {
                    // No target specified, use self.
                    final Account playerAccount = this.accountManager.getAccount(player.getUniqueId());
                    if (playerAccount == null) {
                        sender.sendMessage(I18n.tr("command.generic.invalid_sender.no_account"));
                    } else {
                        sender.sendMessage(I18n.tr("command.balance.result",
                                this.economyMethod.toString(playerAccount.getBalance(), true)));
                    }
                }
            } else {
                sender.sendMessage(I18n.tr("command.balance.permission"));
            }
        } else {
            // Sender is something else so require the Player argument.
            if (args.length != playerArgument + 1) {
                sender.sendMessage("command.generic.invalid_sender.non_player");
            } else {
                final Optional<OfflinePlayer> result = Commands.getPlayer(args[playerArgument]);
                if (result.isPresent()) {
                    final OfflinePlayer target = result.get();
                    final Account targetAccount = this.accountManager.getAccount(target.getUniqueId());
                    if (targetAccount == null) {
                        // Target does not have an active account.
                        sender.sendMessage(I18n.tr("command.generic.invalid_target.no_account", target.getName()));
                    } else {
                        sender.sendMessage(I18n.tr("command.balance.result.others", target.getName(),
                                this.economyMethod.toString(targetAccount.getBalance(), true)));
                    }
                } else {
                    sender.sendMessage(I18n.tr("command.generic.invalid_target", args[playerArgument]));
                }
            }
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
     * @return A List of possible completions for the final argument
     */
    @Override
    public @NotNull List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command,
                                               final @NotNull String alias, final @NotNull String[] args) {
        final int playerArgumentIndex = "xpeconomy".equalsIgnoreCase(command.getName()) ? 2 : 1;
        final List<String> completionOptions = new ArrayList<>();

        if (args.length == playerArgumentIndex) {
            if (sender instanceof final Player player) {
                if (this.getPermission().has(player, "xpeconomy.balance")) {
                    if (this.getPermission().has(player, "xpeconomy.balance.others")) {
                        completionOptions.addAll(Commands.getPlayerNames());
                    } else {
                        completionOptions.add(player.getName());
                    }
                }
            } else {
                completionOptions.addAll(Commands.getPlayerNames());
            }
        }

        return completionOptions;
    }

    @Override
    protected final @NotNull String getUsage(final @NotNull CommandSender sender, final @NotNull Command command) {
        if ("xpeconomy".equalsIgnoreCase(command.getName())) {
            if (sender instanceof Player) {
                return "/xpeconomy balance [player]";
            }
            return "/xpeconomy balance player";
        }
        if (sender instanceof Player) {
            return "/balance [player]";
        }
        return "/balance player";
    }
}
