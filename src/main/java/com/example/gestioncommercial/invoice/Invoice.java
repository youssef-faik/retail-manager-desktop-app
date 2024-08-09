package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.client.Client;
import com.example.gestioncommercial.payment.BankTransfer;
import com.example.gestioncommercial.payment.Check;
import com.example.gestioncommercial.payment.Payment;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Entity(name = "Invoice")
@Table(name = "invoice")
public class Invoice implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference")
    private Long reference;

    @Column(name = "issue_date")
    @Temporal(TemporalType.DATE)
    private LocalDate issueDate;

    @Column(name = "due_date")
    @Temporal(TemporalType.DATE)
    private LocalDate dueDate;

    @Column(name = "total_excluding_taxes")
    private BigDecimal totalExcludingTaxes = BigDecimal.ZERO;

    @Column(name = "total_including_taxes")
    private BigDecimal totalIncludingTaxes = BigDecimal.ZERO;

    @Column(name = "total_taxes")
    private BigDecimal totalTaxes = BigDecimal.ZERO;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @JoinColumn(
            name = "client_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "invoice_client_fk"))
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    private Client client;

    @JoinColumn(
            name = "invoice_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "payment_invoice_fk"))
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Payment> payments = new HashSet<>();

    @JoinColumn(
            name = "invoice_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "invoice_item_invoice_fk"))
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<InvoiceItem> invoiceItems = new HashSet<>();

    public Invoice() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public BigDecimal getTotalExcludingTaxes() {
        return totalExcludingTaxes;
    }

    public void setTotalExcludingTaxes(BigDecimal totalExcludingTaxes) {
        this.totalExcludingTaxes = totalExcludingTaxes;
    }

    public BigDecimal getTotalIncludingTaxes() {
        return totalIncludingTaxes;
    }

    public void setTotalIncludingTaxes(BigDecimal totalIncludingTaxes) {
        this.totalIncludingTaxes = totalIncludingTaxes;
    }

    public BigDecimal getTotalTaxes() {
        return totalTaxes;
    }

    public void setTotalTaxes(BigDecimal totalTaxes) {
        this.totalTaxes = totalTaxes;
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

    public Long getReference() {
        return reference;
    }

    public void setReference(Long reference) {
        this.reference = reference;
    }

    public Set<InvoiceItem> getInvoiceItems() {
        return new HashSet<>(this.invoiceItems);
    }

    public void setInvoiceItems(Set<InvoiceItem> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    public void clearInvoiceItems() {
        this.invoiceItems.clear();
    }

    public void addInvoiceItem(InvoiceItem item) {
        this.invoiceItems.add(item);
    }

    public void removeInvoiceItem(InvoiceItem item) {
        this.invoiceItems.remove(item);
    }

    public void updateInvoiceItem(InvoiceItem updatedInvoiceItem) {
        Optional<InvoiceItem> optionalInvoiceItem = invoiceItems.stream().filter(item -> item.equals(updatedInvoiceItem)).findFirst();

        if (optionalInvoiceItem.isPresent()) {
            InvoiceItem originalInvoiceItem = optionalInvoiceItem.get();

            originalInvoiceItem.setProduct(updatedInvoiceItem.getProduct());
            originalInvoiceItem.setQuantity(updatedInvoiceItem.getQuantity());
            originalInvoiceItem.setUnitPriceExcludingTaxes(updatedInvoiceItem.getUnitPriceExcludingTaxes());
            originalInvoiceItem.setTotalExcludingTaxes(updatedInvoiceItem.getTotalExcludingTaxes());
            originalInvoiceItem.setTotalIncludingTaxes(updatedInvoiceItem.getTotalIncludingTaxes());
            originalInvoiceItem.setTotalTaxes(updatedInvoiceItem.getTotalTaxes());
        }
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

        clonedInvoice.setClient((Client) client.clone());
        clonedInvoice.setPayments(getPayments());
        clonedInvoice.setInvoiceItems(getInvoiceItems());

        return clonedInvoice;
    }

}
