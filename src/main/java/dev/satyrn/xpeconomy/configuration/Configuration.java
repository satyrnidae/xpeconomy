package dev.satyrn.xpeconomy.configuration;

import dev.satyrn.papermc.api.configuration.v1.*;
import dev.satyrn.papermc.api.configuration.v3.BigDecimalNode;
import dev.satyrn.xpeconomy.ExperienceEconomyPlugin;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.logging.Level;

/**
 * Root configuration container for the Experience Economy mod.
 */
public final class Configuration extends ConfigurationContainer {
    /**
     * The MySQL connection settings configuration container.
     */
    public final transient MySQLContainer mysql = new MySQLContainer(this);
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
            try {
                return EconomyMethod.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                ExperienceEconomyPlugin.getPlugin(ExperienceEconomyPlugin.class).getLogger().log(Level.WARNING, "[Configuration] Invalid config value for economyMethod: " + value);
                throw ex;
            }
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
     * Initializes a new root configuration container.
     *
     * @param plugin The plugin instance.
     */
    public Configuration(final Plugin plugin) {
        super(plugin.getConfig());
    }

    /**
     * Represents a container of nodes which dictate the function of and options for the MySQL server backend.
     *
     * @author Isabel Maskrey
     * @since 1.0-SNAPSHOT
     */
    public static final class MySQLContainer extends ConfigurationContainer {
        /**
         * Whether the MySQL server backend should be enabled.
         */
        public final transient BooleanNode enabled = new BooleanNode(this, "enabled");
        /**
         * The MySQL server hostname.
         */
        public final transient StringNode hostname = new StringNode(this, "hostname") {
            @Override
            public @NotNull String defaultValue() {
                return "localhost";
            }
        };
        /**
         * The MySQL server port.
         */
        public final transient IntegerNode port = new IntegerNode(this, "port") {
            @Override
            public @NotNull Integer defaultValue() {
                return 3306;
            }
        };
        /**
         * The name of the database to use.
         */
        public final transient StringNode database = new StringNode(this, "database") {
            @Override
            public @NotNull String defaultValue() {
                return "spigot";
            }
        };
        /**
         * The MySQL user ID.
         */
        public final transient StringNode userID = new StringNode(this, "userID") {
            @Override
            public @NotNull String defaultValue() {
                return "root";
            }
        };
        /**
         * The MySQL user password.
         */
        public final transient StringNode password = new StringNode(this, "password") {
            @Override
            public @NotNull String defaultValue() {
                return "password";
            }
        };
        /**
         * Options for the MySQL connection.
         **/
        public final transient MapListNode flags = new MapListNode(this, "flags");
        /**
         * Optional prefix for any created table's names.
         */
        public final transient StringNode tablePrefix = new StringNode(this, "tablePrefix");

        /**
         * Creates a new MySQL configuration container.
         *
         * @param parent The parent container.
         */
        MySQLContainer(ConfigurationContainer parent) {
            super(parent, "mysql");
        }
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
                try {
                    return Material.valueOf(value.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ex) {
                    ExperienceEconomyPlugin.getPlugin(ExperienceEconomyPlugin.class).getLogger().log(Level.WARNING, "[Configuration] Invalid config value for economyMethod: " + value);
                    throw ex;
                }
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
        public final @NotNull IntegerNode pointsPerBottle = new IntegerNode(this, "pointsPerBottle") {
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
