package com.example.salesmanagement.client;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Table(
        name = "client",
        uniqueConstraints = {
                @UniqueConstraint(name = "client_name_unique", columnNames = "name"),
                @UniqueConstraint(name = "client_common_company_identifier_unique", columnNames = "common_company_identifier"),
                @UniqueConstraint(name = "client_tax_identification_number_unique", columnNames = "tax_identification_number")
        }
)
@Entity(name = "Client")
public class Client implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    private String phoneNumber;

    private String address;

    @Column(name = "common_company_identifier")
    private String commonCompanyIdentifier;

    @Column(name = "tax_identification_number")
    private String taxIdentificationNumber;

    public Client() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCommonCompanyIdentifier() {
        return commonCompanyIdentifier;
    }

    public void setCommonCompanyIdentifier(String commonCompanyIdentifier) {
        this.commonCompanyIdentifier = commonCompanyIdentifier;
    }

    public String getTaxIdentificationNumber() {
        return taxIdentificationNumber;
    }

    public void setTaxIdentificationNumber(String taxIdentificationNumber) {
        this.taxIdentificationNumber = taxIdentificationNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return getId() == client.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
