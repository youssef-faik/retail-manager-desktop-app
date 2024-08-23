package com.example.salesmanagement.document;

import com.example.salesmanagement.supplier.Supplier;
import jakarta.persistence.*;

@Entity(name = "PurchaseOrder")
@Table(name = "purchase_order")
@DiscriminatorValue("PURCHASE_ORDER")
@PrimaryKeyJoinColumn(
        name = "id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "purchase_order_purchase_document_fk"))
public class PurchaseOrder extends PurchaseDocument implements Cloneable {
    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    public PurchaseOrder() {
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrderStatus status) {
        this.status = status;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object cloned = super.clone();
        PurchaseOrder clonePurchaseOrder = (PurchaseOrder) cloned;

        clonePurchaseOrder.setSupplier((Supplier) getSupplier().clone());
        clonePurchaseOrder.setItems(getItems());
        clonePurchaseOrder.setStatus(status);

        return clonePurchaseOrder;
    }

}
