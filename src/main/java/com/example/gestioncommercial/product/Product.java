package com.example.gestioncommercial.product;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {
    private int id;
    private String name;
    private BigDecimal purchasePriceExcludingTax;
    private BigDecimal sellingPriceExcludingTax;
    private String description;
    private int quantity;
    private BigDecimal taxRate;

    public Product() {

    }

    public Product(int id, String name, BigDecimal purchasePriceExcludingTax, BigDecimal sellingPriceExcludingTax, String description, int quantity, BigDecimal taxRate) {
        this.id = id;
        this.name = name;
        this.purchasePriceExcludingTax = purchasePriceExcludingTax;
        this.sellingPriceExcludingTax = sellingPriceExcludingTax;
        this.description = description;
        this.quantity = quantity;
        this.taxRate = taxRate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
