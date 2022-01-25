package dev.satyrn.xpeconomy.commands;

import dev.satyrn.papermc.api.commands.v1.CommandHandler;
import dev.satyrn.papermc.api.lang.v1.I18n;
import dev.satyrn.xpeconomy.api.commands.VaultCommandHandler;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public final class XPEconomyCommandHandler extends VaultCommandHandler {
    // A list of all registered subcommands.
    private final @NotNull List<Subcommand> subcommands = new ArrayList<>();

    /**
     * Creates a new command executor.
     *
     * @param permission The permission manager instance.
     */
    public XPEconomyCommandHandler(final @NotNull Plugin plugin, final @NotNull Permission permission) {
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
        final @NotNull String subcommandAlias = args.length >= 1 ? args[0] : "";

        if (!subcommandAlias.isBlank()) {
            final @NotNull Optional<Subcommand> result = this.subcommands.stream()
                    .filter(subcommand -> subcommand.isMatch(subcommandAlias))
                    .findFirst();

            if (result.isPresent()) {
                final @NotNull Subcommand subcommand = result.get();
                if (!subcommand.isSenderValid(sender)) {
                    sender.sendMessage(I18n.tr(sender instanceof Player ? "command.xpEconomy.invalidSubcommand.requireNonPlayer" : "command.xpEconomy.invalidSubcommand.requirePlayer", subcommand.name()));
                    return true;
                }
                if (!subcommand.isAllowed(sender)) {
                    sender.sendMessage(I18n.tr("command.xpEconomy.invalidSubcommand.permission", subcommand.name()));
                    return true;
                }
                return subcommand.onCommand(sender, command, label, args);
            } else {
                sender.sendMessage(I18n.tr("command.xpEconomy.invalidSubcommand", subcommandAlias));
            }
        }

        sender.sendMessage(I18n.tr("command.xpEconomy.result",
                this.getPlugin().getDescription().getName(),
                this.getPlugin().getDescription().getVersion(),
                String.join(", ", this.getPlugin().getDescription().getAuthors())));
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
        final List<String> completionOptions = new ArrayList<>();
        if (args.length == 1) {
            completionOptions.addAll(this.subcommands.stream()
                    .filter(subcommand -> subcommand.isSenderValid(sender) && subcommand.isAllowed(sender))
                    .map(Subcommand::name)
                    .toList());
        } else if (args.length > 1) {
            final String subcommandAlias = args[0].toLowerCase(Locale.ROOT);
            if (!subcommandAlias.isBlank()) {
                final @NotNull Optional<Subcommand> result = this.subcommands.stream()
                        .filter(subcommand -> subcommand.isMatch(subcommandAlias))
                        .findFirst();
                if (result.isPresent()) {
                    final @NotNull Subcommand subcommand = result.get();
                    if (subcommand.isSenderValid(sender) && subcommand.isAllowed(sender)) {
                        final @Nullable List<String> subcommandCompletionOptions = subcommand.onTabComplete(sender, command, alias, args);
                        if (subcommandCompletionOptions != null && !subcommandCompletionOptions.isEmpty()) {
                            completionOptions.addAll(subcommandCompletionOptions);
                        }
                    }
                }
            }
        }

        return completionOptions;
    }

    /**
     * Adds a new subcommand to the command handler.
     *
     * @param name           The name of the subcommand. Registration will fail if this matches any existing subcommand's name or aliases.
     * @param commandHandler The command handler instance.
     * @return The command handler instance.
     */
    @Contract(value = "_, _, _ -> this", mutates = "this")
    public XPEconomyCommandHandler registerSubcommand(final @NotNull String name, final @NotNull CommandHandler commandHandler, final @NotNull String... aliases) {
        return this.registerSubcommand(name, null, commandHandler, aliases);
    }

    /**
     * Adds a new subcommand to the command handler.
     *
     * @param name              The name of the subcommand. Registration will fail if this matches any existing subcommand's name or aliases.
     * @param commandHandler    The command handler instance.
     * @param defaultPermission The default permission to allow access to the command.
     * @return The command handler instance.
     */
    @Contract(value = "_, _, _, _ -> this", mutates = "this")
    public XPEconomyCommandHandler registerSubcommand(final @NotNull String name,
                                                      final @Nullable String defaultPermission,
                                                      final @NotNull CommandHandler commandHandler,
                                                      final @NotNull String... aliases) {
        return this.registerSubcommand(name, defaultPermission, commandHandler, true, true, aliases);
    }

    /**
     * Adds a new subcommand to the command handler.
     *
     * @param name              The name of the subcommand. Registration will fail if this matches any existing subcommand's name or aliases.
     * @param commandHandler    The command handler instance.
     * @param defaultPermission The default permission to allow access to the command.
     * @param allowPlayer       {@code true} if the command should require a player to invoke; otherwise, {@code false}
     * @param aliases           A list of aliases for the command. This will fail if any of these match any existing subcommand's name or alias.
     * @return The command handler instance.
     */
    @Contract(value = "_, _, _, _, _, _ -> this", mutates = "this")
    public XPEconomyCommandHandler registerSubcommand(final @NotNull String name,
                                                      final @Nullable String defaultPermission,
                                                      final @NotNull CommandHandler commandHandler,
                                                      final boolean allowPlayer,
                                                      final boolean allowNonPlayer,
                                                      final @NotNull String... aliases) {
        if (this.subcommands.stream()
                .noneMatch(subcommand -> subcommand.isMatch(name) || Arrays.stream(aliases)
                        .anyMatch(subcommand::isMatch))) {
            this.subcommands.add(new Subcommand(name, defaultPermission, commandHandler, this.getPermission(), allowPlayer, allowNonPlayer, aliases));
        } else {
            this.getPlugin()
                    .getLogger()
                    .log(Level.SEVERE, String.format("[Command] Failed to register subcommand: %s! A matching subcommand has already been registered!", name));
            throw new RuntimeException(String.format("Duplicate subcommand registered: %s", name));
        }
        return this;
    }

    private record Subcommand(@NotNull String name,
                              @Nullable String defaultPermission,
                              @NotNull CommandHandler commandHandler,
                              @NotNull Permission permission,
                              boolean allowPlayer,
                              boolean allowNonPlayer,
                              @NotNull String[] aliases) implements TabCompleter, CommandExecutor {
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
            return this.commandHandler.onCommand(sender, command, label, args);
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
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            return this.commandHandler.onTabComplete(sender, command, alias, args);
        }

        /**
         * Checks if a given subcommand matches this record.
         *
         * @param subcommand The subcommand to execute.
         * @return {@code true} if the subcommand is a match; otherwise, {@code false}
         */
        public boolean isMatch(final @NotNull String subcommand) {
            return this.name.equalsIgnoreCase(subcommand) || Arrays.stream(this.aliases)
                    .anyMatch(alias -> alias.equalsIgnoreCase(subcommand));
        }

        /**
         * Checks if a given command sender is valid for the subcommand.
         *
         * @param sender The sender to use.
         * @return {@code true} if the sender can execute the subcommand; otherwise, {@code false}
         */
        public boolean isSenderValid(final @NotNull CommandSender sender) {
            return sender instanceof Player ? this.allowPlayer : this.allowNonPlayer;
        }

        /**
         * Checks if a given player has access to this subcommand.
         *
         * @param sender The player who sent the subcommand.
         * @return {@code true} if the player may execute the subcommand; otherwise, {@code false}
         */
        public boolean isAllowed(final @NotNull CommandSender sender) {
            return !(sender instanceof Player) || (this.defaultPermission == null || this.defaultPermission.isBlank() || this.permission.has(sender, this.defaultPermission));
        }
    }
}
