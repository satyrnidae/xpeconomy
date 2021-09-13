package dev.satyrn.xpeconomy.api.configuration;

/**
 * Represents a configuration node with a String value.
 */
public final class StringNode extends ConfigurationNode<String> {
    /**
     * Creates a new configuration node with a String value.
     *
     * @param parent The parent container.
     * @param name   The node's name.
     */
    public StringNode(ConfigurationContainer parent, final String name) {
        super(parent, name);
    }

    /**
     * Returns the String value of the node.
     *
     * @return The String value.
     */
    @Override
    public String value() {
        return this.config.getString(this.getPath());
    }
}
