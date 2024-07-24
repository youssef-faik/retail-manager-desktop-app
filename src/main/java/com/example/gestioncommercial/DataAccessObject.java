package com.example.gestioncommercial;

import com.example.gestioncommercial.client.Client;
import com.example.gestioncommercial.invoice.Invoice;
import com.example.gestioncommercial.invoice.InvoiceItem;
import com.example.gestioncommercial.product.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class DataAccessObject {
    private final DataBaseConnector dataBaseConnector = new DataBaseConnector();
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private Connection connection;
    private int lastInsertedId;

    public DataAccessObject() {
    }

    public void saveData(String query) throws SQLException {
        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(query, preparedStatement.RETURN_GENERATED_KEYS);

            // Execute the insert
            int affectedRows = preparedStatement.executeUpdate();


            // Check if the insert was successful
            if (affectedRows > 0) {
                // Retrieve the generated keys
                try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                    if (rs.next()) {
                        lastInsertedId = rs.getInt(1);
                    }
                }
            }

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

    public int getLastInsertedId() {
        int id = lastInsertedId;
        lastInsertedId = 0;
        return id;
    }

    public ObservableList<Invoice> getAllInvoices(String query) {
        ObservableList<Invoice> invoices = FXCollections.observableArrayList();
        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                invoices.add(
                        new Invoice(
                                resultSet.getLong("id"),
                                LocalDate.parse(resultSet.getString("issue_date")),
                                resultSet.getString("name"),
                                resultSet.getBigDecimal("total_excluding_taxes"),
                                resultSet.getBigDecimal("total_including_taxes"),
                                resultSet.getBigDecimal("total_taxes")
                        )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return invoices;
    }

    public Invoice getInvoiceById(Long id) {
        Invoice invoice = new Invoice();
        invoice.setId(id);

        getInvoiceAndClientDetails(invoice);
        getInvoiceItems(invoice);

        return invoice;
    }

    private void getInvoiceAndClientDetails(Invoice invoice) {
        String query = """
                select
                    issue_date,
                    total_excluding_taxes,
                    total_taxes,
                    total_including_taxes,
                    id_client,
                    name,
                    phone_number,
                    address,
                    common_company_identifier,
                    tax_identification_number
                from invoice as I
                    join Client as C on I.id_client = C.id
                where I.id = %d;""".formatted(invoice.getId());


        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                invoice.setIssueDate(LocalDate.parse(resultSet.getString("issue_date")));
                invoice.setTotalExcludingTaxes(resultSet.getBigDecimal("total_excluding_taxes"));
                invoice.setTotalIncludingTaxes(resultSet.getBigDecimal("total_including_taxes"));
                invoice.setTotalTaxes(resultSet.getBigDecimal("total_taxes"));

                invoice.setClient(new Client(
                        resultSet.getInt("id_client"),
                        resultSet.getString("name"),
                        resultSet.getString("phone_number"),
                        resultSet.getString("address"),
                        resultSet.getString("common_company_identifier"),
                        resultSet.getString("tax_identification_number")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getInvoiceItems(Invoice invoice) {
        String query = """
                select I.id as id_item,
                       I.quantity as quantity_item,
                       unit_price_excluding_taxes,
                       total_excluding_taxes,
                       total_taxes,
                       total_including_taxes,
                       id_product,
                       name,
                       description,
                       purchase_price_excluding_tax,
                       selling_price_excluding_tax,
                       P.quantity as quantity_product,
                       tax_rate
                from invoice_item as I
                         join Product as P on I.id_product = P.id
                where id_invoice = %d;""".formatted(invoice.getId());

        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                invoice.getInvoiceItems().add(
                        new InvoiceItem(
                                resultSet.getLong("id_item"),
                                new Product(
                                        resultSet.getInt("id_product"),
                                        resultSet.getString("name"),
                                        resultSet.getBigDecimal("purchase_price_excluding_tax"),
                                        resultSet.getBigDecimal("selling_price_excluding_tax"),
                                        resultSet.getString("description"),
                                        resultSet.getInt("quantity_product"),
                                        resultSet.getBigDecimal("tax_rate")
                                ),
                                invoice,
                                resultSet.getInt("quantity_item"),
                                resultSet.getBigDecimal("unit_price_excluding_taxes"),
                                resultSet.getBigDecimal("total_excluding_taxes"),
                                resultSet.getBigDecimal("total_including_taxes"),
                                resultSet.getBigDecimal("total_taxes")
                        ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
