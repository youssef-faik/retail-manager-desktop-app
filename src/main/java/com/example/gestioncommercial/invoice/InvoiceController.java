package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.DataAccessObject;
import com.example.gestioncommercial.client.Client;
import com.example.gestioncommercial.product.Product;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InvoiceController implements Initializable {
    @FXML
    private TextField totalExcludingTaxesTextField, totalTaxesTextField, totalIncludingTaxesTextField, addressTextField, commonCompanyIdentifierTextField;
    @FXML
    private Button addProductButton, saveInvoiceButton;
    @FXML
    private DatePicker dueDateDatePicker, issueDateDatePicker;
    @FXML
    private ComboBox<Client> clientComboBox;
    @FXML
    private TableView<InvoiceItemEntry> invoiceItemEntryTableView;

    private Invoice invoice = new Invoice();
    private ObservableList<Product> products;
    private DataAccessObject dao;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dao = new DataAccessObject();
        products = dao.getProducts("SELECT * FROM Product");

        initClientComboBox();
        initInvoiceItemsTableView();

        issueDateDatePicker.setValue(LocalDate.now());

        addProductButton.setOnAction(event -> {
            InvoiceItemEntry entry = new InvoiceItemEntry(products, this);
            invoiceItemEntryTableView.getItems().add(entry);
        });

        saveInvoiceButton.setOnAction(e -> {
            try {
                saveInvoice();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        totalExcludingTaxesTextField.setText("0");
        totalIncludingTaxesTextField.setText("0");
        totalTaxesTextField.setText("0");
    }


    private void refreshTableView() {
        invoiceItemEntryTableView.refresh();
    }

    private void initInvoiceItemsTableView() {
        TableColumn<InvoiceItemEntry, String> productColumn = new TableColumn<>("Produit");
        TableColumn<InvoiceItemEntry, String> quantityColumn = new TableColumn<>("Quantité");
        TableColumn<InvoiceItemEntry, String> unitPriceColumn = new TableColumn<>("Prix unitaire (HT)");
        TableColumn<InvoiceItemEntry, String> taxRateColumn = new TableColumn<>("Taux TVA");
        TableColumn<InvoiceItemEntry, String> totalIncludingTaxesColumn = new TableColumn<>("Total (TTC)");
        TableColumn<InvoiceItemEntry, String> totalExcludingTaxesColumn = new TableColumn<>("Total (HT)");
        TableColumn<InvoiceItemEntry, String> totalTaxesColumn = new TableColumn<>("Taxes");
        TableColumn<InvoiceItemEntry, String> actionColumn = new TableColumn<>("Actions");

        productColumn.setCellValueFactory(new PropertyValueFactory<>("productComboBox"));
        quantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuantity() + ""));
        unitPriceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnitPriceExcludingTaxes().toString()));
        taxRateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTaxRate().multiply(BigDecimal.valueOf(100)).intValue() + " %"));
        totalExcludingTaxesColumn.setCellValueFactory(new PropertyValueFactory<>("totalExcludingTaxes"));
        totalIncludingTaxesColumn.setCellValueFactory(new PropertyValueFactory<>("totalIncludingTaxes"));
        totalTaxesColumn.setCellValueFactory(new PropertyValueFactory<>("totalTaxes"));

        productColumn.setMaxWidth(355);
        productColumn.setMinWidth(355);
        productColumn.setResizable(false);
        productColumn.setReorderable(false);

        actionColumn.setMinWidth(80);
        actionColumn.setMaxWidth(80);
        actionColumn.setResizable(false);
        actionColumn.setReorderable(false);

        invoiceItemEntryTableView.getColumns().addAll(productColumn, quantityColumn, unitPriceColumn, taxRateColumn, totalExcludingTaxesColumn, totalTaxesColumn, totalIncludingTaxesColumn, actionColumn);
        invoiceItemEntryTableView.setEditable(true);

        // delete
        Callback<TableColumn<InvoiceItemEntry, String>, TableCell<InvoiceItemEntry, String>> cellFoctory =
                (TableColumn<InvoiceItemEntry, String> param) -> {
                    // make cell containing button
                    final TableCell<InvoiceItemEntry, String> cell = new TableCell<InvoiceItemEntry, String>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            //that cell created only on non-empty rows
                            if (empty) {
                                setGraphic(null);
                                setText(null);

                            } else {
                                Button deleteButton = new Button("Supprimer");

                                deleteButton.setOnMouseClicked((MouseEvent event) -> {
                                    try {
                                        TableRow tableRow = (TableRow) deleteButton.getParent().getParent().getParent();
                                        InvoiceItemEntry rowItem = (InvoiceItemEntry) tableRow.getItem();
                                        invoiceItemEntryTableView.getItems().remove(rowItem);
                                        updateInvoiceTotals();
                                    } catch (Exception ex) {
                                        Logger.getLogger(InvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                });

                                HBox actionsHBox = new HBox(deleteButton);
                                actionsHBox.setStyle("-fx-alignment:center");
                                setGraphic(actionsHBox);

                                setText(null);
                            }
                        }

                    };

                    return cell;
                };

        actionColumn.setCellFactory(cellFoctory);

        // edit quantity and unit price
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        unitPriceColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        quantityColumn.setOnEditCommit(event -> {
            InvoiceItemEntry entry = event.getTableView().getItems().get(event.getTablePosition().getRow());
            try {
                entry.setQuantity(Integer.parseInt(event.getNewValue()));
                updateInvoiceItemTotals(entry);
                updateInvoiceTotals();

                refreshTableView();
            } catch (NumberFormatException e) {
                // Handle the case where the input is not a valid integer
                System.out.println("Invalid input: " + event.getNewValue());
            }
        });

        unitPriceColumn.setOnEditCommit(event -> {
            InvoiceItemEntry entry = event.getTableView().getItems().get(event.getTablePosition().getRow());
            try {
                entry.setUnitPriceExcludingTaxes(BigDecimal.valueOf(Long.parseLong(event.getNewValue())));
                updateInvoiceItemTotals(entry);
                updateInvoiceTotals();

                refreshTableView();
            } catch (NumberFormatException e) {
                // Handle the case where the input is not a valid integer
                System.out.println("Invalid input: " + event.getNewValue());
            }
        });

    }

    private void updateInvoiceItemTotals(InvoiceItemEntry entry) {
        // 1. find new entry totals
        BigDecimal priceExcludingTaxes = entry.getUnitPriceExcludingTaxes().multiply(BigDecimal.valueOf(entry.getQuantity()));
        BigDecimal taxes = priceExcludingTaxes.multiply(entry.getTaxRate());
        BigDecimal priceIncludingTaxes = priceExcludingTaxes.add(taxes);

        // 2. update entry totals
        entry.setTotalExcludingTaxes(priceExcludingTaxes);
        entry.setTotalTaxes(taxes);
        entry.setTotalIncludingTaxes(priceIncludingTaxes);
    }

    public void updateInvoiceTotals() {
        // update invoice totals
        invoice.setTotalExcludingTaxes(BigDecimal.ZERO);
        invoice.setTotalTaxes(BigDecimal.ZERO);
        invoice.setTotalIncludingTaxes(BigDecimal.ZERO);

        for (InvoiceItemEntry itemEntry : invoiceItemEntryTableView.getItems()) {
            invoice.setTotalExcludingTaxes(invoice.getTotalExcludingTaxes().add(itemEntry.getTotalExcludingTaxes()));
            invoice.setTotalTaxes(invoice.getTotalTaxes().add(itemEntry.getTotalTaxes()));
            invoice.setTotalIncludingTaxes(invoice.getTotalIncludingTaxes().add(itemEntry.getTotalIncludingTaxes()));
        }

        // update total display
        totalExcludingTaxesTextField.setText(invoice.getTotalExcludingTaxes().toString());
        totalIncludingTaxesTextField.setText(invoice.getTotalIncludingTaxes().toString());
        totalTaxesTextField.setText(invoice.getTotalTaxes().toString());
    }

    private void initClientComboBox() {
        clientComboBox.setItems(dao.getClients("SELECT * FROM Client"));
        clientComboBox.setEditable(false);
        clientComboBox.setCellFactory(x -> new ClientComboCell());
        clientComboBox.setButtonCell(new ClientComboCell());

        clientComboBox.setOnAction(event -> {
            commonCompanyIdentifierTextField.setText(clientComboBox.getSelectionModel().getSelectedItem().getCommonCompanyIdentifier());
            addressTextField.setText(clientComboBox.getSelectionModel().getSelectedItem().getAddress());
            invoice.setClient(clientComboBox.getSelectionModel().getSelectedItem());
        });
    }

    private void saveInvoice() throws SQLException {
        invoice.setIssueDate(issueDateDatePicker.getValue());

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
        saveInvoiceItems(df);
        displaySuccessAlert();
    }

    private void saveInvoiceItems(DecimalFormat df) throws SQLException {
        StringBuilder insertInvoiceItemQuery = new StringBuilder("insert into invoice_item(id_invoice, id_product, quantity, unit_price_excluding_taxes, total_excluding_taxes, total_taxes, total_including_taxes) values ");

        invoice.getInvoiceItems().clear();
        invoiceItemEntryTableView.getItems().forEach(invoiceItemEntry -> {
                    invoice.getInvoiceItems().add(
                            new InvoiceItem(
                                    0L,
                                    invoiceItemEntry.getProductComboBox().getSelectionModel().getSelectedItem(),
                                    invoice,
                                    invoiceItemEntry.getQuantity(),
                                    invoiceItemEntry.getUnitPriceExcludingTaxes(),
                                    invoiceItemEntry.getTotalExcludingTaxes(),
                                    invoiceItemEntry.getTotalIncludingTaxes(),
                                    invoiceItemEntry.getTotalTaxes()

                            )
                    );

                    insertInvoiceItemQuery.append("(%d, %d, %d, %s, %s, %s, %s),"
                            .formatted(
                                    invoice.getId(),
                                    invoiceItemEntry.getProductComboBox().getSelectionModel().getSelectedItem().getId(),
                                    invoiceItemEntry.getQuantity(),
                                    df.format(invoiceItemEntry.getUnitPriceExcludingTaxes()),
                                    df.format(invoiceItemEntry.getTotalExcludingTaxes()),
                                    df.format(invoiceItemEntry.getTotalTaxes()),
                                    df.format(invoiceItemEntry.getTotalIncludingTaxes())
                            ));
                }
        );

        insertInvoiceItemQuery.deleteCharAt(insertInvoiceItemQuery.length() - 1);
        insertInvoiceItemQuery.append(";");

        // save invoice items to DB
        dao.saveData(insertInvoiceItemQuery.toString());
    }


    public void initInvoiceUpdate(Invoice invoice) {
        this.invoice = invoice;
        this.issueDateDatePicker.setValue(invoice.getIssueDate());
        this.totalIncludingTaxesTextField.setText(invoice.getTotalIncludingTaxes().toString());
        this.totalExcludingTaxesTextField.setText(invoice.getTotalExcludingTaxes().toString());
        this.totalTaxesTextField.setText(invoice.getTotalTaxes().toString());

        this.clientComboBox.setValue(invoice.getClient());
        this.commonCompanyIdentifierTextField.setText(invoice.getClient().getCommonCompanyIdentifier());
        this.addressTextField.setText(invoice.getClient().getAddress());

        this.invoice.getInvoiceItems().forEach(invoiceItem -> {
                    invoiceItemEntryTableView.getItems().add(
                            new InvoiceItemEntry(products, this, invoiceItem)
                    );
                }
        );

        saveInvoiceButton.setOnAction(e -> {
            try {
                updateInvoice(null);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        refreshTableView();
    }

    public void updateInvoice(ActionEvent actionEvent) throws SQLException {
        // update invoice
        invoice.setIssueDate(issueDateDatePicker.getValue());

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
        saveInvoiceItems(df);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Facture modifier avec success");
        alert.showAndWait();
    }

    private void displaySuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Facture ajouter avec success");
        alert.showAndWait();
    }

    private static class ClientComboCell extends ListCell<Client> {
        @Override
        protected void updateItem(Client client, boolean bln) {
            super.updateItem(client, bln);
            setText(client != null ? client.getName() : null);
        }
    }

}
