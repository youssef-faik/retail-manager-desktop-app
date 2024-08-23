package com.example.salesmanagement.document;

import com.example.salesmanagement.client.Client;
import jakarta.persistence.*;

@Entity(name = "DeliveryNote")
@Table(name = "delivery_note")
@DiscriminatorValue("DELIVERY_NOTE")
@PrimaryKeyJoinColumn(
        name = "id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "delivery_note_sales_document_fk"))
public class DeliveryNote extends SalesDocument implements Cloneable {
    @Enumerated(EnumType.STRING)
    private DeliveryNoteStatus status = DeliveryNoteStatus.DRAFT;

    public DeliveryNote() {
    }

    public DeliveryNoteStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryNoteStatus status) {
        this.status = status;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object cloned = super.clone();
        DeliveryNote clonedDeliveryNote = (DeliveryNote) cloned;

        clonedDeliveryNote.setClient((Client) getClient().clone());
        clonedDeliveryNote.setItems(getItems());
        clonedDeliveryNote.setStatus(status);

        return clonedDeliveryNote;
    }

}
