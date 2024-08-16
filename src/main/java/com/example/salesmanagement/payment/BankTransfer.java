package com.example.salesmanagement.payment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a bank transfer payment.
 */
@Entity(name = "BankTransfer")
@Table(name = "bank_transfer")
@DiscriminatorValue("BANK_TRANSFER")
@PrimaryKeyJoinColumn(
        name = "id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "bank_transfer_payment_fk"))
public class BankTransfer extends Payment {
    private String bankName;
    private String accountNumber;
    private String transactionId;
    private String beneficiaryName;

    public BankTransfer(
            BigDecimal amount,
            LocalDate paymentDate,
            String accountNumber,
            String transactionId,
            PaymentMethod paymentMethod,
            String bankName) {
        super(amount, paymentDate, paymentMethod);
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.transactionId = transactionId;
    }

    public BankTransfer() {
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }
}

