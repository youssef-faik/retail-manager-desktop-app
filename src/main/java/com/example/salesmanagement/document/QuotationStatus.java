package com.example.salesmanagement.document;

/**
 * Represents the possible statuses for a quotation in the sales management system.
 */
public enum QuotationStatus {

    /**
     * The quotation is still being prepared and has not been finalized or sent to the customer.
     */
    DRAFT,

    /**
     * The quotation has been sent to the customer and is awaiting a response.
     */
    SENT,

    /**
     * The customer has accepted the quotation, agreeing to the terms provided.
     */
    ACCEPTED,

    /**
     * The customer has rejected the quotation, declining the terms provided.
     */
    REJECTED,

    /**
     * The quotation has expired and is no longer valid.
     */
    EXPIRED;


    /**
     * Returns a user-friendly string representation of the quotation status.
     *
     * @return a string that represents the status in a more readable form.
     */

    @Override
    public String toString() {
        return switch (this) {
            case DRAFT -> "Brouillon";
            case SENT -> "Envoyé";
            case ACCEPTED -> "Accepté";
            case REJECTED -> "Rejeté";
            case EXPIRED -> "Expiré";
        };
    }

}
