package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.document.Document;
import jakarta.persistence.*;

@Entity(name = "DocumentBasedMouvementSource")
@Table(name = "document_based_mouvement_source")
@DiscriminatorValue("DOCUMENT")
@PrimaryKeyJoinColumn(
        name = "id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "document_based_mouvement_source_fk"))
public class DocumentBasedMouvementSource extends MouvementSource {
    @ManyToOne
    private Document source;

    public DocumentBasedMouvementSource() {
    }

    public DocumentBasedMouvementSource(Document document) {
        this.source = document;
    }

    public Document getSource() {
        return source;
    }

    public void setSource(Document document) {
        this.source = document;
    }
}
