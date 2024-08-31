package com.example.salesmanagement.product;

import com.example.salesmanagement.category.Category;
import com.example.salesmanagement.stockmouvement.MovementType;
import com.example.salesmanagement.stockmouvement.StockMovement;
import com.example.salesmanagement.taxrate.TaxRate;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

    @ManyToOne(optional = false)
    private TaxRate taxRate;

    @ManyToOne
    private Category category;

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "product")
    private List<StockMovement> stockMovements = new ArrayList<>();

    public Product() {
    }

    public List<StockMovement> getStockMovements() {
        return stockMovements;
    }

    public void setStockMovements(List<StockMovement> stockMovements) {
        this.stockMovements = stockMovements;
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

    public TaxRate getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(TaxRate taxRate) {
        this.taxRate = taxRate;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getQuantity() {
        int quantity = 0;

        for (StockMovement stockMovement : stockMovements) {
            if (!stockMovement.isCanceled()) {
                if (Objects.requireNonNull(stockMovement.getMovementType()) == MovementType.STOCK_ENTRY) {
                    quantity += stockMovement.getQuantity();
                } else if (stockMovement.getMovementType() == MovementType.OUT_OF_STOCK) {
                    quantity -= stockMovement.getQuantity();
                }
            }
        }

        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(getId(), product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
