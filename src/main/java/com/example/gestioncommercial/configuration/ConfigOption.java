package com.example.gestioncommercial.configuration;

/**
 * The ConfigOption class represents a configuration option with a key and a value.
 */
public class ConfigOption {
    private final ConfigKey key;
    private String value;

    /**
     * Constructs a new ConfigOption with the specified key and value.
     *
     * @param key   the configuration key
     * @param value the configuration value
     */
    public ConfigOption(ConfigKey key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the configuration key.
     *
     * @return the configuration key
     */
    public ConfigKey getKey() {
        return key;
    }

    /**
     * Returns the configuration value.
     *
     * @return the configuration value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the configuration value.
     *
     * @param value the new configuration value
     */
    public void setValue(String value) {
        this.value = value;
    }
}
