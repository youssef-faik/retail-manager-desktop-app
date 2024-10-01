package com.example.salesmanagement.configuration;

public class InvalideDocumentReferenceException extends RuntimeException {
    private Long nextValideDocumentReference;

    public InvalideDocumentReferenceException(Long nextValideDocumentReference) {
        super("The next sales document number must be greater than or equal to " + nextValideDocumentReference);
        this.nextValideDocumentReference = nextValideDocumentReference;
    }

    public Long getNextValideDocumentReference() {
        return nextValideDocumentReference;
    }

    public void setNextValideDocumentReference(Long nextValideDocumentReference) {
        this.nextValideDocumentReference = nextValideDocumentReference;
    }
}
