package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.document.Document;
import com.example.salesmanagement.product.Product;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity(name = "StockMovement")
@Table(name = "stock_movement")
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @ManyToOne
    private Product product;

    @Column(name = "date_time", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime dateTime;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private MovementSource movementSource;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "is_canceled")
    private boolean isCanceled = Boolean.FALSE;

    public StockMovement(MovementType movementType, Product product, int quantity, MovementSource movementSource) {
        this.movementType = movementType;
        this.product = product;
        this.quantity = quantity;
        this.movementSource = movementSource;
        this.dateTime = LocalDateTime.now();

    }

    public StockMovement(Long id, Product product, int quantity, boolean isCanceled, LocalDateTime dateTime, MovementType movementType, MovementSource movementSource) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.dateTime = dateTime;
        this.movementType = movementType;
        this.movementSource = movementSource;
        this.isCanceled = isCanceled;
    }

    public StockMovement() {
        this.dateTime = LocalDateTime.now();
    }

    public static StockMovement createStockEntryMouvement(Product product, int quantity, Document document) {
        return new StockMovement(MovementType.STOCK_ENTRY,
                product,
                quantity,
                new DocumentBasedMovementSource(document)
        );
    }

    public static StockMovement createStockEntryMouvement(Product product, int quantity, StockCorrection stockCorrection) {
        return new StockMovement(MovementType.STOCK_ENTRY,
                product,
                quantity,
                new StockCorrectionBasedMovementSource(stockCorrection)
        );
    }

    public static StockMovement createOutOfStockMouvement(Product product, int quantity, Document document) {
        return new StockMovement(MovementType.OUT_OF_STOCK,
                product,
                quantity,
                new DocumentBasedMovementSource(document)
        );
    }

    public static StockMovement createOutOfStockMouvement(Product product, int quantity, StockCorrection stockCorrection) {
        return new StockMovement(MovementType.OUT_OF_STOCK,
                product,
                quantity,
                new StockCorrectionBasedMovementSource(stockCorrection)
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
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

    public MovementSource getMovementSource() {
        return movementSource;
    }

    public void setMovementSource(MovementSource movementSource) {
        this.movementSource = movementSource;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }
}
