package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.document.Document;
import jakarta.persistence.*;

@Entity(name = "DocumentBasedMovementSource")
@Table(name = "document_based_mouvement_source")
@DiscriminatorValue("DOCUMENT")
@PrimaryKeyJoinColumn(
        name = "id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "document_based_movement_source_fk"))
public class DocumentBasedMovementSource extends MovementSource {
    @ManyToOne(cascade = CascadeType.ALL)
    private Document source;

    public DocumentBasedMovementSource() {
    }

    public DocumentBasedMovementSource(Document document) {
        this.source = document;
    }

    public Document getSource() {
        return source;
    }

    public void setSource(Document document) {
        this.source = document;
    }
}
