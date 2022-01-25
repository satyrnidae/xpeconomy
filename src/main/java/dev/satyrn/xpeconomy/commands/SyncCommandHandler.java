package dev.satyrn.xpeconomy.commands;

import dev.satyrn.papermc.api.lang.v1.I18n;
import dev.satyrn.xpeconomy.api.commands.AccountCommandHandler;
import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.utils.Commands;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implements a command which can be used to sync a user's account balance to their current XP balance.
 *
 * @author Isabel Maskrey (saturniidae)
 */
public final class SyncCommandHandler extends AccountCommandHandler {
    /**
     * Creates a new instance of the remove command handler.
     *
     * @param plugin         The plugin instance
     * @param permission     The permission instance
     * @param accountManager The account manager
     * @param configuration  The configuration manager
     */
    public SyncCommandHandler(final @NotNull Plugin plugin, final @NotNull Permission permission, final @NotNull AccountManager accountManager, final @NotNull Configuration configuration) {
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
        final int playerArgIndex = isSubCommand ? 1 : 0;

        final Permission permission = this.getPermission();

        if (sender instanceof Player && !permission.has(sender, "xpeconomy.balance.sync")) {
            sender.sendMessage(I18n.tr("command.balance.sync.permission"));
            return true;
        }

        if (args.length > playerArgIndex + 1) {
            sender.sendMessage(I18n.tr("command.generic.usage", this.getUsage(sender, command)));
            return true;
        }

        final Player target;
        if (args.length < playerArgIndex + 1) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(I18n.tr("command.balance.sync.parameter.player.missing"));
                return true;
            }
        } else {
            final String targetName = args[playerArgIndex];
            final Optional<? extends Player> result = Commands.getOnlinePlayer(targetName);
            if (result.isEmpty()) {
                sender.sendMessage(I18n.tr("command.generic.invalidTarget", targetName));
                return true;
            }
            target = result.get();
            if (sender instanceof final Player player && player.getUniqueId() != target.getUniqueId()) {
                if (!permission.has(player, "xpeconomy.balance.sync.others")) {
                    sender.sendMessage(I18n.tr("command.balance.sync.permission.others"));
                    return true;
                }
            }
        }

        final Account account = this.getAccountManager().getAccount(target.getUniqueId());
        if (account == null) {
            if (sender instanceof final Player player && player.getUniqueId() == target.getUniqueId()) {
                sender.sendMessage(I18n.tr("command.generic.invalidSender.noAccount"));
            } else {
                sender.sendMessage(I18n.tr("command.generic.invalidTarget.noAccount", target.getName()));
            }
            return true;
        }

        account.setBalanceRaw(PlayerXPUtils.getPlayerXPTotal(target), false);

        if (sender instanceof final Player player && player.getUniqueId() == target.getUniqueId()) {
            sender.sendMessage(I18n.tr("command.balance.sync.result", this.getEconomyMethod()
                    .toString(account.getBalance(), true)));
        } else {
            sender.sendMessage(I18n.tr("command.balance.sync.result.others", target.getName(), this.getEconomyMethod()
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
        final int playerArgIndex = isSubCommand ? 1 : 0;
        final List<String> completionOptions = new ArrayList<>();

        if (!(sender instanceof Player) || this.getPermission().has(sender, "xpeconomy.balance.sync")) {
            if (args.length == playerArgIndex + 1) { // Are we currently editing the player argument?
                if (!(sender instanceof Player) || this.getPermission().has(sender, "xpeconomy.balance.sync.others")) {
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
                return I18n.tr("command.balance.sync.usage.subcommand");
            }
            return I18n.tr("command.balance.sync.usage.subcommand.console");
        }
        if (sender instanceof Player) {
            return I18n.tr("command.balance.sync.usage");
        }
        return I18n.tr("command.balance.sync.usage.console");
    }
}
