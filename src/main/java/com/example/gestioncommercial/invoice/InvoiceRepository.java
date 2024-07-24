package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.DataAccessObject;
import com.example.gestioncommercial.product.Product;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;

public class InvoiceRepository {
    private final DataAccessObject dao;

    public InvoiceRepository() {
        this.dao = new DataAccessObject();
    }

    public ObservableList<Invoice> findAllJoinClient() {
        String query = """
                select I.id as id, issue_date, name, total_excluding_taxes, total_including_taxes, total_taxes
                from invoice as I
                    join Client as C on I.id_client = C.id;""";

        return dao.getAllInvoices(query);
    }

    public Invoice findById(long id) {
        return dao.getInvoiceById(id);
    }

    public void save(Invoice invoice) throws SQLException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", symbols);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String insertInvoiceQuery = "insert into invoice(issue_date, id_client, total_excluding_taxes, total_taxes, total_including_taxes)" +
                "values ('%s', %s, %s, %s, %s);"
                        .formatted(
                                invoice.getIssueDate().format(formatter),
                                invoice.getClient().getId(),
                                df.format(invoice.getTotalExcludingTaxes()),
                                df.format(invoice.getTotalTaxes()),
                                df.format(invoice.getTotalIncludingTaxes())
                        );

        // save invoice items to DB and retrieve its id
        dao.saveData(insertInvoiceQuery);
        invoice.setId((long) dao.getLastInsertedId());

        // save invoice items
        StringBuilder insertInvoiceItemQuery = new StringBuilder("insert into invoice_item(id_invoice, id_product, quantity, unit_price_excluding_taxes, total_excluding_taxes, total_taxes, total_including_taxes) values ");

        invoice.getInvoiceItems().forEach(item -> insertInvoiceItemQuery.append("(%d, %d, %d, %s, %s, %s, %s),"
                .formatted(
                        invoice.getId(),
                        item.getProduct().getId(),
                        item.getQuantity(),
                        df.format(item.getUnitPriceExcludingTaxes()),
                        df.format(item.getTotalExcludingTaxes()),
                        df.format(item.getTotalTaxes()),
                        df.format(item.getTotalIncludingTaxes())
                )));

        insertInvoiceItemQuery.deleteCharAt(insertInvoiceItemQuery.length() - 1);
        insertInvoiceItemQuery.append(";");

        // save invoice items to DB
        dao.saveData(insertInvoiceItemQuery.toString());
    }

    public void update(Invoice invoice) throws SQLException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", symbols);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String insertInvoiceQuery = """
                update invoice
                set issue_date = '%s',
                id_client = %s,
                total_excluding_taxes = %s,
                total_taxes = %s,
                total_including_taxes = %s
                where id = %d;"""
                .formatted(
                        invoice.getIssueDate().format(formatter),
                        invoice.getClient().getId(),
                        df.format(invoice.getTotalExcludingTaxes()),
                        df.format(invoice.getTotalTaxes()),
                        df.format(invoice.getTotalIncludingTaxes()),
                        invoice.getId()
                );

        // save invoice items to DB and retrieve its id
        dao.saveData(insertInvoiceQuery);

        // delete old invoice items
        String deleteInvoiceItemsQuery = "DELETE FROM invoice_item WHERE id_invoice = " + invoice.getId();
        dao.saveData(deleteInvoiceItemsQuery);

        // insert new invoice items
        StringBuilder insertInvoiceItemQuery = new StringBuilder("insert into invoice_item(id_invoice, id_product, quantity, unit_price_excluding_taxes, total_excluding_taxes, total_taxes, total_including_taxes) values ");

        invoice.getInvoiceItems().forEach(invoiceItem -> insertInvoiceItemQuery.append("(%d, %d, %d, %s, %s, %s, %s),"
                .formatted(
                        invoice.getId(),
                        invoiceItem.getProduct().getId(),
                        invoiceItem.getQuantity(),
                        df.format(invoiceItem.getUnitPriceExcludingTaxes()),
                        df.format(invoiceItem.getTotalExcludingTaxes()),
                        df.format(invoiceItem.getTotalTaxes()),
                        df.format(invoiceItem.getTotalIncludingTaxes())
                )));


        insertInvoiceItemQuery.deleteCharAt(insertInvoiceItemQuery.length() - 1);
        insertInvoiceItemQuery.append(";");

        // save invoice items to DB
        dao.saveData(insertInvoiceItemQuery.toString());
    }

    public void deleteById(long id) throws SQLException {
        String deleteInvoiceItemsQuery = "DELETE FROM invoice_item WHERE id_invoice = " + id;
        String deleteInvoiceQuery = "DELETE FROM invoice WHERE id = " + id;
        dao.saveData(deleteInvoiceItemsQuery);
        dao.saveData(deleteInvoiceQuery);
    }

    public ObservableList<Product> findAll() {
        return dao.getProducts("SELECT * FROM Product");
    }
}
