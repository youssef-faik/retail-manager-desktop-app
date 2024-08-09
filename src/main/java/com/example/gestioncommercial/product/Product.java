package com.example.gestioncommercial.product;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Table(
        name = "product",
        uniqueConstraints = @UniqueConstraint(name = "product_name_unique", columnNames = "name")
)
@Entity(name = "Product")
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal purchasePriceExcludingTax;
    private BigDecimal sellingPriceExcludingTax;
    private String description;
    private int quantity;
    private BigDecimal taxRate;

    public Product() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPurchasePriceExcludingTax() {
        return purchasePriceExcludingTax;
    }

    public void setPurchasePriceExcludingTax(BigDecimal purchasePriceExcludingTax) {
        this.purchasePriceExcludingTax = purchasePriceExcludingTax;
    }

    public BigDecimal getSellingPriceExcludingTax() {
        return sellingPriceExcludingTax;
    }

    public void setSellingPriceExcludingTax(BigDecimal sellingPriceExcludingTax) {
        this.sellingPriceExcludingTax = sellingPriceExcludingTax;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return getId() == product.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
