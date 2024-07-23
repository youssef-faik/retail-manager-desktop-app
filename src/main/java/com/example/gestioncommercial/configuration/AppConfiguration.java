package com.example.gestioncommercial.configuration;

import com.example.gestioncommercial.invoice.InvoiceRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AppConfiguration {
    private static AppConfiguration instance;

    protected AppConfiguration() {
        Set<ConfigOption> configOptions = ConfigOptionRepository.findAll();

        for (ConfigKey key : ConfigKey.values()) {
            if (configOptions.stream().filter(configOption -> configOption.getKey() == key).findFirst().isEmpty()) {
                if (key == ConfigKey.NEXT_INVOICE_NUMBER) {
                    ConfigOptionRepository.save(new ConfigOption(key, "1"));
                } else if (key == ConfigKey.NEXT_CREDIT_INVOICE_NUMBER) {
                    ConfigOptionRepository.save(new ConfigOption(key, "1"));
                } else if (key == ConfigKey.PRINT_INVOICE_HEADING) {
                    ConfigOptionRepository.save(new ConfigOption(key, Boolean.TRUE.toString()));
                } else {
                    ConfigOptionRepository.save(new ConfigOption(key, ""));
                }
            }
        }
    }

    public static AppConfiguration getInstance() {
        if (instance == null) {
            synchronized (AppConfiguration.class) {
                if (instance == null) {
                    instance = new AppConfiguration();
                }
            }
        }
        return instance;
    }

    private static void validateNextInvoiceNumber(Long nextInvoiceNumber) {
        if (nextInvoiceNumber <= 0) {
            throw new RuntimeException("The next invoice number must be strictly greater than 0.");
        }

        if (InvoiceRepository.count() > 0) {
            InvoiceRepository.findFirstByOrderByIdDesc().ifPresent(invoice -> {
                        if (nextInvoiceNumber <= invoice.getReference()) {
                            throw new RuntimeException(
                                    "The next invoice number must be greater than or equal to " + (invoice.getReference() + 1));
                        }
                    }
            );
        }
    }

    private static void updateOptionValue(ConfigKey key, String value) {
        ConfigOption option =
                ConfigOptionRepository
                        .findByKey(key)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Option not found with key " + key.name()));

        option.setValue(value);
        ConfigOptionRepository.update(option);
    }

    private static void updateNextInvoiceNumber(Long nextInvoiceNumber) {
        validateNextInvoiceNumber(nextInvoiceNumber);
        updateOptionValue(ConfigKey.NEXT_INVOICE_NUMBER, String.valueOf(nextInvoiceNumber));
    }

    public ConfigOption getConfigurationValue(ConfigKey key) {
        return ConfigOptionRepository
                .findByKey(key)
                .orElseThrow(
                        () -> new RuntimeException("Option not found with key " + key.name()));
    }

    public void setConfigurationValues(Map<ConfigKey, String> configOptions) {
        if (configOptions.containsKey(ConfigKey.NEXT_INVOICE_NUMBER)) {
            updateNextInvoiceNumber(Long.valueOf((configOptions.get(ConfigKey.NEXT_INVOICE_NUMBER))));
            configOptions.remove(ConfigKey.NEXT_INVOICE_NUMBER);
        }

        configOptions.forEach((key, value) -> updateOptionValue(key, String.valueOf(value)));
    }

    public Map<ConfigKey, String> getAllConfigurations() {
        Map<ConfigKey, String> options = new HashMap<>();

        Set<ConfigOption> rawOptions = ConfigOptionRepository.findAll();

        for (ConfigOption option : rawOptions) {
            options.put(option.getKey(), option.getValue());
        }

        return options;
    }
}
