package com.example.salesmanagement.document;

import com.example.salesmanagement.client.Client;
import com.example.salesmanagement.payment.BankTransfer;
import com.example.salesmanagement.payment.Check;
import com.example.salesmanagement.payment.Payment;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Entity(name = "Invoice")
@Table(name = "invoice")
@DiscriminatorValue("INVOICE")
@PrimaryKeyJoinColumn(
        name = "id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "invoice_sales_document_fk"))
public class Invoice extends SalesDocument implements Cloneable {
    @Column(name = "due_date")
    @Temporal(TemporalType.DATE)
    private LocalDate dueDate;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(nullable = false)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Payment> payments = new HashSet<>();

    public Invoice() {
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public Set<Payment> getPayments() {
        return new HashSet<>(payments);
    }

    public void setPayments(Set<Payment> payments) {
        this.payments = payments;
    }

    public void addPayment(Payment payment) {
        this.payments.add(payment);
    }

    public void removePayment(Payment payment) {
        this.payments.remove(payment);
    }

    public void updatePayment(Payment updatedpayment) {
        Optional<Payment> optionalPayment = payments.stream().filter(payment -> payment.equals(updatedpayment)).findFirst();

        if (optionalPayment.isPresent()) {
            Payment originalPayment = optionalPayment.get();

            originalPayment.setAmount(updatedpayment.getAmount());
            originalPayment.setPaymentDate(updatedpayment.getPaymentDate());

            if (originalPayment instanceof BankTransfer bankTransfer) {
                BankTransfer payment = (BankTransfer) updatedpayment;

                bankTransfer.setBankName(payment.getBankName());
                bankTransfer.setTransactionId(payment.getTransactionId());
                bankTransfer.setBeneficiaryName(payment.getBeneficiaryName());
                bankTransfer.setAccountNumber(payment.getAccountNumber());
            }

            if (originalPayment instanceof Check check) {
                Check payment = (Check) updatedpayment;

                check.setBankName(payment.getBankName());
                check.setCheckNumber(payment.getCheckNumber());
                check.setPayeeName(payment.getPayeeName());
                check.setSenderAccount(payment.getSenderAccount());
                check.setDueDate(payment.getDueDate());
                check.setIssueDate(payment.getIssueDate());
                check.setCheckStatus(payment.getCheckStatus());
            }
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object cloned = super.clone();
        Invoice clonedInvoice = (Invoice) cloned;

        clonedInvoice.setClient((Client) getClient().clone());
        clonedInvoice.setPayments(getPayments());
        clonedInvoice.setItems(getItems());

        return clonedInvoice;
    }

}
