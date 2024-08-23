package com.example.salesmanagement.document;

/**
 * The PurchaseOrderStatus enum represents the various statuses a purchase order can have.
 * Each enum constant corresponds to a specific stage in the purchase order lifecycle.
 */
public enum PurchaseDeliveryNoteStatus {
    /**
     * Indicates that the purchase order is in draft form and not yet finalized.
     */
    DRAFT,
    /**
     * Indicates that the order has been received from the supplier.
     */
    RECEIVED,

    /**
     * Indicates that the purchase order has been canceled and is no longer valid.
     */
    CANCELLED;

    @Override
    public String toString() {
        return switch (this) {
            case DRAFT -> "Brouillon";
            case RECEIVED -> "Commande reçue";
            case CANCELLED -> "Annulé";
        };
    }
}

