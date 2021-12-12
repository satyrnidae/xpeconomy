package dev.satyrn.xpeconomy.commands;

import dev.satyrn.xpeconomy.api.commands.CommandHandler;
import dev.satyrn.xpeconomy.api.economy.Account;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.lang.I18n;
import dev.satyrn.xpeconomy.utils.Commands;
import net.milkbowl.vault.permission.Permission;
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
import java.util.regex.Pattern;

public class SetBalanceCommandHandler extends CommandHandler {
    /**
     * The account manager instance.
     */
    private final transient AccountManager accountManager;
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("\\d*(\\.\\d+)?");

    public SetBalanceCommandHandler(final AccountManager accountManager, final Permission permission) {
        super(permission);
        this.accountManager = accountManager;
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
        final List<String> completionOptions = new ArrayList<>();

        //TODO: tab completions

        return completionOptions;
    }

    /**
     * Gets the value of the amount argument.
     * @param command The command which was executed.
     * @param args The command arguments.
     * @return The amount to set the balance on the account.
     */
    private String getAmount(final @NotNull Command command, final @NotNull String[] args) {
        final int firstArgument = "xpeconomy".equalsIgnoreCase(command.getName()) ? 2 : 1;
        final int secondArgument = "xpeconomy".equalsIgnoreCase(command.getName()) ? 3 : 2;

        if (args.length > firstArgument) {
            if (!DOUBLE_PATTERN.matcher(args[firstArgument]).matches()) {
                if (args.length > secondArgument && DOUBLE_PATTERN.matcher(args[secondArgument]).matches()) {
                    return args[secondArgument];
                }
            } else {
                return args[firstArgument];
            }
        }
        return null;
    }

    /**
     * Gets the value of the target argument.
     * @param command The command which was executed.
     * @param args The command arguments.
     * @return The amount to set the balance on the account.
     */
    private String getTarget(final @NotNull Command command, final @NotNull String[] args) {
        final int targetArgument = "xpeconomy".equalsIgnoreCase(command.getName()) ? 2 : 1;

        if (args.length > targetArgument) {
            if (!DOUBLE_PATTERN.matcher(args[targetArgument]).matches()) {
                return args[targetArgument];
            }
        }
        return null;
    }
}
