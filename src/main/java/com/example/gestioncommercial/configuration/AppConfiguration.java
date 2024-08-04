package com.example.gestioncommercial.configuration;

import com.example.gestioncommercial.invoice.Invoice;
import com.example.gestioncommercial.invoice.InvoiceRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppConfiguration {
    private static ConfigOptionRepository configOptionRepository;
    private static InvoiceRepository invoiceRepository;
    private static AppConfiguration instance;

    protected AppConfiguration(
            ConfigOptionRepository configOptionRepository, InvoiceRepository invoiceRepository) {
        AppConfiguration.configOptionRepository = configOptionRepository;
        AppConfiguration.invoiceRepository = invoiceRepository;

        if (configOptionRepository.findByKey(ConfigKey.NEXT_INVOICE_NUMBER).isEmpty()) {
            ConfigOption nextInvoiceNumber = new ConfigOption(ConfigKey.NEXT_INVOICE_NUMBER, "1");
            AppConfiguration.configOptionRepository.save(nextInvoiceNumber, false);
        }

        if (configOptionRepository.findByKey(ConfigKey.NEXT_CREDIT_INVOICE_NUMBER).isEmpty()) {
            ConfigOption nextInvoiceNumber = new ConfigOption(ConfigKey.NEXT_CREDIT_INVOICE_NUMBER, "1");
            AppConfiguration.configOptionRepository.save(nextInvoiceNumber, false);
        }

        for (ConfigKey key : ConfigKey.values()) {
            if (configOptionRepository.findByKey(key).isEmpty() ) {
                AppConfiguration.configOptionRepository.save(new ConfigOption(key, ""), false);
            }
        }

    }

    public static AppConfiguration getInstance() {
        if (instance == null) {
            synchronized (AppConfiguration.class) {
                if (instance == null) {
                    instance = new AppConfiguration(new ConfigOptionRepository(), new InvoiceRepository());
                }
            }
        }
        return instance;
    }

    private static void validateNextInvoiceNumber(int nextInvoiceNumber) {
        if (nextInvoiceNumber <= 0) {
            throw new RuntimeException("The next invoice number must be strictly greater than 0.");
        }

        if (invoiceRepository.count() > 0) {
            Invoice invoice = invoiceRepository.findFirstByOrderByIdDesc().orElseThrow();
            if (nextInvoiceNumber <= invoice.getReference()) {
                throw new RuntimeException(
                        "The next invoice number must be greater than or equal to " + (invoice.getReference() + 1));
            }
        }
    }

    private static void updateOptionValue(ConfigKey key, String value) {
        ConfigOption option =
                configOptionRepository
                        .findByKey(key)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Option not found with key " + key.name()));

        option.setValue(value);
        configOptionRepository.save(option, true);
    }

    private static void updateNextInvoiceNumber(int nextInvoiceNumber) {
        validateNextInvoiceNumber(nextInvoiceNumber);
        updateOptionValue(ConfigKey.NEXT_INVOICE_NUMBER, String.valueOf(nextInvoiceNumber));
    }

    public ConfigOption getConfigurationValue(ConfigKey key) {
        return configOptionRepository
                .findByKey(key)
                .orElseThrow(
                        () -> new RuntimeException("Option not found with key " + key.name()));
    }

    public void setConfigurationValues(Map<ConfigKey, String> configOptions) {
        if (configOptions.containsKey(ConfigKey.NEXT_INVOICE_NUMBER)) {
            updateNextInvoiceNumber(Integer.parseInt(configOptions.get(ConfigKey.NEXT_INVOICE_NUMBER)));
        }

        configOptions.forEach((key, value) -> {
            updateOptionValue(key, String.valueOf(value));
        });
    }

    public Map<ConfigKey, String> getAllConfigurations() {
        Map<ConfigKey, String> options = new HashMap<>();

        List<ConfigOption> rawOptions = configOptionRepository.findAll();

        for (ConfigOption option : rawOptions) {
            options.put(option.getKey(), option.getValue());
        }

        return options;
    }
}
