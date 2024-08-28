package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.product.Product;
import jakarta.persistence.*;

import java.util.Objects;

@Entity(name = "StockCorrectionItem")
@Table(name = "stock_correction_item")
public class StockCorrectionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private StockCorrectionType correctionType;

    @JoinColumn(
            name = "product_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "stock_mouvement_item_product_fk"))
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private Product product;

    public StockCorrectionItem() {
        quantity = 0;
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

    public StockCorrectionType getCorrectionType() {
        return correctionType;
    }

    public void setCorrectionType(StockCorrectionType correctionType) {
        this.correctionType = correctionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockCorrectionItem that = (StockCorrectionItem) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
