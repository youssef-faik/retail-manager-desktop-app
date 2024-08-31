package com.example.salesmanagement.stockmouvement;

import jakarta.persistence.*;

@Entity(name = "StockCorrectionBasedMouvementSource")
@Table(name = "stock_correction_based_mouvement_source")
@DiscriminatorValue("STOCK_CORRECTION")
@PrimaryKeyJoinColumn(
        name = "id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "stock_correction_based_mouvement_source_fk"))
public class StockCorrectionBasedMovementSource extends MovementSource {
    @ManyToOne
    private StockCorrection source;

    public StockCorrectionBasedMovementSource(StockCorrection source) {
        this.source = source;
    }

    public StockCorrectionBasedMovementSource() {
    }

    public StockCorrection getSource() {
        return source;
    }

    public void setSource(StockCorrection source) {
        this.source = source;
    }
}
