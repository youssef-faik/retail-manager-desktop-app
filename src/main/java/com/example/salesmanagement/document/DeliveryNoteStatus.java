package com.example.salesmanagement.document;

/**
 * Represents the possible statuses for a delivery note in the sales management system.
 */
public enum DeliveryNoteStatus {

    /**
     * The delivery note is being prepared and has not yet been finalized.
     */
    DRAFT,

    /**
     * The goods have been dispatched and are on their way to the customer.
     */
    DISPATCHED,

    /**
     * The goods have been successfully delivered to the customer.
     */
    DELIVERED,

    /**
     * The delivery note has been cancelled, and the delivery will not occur.
     */
    CANCELLED;

    /**
     * Returns a user-friendly string representation of the delivery note status.
     *
     * @return a string that represents the status in a more readable form.
     */
    @Override
    public String toString() {
        return switch (this) {
            case DRAFT -> "Brouillon";
            case DISPATCHED -> "Expédié";
            case DELIVERED -> "Livré";
            case CANCELLED -> "Annulé";
        };
    }
}

