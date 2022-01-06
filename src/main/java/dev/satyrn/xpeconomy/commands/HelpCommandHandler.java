package dev.satyrn.xpeconomy.commands;

import dev.satyrn.xpeconomy.api.commands.CommandHandler;
import dev.satyrn.xpeconomy.lang.I18n;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HelpCommandHandler extends CommandHandler {
    private final @NotNull JavaPlugin plugin;

    public HelpCommandHandler(Permission permission,
                              @NotNull JavaPlugin plugin) {
        super(permission);
        this.plugin = plugin;
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

        final Permission permission = this.getPermission();
        final boolean senderIsPlayer = sender instanceof Player;

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
                        sender.sendMessage(I18n.tr("command.help.invalid_page", commandPage));
                        return true;
                    }
                }
            }
        }

        if (page < 1) {
            sender.sendMessage(I18n.tr("command.help.invalid_page", page));
            return true;
        }

        if (commandName == null) {
            final StringBuilder helpBuilder = new StringBuilder(I18n.tr("command.help.result",
                    this.plugin.getDescription().getName(),
                    this.plugin.getDescription().getVersion(),
                    String.join(", ", this.plugin.getDescription().getAuthors())))
                    .append('\n')
                    .append(I18n.tr("command.help.list.about"));
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.add")) {
                helpBuilder.append('\n')
                        .append(I18n.tr("command.help.list.add"));
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance")) {
                helpBuilder.append('\n')
                        .append(I18n.tr("command.help.list.balance"));
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.deduct")) {
                helpBuilder.append('\n')
                        .append(I18n.tr("command.help.list.deduct"));
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.experience")) {
                helpBuilder.append('\n')
                        .append(I18n.tr("command.help.list.experience"));
            }
            helpBuilder.append('\n')
                    .append(I18n.tr("command.help.list.help"));
            if (senderIsPlayer && permission.has(sender, "xpeconomy.pay")) {
                helpBuilder.append('\n')
                        .append(I18n.tr("command.help.list.pay"));
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.set")) {
                helpBuilder.append('\n')
                        .append(I18n.tr("command.help.list.set"));
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.sync")) {
                helpBuilder.append('\n')
                        .append(I18n.tr("command.help.list.sync"));
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.transfer")) {
                helpBuilder.append('\n')
                        .append(I18n.tr("command.help.list.transfer"));
            }

            final ChatPaginator.ChatPage chatPage = ChatPaginator.paginate(helpBuilder.toString(), page, 65, 8);
            if (page > chatPage.getTotalPages()) {
                if (chatPage.getTotalPages() == 1) {
                    sender.sendMessage(I18n.tr("command.help.invalid_page.single", page));
                } else {
                    sender.sendMessage(I18n.tr("command.help.invalid_page.range", page, chatPage.getTotalPages()));
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

    private void getHelpResults(@NotNull String command, @NotNull CommandSender sender, int page) {
        final boolean senderIsPlayer = sender instanceof Player;
        final Permission permission = this.getPermission();
        String commandHelp = null;
        switch (command.toLowerCase(Locale.ROOT)) {
            case "about" -> commandHelp = I18n.tr("command.help.about", I18n.tr("command.about.usage"));
            case "add", "addbal", "addbalance" -> {
                if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.add")) {
                    final StringBuilder usageBuilder = new StringBuilder();
                    if (senderIsPlayer) {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.balance.add.usage"))
                                .append("\n - ")
                                .append(I18n.tr("command.balance.add.usage.subcommand"));
                    } else {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.balance.add.usage.console"))
                                .append("\n - ")
                                .append(I18n.tr("command.balance.add.usage.subcommand.console"));
                    }
                    commandHelp = I18n.tr("command.help.add", usageBuilder.toString());
                }
            }
            case "bal", "balance" -> {
                if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance")) {
                    final StringBuilder usageBuilder = new StringBuilder();
                    if (senderIsPlayer) {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.balance.usage"))
                                .append("\n - ")
                                .append(I18n.tr("command.balance.usage.subcommand"));
                    } else {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.balance.usage.console"))
                                .append("\n - ")
                                .append(I18n.tr("command.balance.usage.subcommand.console"));
                    }
                    commandHelp = I18n.tr("command.help.balance", usageBuilder.toString());
                }
            }
            case "deduct", "deductbal", "deductbalance", "remove", "removebal", "removebalance" -> {
                if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.deduct")) {
                    final StringBuilder usageBuilder = new StringBuilder();
                    if (senderIsPlayer) {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.balance.deduct.usage"))
                                .append("\n - ")
                                .append(I18n.tr("command.balance.deduct.usage.subcommand"));
                    } else {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.balance.deduct.usage.console"))
                                .append("\n - ")
                                .append(I18n.tr("command.balance.deduct.usage.subcommand.console"));
                    }
                    commandHelp = I18n.tr("command.help.deduct", usageBuilder.toString());
                }
            }
            case "exp", "experience", "xp" -> {
                if (!senderIsPlayer || permission.has(sender, "xpeconomy.experience")) {
                    final StringBuilder usageBuilder = new StringBuilder();
                    if (senderIsPlayer) {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.experience.usage"))
                                .append("\n - ")
                                .append(I18n.tr("command.experience.usage.subcommand"));
                    } else {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.experience.usage.console"))
                                .append("\n - ")
                                .append(I18n.tr("command.experience.usage.subcommand.console"));
                    }
                    commandHelp = I18n.tr("command.help.experience", usageBuilder.toString());
                }
            }
            case "help" -> commandHelp = I18n.tr("command.help.help", I18n.tr("command.help.usage"));
            case "pay", "deposit" -> {
                if (senderIsPlayer && permission.has(sender, "xpeconomy.pay")) {
                    String usage = "\n - "
                            + I18n.tr("command.pay.usage")
                            + "\n - "
                            + I18n.tr("command.pay.usage.subcommand");
                    commandHelp = I18n.tr("command.help.pay", usage);
                }
            }
            case "set", "setbal", "setbalance" -> {
                if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.set")) {
                    final StringBuilder usageBuilder = new StringBuilder();
                    if (senderIsPlayer) {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.balance.set.usage"))
                                .append("\n - ")
                                .append(I18n.tr("command.balance.set.usage.subcommand"));
                    } else {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.balance.set.usage.console"))
                                .append("\n - ")
                                .append(I18n.tr("command.balance.set.usage.subcommand.console"));
                    }
                    commandHelp = I18n.tr("command.help.set", usageBuilder);
                }
            }
            case "sync", "syncxp" -> {
                if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.sync")) {
                    final StringBuilder usageBuilder = new StringBuilder();
                    if (senderIsPlayer) {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.balance.sync.usage"))
                                .append("\n - ")
                                .append(I18n.tr("command.balance.sync.usage.subcommand"));
                    } else {
                        usageBuilder.append("\n - ")
                                .append(I18n.tr("command.balance.sync.usage.console"))
                                .append("\n - ")
                                .append(I18n.tr("command.balance.sync.usage.subcommand.console"));
                    }
                    commandHelp = I18n.tr("command.help.sync", usageBuilder.toString());
                }
            }
            case "transfer" -> {
                if (senderIsPlayer && permission.has(sender, "xpeconomy.balance.transfer")) {
                    String usage = "\n - "
                            + I18n.tr("command.balance.transfer.usage")
                            + "\n - "
                            + I18n.tr("command.balance.transfer.usage.subcommand");
                    commandHelp = I18n.tr("command.help.transfer", usage);
                }
            }
        }

        if (commandHelp == null) {
            sender.sendMessage(I18n.tr("command.help.unknown_command", command));
            return;
        }

        final ChatPaginator.ChatPage chatPage = ChatPaginator.paginate(commandHelp, page, 65, 8);
        if (page > chatPage.getTotalPages()) {
            if (chatPage.getTotalPages() == 1) {
                sender.sendMessage(I18n.tr("command.help.invalid_page.single", page));
            } else {
                sender.sendMessage(I18n.tr("command.help.invalid_page.range", page, chatPage.getTotalPages()));
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
        final Permission permission = this.getPermission();
        final boolean senderIsPlayer = sender instanceof Player;

        if (args.length == pageOrCommandIndex + 1) {
            completionOptions.add("about");
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.add")) {
                completionOptions.add("add");
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance")) {
                completionOptions.add("balance");
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.deduct")) {
                completionOptions.add("deduct");
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.experience")) {
                completionOptions.add("experience");
            }
            completionOptions.add("help");
            if (senderIsPlayer && permission.has(sender, "xpeconomy.pay")) {
                completionOptions.add("pay");
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.set")) {
                completionOptions.add("set");
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.sync")) {
                completionOptions.add("sync");
            }
            if (!senderIsPlayer || permission.has(sender, "xpeconomy.balance.transfer")) {
                completionOptions.add("transfer");
            }
        }

        return completionOptions;
    }
}
