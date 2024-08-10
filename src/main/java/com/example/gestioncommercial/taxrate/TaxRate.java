package com.example.gestioncommercial.taxrate;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity(name = "TaxRate")
@Table(name = "tax_rate")
public class TaxRate implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "value")
    private BigDecimal value;

    public TaxRate() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaxRate taxRate)) return false;

        return getId().equals(taxRate.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
