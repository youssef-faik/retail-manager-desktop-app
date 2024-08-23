package com.example.salesmanagement.document;

/**
 * Represents the possible statuses for a credit invoice in the sales management system.
 */
public enum CreditInvoiceStatus {

    /**
     * The credit invoice is being prepared and has not yet been finalized.
     */
    DRAFT,

    /**
     * The credit invoice has been finalized and issued to the customer.
     */
    ISSUED,

    /**
     * Part of the credit has been applied to outstanding invoices.
     * There is still a remaining balance available.
     */
    PARTIALLY_APPLIED,

    /**
     * The full credit amount has been applied to outstanding invoices.
     * There is no remaining balance on the credit invoice.
     */
    FULLY_APPLIED,

    /**
     * The credit invoice has been cancelled and is no longer valid.
     */
    CANCELLED;

    /**
     * Returns a user-friendly string representation of the credit invoice status.
     *
     * @return a string that represents the status in a more readable form.
     */
    @Override
    public String toString() {
        return switch (this) {
            case DRAFT -> "Brouillon";
            case ISSUED -> "Émise";
            case PARTIALLY_APPLIED -> "Partiellement appliquée";
            case FULLY_APPLIED -> "Totalement appliquée";
            case CANCELLED -> "Annulée";
        };
    }
}

