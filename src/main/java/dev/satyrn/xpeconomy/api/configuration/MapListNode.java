package dev.satyrn.xpeconomy.api.configuration;

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
    public MapListNode(final ConfigurationContainer parent, final String name) {
        super(parent, name);
    }

    /**
     * Returns the entire configuration node as a Map list.
     *
     * @return The entire configuration node as a Map list.
     */
    @Override
    public List<Map<?,?>> value() {
        return this.config.getMapList(this.getPath());
    }
}
