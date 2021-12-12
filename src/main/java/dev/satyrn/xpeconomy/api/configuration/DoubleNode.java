package dev.satyrn.xpeconomy.api.configuration;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a configuration node with a double-precision floating point value.
 */
public final class DoubleNode extends ConfigurationNode<Double> {
    /**
     * Creates a new configuration node with a double-precision floating point value.
     *
     * @param parent The parent container.
     * @param name   The node name.
     */
    public DoubleNode(final @NotNull ConfigurationContainer parent, final @NotNull String name) {
        super(parent, name, parent.config);
    }

    /**
     * Returns the double-precision floating point value of the node.
     *
     * @return The double-precision floating point value.
     */
    @Override
    public Double value() {
        return this.config.getDouble(this.getPath());
    }
}
