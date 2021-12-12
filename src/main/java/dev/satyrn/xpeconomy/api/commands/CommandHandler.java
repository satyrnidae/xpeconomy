package dev.satyrn.xpeconomy.api.commands;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Handles commands
 */
public abstract class CommandHandler implements CommandExecutor, TabCompleter {
    /**
     * The permission manager instance.
     */
    private final transient @NotNull Permission permission;
    private transient @Nullable String usage;

    /**
     * Initializes a new command handler with the permissions manager instance.
     * @param permission The permission manager instance.
     */
    protected CommandHandler(final @NotNull Permission permission) {
        this.permission = permission;
    }

    /**
     * Sets up a plugin command.
     * @param command The plugin command.
     */
    public final @NotNull CommandHandler setupCommand(final @NotNull PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
        this.setUsage(command.getUsage());
        return this;
    }

    /**
     * Sets up a plugin command by name.
     * @param plugin The plugin instance.
     * @param commandName The command name.
     * @return The command handler instance.
     */
    public final @NotNull CommandHandler setupCommand(final @NotNull Plugin plugin, final @NotNull String commandName) {
        final @NotNull PluginCommand command = Objects.requireNonNull(plugin.getServer().getPluginCommand(commandName));
        return this.setupCommand(command);
    }

    /**
     * Gets the permission manager instance.
     * @return The permission manager instance.
     */
    protected final @NotNull Permission getPermission() {
        return permission;
    }

    /**
     * Gets the command usage hint.
     * @param command The command to default to if the usage is not set on the handler.
     * @return The command usage hint.
     */
    protected @NotNull String getUsage(final @NotNull CommandSender sender, final @NotNull Command command) {
        return this.usage == null || this.usage.isEmpty() ? command.getUsage() : this.usage;
    }

    /**
     * Sets the command usage hint.
     * @param usage The new usage hint.
     */
    public final CommandHandler setUsage(@Nullable String usage) {
        this.usage = usage;
        return this;
    }
}
