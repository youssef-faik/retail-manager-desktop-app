package com.example.salesmanagement.invoice;

import com.example.salesmanagement.payment.Payment;

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
