package dev.satyrn.xpeconomy.commands;

import com.google.gson.*;
import dev.satyrn.xpeconomy.api.commands.VaultCommandHandler;
import dev.satyrn.xpeconomy.lang.I18n;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelpCommandHandler extends VaultCommandHandler {
    private final @NotNull List<HelpProvider> helpProviders = new ArrayList<>();

    /**
     * Creates a handler for the help subcommand.
     *
     * @param plugin The plugin instance.
     * @param permission The permission instance.
     */
    public HelpCommandHandler(final @NotNull Plugin plugin,
                              final @NotNull Permission permission) {
        super(plugin, permission);
        loadHelpProviders();
    }

    private void loadHelpProviders() {
        final @NotNull Logger logger = this.getPlugin().getLogger();
        final @Nullable InputStream helpProvidersStream = this.getClass().getResourceAsStream("/helpProviders.json");

        if (helpProvidersStream == null) {
            logger.log(Level.SEVERE, "[Help] Unable to find help providers file! Plugin help will not work!");
            return;
        }

        try (final @NotNull BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(helpProvidersStream))) {
            final @NotNull JsonArray helpProvidersJsonArray = JsonParser.parseReader(bufferedReader).getAsJsonArray();
            for (final @NotNull JsonElement jsonElement : helpProvidersJsonArray) {
                final @NotNull JsonObject helpProvider = jsonElement.getAsJsonObject();

                final @Nullable String name = helpProvider.get("name").getAsString();
                if (name == null) {
                    logger.log(Level.WARNING, "[Help] Name was not present for a help provider!");
                    continue;
                }

                final @Nullable String permission = helpProvider.has("permission") ? helpProvider.get("permission").getAsString() : null;
                final boolean allowPlayers = !helpProvider.has("allowPlayers") || helpProvider.get("allowPlayers").getAsBoolean();
                final boolean allowNonPlayers = !helpProvider.has("allowNonPlayers") || helpProvider.get("allowNonPlayers").getAsBoolean();
                final @Nullable JsonArray playerUsageKeysJsonArray = helpProvider.has("playerUsageKeys") ? helpProvider.getAsJsonArray("playerUsageKeys") : null;
                final @Nullable JsonArray nonPlayerUsageKeysJsonArray = helpProvider.has("nonPlayerUsageKeys") ? helpProvider.getAsJsonArray("nonPlayerUsageKeys") : null;
                final @Nullable JsonArray aliasesJsonArray = helpProvider.has("aliases") ? helpProvider.getAsJsonArray("aliases") : null;

                final @NotNull List<String> playerUsageKeys = new ArrayList<>();
                if (playerUsageKeysJsonArray != null) {
                    for (JsonElement item : playerUsageKeysJsonArray) {
                        playerUsageKeys.add(item.getAsString());
                    }
                }

                final @NotNull List<String> nonPlayerUsageKeys = new ArrayList<>();
                if (nonPlayerUsageKeysJsonArray != null) {
                    for (JsonElement item : nonPlayerUsageKeysJsonArray) {
                        nonPlayerUsageKeys.add(item.getAsString());
                    }
                }

                final @NotNull List<String> aliases = new ArrayList<>();
                if (aliasesJsonArray != null) {
                    for (JsonElement item : aliasesJsonArray) {
                        aliases.add(item.getAsString());
                    }
                }

                this.helpProviders.add(new HelpProvider(name, permission, allowPlayers, allowNonPlayers, playerUsageKeys, nonPlayerUsageKeys, aliases, this.getPermission()));
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "[Help] Failed to load help providers file. Plugin help command will not work properly.", ex);
        }
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
        boolean isSubCommand = "xpeconomy".equalsIgnoreCase(command.getName());
        final int pageOrCommandIndex = isSubCommand ? 1 : 0;
        final int commandPageIndex = isSubCommand ? 2 : 1;

        int page = 1;
        @Nullable String commandName = null;

        if (args.length > pageOrCommandIndex) {
            final String pageOrCommand = args[pageOrCommandIndex];
            try {
                page = Integer.parseInt(pageOrCommand);
            } catch (NumberFormatException nfe) {
                commandName = pageOrCommand;

                if (args.length > commandPageIndex) {
                    final String commandPage = args[commandPageIndex];
                    try {
                        page = Integer.parseInt(commandPage);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(I18n.tr("command.help.invalidPage", commandPage));
                        return true;
                    }
                }
            }
        }

        if (page < 1) {
            sender.sendMessage(I18n.tr("command.help.invalidPage", page));
            return true;
        }

        if (commandName == null) {
            final StringBuilder helpBuilder = new StringBuilder(I18n.tr("command.help.result",
                    this.getPlugin().getDescription().getName(),
                    this.getPlugin().getDescription().getVersion(),
                    String.join(", ", this.getPlugin().getDescription().getAuthors())));
            for(final @NotNull HelpProvider provider : this.helpProviders) {
                final @Nullable String listEntry = provider.getListEntry(sender);
                if (listEntry != null && !listEntry.isBlank()) {
                    helpBuilder.append('\n').append(listEntry);
                }
            }

            final ChatPaginator.ChatPage chatPage = ChatPaginator.paginate(helpBuilder.toString(), page, 65, 8);
            if (page > chatPage.getTotalPages()) {
                if (chatPage.getTotalPages() == 1) {
                    sender.sendMessage(I18n.tr("command.help.invalidPage.single", page));
                } else {
                    sender.sendMessage(I18n.tr("command.help.invalidPage.range", page, chatPage.getTotalPages()));
                }
                return true;
            }

            final StringBuilder message = new StringBuilder(String.join("\n", chatPage.getLines()));
            if (chatPage.getTotalPages() > 1) {
                message.append("\n")
                        .append(I18n.tr("command.help.list.pagination",
                                chatPage.getPageNumber(),
                                chatPage.getTotalPages()));
            }
            sender.sendMessage(message.toString());
        } else {
            getHelpResults(commandName, sender, page);
        }

        return true;
    }

    /**
     * Gets help on a single command.
     *
     * @param command The command to get help for.
     * @param sender The command sender.
     * @param page The page to display.
     */
    private void getHelpResults(@NotNull String command, @NotNull CommandSender sender, int page) {
        @Nullable String commandHelp = null;

        final @NotNull Optional<HelpProvider> result = this.helpProviders.stream().filter(provider -> provider.isMatch(command)).findFirst();

        if (result.isPresent()) {
            final @NotNull HelpProvider helpProvider = result.get();
            commandHelp = helpProvider.getEntry(sender);
        }

        if (commandHelp == null) {
            sender.sendMessage(I18n.tr("command.help.unknown_command", command));
            return;
        }

        final ChatPaginator.ChatPage chatPage = ChatPaginator.paginate(commandHelp, page, 65, 8);
        if (page > chatPage.getTotalPages()) {
            if (chatPage.getTotalPages() == 1) {
                sender.sendMessage(I18n.tr("command.help.invalidPage.single", page));
            } else {
                sender.sendMessage(I18n.tr("command.help.invalidPage.range", page, chatPage.getTotalPages()));
            }
            return;
        }
        final StringBuilder message = new StringBuilder(String.join("\n", chatPage.getLines()));
        if (chatPage.getTotalPages() > 1) {
            message.append("\n")
                    .append(I18n.tr("command.help.pagination",
                    chatPage.getPageNumber(),
                    chatPage.getTotalPages(),
                    command.toLowerCase(Locale.ROOT)));
        }
        sender.sendMessage(message.toString());
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
        final boolean isSubCommand = "xpeconomy".equalsIgnoreCase(command.getName());
        final int pageOrCommandIndex = isSubCommand ? 1 : 0;
        final List<String> completionOptions = new ArrayList<>();

        if (args.length == pageOrCommandIndex + 1) {
            for(final @NotNull HelpProvider helpProvider : helpProviders) {
                if (helpProvider.isSenderValid(sender) && helpProvider.isAllowed(sender)) {
                    completionOptions.add(helpProvider.name);
                }
            }
        }

        return completionOptions;
    }

    private record HelpProvider(String name,
                                String defaultPermission,
                                boolean allowPlayer,
                                boolean allowNonPlayer,
                                @NotNull List<String> playerUsageKeys,
                                @NotNull List<String> nonPlayerUsageKeys,
                                @NotNull List<String> aliases,
                                @NotNull Permission permission) {
        private boolean isSenderValid(final @NotNull CommandSender sender) {
            return sender instanceof Player ? this.allowPlayer : this.allowNonPlayer;
        }

        private boolean isAllowed(final @NotNull CommandSender sender) {
            return !(sender instanceof Player) || (this.defaultPermission == null || this.defaultPermission.isBlank() || this.permission.has(sender, this.defaultPermission));
        }

        public @Nullable String getListEntry(final @NotNull CommandSender sender) {
            if (this.isSenderValid(sender) && this.isAllowed(sender)) {
                return I18n.tr("command.help.list." + this.name);
            }
            return null;
        }

        private @NotNull String getUsage(final @NotNull CommandSender sender) {
            final @NotNull List<String> usage = sender instanceof Player ? this.playerUsageKeys : this.nonPlayerUsageKeys;
            return String.join("\n", usage.stream().map(key -> String.format(" - %s", I18n.tr(key))).toList());
        }

        public @Nullable String getEntry(final @NotNull CommandSender sender) {
            if (this.isSenderValid(sender) && this.isAllowed(sender)) {
                return I18n.tr("command.help." + this.name, "\n" + this.getUsage(sender));
            }
            return null;
        }

        public boolean isMatch(final @NotNull String name) {
            return this.name.equalsIgnoreCase(name) || this.aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(name));
        }

    }
}
