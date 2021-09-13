package dev.satyrn.xpeconomy.api.configuration;

import java.util.Locale;
import java.util.logging.Level;

/**
 * Represents a configuration node with a Log Level value.
 */
public final class LevelNode extends ConfigurationNode<Level> {
    /**
     * Creates a new configuration node with a Log Level value.
     *
     * @param parent The parent container.
     * @param name   The node's name.
     */
    public LevelNode(ConfigurationContainer parent, final String name) {
        super(parent, name);
    }

    /**
     * Returns the Log Level value of the node.
     *
     * @return The Log Level value.
     */
    @Override
    public Level value() {
        final String levelName = this.config.getString(this.getPath());
        if (levelName != null && !levelName.isEmpty()) {
            try {
                final Level level = Level.parse(levelName.toUpperCase(Locale.ROOT));
                return level;
            } catch (IllegalArgumentException ex) {
                // Toss it
            }
        }
        return Level.INFO;
    }
}
