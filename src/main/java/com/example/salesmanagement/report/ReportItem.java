package com.example.salesmanagement.report;

import java.math.BigDecimal;

public class ReportItem {
    private String productDesignation;
    private int quantity;
    private int taxRate;
    private BigDecimal unitPriceExcludingTaxes;
    private BigDecimal totalExcludingTaxes;
    private BigDecimal totalIncludingTaxes;
    private BigDecimal totalTaxes;

    public ReportItem(String productDesignation, int quantity, BigDecimal unitPriceExcludingTaxes, int taxRate, BigDecimal totalExcludingTaxes, BigDecimal totalIncludingTaxes, BigDecimal totalTaxes) {
        this.productDesignation = productDesignation;
        this.quantity = quantity;
        this.unitPriceExcludingTaxes = unitPriceExcludingTaxes;
        this.taxRate = taxRate;
        this.totalExcludingTaxes = totalExcludingTaxes;
        this.totalIncludingTaxes = totalIncludingTaxes;
        this.totalTaxes = totalTaxes;
    }

    public String getProductDesignation() {
        return productDesignation;
    }

    public void setProductDesignation(String productDesignation) {
        this.productDesignation = productDesignation;
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

    public int getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(int taxRate) {
        this.taxRate = taxRate;
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
