package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.client.Client;
import com.example.gestioncommercial.payment.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Invoice {
    private final Set<InvoiceItem> invoiceItems = new HashSet<>();
    private List<Payment> payments = new ArrayList<>();
    private Long id;
    private Long reference;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private Client client;
    private BigDecimal totalExcludingTaxes = BigDecimal.ZERO;
    private BigDecimal totalIncludingTaxes = BigDecimal.ZERO;
    private BigDecimal totalTaxes = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    public Invoice() {
    }

    public Invoice(Long id, Long reference, LocalDate issueDate, LocalDate dueDate, InvoiceStatus status, BigDecimal paidAmount, Client client, BigDecimal totalExcludingTaxes, BigDecimal totalIncludingTaxes, BigDecimal totalTaxes) {
        this.id = id;
        this.reference = reference;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
        this.paidAmount = paidAmount;
        this.client = client;
        this.totalExcludingTaxes = totalExcludingTaxes;
        this.totalIncludingTaxes = totalIncludingTaxes;
        this.totalTaxes = totalTaxes;
    }

    public Invoice(Long id, Long reference, LocalDate issueDate, InvoiceStatus status, BigDecimal paidAmount, String clientName, BigDecimal totalExcludingTaxes, BigDecimal totalIncludingTaxes, BigDecimal totalTaxes) {
        this.id = id;
        this.reference = reference;
        this.issueDate = issueDate;
        this.client = new Client();
        this.client.setName(clientName);
        this.status = status;
        this.paidAmount = paidAmount;
        this.totalExcludingTaxes = totalExcludingTaxes;
        this.totalIncludingTaxes = totalIncludingTaxes;
        this.totalTaxes = totalTaxes;
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

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
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

    public Set<InvoiceItem> getInvoiceItems() {
        return invoiceItems;
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
}
