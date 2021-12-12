package dev.satyrn.xpeconomy.configuration;

import dev.satyrn.xpeconomy.api.configuration.*;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Root configuration container for the Experience Economy mod.
 */
public final class ExperienceEconomyConfiguration extends ConfigurationContainer {
    /**
     * The MySQL connection settings configuration container.
     */
    public final transient MySQLContainer mysql = new MySQLContainer(this);
    /**
     * The initial account balance for new player accounts.
     */
    public final transient DoubleNode startingValue = new DoubleNode(this, "startingValue");
    /**
     * The locale to use while translating chat messages.
     */
    public final transient StringNode locale = new StringNode(this, "locale");
    /**
     * The economy method to use.
     */
    public final transient EnumNode<EconomyMethod> economyMethod = new EnumNode<>(this, "economyMethod") {
        /**
         * Parses an EconomyMethod enum from a string value.
         *
         * @param value The string value from the config file
         * @return The parsed economy method.
         * @throws IllegalArgumentException The string value could not be parsed as an EconomyMethod.
         */
        @Override
        public @NotNull EconomyMethod parse(@NotNull String value) throws IllegalArgumentException {
            return EconomyMethod.valueOf(value.toUpperCase(Locale.ROOT));
        }

        /**
         * Gets the default EconomyMethod value.
         *
         * @return The default EconomyMethod value.
         */
        @Override
        public @NotNull EconomyMethod getDefault() {
            return EconomyMethod.getDefault();
        }
    };

    /**
     * Initializes a new root configuration container.
     *
     * @param plugin The plugin instance.
     */
    public ExperienceEconomyConfiguration(final Plugin plugin) {
        super(plugin.getConfig());
    }

    /**
     * Represents a container of nodes which dictate the function of and options for the MySQL server backend.
     */
    public static final class MySQLContainer extends ConfigurationContainer {
        /**
         * Whether the MySQL server backend should be enabled.
         */
        public final transient BooleanNode enabled = new BooleanNode(this, "enabled");
        /**
         * The MySQL server hostname.
         */
        public final transient StringNode hostname = new StringNode(this, "hostname");
        /**
         * The MySQL server port.
         */
        public final transient IntegerNode port = new IntegerNode(this, "port");
        /**
         * The name of the database to use.
         */
        public final transient StringNode database = new StringNode(this, "database");
        /**
         * The MySQL user ID.
         */
        public final transient StringNode userID = new StringNode(this, "userID");
        /**
         * The MySQL user password.
         */
        public final transient StringNode password = new StringNode(this, "password");
        /**
         * Options for the MySQL connection.
         */
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
}
