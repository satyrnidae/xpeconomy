package dev.satyrn.xpeconomy.api.configuration;

import org.bukkit.configuration.Configuration;

/**
 * Represents a base type for a class which contains several other configuration containers and/or nodes.
 */
public abstract class ConfigurationContainer extends ConfigurationNode<Void> {
    /**
     * Initializes the configuration container as a child of another container.
     *
     * @param parent The node parent.
     */
    protected ConfigurationContainer(final ConfigurationContainer parent, String name) {
        super(parent, name);
    }

    /**
     * Initializes the configuration container as a root container.
     *
     * @param config The configuration instance.
     */
    protected ConfigurationContainer(final Configuration config) {
        super(null, "");
        this.config = config;
    }

    /**
     * Always returns null for configuration containers.
     *
     * @return Null.
     */
    @Override
    public final Void value() {
        return null;
    }
}
