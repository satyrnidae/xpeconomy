package dev.satyrn.xpeconomy.commands;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class XPEconomyCommandTabCompleter implements TabCompleter {
    private final transient Permission permission;

    public XPEconomyCommandTabCompleter(Permission permission) {
        this.permission = permission;
    }

    private static Collection<String> getAllPlayerNames() {
        final OfflinePlayer[] players = Bukkit.getOfflinePlayers();
        return Arrays.stream(players)
                .filter(player -> player.getName() != null)
                .map(player -> Objects.requireNonNull(player.getName()))
                .sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    private static Collection<String> getOnlinePlayerNames() {
        final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        return players.stream()
                .map(HumanEntity::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
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
        Bukkit.getLogger().info("SENDER - " + sender.getName() + " CMD - " + command.getName() + " ARGS - " + args.length);
        if ("xpeconomy".equalsIgnoreCase(command.getName())) {
            if (args.length == 1 || args.length == 2 && "help".equalsIgnoreCase(args[0])) {
                completionOptions.add("about");

                if (this.permission.has(sender, "xpeconomy.balance")) {
                    completionOptions.add("balance");
                }
                if (this.permission.has(sender, "xpeconomy.experience")) {
                    completionOptions.add("experience");
                }
                if (this.permission.has(sender, "xpeconomy.give")) {
                    completionOptions.add("give");
                }

                completionOptions.add("help");

                if (sender instanceof Player && this.permission.has(sender, "xpeconomy.pay")) {
                    completionOptions.add("pay");
                }
                if (this.permission.has(sender, "xpeconomy.balance.set")) {
                    completionOptions.add("setbalance");
                }
                if (this.permission.has(sender, "xpeconomy.sync")) {
                    completionOptions.add("sync");
                }
                if (this.permission.has(sender, "xpeconomy.take")) {
                    completionOptions.add("take");
                }
            } else if (args.length == 2) {
                final String commandName = args[0].toLowerCase(Locale.ROOT);
                switch (commandName) {
                    case "balance":
                    case "bal":
                        if (this.permission.has(sender, "xpeconomy.balance")
                                && this.permission.has(sender, "xpeconomy.balance.others")) {
                            completionOptions.addAll(getAllPlayerNames());
                        }
                        break;
                    case "setbalance":
                    case "setbal":
                        if (this.permission.has(sender, "xpeconomy.balance.set")
                                && this.permission.has(sender, "xpeconomy.balance.set.others")) {
                            completionOptions.addAll(getAllPlayerNames());
                        }
                        break;
                    case "pay":
                        if (sender instanceof Player && this.permission.has(sender, "xpeconomy.pay")) {
                            completionOptions.addAll(getAllPlayerNames());
                        }
                        break;
                    case "give":
                        if (this.permission.has(sender, "xpeconomy.give")) {
                            completionOptions.addAll(getAllPlayerNames());
                        }
                        break;
                    case "take":
                        if (this.permission.has(sender, "xpeconomy.take")) {
                            completionOptions.addAll(getAllPlayerNames());
                        }
                        break;
                    case "sync":
                        if (this.permission.has(sender, "xpeconomy.sync")) {
                            if (this.permission.has(sender, "xpeconomy.sync.others")) {
                                if (this.permission.has(sender, "xpeconomy.sync.all")) {
                                    completionOptions.add("all");
                                }
                                completionOptions.addAll(getOnlinePlayerNames());
                            }
                        }
                        break;
                    case "experience":
                    case "exp":
                    case "xp":
                        if (this.permission.has(sender, "xpeconomy.experience")
                                && this.permission.has(sender, "xpeconomy.experience.others")) {
                            completionOptions.addAll(getOnlinePlayerNames());
                        }
                        break;
                }
            } else if (args.length == 3 && "take".equalsIgnoreCase(args[0]) && this.permission.has(sender, "xpeconomy.take")) {
                completionOptions.addAll(getAllPlayerNames());
            }
        }
        return completionOptions;
    }
}
