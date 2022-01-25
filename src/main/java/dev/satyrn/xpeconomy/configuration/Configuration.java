package dev.satyrn.xpeconomy.configuration;

import dev.satyrn.papermc.api.configuration.v1.*;
import dev.satyrn.papermc.api.configuration.v3.BigDecimalNode;
import dev.satyrn.papermc.api.configuration.v5.MySQLConfiguration;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Root configuration container for the Experience Economy mod.
 */
public final class Configuration extends ConfigurationContainer {
    /**
     * The MySQL connection settings configuration container.
     */
    public final transient MySQLConfiguration mysql = new MySQLConfiguration(this);

    /**
     * The initial account balance for new player accounts.
     */
    public final transient BigDecimalNode startingBalance = new BigDecimalNode(this, "startingBalance");

    /**
     * The locale to use while translating chat messages.
     */
    public final transient StringNode locale = new StringNode(this, "locale") {
        @Override
        public @NotNull String defaultValue() {
            return "en_US";
        }
    };

    /**
     * The economy method to use.
     */
    public final transient EnumNode<EconomyMethod> economyMethod = new EnumNode<>(this, "economyMethod") {
        @Override
        public @NotNull EconomyMethod parse(@NotNull String value) throws IllegalArgumentException {
            return EconomyMethod.valueOf(value.toUpperCase(Locale.ROOT));
        }

        @Override
        public @NotNull EconomyMethod getDefault() {
            return EconomyMethod.getDefault();
        }
    };

    /**
     * The Experience Bottle mechanics options.
     */
    public final @NotNull BottleOptionsContainer bottleOptions = new BottleOptionsContainer(this);

    /**
     * Whether to enable debug logging.
     */
    public final @NotNull BooleanNode debug = new BooleanNode(this, "debug");

    /**
     * Whether to enable plugin metrics.
     */
    public final @NotNull BooleanNode metrics = new BooleanNode(this, "metrics");

    /**
     * Initializes a new root configuration container.
     *
     * @param plugin The plugin instance.
     */
    public Configuration(final Plugin plugin) {
        super(plugin);
    }

    /**
     * Represents a bottle options configuration container.
     *
     * @author Isabel Maskrey
     * @since 1.0-SNAPSHOT
     */
    public static final class BottleOptionsContainer extends ConfigurationContainer {
        /**
         * Enable or disable experience bottle management functionality.
         */
        public final @NotNull BooleanNode enabled = new BooleanNode(this, "enabled");
        /**
         * Material of the block to interact with to allow players to fill bottles.
         */
        public final @NotNull EnumNode<Material> fillInteractBlock = new EnumNode<>(this, "fillInteractBlock") {
            @Override
            protected @NotNull Material parse(@NotNull String value) throws IllegalArgumentException {
                return Material.valueOf(value.toUpperCase(Locale.ROOT));
            }

            @Override
            protected @NotNull Material getDefault() {
                return Material.AIR;
            }
        };
        /**
         * Whether bottles should be thrown when the player is not crouching.
         */
        public final @NotNull BooleanNode throwBottles = new BooleanNode(this, "throwBottles") {
            @Override
            public @NotNull Boolean defaultValue() {
                return true;
            }
        };
        /**
         * The number of experience points awarded or stored per bottle.
         */
        public final @NotNull IntegerNode pointsPerBottle = new IntegerNode(this, "pointsPerBottle", 1, Integer.MAX_VALUE) {
            @Override
            public @NotNull Integer defaultValue() {
                return 7;
            }
        };
        /**
         * Whether thrown experience bottles will refund thrown bottles.
         */
        public final @NotNull BooleanNode refundThrownBottles = new BooleanNode(this, "refundThrownBottles");

        /**
         * Creates a new bottle options configuration container.
         *
         * @param parent The parent configuration container.
         */
        BottleOptionsContainer(final @NotNull ConfigurationContainer parent) {
            super(parent, "experienceBottleOptions");
        }
    }
}
