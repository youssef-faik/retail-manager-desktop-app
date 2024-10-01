package com.example.salesmanagement.configuration;

import com.example.salesmanagement.document.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AppConfiguration {
    private static AppConfiguration instance;

    protected AppConfiguration() {
        Set<ConfigOption> configOptions = ConfigOptionRepository.findAll();

        for (ConfigKey key : ConfigKey.values()) {
            if (configOptions.stream().filter(configOption -> configOption.getKey() == key).findFirst().isEmpty()) {
                if (key == ConfigKey.NEXT_PURCHASE_ORDER_NUMBER) {
                    ConfigOptionRepository.save(new ConfigOption(key, "1"));
                } else if (key == ConfigKey.NEXT_PURCHASE_DELIVERY_NOTE_NUMBER) {
                    ConfigOptionRepository.save(new ConfigOption(key, "1"));
                } else if (key == ConfigKey.NEXT_QUOTATION_NUMBER) {
                    ConfigOptionRepository.save(new ConfigOption(key, "1"));
                } else if (key == ConfigKey.NEXT_DELIVERY_NOTE_NUMBER) {
                    ConfigOptionRepository.save(new ConfigOption(key, "1"));
                } else if (key == ConfigKey.NEXT_INVOICE_NUMBER) {
                    ConfigOptionRepository.save(new ConfigOption(key, "1"));
                } else if (key == ConfigKey.NEXT_CREDIT_INVOICE_NUMBER) {
                    ConfigOptionRepository.save(new ConfigOption(key, "1"));
                } else if (key == ConfigKey.PRINT_SALES_DOCUMENT_HEADING) {
                    ConfigOptionRepository.save(new ConfigOption(key, Boolean.TRUE.toString()));
                } else if (key == ConfigKey.PRINT_DELIVERY_NOTE_UNIT_PRICE) {
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

    public static <T extends Document> void validateNextSalesDocumentReference(Long nextReference, Class<T> aClass) {
        if (nextReference <= 0) {
            throw new RuntimeException("The next sales document number must be strictly greater than 0.");
        }

        if (DocumentRepository.count(aClass) > 0) {
            DocumentRepository.findFirstByOrderByIdDesc(aClass).ifPresent(document -> {
                if (nextReference <= document.getReference()) {
                    throw new InvalideDocumentReferenceException(document.getReference() + 1);
                        }
                    }
            );
        }
    }

    private static void updateOptionValue(ConfigKey key, String value) {
        ConfigOption option =
                ConfigOptionRepository
                        .findByKey(key)
                        .orElseThrow(() -> new RuntimeException("Option not found with key " + key.name()));

        option.setValue(value);
        ConfigOptionRepository.update(option);
    }

    private static <T extends Document> void updateNextSalesDocumentNumber(Long nextInvoiceNumber, Class<T> tClass) {
        validateNextSalesDocumentReference(nextInvoiceNumber, tClass);
        ConfigKey configKey = null;

        if (tClass == PurchaseOrder.class) {
            configKey = ConfigKey.NEXT_PURCHASE_ORDER_NUMBER;
        } else if (tClass == PurchaseDeliveryNote.class) {
            configKey = ConfigKey.NEXT_PURCHASE_DELIVERY_NOTE_NUMBER;
        } else if (tClass == Invoice.class) {
            configKey = ConfigKey.NEXT_INVOICE_NUMBER;
        } else if (tClass == Quotation.class) {
            configKey = ConfigKey.NEXT_QUOTATION_NUMBER;
        } else if (tClass == DeliveryNote.class) {
            configKey = ConfigKey.NEXT_DELIVERY_NOTE_NUMBER;
        } else if (tClass == CreditInvoice.class) {
            configKey = ConfigKey.NEXT_CREDIT_INVOICE_NUMBER;
        }
        updateOptionValue(configKey, String.valueOf(nextInvoiceNumber));
    }

    public ConfigOption getConfigurationValue(ConfigKey key) {
        return ConfigOptionRepository
                .findByKey(key)
                .orElseThrow(() -> new RuntimeException("Option not found with key " + key.name()));
    }

    public void setConfigurationValues(Map<ConfigKey, String> configOptions) {
        configOptions.forEach((key, value) -> {

            switch (key) {
                case NEXT_PURCHASE_ORDER_NUMBER ->
                        updateNextSalesDocumentNumber(Long.valueOf((configOptions.get(key))), PurchaseOrder.class);
                case NEXT_PURCHASE_DELIVERY_NOTE_NUMBER ->
                        updateNextSalesDocumentNumber(Long.valueOf((configOptions.get(key))), PurchaseDeliveryNote.class);
                case NEXT_QUOTATION_NUMBER ->
                        updateNextSalesDocumentNumber(Long.valueOf((configOptions.get(key))), Quotation.class);
                case NEXT_DELIVERY_NOTE_NUMBER ->
                        updateNextSalesDocumentNumber(Long.valueOf((configOptions.get(key))), DeliveryNote.class);
                case NEXT_INVOICE_NUMBER ->
                        updateNextSalesDocumentNumber(Long.valueOf((configOptions.get(key))), Invoice.class);
                case NEXT_CREDIT_INVOICE_NUMBER ->
                        updateNextSalesDocumentNumber(Long.valueOf((configOptions.get(key))), CreditInvoice.class);
                default -> updateOptionValue(key, String.valueOf(value));
            }
        });
    }

    public Map<ConfigKey, String> getAllConfigurations() {
        return ConfigOptionRepository.findAll()
                .stream()
                .collect(Collectors.toMap(ConfigOption::getKey, ConfigOption::getValue));
    }
}
