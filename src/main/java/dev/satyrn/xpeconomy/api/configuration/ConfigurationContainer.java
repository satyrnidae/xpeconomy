package dev.satyrn.xpeconomy.api.configuration;

import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a base type for a class which contains several other configuration containers and/or nodes.
 */
public abstract class ConfigurationContainer extends ConfigurationNode<Void> {
    /**
     * Initializes the configuration container as a child of another container.
     *
     * @param parent The node parent.
     */
    protected ConfigurationContainer(final @NotNull ConfigurationContainer parent, final @NotNull String name) {
        super(parent, name, parent.config);
    }

    /**
     * Initializes the configuration container as a root container.
     *
     * @param config The configuration instance.
     */
    protected ConfigurationContainer(final @NotNull Configuration config) {
        super(null, "", config);
    }

    /**
     * Always returns null for configuration containers.
     *
     * @return Null.
     */
    @Override
    public final @Nullable Void value() {
        return null;
    }
}
