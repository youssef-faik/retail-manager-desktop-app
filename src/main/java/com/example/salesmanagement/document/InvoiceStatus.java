package com.example.salesmanagement.document;

/**
 * Enum representing the status of an invoice.
 *
 * <p>This enum provides a set of predefined constants to represent the various
 * states that an invoice can be in during its lifecycle. Each constant corresponds
 * to a specific status that can be assigned to an invoice.</p>
 * <p>Here's a logical progression for the invoice statuses:
 * <br>
 * 1. DRAFT: The initial state of an invoice when it is being created but not yet finalized.
 * <br>
 * 2. PENDING: The invoice has been issued to the customer but payment has not yet been received.
 * <br>
 * 3. PARTIALLY_PAID: The customer has made a partial payment toward the invoice.
 * <br>
 * 4. PAID: The invoice has been fully paid.
 * <br>
 * 5. OVERDUE: The payment deadline has passed and the invoice is still not fully paid. This status can occur if the invoice is still PENDING or PARTIALLY_PAID and the due date has passed.
 * <br>
 * 6. CANCELLED: The invoice has been cancelled and is no longer valid. This status can occur at any point in the lifecycle if the invoice is deemed invalid or the order is cancelled.</p>
 */
public enum InvoiceStatus {
    /**
     * Indicates that the invoice is still a draft and not yet finalized.
     *
     * <p>This status is used for invoices that are in the process of being created
     * and have not yet been issued to the customer. Draft invoices can be edited
     * before they are finalized and sent out for payment.</p>
     */
    DRAFT,

    /**
     * Indicates that the invoice has been created but not yet paid.
     *
     * <p>This status is used when an invoice has been issued to the customer,
     * but payment has not yet been received. It signifies that the invoice is
     * awaiting payment.</p>
     */
    PENDING,

    /**
     * Indicates that the invoice has been partially paid.
     *
     * <p>This status is used when the customer has made a partial payment towards
     * the invoice amount. It signifies that a portion of the total amount has been
     * paid, but there is still an outstanding balance.</p>
     */
    PARTIALLY_PAID,

    /**
     * Indicates that the invoice has been fully paid.
     *
     * <p>This status is used when the customer has paid the full amount
     * specified in the invoice. It signifies that no further payment is required.</p>
     */
    PAID,

    /**
     * Indicates that the payment for the invoice is overdue.
     *
     * <p>This status is used when the payment deadline has passed and the
     * customer has not yet paid the invoice. It signifies that the invoice is
     * past due and may require follow-up actions.</p>
     */
    OVERDUE,

    /**
     * Indicates that the invoice has been cancelled.
     *
     * <p>This status is used when an invoice is no longer valid and has been
     * nullified. Cancellation can occur due to various reasons such as errors,
     * order cancellations, or other administrative decisions.</p>
     */
    CANCELLED;

    /**
     * Returns a string representation of the invoice status.
     *
     * @return a string that represents the current status of the invoice
     */
    @Override
    public String toString() {
        return switch (this) {
            case PENDING -> "En attente";
            case PAID -> "Payée";
            case CANCELLED -> "Annulée";
            case OVERDUE -> "En retard";
            case PARTIALLY_PAID -> "Partiellement payée";
            case DRAFT -> "Brouillon";
        };
    }

}

