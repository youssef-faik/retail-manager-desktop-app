package com.example.gestioncommercial;

import com.example.gestioncommercial.client.Client;
import com.example.gestioncommercial.product.Product;
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

    public ObservableList<Product> getProducts(String selectQuery) {
        ObservableList<Product> products = FXCollections.observableArrayList();
        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(selectQuery);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                products.add(
                        new Product(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getBigDecimal("purchase_price_excluding_tax"),
                                resultSet.getBigDecimal("selling_price_excluding_tax"),
                                resultSet.getString("description"),
                                resultSet.getInt("quantity"),
                                resultSet.getBigDecimal("tax_rate")
                        )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }
}
