package dev.satyrn.xpeconomy.commands;

import dev.satyrn.xpeconomy.api.commands.VaultCommandHandler;
import dev.satyrn.xpeconomy.configuration.Configuration;
import dev.satyrn.xpeconomy.lang.I18n;
import dev.satyrn.xpeconomy.utils.ConfigurationConsumerRegistry;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Models a command which allows a user to trigger a configuration reload.
 */
public class ReloadCommandHandler extends VaultCommandHandler {
    /**
     * Initializes a new command handler with the permissions manager instance.
     *
     * @param plugin The plugin instance.
     * @param permission The permission manager instance.
     */
    public ReloadCommandHandler(@NotNull Plugin plugin, @NotNull Permission permission) {
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
        if (!(sender instanceof Player) || this.getPermission().has(sender, "xpeconomy.reload")) {
            sender.sendMessage(I18n.tr("command.reload.start", this.getPlugin().getDescription().getName()));

            this.getPlugin().reloadConfig();

            final Configuration configuration = new Configuration(this.getPlugin());
            if (configuration.debug.value()) {
                this.getPlugin().getLogger().setLevel(Level.ALL);
            } else {
                this.getPlugin().getLogger().setLevel(Level.INFO);
            }

            ConfigurationConsumerRegistry.reloadConfiguration(configuration);

            sender.sendMessage(I18n.tr("command.reload.complete"));
        } else {
            sender.sendMessage(I18n.tr("command.reload.permission"));
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
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
