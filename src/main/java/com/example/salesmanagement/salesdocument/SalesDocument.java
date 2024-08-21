package com.example.salesmanagement.salesdocument;

import com.example.salesmanagement.client.Client;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity(name = "SalesDocument")
@Table(name = "sales_document")
@DiscriminatorColumn()
@DiscriminatorValue("SALES_DOCUMENT")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class SalesDocument implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference")
    private Long reference;

    @Column(name = "issue_date")
    @Temporal(TemporalType.DATE)
    private LocalDate issueDate;

    @Column(name = "total_excluding_taxes")
    private BigDecimal totalExcludingTaxes = BigDecimal.ZERO;

    @Column(name = "total_including_taxes")
    private BigDecimal totalIncludingTaxes = BigDecimal.ZERO;

    @Column(name = "total_taxes")
    private BigDecimal totalTaxes = BigDecimal.ZERO;

    @JoinColumn(
            name = "client_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "sales_document_client_fk"))
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    private Client client;

    @JoinColumn(
            name = "sales_document_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "sales_document_item_sales_document_fk"))
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SalesDocumentItem> salesDocumentItems = new ArrayList<>();

    public SalesDocument() {
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

    public Long getReference() {
        return reference;
    }

    public void setReference(Long reference) {
        this.reference = reference;
    }

    public List<SalesDocumentItem> getItems() {
        return new ArrayList<>(this.salesDocumentItems);
    }

    public void setItems(List<SalesDocumentItem> salesDocumentItems) {
        this.salesDocumentItems = salesDocumentItems;
    }

    public void clearItems() {
        this.salesDocumentItems.clear();
    }

    public void addItem(SalesDocumentItem item) {
        this.salesDocumentItems.add(item);
    }

    public void removeItem(SalesDocumentItem item) {
        this.salesDocumentItems.remove(item);
    }

    public void updateItem(SalesDocumentItem updatedSalesDocumentItem) {
        Optional<SalesDocumentItem> optionalInvoiceItem = salesDocumentItems.stream().filter(item -> item.equals(updatedSalesDocumentItem)).findFirst();

        if (optionalInvoiceItem.isPresent()) {
            SalesDocumentItem originalSalesDocumentItem = optionalInvoiceItem.get();

            originalSalesDocumentItem.setProduct(updatedSalesDocumentItem.getProduct());
            originalSalesDocumentItem.setQuantity(updatedSalesDocumentItem.getQuantity());
            originalSalesDocumentItem.setUnitPriceExcludingTaxes(updatedSalesDocumentItem.getUnitPriceExcludingTaxes());
            originalSalesDocumentItem.setTotalExcludingTaxes(updatedSalesDocumentItem.getTotalExcludingTaxes());
            originalSalesDocumentItem.setTotalIncludingTaxes(updatedSalesDocumentItem.getTotalIncludingTaxes());
            originalSalesDocumentItem.setTotalTaxes(updatedSalesDocumentItem.getTotalTaxes());
        }
    }

}

