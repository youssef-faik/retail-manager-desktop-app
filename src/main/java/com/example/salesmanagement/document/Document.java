package com.example.salesmanagement.document;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity(name = "Document")
@Table(name = "document")
@DiscriminatorColumn()
@DiscriminatorValue("DOCUMENT")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Document implements Serializable {
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
            name = "sales_document_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "sales_document_item_sales_document_fk"))
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DocumentItem> documentItems = new ArrayList<>();

    public Document() {
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

    public List<DocumentItem> getItems() {
        return new ArrayList<>(this.documentItems);
    }

    public void setItems(List<DocumentItem> documentItems) {
        this.documentItems = documentItems;
    }

    public void clearItems() {
        this.documentItems.clear();
    }

    public void addItem(DocumentItem item) {
        this.documentItems.add(item);
    }

    public void removeItem(DocumentItem item) {
        this.documentItems.remove(item);
    }

    public void updateItem(DocumentItem updatedDocumentItem) {
        Optional<DocumentItem> optionalItem = documentItems.stream().filter(item -> item.equals(updatedDocumentItem)).findFirst();

        if (optionalItem.isPresent()) {
            DocumentItem originalDocumentItem = optionalItem.get();

            originalDocumentItem.setProduct(updatedDocumentItem.getProduct());
            originalDocumentItem.setQuantity(updatedDocumentItem.getQuantity());
            originalDocumentItem.setUnitPriceExcludingTaxes(updatedDocumentItem.getUnitPriceExcludingTaxes());
            originalDocumentItem.setTotalExcludingTaxes(updatedDocumentItem.getTotalExcludingTaxes());
            originalDocumentItem.setTotalIncludingTaxes(updatedDocumentItem.getTotalIncludingTaxes());
            originalDocumentItem.setTotalTaxes(updatedDocumentItem.getTotalTaxes());
        }
    }

}

