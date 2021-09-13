package dev.satyrn.xpeconomy.api.configuration;

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
    public IntegerNode(ConfigurationContainer parent, final String name) {
        super(parent, name);
    }

    /**
     * Returns the integer value of the node.
     *
     * @return The integer value.
     */
    @Override
    public Integer value() {
        return this.config.getInt(this.getPath());
    }
}
