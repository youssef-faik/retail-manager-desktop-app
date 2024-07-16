package com.example.gestioncommercial;

import com.example.gestioncommercial.client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataAccessObject {
    private final DataBaseConnector dataBaseConnector = new DataBaseConnector();
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private Connection connection;

    public DataAccessObject() {
    }

    public void saveData(String query) throws SQLException {
        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            preparedStatement.close();
            connection.close();
        }
    }

    public ObservableList<Client> getClients(String selectQuery) {
        ObservableList<Client> clients = FXCollections.observableArrayList();
        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(selectQuery);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                clients.add(
                        new Client(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("phone_number"),
                                resultSet.getString("address"),
                                resultSet.getString("common_company_identifier"),
                                resultSet.getString("tax_identification_number")
                        )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return clients;
    }
}
