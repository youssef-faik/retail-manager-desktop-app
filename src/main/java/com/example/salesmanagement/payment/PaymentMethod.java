package com.example.salesmanagement.payment;

/**
 * Enum representing different methods of payment.
 */
public enum PaymentMethod {
    /**
     * Payment made in cash.
     */
    CASH("Espèce"),

    /**
     * Payment made via bank transfer.
     */
    BANK_TRANSFER("Virement"),

    /**
     * Payment made by check.
     */
    CHECK("Chèque");

    private final String description;

    /**
     * Constructs a PaymentMethod with the given description.
     *
     * @param description the description of the payment method
     */
    PaymentMethod(String description) {
        this.description = description;
    }

    /**
     * Returns the description of the payment method.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a string representation of the PaymentMethod.
     *
     * @return the description of the payment method
     */
    @Override
    public String toString() {
        return description;
    }
}
