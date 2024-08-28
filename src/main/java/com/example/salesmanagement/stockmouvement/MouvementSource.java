package com.example.salesmanagement.stockmouvement;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity(name = "MouvementSource")
@Table(name = "mouvement_source")
@DiscriminatorColumn()
@DiscriminatorValue("MOUVEMENT_SOURCE")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class MouvementSource implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
