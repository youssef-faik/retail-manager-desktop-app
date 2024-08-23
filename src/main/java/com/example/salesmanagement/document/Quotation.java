package com.example.salesmanagement.document;

import com.example.salesmanagement.client.Client;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity(name = "Quotation")
@Table(name = "quotation")
@DiscriminatorValue("QUOTATION")
@PrimaryKeyJoinColumn(
        name = "id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "quotation_sales_document_fk"))
public class Quotation extends SalesDocument implements Cloneable {
    @Column(name = "valid_until")
    @Temporal(TemporalType.DATE)
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    private QuotationStatus status = QuotationStatus.DRAFT;


    public Quotation() {
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public QuotationStatus getStatus() {
        return status;
    }

    public void setStatus(QuotationStatus status) {
        this.status = status;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object cloned = super.clone();
        Quotation clonedQuotation = (Quotation) cloned;

        clonedQuotation.setClient((Client) getClient().clone());
        clonedQuotation.setItems(getItems());
        clonedQuotation.setValidUntil(validUntil);
        clonedQuotation.setStatus(status);

        return clonedQuotation;
    }

}
