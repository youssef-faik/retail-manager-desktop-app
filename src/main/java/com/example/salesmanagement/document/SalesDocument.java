package com.example.salesmanagement.document;

import com.example.salesmanagement.client.Client;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity(name = "SalesDocument")
@Table(name = "sales_document")
@DiscriminatorColumn()
@DiscriminatorValue("SALES_DOCUMENT")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class SalesDocument extends Document implements Serializable {
    @JoinColumn(
            name = "client_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "sales_document_client_fk"))
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private Client client;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

}

