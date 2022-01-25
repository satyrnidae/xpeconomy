package dev.satyrn.xpeconomy.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles common Command tasks.
 */
public final class Commands {
    /**
     * UUID pattern matcher. h/t code4copy
     */
    private final static Pattern UUID_PATTERN = Pattern.compile("(?i)^[{]?[0-9A-F]{8}-([0-9A-F]{4}-){3}[0-9A-F]{12}[}]?$");

    /**
     * Do not instantiate this class.
     */
    private Commands() {
    }

    /**
     * Gets all player names from the online player list.
     *
     * @return All player names from the online player list.
     */
    public static Collection<String> getPlayerNames() {
        final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        return players.stream()
                .map(HumanEntity::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    /**
     * Gets a target from all online players
     *
     * @param target The target identifier.
     * @return An optional of player.
     */
    public static @NotNull Optional<? extends Player> getOnlinePlayer(final @NotNull String target) {
        final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        final Optional<? extends Player> onlinePlayer;
        if (UUID_PATTERN.matcher(target).matches()) {
            final UUID uuid = UUID.fromString(target);
            onlinePlayer = players.stream()
                    .filter(player -> player.getUniqueId() == uuid && player.isOnline())
                    .findFirst();
        } else {
            onlinePlayer = Optional.ofNullable(Bukkit.getPlayerExact(target));
        }
        return onlinePlayer;
    }

    /**
     * Gets a target from all players.
     *
     * @param target The target identifier.
     * @return An optional of OfflinePlayer.
     */
    public static @NotNull Optional<OfflinePlayer> getPlayer(final @NotNull String target) {
        final Optional<OfflinePlayer> player;
        if (UUID_PATTERN.matcher(target).matches()) {
            final UUID uuid = UUID.fromString(target);
            player = Optional.of(Bukkit.getOfflinePlayer(uuid));
        } else {
            player = Optional.ofNullable(Bukkit.getOfflinePlayerIfCached(target));
        }
        return player;
    }
}
