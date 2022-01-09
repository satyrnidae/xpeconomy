package dev.satyrn.xpeconomy.api.configuration;

import dev.satyrn.xpeconomy.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a class which requires access to the Configuration class and should be able to refresh its state when
 * the configuration is reloaded from disk.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
public interface ConfigurationConsumer {
    /**
     * Called when the configuration is reloaded. Sets the state of the consumer based on the new configuration.
     *
     * @param configuration The configuration.
     */
    void reloadConfiguration(final @NotNull Configuration configuration);
}
