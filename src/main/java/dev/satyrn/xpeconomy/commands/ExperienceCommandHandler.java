package dev.satyrn.xpeconomy.commands;

import dev.satyrn.xpeconomy.api.commands.CommandHandler;
import dev.satyrn.xpeconomy.api.economy.AccountManager;
import dev.satyrn.xpeconomy.lang.I18n;
import dev.satyrn.xpeconomy.utils.Commands;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import dev.satyrn.xpeconomy.utils.PlayerXPUtils;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ExperienceCommandHandler extends CommandHandler {
    private final @NotNull AccountManager accountManager;
    private final @NotNull EconomyMethod economyMethod;

    /**
     * Initializes a new command handler with the permissions manager instance.
     *
     * @param permission The permission manager instance.
     */
    public ExperienceCommandHandler(@NotNull Permission permission,
                                    @NotNull AccountManager accountManager,
                                    @NotNull EconomyMethod economyMethod) {
        super(permission);
        this.accountManager = accountManager;
        this.economyMethod = economyMethod;
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
        final boolean isSubCommand = "xpeconomy".equalsIgnoreCase(command.getName());
        final int playerArgIndex = isSubCommand ? 1 : 0;

        // Check that there are no extra arguments
        if (sender instanceof final Player player) {
            if (args.length > playerArgIndex + 1) {
                sender.sendMessage(I18n.tr("command.generic.usage", this.getUsage(sender, command)));
            } else if (this.getPermission().has(player, "xpeconomy.experience")) {
                if (args.length == playerArgIndex + 1) {
                    final Optional<? extends Player> result = Commands.getOnlinePlayer(args[playerArgIndex]);
                    if (result.isPresent()) {
                        final Player target = result.get();

                        final BigDecimal totalXPValue = PlayerXPUtils.getTotalXPValue(target.getLevel(), target.getExp()).setScale(0, RoundingMode.DOWN);
                        final BigDecimal currentLevelProgress = PlayerXPUtils.getCurrentLevelProgress(
                                BigDecimal.valueOf(target.getLevel()), BigDecimal.valueOf(target.getExp())).setScale(0, RoundingMode.DOWN);
                        final DecimalFormat formatter = new DecimalFormat("#,##0");

                        // Check if the player can check the balance of the target.
                        // Players can check a specified player if the following is true:
                        // The target player is the sender player; or
                        // The sender player has the check others permission and the target is not exempt, or
                        // The sender player has the check others permission, the target is exempt, and the sender bypasses exempt checks.
                        if (target.getUniqueId() == player.getUniqueId()) {
                            sender.sendMessage(I18n.tr("command.experience.result",
                                    target.getLevel(),
                                    I18n.tr(target.getLevel() == 1 ? "experience.level" : "experience.level.plural"),
                                    formatter.format(currentLevelProgress),
                                    I18n.tr(currentLevelProgress.compareTo(BigDecimal.ONE) == 0 ? "experience.point" : "experience.point.plural"),
                                    formatter.format(totalXPValue),
                                    I18n.tr(totalXPValue.compareTo(BigDecimal.ONE) == 0 ? "experience.point" : "experience.point.plural")));
                        } else {
                            if (!this.getPermission().has(player, "xpeconomy.experience.others")) {
                                sender.sendMessage(I18n.tr("command.experience.permission.others"));
                            } else if (this.getPermission().has(target, "xpeconomy.experience.exempt") &&
                                    !this.getPermission().has(player, "xpeconomy.experience.exempt.bypass")) {
                                sender.sendMessage(I18n.tr("command.experience.permission.exempt", target.getName()));
                            } else {
                                sender.sendMessage(I18n.tr("command.experience.result.others",
                                        target.getName(),
                                        target.getLevel(),
                                        I18n.tr(target.getLevel() == 1 ? "experience.level" : "experience.level.plural"),
                                        formatter.format(currentLevelProgress),
                                        I18n.tr(currentLevelProgress.compareTo(BigDecimal.ONE) == 0 ? "experience.point" : "experience.point.plural"),
                                        formatter.format(totalXPValue),
                                        I18n.tr(totalXPValue.compareTo(BigDecimal.ONE) == 0 ? "experience.point" : "experience.point.plural")));
                            }
                        }
                    } else {
                        sender.sendMessage(I18n.tr("command.generic.invalid_target", args[playerArgIndex]));
                    }
                } else {
                    final BigDecimal totalXPValue = PlayerXPUtils.getTotalXPValue(player.getLevel(), player.getExp()).setScale(0, RoundingMode.DOWN);
                    final BigDecimal currentLevelProgress = PlayerXPUtils.getCurrentLevelProgress(
                            BigDecimal.valueOf(player.getLevel()), BigDecimal.valueOf(player.getExp())).setScale(0, RoundingMode.DOWN);
                    final DecimalFormat formatter = new DecimalFormat("#,##0");
                    sender.sendMessage(I18n.tr("command.experience.result",
                            player.getLevel(),
                            I18n.tr(player.getLevel() == 1 ? "experience.level" : "experience.level.plural"),
                            formatter.format(currentLevelProgress),
                            I18n.tr(currentLevelProgress.compareTo(BigDecimal.ONE) == 0 ? "experience.point" : "experience.point.plural"),
                            formatter.format(totalXPValue),
                            I18n.tr(totalXPValue.compareTo(BigDecimal.ONE) == 0 ? "experience.point" : "experience.point.plural")));
                }
            } else {
                sender.sendMessage(I18n.tr("command.experience.permission"));
            }
        } else {
            if (args.length < playerArgIndex + 1) {
                sender.sendMessage(I18n.tr("command.generic.invalid_sender.non_player"));
            } else if (args.length > playerArgIndex + 1) {
                sender.sendMessage(I18n.tr("command.generic.usage", this.getUsage(sender, command)));
            } else {
                Optional<? extends Player> result = Commands.getOnlinePlayer(args[playerArgIndex]);
                if (result.isPresent()) {
                    final Player target = result.get();

                    final BigDecimal totalXPValue = PlayerXPUtils.getTotalXPValue(target.getLevel(), target.getExp()).setScale(0, RoundingMode.DOWN);
                    final BigDecimal currentLevelProgress = PlayerXPUtils.getCurrentLevelProgress(
                            BigDecimal.valueOf(target.getLevel()), BigDecimal.valueOf(target.getExp())).setScale(0, RoundingMode.DOWN);
                    final DecimalFormat formatter = new DecimalFormat("#,##0");

                    sender.sendMessage(I18n.tr("command.experience.result.others",
                            target.getName(),
                            target.getLevel(),
                            I18n.tr(target.getLevel() == 1 ? "experience.level" : "experience.level.plural"),
                            formatter.format(currentLevelProgress),
                            I18n.tr(currentLevelProgress.compareTo(BigDecimal.ONE) == 0 ? "experience.point" : "experience.point.plural"),
                            formatter.format(totalXPValue),
                            I18n.tr(totalXPValue.compareTo(BigDecimal.ONE) == 0 ? "experience.point" : "experience.point.plural")));
                } else {
                    sender.sendMessage(I18n.tr("command.generic.invalid_target", args[playerArgIndex]));
                }
            }
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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final int playerArgIndex = "xpeconomy".equalsIgnoreCase(command.getName()) ? 2 : 1;
        final List<String> completionOptions = new ArrayList<>();

        if (args.length == playerArgIndex) {
            if (sender instanceof final Player player) {
                if (this.getPermission().has(player, "xpeconomy.experience")) {
                    if (this.getPermission().has(player, "xpeconomy.experience.others")) {
                        completionOptions.addAll(Commands.getPlayerNames());
                    } else {
                        completionOptions.add(player.getName());
                    }
                }
            } else {
                completionOptions.addAll(Commands.getPlayerNames());
            }
        }

        return completionOptions;
    }

    @Override
    protected final @NotNull String getUsage(final @NotNull CommandSender sender, final @NotNull Command command) {
        if ("xpeconomy".equalsIgnoreCase(command.getName())) {
            if (sender instanceof Player) {
                return I18n.tr("command.experience.usage.subcommand");
            }
            return I18n.tr("command.experience.usage.subcommand.console");
        }
        if (sender instanceof Player) {
            return I18n.tr("command.experience.usage");
        }
        return I18n.tr("command.experience.usage.console");
    }
}
