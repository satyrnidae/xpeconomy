package dev.satyrn.xpeconomy.commands;

import dev.satyrn.xpeconomy.api.commands.CommandHandler;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.utils.Commands;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class XPEconomyCommandHandler extends CommandHandler {
    private final @NotNull CommandHandler aboutCommandHandler;
    private final @NotNull CommandHandler addCommandHandler;
    private final @NotNull CommandHandler balanceCommandHandler;
    private final @NotNull CommandHandler deductCommandHandler;
    private final @NotNull CommandHandler experienceCommandHandler;
    private final @NotNull CommandHandler payCommandHandler;

    /**
     * Creates a new command executor.
     * @param permission The permission manager instance.
     */
    public XPEconomyCommandHandler(final @NotNull Permission permission,
                                   final @NotNull CommandHandler aboutCommandHandler,
                                   final @NotNull CommandHandler addCommandHandler,
                                   final @NotNull CommandHandler balanceCommandHandler,
                                   final @NotNull CommandHandler deductCommandHandler,
                                   final @NotNull CommandHandler experienceCommandHandler,
                                   final @NotNull CommandHandler payCommandHandler) {
        super(permission);
        this.aboutCommandHandler = aboutCommandHandler;
        this.addCommandHandler = addCommandHandler;
        this.balanceCommandHandler = balanceCommandHandler;
        this.deductCommandHandler = deductCommandHandler;
        this.experienceCommandHandler = experienceCommandHandler;
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

        switch (subCommand.toLowerCase(Locale.ROOT)) {
            case "about" -> {
                return this.aboutCommandHandler.onCommand(sender, command, label, args);
            }
            case "add", "addbal", "addbalance" -> {
                return this.addCommandHandler.onCommand(sender, command, label, args);
            }
            case "bal", "balance" -> {
                return this.balanceCommandHandler.onCommand(sender, command, label, args);
            }
            case "deduct", "deductbal", "deductbalance", "remove", "removebal", "removebalance" -> {
                return this.deductCommandHandler.onCommand(sender, command, label, args);
            }
            case "deposit", "pay" -> {
                return this.payCommandHandler.onCommand(sender, command, label, args);
            }
            case "exp", "experience", "xp" -> {
                return this.experienceCommandHandler.onCommand(sender, command, label, args);
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
                if (sender instanceof Player && this.getPermission().has(sender, "xpeconomy.pay")) {
                    completionOptions.add("pay");
                }
            } else if (args.length > 1) {
                final String commandName = args[0].toLowerCase(Locale.ROOT);
                switch (commandName) {
                    case "about" -> {
                        return this.aboutCommandHandler.onTabComplete(sender, command, alias, args);
                    }
                    case "add", "addbal", "addbalance" -> {
                        return this.addCommandHandler.onTabComplete(sender, command, alias, args);
                    }
                    case "bal", "balance" -> {
                        return this.balanceCommandHandler.onTabComplete(sender, command, alias, args);
                    }
                    case "deduct", "deductbal", "deductbalance", "remove", "removebal", "removebalance" -> {
                        return this.deductCommandHandler.onTabComplete(sender, command, alias, args);
                    }
                    case "deposit", "pay" -> {
                        return this.payCommandHandler.onTabComplete(sender, command, alias, args);
                    }
                    case "exp", "experience", "xp" -> {
                        return this.experienceCommandHandler.onTabComplete(sender, command, alias, args);
                    }
                    default -> {
                        return new ArrayList<>();
                    }
                }
            }
        }
        return completionOptions;
    }
}
