package dev.satyrn.xpeconomy.api.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Represents a configuration node with a MapList value.
 */
public final class MapListNode extends ConfigurationNode<List<Map<?,?>>> {
    /**
     * Creates a new configuration node with a Map list value.
     *
     * @param parent The parent container.
     * @param name The node's name.
     */
    public MapListNode(final @NotNull ConfigurationContainer parent, final @NotNull String name) {
        super(parent, name, parent.config);
    }

    /**
     * Returns the entire configuration node as a Map list.
     *
     * @return The entire configuration node as a Map list.
     */
    @Override
    public @NotNull List<Map<?,?>> value() {
        return this.config.getMapList(this.getPath());
    }
}
