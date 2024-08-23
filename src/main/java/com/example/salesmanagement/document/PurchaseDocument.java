package com.example.salesmanagement.document;

import com.example.salesmanagement.supplier.Supplier;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity(name = "PurchaseDocument")
@Table(name = "purchase_document")
@DiscriminatorColumn()
@DiscriminatorValue("PURCHASE_DOCUMENT")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PurchaseDocument extends Document implements Serializable {
    @JoinColumn(
            name = "supplier_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "purchase_document_supplier_fk"))
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    private Supplier supplier;

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

}

