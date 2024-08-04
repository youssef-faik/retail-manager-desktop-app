package com.example.gestioncommercial;

import com.example.gestioncommercial.client.Client;
import com.example.gestioncommercial.configuration.ConfigKey;
import com.example.gestioncommercial.configuration.ConfigOption;
import com.example.gestioncommercial.invoice.Invoice;
import com.example.gestioncommercial.invoice.InvoiceItem;
import com.example.gestioncommercial.invoice.InvoiceStatus;
import com.example.gestioncommercial.payment.*;
import com.example.gestioncommercial.product.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                                resultSet.getLong("reference"),
                                LocalDate.parse(resultSet.getString("issue_date")),
                                InvoiceStatus.valueOf(resultSet.getString("status")),
                                resultSet.getBigDecimal("paid_amount"),
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


    public Long getCountInvoice() {
        String query = "select count(*) count_invoice from invoice;";
        long count = 0L;
        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                count = resultSet.getLong("count_invoice");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    public Invoice getInvoiceById(Long id) {
        Invoice invoice = new Invoice();
        invoice.setId(id);

        getInvoiceAndClientDetails(invoice);
        getInvoiceItems(invoice);
        getInvoicePayments(invoice);

        return invoice;
    }

    private void getInvoiceAndClientDetails(Invoice invoice) {
        String query = """
                select
                    issue_date,
                    reference,
                    due_date,
                    total_excluding_taxes,
                    total_taxes,
                    total_including_taxes,
                    client_id,
                    name,
                    phone_number,
                    address,
                    common_company_identifier,
                    tax_identification_number,
                    status,
                    paid_amount
                from invoice as i
                    join client as c on i.client_id = c.id
                where i.id = %d;""".formatted(invoice.getId());


        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String dueDate = resultSet.getString("due_date");
                if (dueDate != null) {
                    invoice.setDueDate(LocalDate.parse(dueDate));
                }

                invoice.setReference(resultSet.getLong("reference"));
                invoice.setIssueDate(LocalDate.parse(resultSet.getString("issue_date")));
                invoice.setTotalExcludingTaxes(resultSet.getBigDecimal("total_excluding_taxes"));
                invoice.setTotalIncludingTaxes(resultSet.getBigDecimal("total_including_taxes"));
                invoice.setTotalTaxes(resultSet.getBigDecimal("total_taxes"));
                invoice.setStatus(InvoiceStatus.valueOf(resultSet.getString("status")));
                invoice.setPaidAmount(resultSet.getBigDecimal("paid_amount"));

                invoice.setClient(new Client(
                        resultSet.getInt("client_id"),
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

    private void getInvoicePayments(Invoice invoice) {
        String query = """
                select p.id as payment_id,
                       p.amount,
                       p.payment_date,
                       p.invoice_id,
                       p.payment_method,
                       c.id as cash_id,
                       c.cash_flow_type,
                       b.id as id_bank_transfer,
                       b.bank_name as bank_name_bank_transfer,
                       b.account_number,
                       b.transaction_id,
                       k.id as check_id,
                       k.bank_name as check_bank_name,
                       k.check_number,
                       k.due_date,
                       k.payee_name,
                       k.sender_account,
                       k.status
                from payment p
                         left join cash c on p.id = c.payment_id
                         left join bank_transfer b on p.id = b.payment_id
                         left join payment_check k on p.id = k.payment_id
                where invoice_id = %d;""".formatted(invoice.getId());


        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();


            while (resultSet.next()) {
                PaymentMethod paymentMethod = PaymentMethod.valueOf(resultSet.getString("payment_method"));

                if (paymentMethod == PaymentMethod.CASH) {
                    invoice.getPayments().add(new Cash(
                            resultSet.getLong("payment_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("payment_date") == null ? null : LocalDate.parse(resultSet.getString("payment_date")),
                            PaymentMethod.CASH,
                            CashFlowType.valueOf(resultSet.getString("cash_flow_type"))
                    ));
                } else if (paymentMethod == PaymentMethod.BANK_TRANSFER) {
                    invoice.getPayments().add(new BankTransfer(
                            resultSet.getLong("payment_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("payment_date") == null ? null : LocalDate.parse(resultSet.getString("payment_date")),
                            resultSet.getString("account_number"),
                            resultSet.getString("transaction_id"),
                            PaymentMethod.BANK_TRANSFER,
                            resultSet.getString("bank_name_bank_transfer")
                    ));
                } else if (paymentMethod == PaymentMethod.CHECK) {
                    invoice.getPayments().add(new Check(
                            resultSet.getLong("payment_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("payment_date") == null ? null : LocalDate.parse(resultSet.getString("payment_date")),
                            resultSet.getString("payee_name"),
                            resultSet.getString("check_number"),
                            resultSet.getString("sender_account"),
                            PaymentMethod.CHECK,
                            resultSet.getString("due_date") == null ? null : LocalDate.parse(resultSet.getString("due_date")),
                            resultSet.getString("check_bank_name"),
                            CheckStatus.valueOf(resultSet.getString("status").toUpperCase())
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getInvoiceItems(Invoice invoice) {
        String query = """
                select i.id as item_id,
                       i.quantity as quantity_item,
                       unit_price_excluding_taxes,
                       total_excluding_taxes,
                       total_taxes,
                       total_including_taxes,
                       product_id,
                       name,
                       description,
                       purchase_price_excluding_tax,
                       selling_price_excluding_tax,
                       p.quantity as quantity_product,
                       tax_rate
                from invoice_item as i
                         join product as p on i.product_id = p.id
                where invoice_id = %d;""".formatted(invoice.getId());

        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                invoice.getInvoiceItems().add(
                        new InvoiceItem(
                                resultSet.getLong("item_id"),
                                new Product(
                                        resultSet.getInt("product_id"),
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

    public List<ConfigOption> getConfigOptions() {
        List<ConfigOption> configOptions = new ArrayList<>();
        String query = "select * from config_option";

        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                configOptions.add(new ConfigOption(
                        ConfigKey.valueOf(resultSet.getString("option_key")),
                        resultSet.getString("value"))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return configOptions;
    }

    public Optional<Invoice> getMostRecentInvoice() {
        String query = "SELECT * FROM invoice ORDER BY reference DESC LIMIT 1;";
        Invoice invoice = null;
        boolean isEmpty = true;

        try {
            connection = dataBaseConnector.getConnection();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                isEmpty = false;

                invoice = new Invoice();
                invoice.setId(resultSet.getLong("id"));
                invoice.setReference(resultSet.getLong("reference"));
                invoice.setStatus(InvoiceStatus.valueOf(resultSet.getString("status")));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isEmpty) {
            return Optional.empty();
        } else {
            return Optional.of(invoice);
        }

    }
}
