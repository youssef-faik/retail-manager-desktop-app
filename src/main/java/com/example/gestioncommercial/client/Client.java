package com.example.gestioncommercial.client;

import java.util.Objects;

public class Client {
    private int id;
    private String name;
    private String phoneNumber;
    private String address;
    private String commonCompanyIdentifier;
    private String taxIdentificationNumber;

    public Client() {
    }

    public Client(int id, String name, String phoneNumber, String address, String commonCompanyIdentifier, String taxIdentificationNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.commonCompanyIdentifier = commonCompanyIdentifier;
        this.taxIdentificationNumber = taxIdentificationNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
}
