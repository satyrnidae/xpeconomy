package dev.satyrn.xpeconomy.commands;

import dev.satyrn.papermc.api.lang.v1.I18n;
import dev.satyrn.xpeconomy.api.commands.VaultCommandHandler;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Command which can be used to print information about the plugin, such as name, author, and version.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
public final class AboutCommandHandler extends VaultCommandHandler {
    /**
     * Initializes a new command handler with the permission manager instance.
     *
     * @param plugin     The plugin instance.
     * @param permission The permission manager instance.
     */
    public AboutCommandHandler(final @NotNull Plugin plugin, final @NotNull Permission permission) {
        super(plugin, permission);
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
        final PluginDescriptionFile description = this.getPlugin().getDescription();
        final List<String> authors = description.getAuthors();
        final StringBuilder authorsStringBuilder = new StringBuilder();

        for (int index = 0; index < authors.size(); index++) {
            if (index > 0) {
                authorsStringBuilder.append(", ");
            }
            authorsStringBuilder.append(authors.get(index));
        }

        sender.sendMessage(I18n.tr("command.about.result", description.getName(), description.getVersion(), authorsStringBuilder.toString()));
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
        return new ArrayList<>();
    }
}
