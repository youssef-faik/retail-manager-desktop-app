package com.example.salesmanagement.invoice;

import com.example.salesmanagement.product.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity(name = "InvoiceItem")
@Table(name = "invoice_item")
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;
    private BigDecimal unitPriceExcludingTaxes;
    private BigDecimal totalExcludingTaxes;
    private BigDecimal totalIncludingTaxes;
    private BigDecimal totalTaxes;

    @JoinColumn(
            name = "product_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "invoice_item_product_fk"))
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private Product product;

    public InvoiceItem() {
        this.quantity = 0;
        this.unitPriceExcludingTaxes = BigDecimal.ZERO;
        this.totalExcludingTaxes = BigDecimal.ZERO;
        this.totalIncludingTaxes = BigDecimal.ZERO;
        this.totalTaxes = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPriceExcludingTaxes() {
        return unitPriceExcludingTaxes;
    }

    public void setUnitPriceExcludingTaxes(BigDecimal unitPriceExcludingTaxes) {
        this.unitPriceExcludingTaxes = unitPriceExcludingTaxes;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceItem that = (InvoiceItem) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
