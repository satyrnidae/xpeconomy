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
 * Models a command which allows a player to check another player's balance.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
public final class BalanceCommandHandler extends AccountCommandHandler {
    /**
     * Creates a new command executor.
     *
     * @param accountManager The account manager instance.
     * @param permission     The permission manager instance.
     */
    public BalanceCommandHandler(final @NotNull Plugin plugin, final @NotNull Permission permission, final @NotNull AccountManager accountManager, final @NotNull Configuration configuration) {
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
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String[] args) {
        // args is either [0: balance, 1: player] or [0: player]
        final boolean isSubCommand = "xpeconomy".equalsIgnoreCase(command.getName());
        final int playerArgIndex = isSubCommand ? 1 : 0;

        final Permission permission = this.getPermission();

        if (sender instanceof Player && !permission.has(sender, "xpeconomy.balance")) {
            sender.sendMessage(I18n.tr("command.balance.permission"));
            return true;
        }

        if (args.length > playerArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.generic.usage", this.getUsage(sender, command)));
            return true;
        }

        final OfflinePlayer target;

        if (args.length < playerArgIndex + 1) {
            if (sender instanceof final Player player) {
                target = player;
            } else {
                sender.sendMessage(I18n.tr("command.balance.parameter.player.missing"));
                return true;
            }
        } else {
            final String playerArg = args[playerArgIndex];
            final Optional<OfflinePlayer> result = Commands.getPlayer(playerArg);
            if (result.isEmpty()) {
                sender.sendMessage(I18n.tr("command.generic.invalidTarget", playerArg));
                return true;
            }
            target = result.get();

            if (sender instanceof final Player player && player.getUniqueId() != target.getUniqueId()) {
                if (!permission.has(sender, "xpeconomy.balance.others")) {
                    sender.sendMessage(I18n.tr("command.balance.permission.others"));
                    return true;
                }

                if (permission.has(target.getPlayer(), "xpeconomy.balance.exempt") && !permission.has(sender, "xpeconomy.balance.exempt.bypass")) {
                    sender.sendMessage(I18n.tr("command.balance.permission.exempt", target.getName() == null ? target.getUniqueId() : target.getName()));
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

        final BigDecimal amount = account.getBalance();

        if (sender instanceof final Player player && player.getUniqueId() == target.getUniqueId()) {
            sender.sendMessage(I18n.tr("command.balance.result", this.getEconomyMethod().toString(amount, true)));
        } else {
            sender.sendMessage(I18n.tr("command.balance.result.others", target.getName() == null ? account.getName() : target.getName(), this.getEconomyMethod()
                    .toString(amount, true)));
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
    public @NotNull List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String alias, final @NotNull String[] args) {
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


    /**
     * Gets the command usage.
     *
     * @param sender  The command sender.
     * @param command The command.
     * @return The command usage.
     */
    @Override
    protected @NotNull String getUsage(final @NotNull CommandSender sender, final @NotNull Command command) {
        if ("xpeconomy".equalsIgnoreCase(command.getName())) {
            if (sender instanceof Player) {
                return I18n.tr("command.balance.usage.subcommand");
            }
            return I18n.tr("command.balance.usage.subcommand.console");
        }
        if (sender instanceof Player) {
            return I18n.tr("command.balance.usage");
        }
        return I18n.tr("command.balance.usage.console");
    }
}
