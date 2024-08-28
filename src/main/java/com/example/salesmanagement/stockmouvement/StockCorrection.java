package com.example.salesmanagement.stockmouvement;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity(name = "StockCorrection")
@Table(name = "stock_correction")
public class StockCorrection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dateTime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime dateTime;

    @JoinColumn(
            name = "stock_correction_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "stock_correction_item_fk"))
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<StockCorrectionItem> items = new ArrayList<>();

    public StockCorrection() {
        dateTime = LocalDateTime.now();
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<StockCorrectionItem> getItems() {
        return new ArrayList<>(this.items);
    }

    public void setItems(List<StockCorrectionItem> documentItems) {
        this.items = documentItems;
    }

    public void clearItems() {
        this.items.clear();
    }

    public void addItem(StockCorrectionItem item) {
        this.items.add(item);
    }

    public void removeItem(StockCorrectionItem item) {
        this.items.remove(item);
    }

    public void updateItem(StockCorrectionItem updatedDocumentItem) {
        Optional<StockCorrectionItem> optionalItem = items.stream().filter(item -> item.equals(updatedDocumentItem)).findFirst();

        if (optionalItem.isPresent()) {
            StockCorrectionItem originaltItem = optionalItem.get();

            originaltItem.setProduct(updatedDocumentItem.getProduct());
            originaltItem.setQuantity(updatedDocumentItem.getQuantity());
            originaltItem.setCorrectionType(updatedDocumentItem.getCorrectionType());
        }
    }


}
