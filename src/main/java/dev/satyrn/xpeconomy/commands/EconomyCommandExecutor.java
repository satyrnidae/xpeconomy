package dev.satyrn.xpeconomy.commands;

import dev.satyrn.xpeconomy.api.economy.AccountManager;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class EconomyCommandExecutor implements CommandExecutor {
    /**
     * The account manager instance.
     */
    private final transient AccountManager accountManager;
    /**
     * The plugin instance.
     */
    private final transient Plugin plugin;
    /**
     * The permissions instance.
     */
    private final transient Permission permission;

    /**
     * Creates a new command executor.
     * @param plugin The plugin instance.
     * @param accountManager The account manager instance.
     * @param permission
     */
    public EconomyCommandExecutor(Plugin plugin, AccountManager accountManager, Permission permission) {
        this.plugin = plugin;
        this.accountManager = accountManager;
        this.permission = permission;
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
        if (!"xpeconomy".equalsIgnoreCase(command.getName())) {
            return false;
        }

        return false;
    }
}
