package dev.satyrn.xpeconomy.utils;

import dev.satyrn.xpeconomy.api.configuration.ConfigurationConsumer;
import dev.satyrn.xpeconomy.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigurationConsumerRegistry {
    private static @Nullable ConfigurationConsumerRegistry instance;

    private @NotNull List<ConfigurationConsumer> consumers = new ArrayList<>();

    private ConfigurationConsumerRegistry() { }

    public static ConfigurationConsumerRegistry getInstance() {
        if (instance == null) {
            instance = new ConfigurationConsumerRegistry();
        }
        return instance;
    }

    public static <T extends ConfigurationConsumer> void register(T instance) {
        getInstance().registerInstance(instance);
    }

    public static void reloadConfiguration(final @NotNull Configuration configuration) {
        getInstance().reloadConfigurationInstance(configuration);
    }

    private <T extends ConfigurationConsumer> void registerInstance(T instance) {
        if (!this.consumers.contains(instance)) {
            this.consumers.add(instance);
        }
    }

    private void reloadConfigurationInstance(final @NotNull Configuration configuration) {
        for (final ConfigurationConsumer instance : this.consumers) {
            instance.reloadConfiguration(configuration);
        }
    }
}
