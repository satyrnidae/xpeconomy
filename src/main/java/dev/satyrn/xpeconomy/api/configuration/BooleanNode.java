package dev.satyrn.xpeconomy.api.configuration;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a configuration node with a boolean value.
 */
public final class BooleanNode extends ConfigurationNode<Boolean> {
    /**
     * Creates a new configuration node with a boolean value.
     *
     * @param parent The parent container.
     * @param name   The name of the configuration node.
     */
    public BooleanNode(final @NotNull ConfigurationContainer parent, final @NotNull String name) {
        super(parent, name, parent.config);
    }

    /**
     * Returns the boolean value of the node.
     *
     * @return The boolean value.
     */
    @Override
    public @NotNull Boolean value() {
        return this.config.getBoolean(this.getPath());
    }
}
