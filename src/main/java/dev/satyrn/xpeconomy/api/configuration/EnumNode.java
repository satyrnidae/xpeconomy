package dev.satyrn.xpeconomy.api.configuration;

/**
 * Represents a configuration node with a Log Level value.
 */
public abstract class EnumNode<E extends Enum<E>> extends ConfigurationNode<E> {
    /**
     * Creates a new configuration node with a Log Level value.
     *
     * @param parent The parent container.
     * @param name   The node's name.
     */
    public EnumNode(ConfigurationContainer parent, final String name) {
        super(parent, name);
    }

    /**
     * Returns the Log Level value of the node.
     *
     * @return The Log Level value.
     */
    @Override
    public final E value() {
        final String enumName = this.config.getString(this.getPath());
        if (enumName != null && !enumName.isEmpty()) {
            try {
                return this.parse(enumName);
            } catch (IllegalArgumentException ex) {
                return this.getDefault();
            }
        }
        return this.getDefault();
    }

    /**
     * Parses the enum value.
     * @param value The string value from the config file
     * @return The parsed enum value.
     * @throws IllegalArgumentException Thrown when the enum value parses.
     */
    protected abstract E parse(String value) throws IllegalArgumentException;

    /**
     * Gets the default enum value.
     * @return The default enum value.
     */
    protected abstract E getDefault();
}
