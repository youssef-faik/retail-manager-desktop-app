package com.example.salesmanagement.document;

/**
 * The PurchaseOrderStatus enum represents the various statuses a purchase order can have.
 * Each enum constant corresponds to a specific stage in the purchase order lifecycle.
 */
public enum PurchaseOrderStatus {
    /**
     * Indicates that the purchase order is in draft form and not yet finalized.
     */
    DRAFT,

    /**
     * Indicates that the purchase order has been sent to the supplier.
     */
    SENT_TO_SUPPLIER,

    /**
     * Indicates that the order has been received from the supplier.
     */
    RECEIVED_ORDER,

    /**
     * Indicates that the purchase order has been canceled and is no longer valid.
     */
    CANCELLED;

    @Override
    public String toString() {
        return switch (this) {
            case DRAFT -> "Brouillon";
            case SENT_TO_SUPPLIER -> "Envoyé au fournisseur";
            case RECEIVED_ORDER -> "Commande reçue";
            case CANCELLED -> "Annulé";
        };
    }
}

