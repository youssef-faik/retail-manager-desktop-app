package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.client.Client;
import com.example.gestioncommercial.client.ClientRepository;
import com.example.gestioncommercial.configuration.AppConfiguration;
import com.example.gestioncommercial.configuration.ConfigKey;
import com.example.gestioncommercial.payment.*;
import com.example.gestioncommercial.product.Product;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InvoiceController implements Initializable {
    @FXML
    private TextField totalExcludingTaxesTextField, totalTaxesTextField, totalIncludingTaxesTextField, addressTextField, commonCompanyIdentifierTextField, remainingAmountTextField, paidAmountTextField;

    @FXML
    private Label invoiceReferenceLabel;

    @FXML
    private Button addProductButton, saveInvoiceButton, addPaymentButton;

    @FXML
    private DatePicker issueDateDatePicker, dueDateDatePicker;

    @FXML
    private ComboBox<Client> clientComboBox;

    @FXML
    private ComboBox<InvoiceStatus> invoiceStatusComboBox;

    @FXML
    private TableView<InvoiceItemEntry> invoiceItemEntryTableView;

    @FXML
    private TableView<Payment> paymentsTableView;

    private Invoice invoice = new Invoice();
    private ObservableList<Product> products;

    private InvoiceRepository invoiceRepository;
    private ClientRepository clientRepository;
    private InvoiceController invoiceController;
    private boolean isEditMode = false;
    private boolean isDraftInvoice = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        invoiceController = this;
        invoiceRepository = new InvoiceRepository();
        clientRepository = new ClientRepository();

        Thread loadProductsThread = new Thread(() -> products = invoiceRepository.findAll());
        loadProductsThread.start();

        issueDateDatePicker.setValue(LocalDate.now());
        invoice.setIssueDate(LocalDate.now());

        totalExcludingTaxesTextField.setText("0");
        totalIncludingTaxesTextField.setText("0");
        totalTaxesTextField.setText("0");

        initClientComboBox();
        initInvoiceItemsTableView();
        initPaymentsTableView();

        refreshPaidAndRemainingAmounts();

        invoiceItemEntryTableView.getItems().addListener((ListChangeListener<InvoiceItemEntry>) c -> {
            c.next();

            if (!c.wasAdded()) {
                refreshPaidAndRemainingAmounts();
            }
        });

        paymentsTableView.getItems().addListener((ListChangeListener<Payment>) c -> refreshPaidAndRemainingAmounts());

        addProductButton.setOnAction(event -> {
            InvoiceItemEntry entry = new InvoiceItemEntry(products, this);
            invoiceItemEntryTableView.getItems().add(entry);
        });

        addPaymentButton.setOnAction(e -> {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/gestioncommercial/payment/form-payment.fxml"));
            Parent root;
            try {
                root = fxmlLoader.load();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            PaymentController paymentController = fxmlLoader.getController();
            paymentController.setPaymentsTableView(paymentsTableView);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        });

        dueDateDatePicker.setOnAction(event -> {
            invoice.setDueDate(dueDateDatePicker.getValue());
            refreshInvoiceStatus();
            updateInvoiceStatusDisplay();
            System.out.println("dueDateDatePicker");
        });

        issueDateDatePicker.setOnAction(event -> {
            invoice.setIssueDate(issueDateDatePicker.getValue());
            refreshInvoiceStatus();
            updateInvoiceStatusDisplay();
            System.out.println("issueDateDatePicker");
        });

        saveInvoiceButton.setOnAction(e -> {
            try {
                addInvoice();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

    }


    private void addInvoice() throws SQLException {
        mapInvoice();

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            AppConfiguration configuration = AppConfiguration.getInstance();
            long lastInvoiceNumber = Long.parseLong(configuration.getConfigurationValue(ConfigKey.NEXT_INVOICE_NUMBER).getValue());

            invoice.setReference(lastInvoiceNumber);

            invoiceRepository.save(invoice);

            lastInvoiceNumber++;
            configuration.setConfigurationValues(Map.of(ConfigKey.NEXT_INVOICE_NUMBER, String.valueOf(lastInvoiceNumber)));
        } else {
            invoiceRepository.save(invoice);
        }

        displaySuccessAlert();
    }

    public void updateInvoice() throws SQLException {
        mapInvoice();

        if (isDraftInvoice && invoice.getStatus() != InvoiceStatus.DRAFT) {
            AppConfiguration configuration = AppConfiguration.getInstance();
            long lastInvoiceNumber = Long.parseLong(configuration.getConfigurationValue(ConfigKey.NEXT_INVOICE_NUMBER).getValue());

            invoice.setReference(lastInvoiceNumber);

            invoiceRepository.update(invoice);

            lastInvoiceNumber++;
            configuration.setConfigurationValues(Map.of(ConfigKey.NEXT_INVOICE_NUMBER, String.valueOf(lastInvoiceNumber)));
        } else {
            invoiceRepository.update(invoice);
        }

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

        actionColumn.setMinWidth(100);
        actionColumn.setMaxWidth(100);
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
                                FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                                Button deleteButton = new Button("Supprimer", deleteIcon);

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
                entry.setUnitPriceExcludingTaxes(BigDecimal.valueOf(Double.parseDouble(event.getNewValue())));
                updateInvoiceItemTotals(entry);
                updateInvoiceTotals();

                refreshTableView();
            } catch (NumberFormatException e) {
                // Handle the case where the input is not a valid integer
                System.out.println("Invalid input: " + event.getNewValue());
            }
        });

    }

    private void initPaymentsTableView() {
        TableColumn<Payment, String> paymentMethodColumn = new TableColumn<>("Mode");
        TableColumn<Payment, String> paymentDateColumn = new TableColumn<>("Date règlement");
        TableColumn<Payment, String> paymentAmountColumn = new TableColumn<>("Montant");
//        TableColumn<Payment, String> paymentReferenceColumn = new TableColumn<>("Reference");
        TableColumn<Payment, String> bankNameColumn = new TableColumn<>("Banque");
        TableColumn<Payment, String> ckeckDueDateColumn = new TableColumn<>("Date d'échéance");
        TableColumn<Payment, String> ckeckStatusColumn = new TableColumn<>("Status");
        TableColumn<Payment, String> actionColumn = new TableColumn<>("Actions");

        paymentMethodColumn.setCellValueFactory(cellData -> {
            String paymentMethod = "";

            if (cellData.getValue() instanceof Cash) {
                paymentMethod = "Espèce";
            } else if (cellData.getValue() instanceof BankTransfer) {
                paymentMethod = "Virement";
            } else if (cellData.getValue() instanceof Check) {
                paymentMethod = "Chèque";
            }

            return new SimpleStringProperty(paymentMethod);
        });

        bankNameColumn.setCellValueFactory(cellData -> {
            String paymentMethod = "";

            if (cellData.getValue() instanceof Cash) {
                paymentMethod = "N/A";
            } else if (cellData.getValue() instanceof BankTransfer bankTransfer) {
                paymentMethod = bankTransfer.getBankName();
            } else if (cellData.getValue() instanceof Check check) {
                paymentMethod = check.getBankName();
            }

            return new SimpleStringProperty(paymentMethod);
        });

        ckeckDueDateColumn.setCellValueFactory(cellData -> {
            String ckeckDueDate = "";

            if (cellData.getValue() instanceof Cash) {
                ckeckDueDate = "N/A";
            } else if (cellData.getValue() instanceof BankTransfer) {
                ckeckDueDate = "N/A";
            } else if (cellData.getValue() instanceof Check check) {
                if (check.getDueDate() != null) {
                    ckeckDueDate = check.getDueDate().toString();
                } else {
                    ckeckDueDate = "N/A";
                }
            }

            return new SimpleStringProperty(ckeckDueDate);
        });

        ckeckStatusColumn.setCellValueFactory(cellData -> {
            String ckeckStatus = "";

            if (cellData.getValue() instanceof Cash) {
                ckeckStatus = "N/A";
            } else if (cellData.getValue() instanceof BankTransfer) {
                ckeckStatus = "N/A";
            } else if (cellData.getValue() instanceof Check check) {
                ckeckStatus = check.getCheckStatus().toString();
            }

            return new SimpleStringProperty(ckeckStatus);
        });

        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        paymentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
//        paymentReferenceColumn.setCellValueFactory(new PropertyValueFactory<>("reference"));

        actionColumn.setMinWidth(176);
        actionColumn.setMaxWidth(176);
        actionColumn.setResizable(false);
        actionColumn.setReorderable(false);

        paymentMethodColumn.setMinWidth(80);
        paymentMethodColumn.setMaxWidth(80);
        paymentMethodColumn.setResizable(false);
        paymentMethodColumn.setReorderable(false);

        paymentsTableView.getColumns().addAll(
                paymentMethodColumn,
                paymentDateColumn,
                paymentAmountColumn,
//                paymentReferenceColumn,
                bankNameColumn,
                ckeckDueDateColumn,
                ckeckStatusColumn,
                actionColumn
        );

        paymentsTableView.setEditable(true);
        paymentsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Callback<TableColumn<Payment, String>, TableCell<Payment, String>> cellFactory =
                (TableColumn<Payment, String> param) -> new TableCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        //that cell created only on non-empty rows
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                            FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.EDIT);

                            Button deleteButton = new Button("Supprimer", deleteIcon);
                            Button editButton = new Button("Modifier", editIcon);

                            deleteButton.setOnMouseClicked((MouseEvent event) -> {
                                try {
                                    TableRow tableRow = (TableRow) deleteButton.getParent().getParent().getParent();
                                    Payment rowItem = (Payment) tableRow.getItem();
                                    paymentsTableView.getItems().remove(rowItem);
                                } catch (Exception ex) {
                                    Logger.getLogger(InvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            });

                            editButton.setOnMouseClicked((MouseEvent event) -> {
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/gestioncommercial/payment/form-payment.fxml"));
                                Parent root;
                                try {
                                    root = fxmlLoader.load();
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }

                                PaymentController paymentController = fxmlLoader.getController();
                                paymentController.setPaymentsTableView(paymentsTableView);
                                paymentController.setInvoiceController(invoiceController);
                                TableRow tableRow = (TableRow) editButton.getParent().getParent().getParent();
                                Payment rowItem = (Payment) tableRow.getItem();
                                paymentController.initUpdate(rowItem);

                                Stage stage = new Stage();
                                stage.initModality(Modality.APPLICATION_MODAL);
                                stage.setScene(new Scene(root));
                                stage.setResizable(false);
                                stage.showAndWait();
                                refreshPaidAndRemainingAmounts();
                            });

                            HBox actionHBox = new HBox(editButton, deleteButton);
                            actionHBox.setStyle("-fx-alignment:center");
                            actionHBox.setSpacing(4);

                            setGraphic(actionHBox);
                            setText(null);
                        }
                    }
                };

        actionColumn.setCellFactory(cellFactory);
    }

    public void initInvoiceUpdate(Invoice invoice) {
        this.invoice = invoice;

        if (invoice.getReference() != 0) {
            this.invoiceReferenceLabel.setText(invoiceReferenceLabel.getText() + " N° : " + invoice.getReference());
        }

        this.issueDateDatePicker.setValue(invoice.getIssueDate());
        this.dueDateDatePicker.setValue(invoice.getDueDate());
        this.totalIncludingTaxesTextField.setText(invoice.getTotalIncludingTaxes().toString());
        this.totalExcludingTaxesTextField.setText(invoice.getTotalExcludingTaxes().toString());
        this.totalTaxesTextField.setText(invoice.getTotalTaxes().toString());

        this.clientComboBox.setValue(invoice.getClient());
        this.commonCompanyIdentifierTextField.setText(invoice.getClient().getCommonCompanyIdentifier());
        this.addressTextField.setText(invoice.getClient().getAddress());

        invoiceStatusComboBox.getItems().clear();
        invoiceStatusComboBox.getItems().add(invoice.getStatus());
        this.invoiceStatusComboBox.setValue(invoice.getStatus());

        isEditMode = true;
        isDraftInvoice = invoice.getStatus() == InvoiceStatus.DRAFT;

        this.invoice.getInvoiceItems().forEach(invoiceItem -> invoiceItemEntryTableView.getItems().add(
                        new InvoiceItemEntry(products, this, invoiceItem)
                )
        );

        this.invoice.getPayments().forEach(payment -> paymentsTableView.getItems().add(payment));

        saveInvoiceButton.setOnAction(e -> {
            try {
                updateInvoice();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        refreshTableView();
        refreshPaidAndRemainingAmounts();
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

        refreshPaidAndRemainingAmounts();
    }

    private void mapInvoice() {
        invoice.setIssueDate(issueDateDatePicker.getValue());
        invoice.setDueDate(dueDateDatePicker.getValue());
        invoice.setPayments(paymentsTableView.getItems());
        invoice.setStatus(invoiceStatusComboBox.getValue());

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

    public void refreshPaidAndRemainingAmounts() {
        if (paymentsTableView.getItems().isEmpty()) {
            invoice.setPaidAmount(BigDecimal.ZERO);
        } else {
            BigDecimal paidAmount = BigDecimal.ZERO;

            for (Payment payment : paymentsTableView.getItems()) {
                if (payment instanceof Check check && check.getCheckStatus() != CheckStatus.CLEARED) {
                    continue;
                }

                paidAmount = paidAmount.add(payment.getAmount());
            }

            invoice.setPaidAmount(paidAmount);
        }

        paidAmountTextField.setText(invoice.getPaidAmount().toString());
        remainingAmountTextField.setText(invoice.getTotalIncludingTaxes().subtract(invoice.getPaidAmount()).toString());

        refreshInvoiceStatus();
        updateInvoiceStatusDisplay();
    }

    private void updateInvoiceStatusDisplay() {
        InvoiceStatus selectedItem = invoiceStatusComboBox.getSelectionModel().getSelectedItem();

        invoiceStatusComboBox.getItems().clear();
        invoiceStatusComboBox.getItems().add(invoice.getStatus());

        if (selectedItem != null) {
            if (!isEditMode) {
                if (!invoiceStatusComboBox.getItems().contains(InvoiceStatus.DRAFT)) {
                    invoiceStatusComboBox.getItems().add(InvoiceStatus.DRAFT);
                }
            } else {
                if (isDraftInvoice) {
                    invoiceStatusComboBox.getItems().add(InvoiceStatus.DRAFT);
                } else {
                    invoiceStatusComboBox.getItems().add(InvoiceStatus.CANCELLED);
                }
            }
        }

        if (selectedItem == InvoiceStatus.CANCELLED) {
            invoiceStatusComboBox.setValue(InvoiceStatus.CANCELLED);
        } else if (selectedItem == InvoiceStatus.DRAFT) {
            invoiceStatusComboBox.setValue(InvoiceStatus.DRAFT);
        } else {
            invoiceStatusComboBox.setValue(invoice.getStatus());
        }
    }

    private void refreshInvoiceStatus() {
        if (invoice.getTotalIncludingTaxes().compareTo(invoice.getPaidAmount()) == 0
                && invoice.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) == 0) {
            if (invoice.getDueDate() != null) {
                invoice.setStatus(InvoiceStatus.DRAFT);
            }
        } else if (invoice.getTotalIncludingTaxes().compareTo(invoice.getPaidAmount()) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(LocalDate.now())) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
        } else if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PENDING);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
    }


    private static class ClientComboCell extends ListCell<Client> {
        @Override
        protected void updateItem(Client client, boolean bln) {
            super.updateItem(client, bln);
            setText(client != null ? client.getName() : null);
        }
    }

}
