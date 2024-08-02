package com.example.gestioncommercial.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class Payment {
    private long id;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;

    public Payment() {
    }

    public Payment(long id, BigDecimal amount, LocalDate paymentDate, PaymentMethod paymentMethod) {
        this.id = id;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
