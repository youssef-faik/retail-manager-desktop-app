package com.example.salesmanagement.payment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "Check")
@Table(name = "check_payment")
@DiscriminatorValue("CHECK")
@PrimaryKeyJoinColumn(
        name = "id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "check_payment_fk"))
public class Check extends Payment {
    private String bankName;
    private String checkNumber;
    private String payeeName;
    private String senderAccount;

    @Enumerated(EnumType.STRING)
    private CheckStatus checkStatus;

    @Temporal(TemporalType.DATE)
    private LocalDate dueDate;

    @Temporal(TemporalType.DATE)
    private LocalDate issueDate;


    /**
     * Constructs a Check with the given details.
     *
     * @param amount        the amount specified on the check
     * @param payeeName     the name of the individual or entity to whom the check is made out
     * @param senderAccount the account from which the money is being transferred
     * @param checkNumber   the unique identifier of the check
     * @param dueDate       the date by which the check should be presented or cleared
     * @param status        the status of the check
     */
    public Check(
            BigDecimal amount,
            LocalDate paymentDate,
            String payeeName,
            String senderAccount,
            String checkNumber,
            PaymentMethod paymentMethod,
            LocalDate dueDate,
            String bankName,
            CheckStatus status) {
        super(amount, paymentDate, paymentMethod);
        this.dueDate = dueDate;
        this.bankName = bankName;
        this.checkStatus = status;
        this.checkNumber = checkNumber;
        this.payeeName = payeeName;
        this.senderAccount = senderAccount;
    }

    public Check() {
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public CheckStatus getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(CheckStatus checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }
}
