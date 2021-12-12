package dev.satyrn.xpeconomy.api.configuration;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a configuration node with an integer value.
 */
public final class IntegerNode extends ConfigurationNode<Integer> {
    /**
     * Creates a new configuration node with an integer value.
     *
     * @param parent The parent container.
     * @param name   The node's name.
     */
    public IntegerNode(final @NotNull ConfigurationContainer parent, final @NotNull String name) {
        super(parent, name, parent.config);
    }

    /**
     * Returns the integer value of the node.
     *
     * @return The integer value.
     */
    @Override
    public @NotNull Integer value() {
        return this.config.getInt(this.getPath());
    }
}
