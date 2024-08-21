package com.example.salesmanagement.configuration;

/**
 * The {@code ConfigKey} enum represents various configuration keys used within the application.
 * Each enum constant corresponds to a specific configuration setting that can be adjusted
 * to customize the behavior of the application according to the user's needs.
 */
public enum ConfigKey {
    /**
     * The company name.
     */
    COMPANY_NAME,

    /**
     * The company common identifier number.
     */
    COMMON_IDENTIFIER_NUMBER,

    /**
     * The company tax identifier number.
     */
    TAX_IDENTIFIER_NUMBER,

    /**
     * The company commercial registration number.
     */
    COMMERCIAL_REGISTRATION_NUMBER,

    /**
     * The company patent number.
     */
    COMPANY_PATENT_NUMBER,

    /**
     * The business address.
     */
    BUSINESS_ADDRESS,

    /**
     * The company phone number.
     */
    COMPANY_PHONE_NUMBER,

    /**
     * The company fixed phone number.
     */
    COMPANY_FIXED_PHONE_NUMBER,

    /**
     * The company email address.
     */
    COMPANY_EMAIL_ADDRESS,

    /**
     * Configuration key for enabling or disabling the printing of sales document headings.
     */
    PRINT_SALES_DOCUMENT_HEADING,

    /**
     * Configuration key for enabling or disabling the printing of unit prices on delivery notes.
     */
    PRINT_DELIVERY_NOTE_UNIT_PRICE,

    /**
     * The next quotation number to be used.
     */
    NEXT_QUOTATION_NUMBER,

    /**
     * The next deliver note number to be used.
     */
    NEXT_DELIVERY_NOTE_NUMBER,

    /**
     * The next invoice number to be used.
     */
    NEXT_INVOICE_NUMBER,

    /**
     * The next credit invoice number to be used.
     */
    NEXT_CREDIT_INVOICE_NUMBER,

}
