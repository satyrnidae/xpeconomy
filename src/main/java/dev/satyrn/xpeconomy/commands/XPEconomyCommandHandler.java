package dev.satyrn.xpeconomy.commands;

import dev.satyrn.xpeconomy.api.commands.CommandHandler;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.utils.Commands;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class XPEconomyCommandHandler extends CommandHandler {
    private final @NotNull CommandHandler balanceCommandHandler;
    private final @NotNull CommandHandler payCommandHandler;

    /**
     * Creates a new command executor.
     * @param permission The permission manager instance.
     */
    public XPEconomyCommandHandler(final @NotNull Permission permission,
                                   final @NotNull CommandHandler balanceCommandHandler,
                                   final @NotNull CommandHandler payCommandHandler) {
        super(permission);
        this.balanceCommandHandler = balanceCommandHandler;
        this.payCommandHandler = payCommandHandler;
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
        final String subCommand = "xpeconomy".equalsIgnoreCase(command.getName()) && args.length >= 1 ? args[0] : "";

        // /xpeconomy balance or /xpeconomy bal or /balance
        if ("balance".equalsIgnoreCase(subCommand)
                || "bal".equalsIgnoreCase(subCommand)
                || "balance".equalsIgnoreCase(command.getName())) {
            return this.balanceCommandHandler.onCommand(sender, command, label, args);
        }
        if("setbalance".equalsIgnoreCase(subCommand)
                || "setbal".equalsIgnoreCase(subCommand)
                || "setbalance".equalsIgnoreCase(command.getName())) {
            //return this.setBalanceCommandHandler.onCommand(sender, command, label, args);
        }
        if ("pay".equalsIgnoreCase(subCommand) || "pay".equalsIgnoreCase(command.getName())) {
            return this.payCommandHandler.onCommand(sender, command, label, args);
        }
        return false;
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
        final List<String> completionOptions = new ArrayList<>();
        if ("xpeconomy".equalsIgnoreCase(command.getName())) {
            if (args.length == 1) {
                completionOptions.add("about");

                if (this.getPermission().has(sender, "xpeconomy.balance")) {
                    completionOptions.add("balance");
                }
                if (this.getPermission().has(sender, "xpeconomy.experience")) {
                    completionOptions.add("experience");
                }
                if (this.getPermission().has(sender, "xpeconomy.give")) {
                    completionOptions.add("give");
                }

                completionOptions.add("help");

                if (sender instanceof Player && this.getPermission().has(sender, "xpeconomy.pay")) {
                    completionOptions.add("pay");
                }
                if (this.getPermission().has(sender, "xpeconomy.balance.set")) {
                    completionOptions.add("setbalance");
                }
                if (this.getPermission().has(sender, "xpeconomy.sync")) {
                    completionOptions.add("sync");
                }
                if (this.getPermission().has(sender, "xpeconomy.take")) {
                    completionOptions.add("take");
                }
            } else if (args.length > 1) {
                final String commandName = args[0].toLowerCase(Locale.ROOT);
                switch (commandName) {
                    case "balance":
                    case "bal":
                        return this.balanceCommandHandler.onTabComplete(sender, command, alias, args);
                    case "setbalance":
                    case "setbal":
                        //return this.setBalanceCommandHandler.onTabComplete(sender, command, alias, args);
                    case "pay":
                        return this.payCommandHandler.onTabComplete(sender, command, alias, args);
                    case "give":
                        if (this.getPermission().has(sender, "xpeconomy.give")) {
                            completionOptions.addAll(Commands.getPlayerNames());
                        }
                        break;
                    case "take":
                        if (this.getPermission().has(sender, "xpeconomy.take")) {
                            completionOptions.addAll(Commands.getPlayerNames());
                        }
                        break;
                    case "sync":
                        if (this.getPermission().has(sender, "xpeconomy.sync")) {
                            if (this.getPermission().has(sender, "xpeconomy.sync.others")) {
                                if (this.getPermission().has(sender, "xpeconomy.sync.all")) {
                                    completionOptions.add("all");
                                }
                                completionOptions.addAll(Commands.getPlayerNames());
                            }
                        }
                        break;
                    case "experience":
                    case "exp":
                    case "xp":
                        if (this.getPermission().has(sender, "xpeconomy.experience")
                                && this.getPermission().has(sender, "xpeconomy.experience.others")) {
                            completionOptions.addAll(Commands.getPlayerNames());
                        }
                        break;
                }
            }
        }
        return completionOptions;
    }
}
