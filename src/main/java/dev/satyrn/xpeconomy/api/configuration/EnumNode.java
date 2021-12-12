package dev.satyrn.xpeconomy.api.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public EnumNode(final @NotNull ConfigurationContainer parent, final @NotNull String name) {
        super(parent, name, parent.config);
    }

    /**
     * Returns the Log Level value of the node.
     *
     * @return The Log Level value.
     */
    @Override
    public final @NotNull E value() {
        final @Nullable String enumName = this.config.getString(this.getPath());
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
     *
     * @param value The string value from the config file
     * @return The parsed enum value.
     * @throws IllegalArgumentException Thrown when the enum value parses.
     */
    protected abstract @NotNull E parse(final @NotNull String value) throws IllegalArgumentException;

    /**
     * Gets the default enum value.
     *
     * @return The default enum value.
     */
    protected abstract @NotNull E getDefault();
}
