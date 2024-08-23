package com.example.salesmanagement.document;

import com.example.salesmanagement.payment.Payment;

public class PaymentFormEntry {
    private Payment payment;

    public PaymentFormEntry() {
    }

    public PaymentFormEntry(Payment payment) {
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
