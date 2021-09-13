package dev.satyrn.xpeconomy.api.configuration;

import org.bukkit.configuration.Configuration;

/**
 * Represents a single configuration node. Cannot contain sub-nodes or containers.
 *
 * @param <E> The value type.
 */
public abstract class ConfigurationNode<E> {
    /**
     * The parent configuration container.
     */
    protected final ConfigurationContainer parent;
    /**
     * The name of the node.
     */
    protected final String name;
    /**
     * The configuration file instance.
     */
    protected Configuration config;

    /**
     * Initializes a new Configuration node.
     *
     * @param parent The parent node.
     */
    protected ConfigurationNode(final ConfigurationContainer parent, String name) {
        this.parent = parent;
        this.name = name;
        if (parent != null) {
            this.config = parent.config;
        }
    }

    /**
     * Gets the name of the node.
     *
     * @return The name of the node.
     */
    protected final String getName() {
        return this.name;
    }

    /**
     * Constructs the full node path.
     *
     * @return The full node path.
     */
    protected final String getPath() {
        return this.getPath(new StringBuilder());
    }

    /**
     * Constructs the full node path.
     *
     * @param stringBuilder The StringBuilder with which to build out the full node path.
     * @return The full node path.
     */
    protected final String getPath(StringBuilder stringBuilder) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }
        if (parent != null) {
            parent.getPath(stringBuilder);
            stringBuilder.append('.');
        }
        stringBuilder.append(this.getName());
        return stringBuilder.toString();
    }

    /**
     * Returns the value of the node as a String.
     *
     * @return The value as a String.
     */
    @Override
    public final String toString() {
        return this.value() == null ? "" : this.value().toString();
    }

    /**
     * Gets the value of the node.
     *
     * @return The value.
     */
    public abstract E value();
}
