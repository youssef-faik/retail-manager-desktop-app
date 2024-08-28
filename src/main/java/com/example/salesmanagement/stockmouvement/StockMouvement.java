package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.document.Document;
import com.example.salesmanagement.product.Product;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity(name = "StockMouvement")
@Table(name = "stock_mouvement")
public class StockMouvement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MouvementType mouvementType;

    @ManyToOne
    private Product product;

    @Column(name = "date_time", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime dateTime;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private MouvementSource mouvementSource;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "is_canceled")
    private boolean isCanceled = Boolean.FALSE;

    public StockMouvement(MouvementType mouvementType, Product product, int quantity, MouvementSource mouvementSource) {
        this.mouvementType = mouvementType;
        this.product = product;
        this.quantity = quantity;
        this.mouvementSource = mouvementSource;
        this.dateTime = LocalDateTime.now();

    }

    public StockMouvement(Long id, Product product, int quantity, boolean isCanceled, LocalDateTime dateTime, MouvementType mouvementType, MouvementSource mouvementSource) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.dateTime = dateTime;
        this.mouvementType = mouvementType;
        this.mouvementSource = mouvementSource;
        this.isCanceled = isCanceled;
    }

    public StockMouvement() {
        this.dateTime = LocalDateTime.now();
    }

    public static StockMouvement createStockEntryMouvement(Product product, int quantity, Document document) {
        return new StockMouvement(MouvementType.STOCK_ENTRY,
                product,
                quantity,
                new DocumentBasedMouvementSource(document)
        );
    }

    public static StockMouvement createStockEntryMouvement(Product product, int quantity, StockCorrection stockCorrection) {
        return new StockMouvement(MouvementType.STOCK_ENTRY,
                product,
                quantity,
                new StockCorrectionBasedMouvementSource(stockCorrection)
        );
    }

    public static StockMouvement createOutOfStockMouvement(Product product, int quantity, Document document) {
        return new StockMouvement(MouvementType.OUT_OF_STOCK,
                product,
                quantity,
                new DocumentBasedMouvementSource(document)
        );
    }

    public static StockMouvement createOutOfStockMouvement(Product product, int quantity, StockCorrection stockCorrection) {
        return new StockMouvement(MouvementType.OUT_OF_STOCK,
                product,
                quantity,
                new StockCorrectionBasedMouvementSource(stockCorrection)
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MouvementType getMovementType() {
        return mouvementType;
    }

    public void setMovementType(MouvementType mouvementType) {
        this.mouvementType = mouvementType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public MouvementSource getMovementSource() {
        return mouvementSource;
    }

    public void setMovementSource(MouvementSource mouvementSource) {
        this.mouvementSource = mouvementSource;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }
}
