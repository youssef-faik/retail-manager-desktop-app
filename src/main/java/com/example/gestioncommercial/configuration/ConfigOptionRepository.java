package com.example.gestioncommercial.configuration;

import com.example.gestioncommercial.DataAccessObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The ConfigOptionRepository class is a repository for managing configuration options.
 */
public class ConfigOptionRepository {
    private final List<ConfigOption> configOptions = new ArrayList<>();
    private final DataAccessObject dao;

    public ConfigOptionRepository() {
        this.dao = new DataAccessObject();
    }


    /**
     * Finds a configuration option by its key.
     *
     * @param key the configuration key
     * @return an Optional containing the configuration option if found, or an empty Optional if not found
     */
    public Optional<ConfigOption> findByKey(ConfigKey key) {
        return dao.getConfigOptions().stream()
                .filter(option -> option.getKey().equals(key))
                .findFirst();
    }

    /**
     * Finds all configuration options.
     *
     * @return a list of all configuration options
     */
    public List<ConfigOption> findAll() {
        return dao.getConfigOptions();
    }

    /**
     * Saves a configuration option.
     *
     * @param option        the configuration option to save
     * @param isUpdateQuery
     */
    public void save(ConfigOption option, boolean isUpdateQuery) {
        String query = "insert into config_option (option_key,value) values ('%s','%s')".formatted(option.getKey(), option.getValue());

        if (isUpdateQuery) {
            query = "update config_option set value = '%s' where option_key = '%s';".formatted(option.getValue(), option.getKey());
        }

        try {
            dao.saveData(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
