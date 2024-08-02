package com.example.gestioncommercial.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Cash extends Payment {
    private CashFlowType cashFlowType;

    public Cash() {

    }

    public Cash(long id, BigDecimal amount, LocalDate paymentDate, PaymentMethod paymentMethod, CashFlowType cashFlowType) {
        super(id, amount, paymentDate, paymentMethod);
        this.cashFlowType = cashFlowType;
    }


    public CashFlowType getCashFlowType() {
        return cashFlowType;
    }

    public void setCashFlowType(CashFlowType cashFlowType) {
        this.cashFlowType = cashFlowType;
    }
}
