package dev.satyrn.xpeconomy.api.configuration;

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
    public BooleanNode(final ConfigurationContainer parent, final String name) {
        super(parent, name);
    }

    /**
     * Returns the boolean value of the node.
     *
     * @return The boolean value.
     */
    @Override
    public Boolean value() {
        return this.config.getBoolean(this.getPath());
    }
}
