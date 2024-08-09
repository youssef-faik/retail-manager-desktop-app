package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.payment.Payment;

public class InvoicePaymentEntry {
    private Payment payment;

    public InvoicePaymentEntry() {
    }

    public InvoicePaymentEntry(Payment payment) {
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
