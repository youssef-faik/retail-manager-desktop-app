package com.example.salesmanagement.stockmouvement;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity(name = "MovementSource")
@Table(name = "movement_source")
@DiscriminatorColumn()
@DiscriminatorValue("MOVEMENT_SOURCE")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class MovementSource implements Serializable {
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
