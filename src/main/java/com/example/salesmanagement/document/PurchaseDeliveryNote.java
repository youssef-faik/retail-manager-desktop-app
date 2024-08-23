package com.example.salesmanagement.document;

import com.example.salesmanagement.supplier.Supplier;
import jakarta.persistence.*;

@Entity(name = "PurchaseDeliveryNote")
@Table(name = "purchase_delivery_note")
@DiscriminatorValue("PURCHASE_DELIVERY_NOTE")
@PrimaryKeyJoinColumn(
        name = "id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "purchase_delivery_note_purchase_document_fk"))
public class PurchaseDeliveryNote extends PurchaseDocument implements Cloneable {
    @Enumerated(EnumType.STRING)
    private PurchaseDeliveryNoteStatus status = PurchaseDeliveryNoteStatus.DRAFT;

    public PurchaseDeliveryNote() {
    }

    public PurchaseDeliveryNoteStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseDeliveryNoteStatus status) {
        this.status = status;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object cloned = super.clone();
        PurchaseDeliveryNote purchaseDeliveryNote = (PurchaseDeliveryNote) cloned;

        purchaseDeliveryNote.setSupplier((Supplier) getSupplier().clone());
        purchaseDeliveryNote.setItems(getItems());
        purchaseDeliveryNote.setStatus(status);

        return purchaseDeliveryNote;
    }

}
