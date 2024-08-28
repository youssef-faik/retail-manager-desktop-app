package com.example.salesmanagement.product;

import com.example.salesmanagement.category.Category;
import com.example.salesmanagement.stockmouvement.MouvementType;
import com.example.salesmanagement.stockmouvement.StockMouvement;
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
    private List<StockMouvement> stockMouvements = new ArrayList<>();

    public Product() {
    }

    public List<StockMouvement> getStockMovements() {
        return stockMouvements;
    }

    public void setStockMovements(List<StockMouvement> stockMouvements) {
        this.stockMouvements = stockMouvements;
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

        for (StockMouvement stockMouvement : stockMouvements) {
            if (!stockMouvement.isCanceled()) {
                if (Objects.requireNonNull(stockMouvement.getMovementType()) == MouvementType.STOCK_ENTRY) {
                    quantity += stockMouvement.getQuantity();
                } else if (stockMouvement.getMovementType() == MouvementType.OUT_OF_STOCK) {
                    quantity -= stockMouvement.getQuantity();
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
