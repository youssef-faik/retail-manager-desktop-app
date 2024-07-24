package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.client.Client;
import com.example.gestioncommercial.client.ClientRepository;
import com.example.gestioncommercial.product.Product;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
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
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InvoiceController implements Initializable {
    @FXML
    private TextField totalExcludingTaxesTextField, totalTaxesTextField, totalIncludingTaxesTextField, addressTextField, commonCompanyIdentifierTextField;
    @FXML
    private Button addProductButton, saveInvoiceButton;
    @FXML
    private DatePicker issueDateDatePicker;
    @FXML
    private ComboBox<Client> clientComboBox;
    @FXML
    private TableView<InvoiceItemEntry> invoiceItemEntryTableView;

    private Invoice invoice = new Invoice();
    private ObservableList<Product> products;

    private InvoiceRepository invoiceRepository;
    private ClientRepository clientRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        invoiceRepository = new InvoiceRepository();
        clientRepository = new ClientRepository();

        products = invoiceRepository.findAll();

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

    private void saveInvoice() throws SQLException {
        mapInvoice();
        invoiceRepository.save(invoice);
        displaySuccessAlert();
    }

    public void updateInvoice() throws SQLException {
        mapInvoice();
        invoiceRepository.update(invoice);
        displaySuccessAlert();
    }

    private void initClientComboBox() {
        clientComboBox.setItems(clientRepository.findAll());
        clientComboBox.setEditable(false);
        clientComboBox.setCellFactory(x -> new ClientComboCell());
        clientComboBox.setButtonCell(new ClientComboCell());

        clientComboBox.setOnAction(event -> {
            commonCompanyIdentifierTextField.setText(clientComboBox.getSelectionModel().getSelectedItem().getCommonCompanyIdentifier());
            addressTextField.setText(clientComboBox.getSelectionModel().getSelectedItem().getAddress());
            invoice.setClient(clientComboBox.getSelectionModel().getSelectedItem());
        });
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
        Callback<TableColumn<InvoiceItemEntry, String>, TableCell<InvoiceItemEntry, String>> cellFactory =
                (TableColumn<InvoiceItemEntry, String> param) -> {
                    // make cell containing button

                    return new TableCell<>() {
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
                };

        actionColumn.setCellFactory(cellFactory);

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

    private void displaySuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Operation effectué avec success");
        alert.showAndWait();
    }

    private void refreshTableView() {
        invoiceItemEntryTableView.refresh();
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

    private void mapInvoice() {
        invoice.setIssueDate(issueDateDatePicker.getValue());

        invoice.getInvoiceItems().clear();
        invoiceItemEntryTableView.getItems().forEach(invoiceItemEntry -> invoice.getInvoiceItems().add(
                new InvoiceItem(
                        0L,
                        invoiceItemEntry.getProductComboBox().getSelectionModel().getSelectedItem(),
                        invoice,
                        invoiceItemEntry.getQuantity(),
                        invoiceItemEntry.getUnitPriceExcludingTaxes(),
                        invoiceItemEntry.getTotalExcludingTaxes(),
                        invoiceItemEntry.getTotalIncludingTaxes(),
                        invoiceItemEntry.getTotalTaxes()
                )));
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

        this.invoice.getInvoiceItems().forEach(invoiceItem -> invoiceItemEntryTableView.getItems().add(
                        new InvoiceItemEntry(products, this, invoiceItem)
                )
        );

        saveInvoiceButton.setOnAction(e -> {
            try {
                updateInvoice();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        refreshTableView();
    }

    private static class ClientComboCell extends ListCell<Client> {
        @Override
        protected void updateItem(Client client, boolean bln) {
            super.updateItem(client, bln);
            setText(client != null ? client.getName() : null);
        }
    }

}
