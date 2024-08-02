package com.example.gestioncommercial.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a bank transfer payment.
 */
public class BankTransfer extends Payment {
    private String bankName;
    private String accountNumber;
    private String transactionId;
    private String beneficiaryName;

    public BankTransfer(
            long id,
            BigDecimal amount,
            LocalDate paymentDate,
            String accountNumber,
            String transactionId,
            PaymentMethod paymentMethod,
            String bankName) {
        super(id, amount, paymentDate, paymentMethod);
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.transactionId = transactionId;
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
}

