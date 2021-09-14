package dev.satyrn.xpeconomy.configuration;

import dev.satyrn.xpeconomy.api.configuration.*;
import dev.satyrn.xpeconomy.utils.EconomyMethod;
import org.bukkit.plugin.PluginBase;

import java.util.Locale;

/**
 * Root configuration container for the Experience Economy mod.
 */
public class ExperienceEconomyConfiguration extends ConfigurationContainer {
    /**
     * The MySQL connection settings configuration container.
     */
    public final MySQLContainer mysql = new MySQLContainer(this);
    /**
     * The initial account balance for new player accounts.
     */
    public final DoubleNode startingValue = new DoubleNode(this, "startingValue");
    /**
     * The locale to use while translating chat messages.
     */
    public final StringNode locale = new StringNode(this, "locale");
    public final EnumNode<EconomyMethod> economyMethod = new EnumNode<>(this, "economyMethod") {
        @Override
        public EconomyMethod parse(String value) throws IllegalArgumentException {
            return EconomyMethod.valueOf(value.toUpperCase(Locale.ROOT));
        }

        @Override
        public EconomyMethod getDefault() {
            return EconomyMethod.POINTS;
        }
    };

    /**
     * Initializes a new root configuration container.
     *
     * @param plugin The plugin instance.
     */
    public ExperienceEconomyConfiguration(final PluginBase plugin) {
        super(plugin.getConfig());
    }

    /**
     * Represents a container of nodes which dictate the function of and options for the MySQL server backend.
     */
    public static final class MySQLContainer extends ConfigurationContainer {
        /**
         * Whether the MySQL server backend should be enabled.
         */
        public final BooleanNode enabled = new BooleanNode(this, "enabled");
        /**
         * The MySQL server hostname.
         */
        public final StringNode hostname = new StringNode(this, "hostname");
        /**
         * The MySQL server port.
         */
        public final IntegerNode port = new IntegerNode(this, "port");
        /**
         * The name of the database to use.
         */
        public final StringNode database = new StringNode(this, "database");
        /**
         * The MySQL user ID.
         */
        public final StringNode userID = new StringNode(this, "userID");
        /**
         * The MySQL user password.
         */
        public final StringNode password = new StringNode(this, "password");
        /**
         * Options for the MySQL connection.
         */
        public final MapListNode flags = new MapListNode(this, "flags");
        /**
         * Optional prefix for any created table's names.
         */
        public final StringNode tablePrefix = new StringNode(this, "tablePrefix");

        private MySQLContainer(ConfigurationContainer parent) {
            super(parent, "mysql");
        }
    }
}
