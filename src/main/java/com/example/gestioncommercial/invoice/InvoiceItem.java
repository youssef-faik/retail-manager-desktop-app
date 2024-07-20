package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.product.Product;

import java.math.BigDecimal;

public class InvoiceItem {
    private Long id;
    private Product product;
    private Invoice invoice;

    private int quantity;
    private BigDecimal unitPriceExcludingTaxes;

    private BigDecimal totalExcludingTaxes;
    private BigDecimal totalIncludingTaxes;
    private BigDecimal totalTaxes;

    public InvoiceItem() {
    }

    public InvoiceItem(Long id, Product product, Invoice invoice, int quantity, BigDecimal unitPriceExcludingTaxes, BigDecimal totalExcludingTaxes, BigDecimal totalIncludingTaxes, BigDecimal totalTaxes) {
        this.id = id;
        this.product = product;
        this.invoice = invoice;
        this.quantity = quantity;
        this.unitPriceExcludingTaxes = unitPriceExcludingTaxes;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
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
}
