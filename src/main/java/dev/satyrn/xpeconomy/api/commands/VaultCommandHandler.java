package dev.satyrn.xpeconomy.api.commands;

import dev.satyrn.papermc.api.commands.v1.CommandHandler;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Handles commands that require access to the vault configuration instance.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
public abstract class VaultCommandHandler extends CommandHandler {
    // The permission manager instance.
    private final transient @NotNull Permission permission;

    /**
     * Initializes a new command handler with the permission manager instance.
     *
     * @param permission The permission manager instance.
     */
    protected VaultCommandHandler(final @NotNull Plugin plugin, final @NotNull Permission permission) {
        super(plugin);
        this.permission = permission;
    }

    /**
     * Gets the permission manager instance.
     *
     * @return The permission manager instance.
     */
    protected final @NotNull Permission getPermission() {
        return permission;
    }
}
