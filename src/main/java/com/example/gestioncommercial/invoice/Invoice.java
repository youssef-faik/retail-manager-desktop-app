package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.client.Client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Invoice {
    private final Set<InvoiceItem> invoiceItems = new HashSet<>();
    private Long id;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private Client client;
    private BigDecimal totalExcludingTaxes = BigDecimal.ZERO;
    private BigDecimal totalIncludingTaxes = BigDecimal.ZERO;
    private BigDecimal totalTaxes = BigDecimal.ZERO;

    public Invoice() {
    }

    public Invoice(Long id, LocalDate issueDate, LocalDate dueDate, Client client, BigDecimal totalExcludingTaxes, BigDecimal totalIncludingTaxes, BigDecimal totalTaxes) {
        this.id = id;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.client = client;
        this.totalExcludingTaxes = totalExcludingTaxes;
        this.totalIncludingTaxes = totalIncludingTaxes;
        this.totalTaxes = totalTaxes;
    }

    public Invoice(Long id, LocalDate issueDate, String clientName, BigDecimal totalExcludingTaxes, BigDecimal totalIncludingTaxes, BigDecimal totalTaxes) {
        this.id = id;
        this.issueDate = issueDate;
        this.client = new Client();
        this.client.setName(clientName);
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
}
