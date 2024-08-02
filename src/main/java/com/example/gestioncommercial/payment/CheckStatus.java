package com.example.gestioncommercial.payment;

/**
 * Enum representing the status of a check in its lifecycle.
 */
public enum CheckStatus {
    /**
     * Check has been created but not yet issued or handed over to the payee.
     */
    CREATED,

    /**
     * Check has been received by the payee but no action has been taken to deposit or cash it.
     */
    RECEIVED,

    /**
     * Check is in the process of being cleared.
     */
    PENDING,

    /**
     * Check has been successfully cleared.
     */
    CLEARED,

    /**
     * Check could not be cleared (e.g., due to insufficient funds).
     */
    BOUNCED,

    /**
     * Check has been cancelled and will not be processed.
     */
    CANCELLED;



    /**
     * Returns the display name of the check status.
     *
     * @return the display name of the check status
     */
    @Override
    public String toString() {
        return switch (this) {
            case CREATED -> "Créé";
            case RECEIVED -> "Reçu";
            case PENDING -> "En attente";
            case CLEARED -> "Compensé";
            case BOUNCED -> "Rejeté";
            case CANCELLED -> "Annulé";
        };
    }
}
