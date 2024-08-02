package com.example.gestioncommercial.client;

import com.example.gestioncommercial.DataAccessObject;
import javafx.collections.ObservableList;

import java.sql.SQLException;

public class ClientRepository {
    private final DataAccessObject dao;

    public ClientRepository() {
        this.dao = new DataAccessObject();
    }

    public ObservableList<Client> findAll() {
        String clientQuery = "select * from client";
        return dao.getClients(clientQuery);
    }

    public void save(Client client) throws SQLException {
        String query = "insert into client(name, phone_number, address, common_company_identifier, tax_identification_number) values('%s', '%s', '%s', '%s', '%s')"
                .formatted(
                        client.getName(),
                        client.getPhoneNumber(),
                        client.getAddress(),
                        client.getCommonCompanyIdentifier(),
                        client.getTaxIdentificationNumber()
                );

        dao.saveData(query);
    }

    public void update(Client client) throws SQLException {
        String updateClientQuery = "update client set name = '%s', phone_number ='%s', address ='%s', common_company_identifier ='%s', tax_identification_number ='%s' where id = %d"
                .formatted(
                        client.getName(),
                        client.getPhoneNumber(),
                        client.getAddress(),
                        client.getCommonCompanyIdentifier(),
                        client.getTaxIdentificationNumber(),
                        client.getId()
                );

        dao.saveData(updateClientQuery);
    }

    public void deleteById(int id) throws SQLException {
        String query = "delete from client where id = " + id;
        dao.saveData(query);
    }
}
