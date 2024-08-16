package com.example.salesmanagement.payment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "Cash")
@Table(name = "cash")
@DiscriminatorValue("CASH")
@PrimaryKeyJoinColumn(
        name = "id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "cash_payment_fk"))
public class Cash extends Payment {
    @Enumerated(EnumType.STRING)
    private CashFlowType cashFlowType;

    public Cash() {
    }

    public Cash(BigDecimal amount, LocalDate paymentDate, PaymentMethod paymentMethod, CashFlowType cashFlowType) {
        super(amount, paymentDate, paymentMethod);
        this.cashFlowType = cashFlowType;
    }

    public CashFlowType getCashFlowType() {
        return cashFlowType;
    }

    public void setCashFlowType(CashFlowType cashFlowType) {
        this.cashFlowType = cashFlowType;
    }
}
