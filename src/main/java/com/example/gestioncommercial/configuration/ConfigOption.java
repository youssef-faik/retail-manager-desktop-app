package com.example.gestioncommercial.configuration;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 * The ConfigOption class represents a configuration option with a key and a value.
 */
@Table(name = "config_option")
@Entity(name = "ConfigOption")
public class ConfigOption implements Serializable {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "option_key")
    private ConfigKey key;

    private String value;

    public ConfigOption() {
    }

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
     * Sets the configuration key.
     *
     * @param key the new configuration key
     */
    public void setKey(ConfigKey key) {
        this.key = key;
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


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigOption that)) return false;

        return getKey() == that.getKey();
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}
