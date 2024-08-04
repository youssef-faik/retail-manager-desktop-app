package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.DataAccessObject;
import com.example.gestioncommercial.payment.BankTransfer;
import com.example.gestioncommercial.payment.Cash;
import com.example.gestioncommercial.payment.Check;
import com.example.gestioncommercial.payment.Payment;
import com.example.gestioncommercial.product.Product;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class InvoiceRepository {
    private final DataAccessObject dao;

    public InvoiceRepository() {
        this.dao = new DataAccessObject();
    }

    public ObservableList<Invoice> findAllJoinClient() {
        String query = """
                select i.id as id, reference, issue_date, status, paid_amount, name, total_excluding_taxes, total_including_taxes, total_taxes
                from invoice as i
                    join client as c on i.client_id = c.id;""";

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

        String insertInvoiceQuery = "insert into invoice(reference, issue_date, due_date, client_id, total_excluding_taxes, total_taxes, total_including_taxes, status, paid_amount)" +
                "values (%d, '%s', %s, %s, %s, %s, %s, '%s', %s);"
                        .formatted(
                                invoice.getReference() == null ? 0L : invoice.getReference(),
                                invoice.getIssueDate().format(formatter),
                                invoice.getDueDate() == null ? "null" : "'" + invoice.getDueDate().format(formatter) + "'",
                                invoice.getClient().getId(),
                                df.format(invoice.getTotalExcludingTaxes()),
                                df.format(invoice.getTotalTaxes()),
                                df.format(invoice.getTotalIncludingTaxes()),
                                invoice.getStatus().name(),
                                df.format(invoice.getPaidAmount())
                        );

        // save invoice items to DB and retrieve its id
        dao.saveData(insertInvoiceQuery);
        invoice.setId((long) dao.getLastInsertedId());

        // save invoice items
        StringBuilder insertInvoiceItemQuery = new StringBuilder("insert into invoice_item(invoice_id, product_id, quantity, unit_price_excluding_taxes, total_excluding_taxes, total_taxes, total_including_taxes) values ");

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

        savePayments(invoice, df, formatter);
    }

    public void update(Invoice invoice) throws SQLException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", symbols);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String insertInvoiceQuery = """
                update invoice
                set issue_date = %s,
                due_date = %s,
                reference = %d,
                client_id = %s,
                total_excluding_taxes = %s,
                total_taxes = %s,
                total_including_taxes = %s,
                status = '%s',
                paid_amount = %s
                where id = %d;"""
                .formatted(
                        invoice.getIssueDate() == null ? "null" : "'" + invoice.getIssueDate().format(formatter) + "'",
                        invoice.getDueDate() == null ? "null" : "'" + invoice.getDueDate().format(formatter) + "'",
                        invoice.getReference(),
                        invoice.getClient().getId(),
                        df.format(invoice.getTotalExcludingTaxes()),
                        df.format(invoice.getTotalTaxes()),
                        df.format(invoice.getTotalIncludingTaxes()),
                        invoice.getStatus().name(),
                        df.format(invoice.getPaidAmount()),
                        invoice.getId()
                );

        // save invoice items to DB and retrieve its id
        dao.saveData(insertInvoiceQuery);

        // delete old invoice items
        String deleteInvoiceItemsQuery = "delete from invoice_item where invoice_id = " + invoice.getId();
        dao.saveData(deleteInvoiceItemsQuery);

        // insert new invoice items
        StringBuilder insertInvoiceItemQuery = new StringBuilder("insert into invoice_item(invoice_id, product_id, quantity, unit_price_excluding_taxes, total_excluding_taxes, total_taxes, total_including_taxes) values ");

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

        deletePaymentsByInvoiceId(invoice.getId());
        savePayments(invoice, df, formatter);
    }

    public void deleteById(long id) throws SQLException {
        deletePaymentsByInvoiceId(id);

        String deleteInvoiceItemsQuery = "delete from invoice_item where invoice_id = " + id;
        String deleteInvoiceQuery = "delete from invoice where id = " + id;

        dao.saveData(deleteInvoiceItemsQuery);
        dao.saveData(deleteInvoiceQuery);
    }

    public ObservableList<Product> findAll() {
        return dao.getProducts("select * from product");
    }

    private void savePayments(Invoice invoice, DecimalFormat df, DateTimeFormatter formatter) throws SQLException {
        for (Payment payment : invoice.getPayments()) {
            String insertPaymentQuery = """
                    insert into payment (amount, payment_date, invoice_id, payment_method)
                    values (%s, %s, %d, '%s');"""
                    .formatted(
                            df.format(payment.getAmount()),
                            payment.getPaymentDate() == null ? "null" : "'" + payment.getPaymentDate().format(formatter) + "'",
                            invoice.getId(),
                            payment.getPaymentMethod().name()
                    );

            // save payment to DB and retrieve its id
            dao.saveData(insertPaymentQuery);
            payment.setId(dao.getLastInsertedId());

            if (payment instanceof Cash cash) {
                insertPaymentQuery = """
                        insert into cash (cash_flow_type, payment_id)
                        values ('%s', %d)"""
                        .formatted(cash.getCashFlowType(), payment.getId());
            } else if (payment instanceof BankTransfer bankTransfer) {
                insertPaymentQuery = """
                        insert into bank_transfer (bank_name, payment_id, account_number, transaction_id)
                        values ('%s', %d, '%s', '%s');"""
                        .formatted(
                                bankTransfer.getBankName(),
                                payment.getId(),
                                bankTransfer.getAccountNumber(),
                                bankTransfer.getTransactionId()
                        );
            } else if (payment instanceof Check check) {
                insertPaymentQuery = """
                        insert into payment_check(bank_name, due_date, status, payment_id, check_number, payee_name, sender_account)
                        values ('%s', %s, '%s', %d, '%s', '%s', '%s');"""
                        .formatted(
                                check.getBankName(),
                                check.getDueDate() == null ? "null" : "'" + check.getDueDate().format(formatter) + "'",
                                check.getCheckStatus().name(),
                                check.getId(),
                                check.getCheckNumber(),
                                check.getPayeeName(),
                                check.getSenderAccount()
                        );
            }

            // save
            dao.saveData(insertPaymentQuery);
        }
    }

    private void deletePaymentsByInvoiceId(long id) throws SQLException {
        String deleteCheckPaymentsQuery = """
                delete from payment_check
                where payment_id in (
                        select id
                        from payment
                        where invoice_id = %d
                );""".formatted(id);

        String deleteCashPaymentsQuery = """
                delete from cash
                where payment_id in (
                        select id
                        from payment
                        where invoice_id = %d
                );""".formatted(id);

        String deleteBankTransferPaymentsQuery = """
                delete from bank_transfer
                where payment_id in (
                        select id
                        from payment
                        where invoice_id = %d
                );""".formatted(id);

        String deletePaymentsQuery = """
                delete from payment
                where invoice_id = %d""".formatted(id);

        dao.saveData(deleteCashPaymentsQuery);
        dao.saveData(deleteCheckPaymentsQuery);
        dao.saveData(deleteBankTransferPaymentsQuery);
        dao.saveData(deletePaymentsQuery);
    }

    public long count() {
        return dao.getCountInvoice();
    }

    public Optional<Invoice> findFirstByOrderByIdDesc() {
        return dao.getMostRecentInvoice();
    }
}
