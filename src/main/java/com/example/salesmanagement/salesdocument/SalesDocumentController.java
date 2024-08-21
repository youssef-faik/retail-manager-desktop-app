package com.example.salesmanagement.salesdocument;

import com.example.salesmanagement.client.Client;
import com.example.salesmanagement.client.ClientRepository;
import com.example.salesmanagement.payment.*;
import com.example.salesmanagement.product.Product;
import com.example.salesmanagement.product.ProductRepository;
import com.example.salesmanagement.report.SalesDocumentReportManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SalesDocumentController implements Initializable {
    @FXML
    public VBox paymentsHBox;
    @FXML
    public HBox statusHBox, remainingAmountHBox, paidAmountHBox, headerHBox;
    @FXML
    private Separator totalSeparator;
    @FXML
    private TextField totalExcludingTaxesTextField, totalTaxesTextField, totalIncludingTaxesTextField, addressTextField, commonCompanyIdentifierTextField, remainingAmountTextField, paidAmountTextField;

    @FXML
    private Label salesDocumentReferenceLabel, dueDateLabel;

    @FXML
    private Button addProductButton, saveSalesDocumentButton, addPaymentButton, printSalesDocumentButton;

    @FXML
    private DatePicker issueDateDatePicker, dueDateDatePicker;

    @FXML
    private ComboBox<Client> clientComboBox;

    @FXML
    private ComboBox salesDocumentStatusComboBox;

    @FXML
    private TableView<SalesDocumentItemFormEntry> salesDocumentItemEntryTableView;

    @FXML
    private TableView<PaymentFormEntry> paymentsTableView;

    private SalesDocument salesDocument;
    private ObservableList<Product> products;
    private SalesDocumentController salesDocumentController;
    private boolean isEditMode = false, isDraftDocument = false, originalSalesHasReference;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        salesDocumentController = this;

        products = ProductRepository.findAll();

        issueDateDatePicker.setValue(LocalDate.now());
        totalExcludingTaxesTextField.setText("0");
        totalIncludingTaxesTextField.setText("0");
        totalTaxesTextField.setText("0");

        initClientComboBox();
        initSalesDocumentItemsTableView();
        updatePaidAndRemainingAmounts();

        saveSalesDocumentButton.setDisable(true);

        salesDocumentItemEntryTableView.getItems().addListener((ListChangeListener<SalesDocumentItemFormEntry>) c -> {
            c.next();

            if (!c.wasAdded()) {
                updatePaidAndRemainingAmounts();
            }

            saveSalesDocumentButton.setDisable(
                    !isItemTableViewValide()
                            || salesDocument.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) == 0
                            || clientComboBox.getSelectionModel().getSelectedItem() == null
            );
        });

        paymentsTableView.getItems().addListener((ListChangeListener<PaymentFormEntry>) c -> updatePaidAndRemainingAmounts());

        addProductButton.setOnAction(event -> {
            SalesDocumentItemFormEntry entry = new SalesDocumentItemFormEntry(products, this);
            salesDocumentItemEntryTableView.getItems().add(entry);
        });

        saveSalesDocumentButton.setOnAction(e -> addSalesDocument());
    }

    private boolean isItemTableViewValide() {
        if (salesDocumentItemEntryTableView.getItems().isEmpty()) {
            return false;
        }

        for (SalesDocumentItemFormEntry entry : salesDocumentItemEntryTableView.getItems()) {
            if (entry.getProductComboBox().getSelectionModel().getSelectedItem() == null) {
                return false;
            }
        }
        return true;
    }

    private void addSalesDocument() {
        mapSalesDocument();

        Optional<SalesDocument> optionalSalesDocument = SalesDocumentRepository.add(salesDocument);

        if (optionalSalesDocument.isPresent()) {
            SalesDocument document = optionalSalesDocument.get();

            if (document.getReference() != null) {
                this.salesDocumentReferenceLabel.setText(salesDocumentReferenceLabel.getText() + " N° : " + document.getReference());
            }

            initSalesDocumentUpdate(document);

            saveSalesDocumentButton.setText("Modifier");
            Text icon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.EDIT);
            saveSalesDocumentButton.setGraphic(icon);

            saveSalesDocumentButton.setOnAction(
                    e -> {
                        try {
                            updateSalesDocument();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
            );

            printSalesDocumentButton.setVisible(true);
            printSalesDocumentButton.setOnAction(
                    event -> {
                        try {
                            SalesDocumentReportManager salesDocumentReportManager = new SalesDocumentReportManager();
                            salesDocumentReportManager.displaySalesDocumentReport(document);
                        } catch (Exception ex) {
                            Logger.getLogger(SalesDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
            );

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateSalesDocument() throws SQLException {
        mapSalesDocument();

        Optional<SalesDocument> optionalSalesDocument = SalesDocumentRepository.update(salesDocument);

        if (optionalSalesDocument.isPresent()) {
            SalesDocument document = optionalSalesDocument.get();
            if (document.getReference() != null && originalSalesHasReference) {
                this.salesDocumentReferenceLabel.setText(salesDocumentReferenceLabel.getText() + " N° : " + document.getReference());
            }

            isDraftDocument = isDraftStatus(document);
            updatePaidAndRemainingAmounts();
            originalSalesHasReference = document.getReference() == null;

            printSalesDocumentButton.setOnAction(
                    event -> {
                        try {
                            SalesDocumentReportManager salesDocumentReportManager = new SalesDocumentReportManager();
                            salesDocumentReportManager.displaySalesDocumentReport(document);
                        } catch (Exception ex) {
                            Logger.getLogger(SalesDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
            );

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private void mapSalesDocument() {
        salesDocument.setIssueDate(issueDateDatePicker.getValue());
        salesDocument.clearItems();
        salesDocumentItemEntryTableView.getItems().forEach(salesDocumentItemFormEntry -> salesDocument.addItem(salesDocumentItemFormEntry.getInvoiceItem()));

        if (salesDocument instanceof Invoice invoice) {
            invoice.setDueDate(dueDateDatePicker.getValue());
            invoice.setStatus((InvoiceStatus) salesDocumentStatusComboBox.getValue());

            Set<Payment> payments = paymentsTableView.getItems()
                    .stream()
                    .map(PaymentFormEntry::getPayment)
                    .collect(Collectors.toSet());

            invoice.setPayments(new HashSet<>(payments));
        }
        if (salesDocument instanceof Quotation quotation) {
            quotation.setValidUntil(dueDateDatePicker.getValue());
            quotation.setStatus((QuotationStatus) salesDocumentStatusComboBox.getValue());
        }
        if (salesDocument instanceof DeliveryNote deliveryNote) {
            deliveryNote.setStatus((DeliveryNoteStatus) salesDocumentStatusComboBox.getValue());
        }
        if (salesDocument instanceof CreditInvoice creditInvoice) {
            creditInvoice.setStatus((CreditInvoiceStatus) salesDocumentStatusComboBox.getValue());

            Set<Payment> payments = paymentsTableView.getItems()
                    .stream()
                    .map(PaymentFormEntry::getPayment)
                    .collect(Collectors.toSet());

            creditInvoice.setPayments(new HashSet<>(payments));
        }
    }

    private void initClientComboBox() {
        clientComboBox.setEditable(false);
        clientComboBox.setCellFactory(x -> new ClientComboCell());
        clientComboBox.setButtonCell(new ClientComboCell());

        clientComboBox.setOnAction(event -> {
            commonCompanyIdentifierTextField.setText(clientComboBox.getSelectionModel().getSelectedItem().getCommonCompanyIdentifier());
            addressTextField.setText(clientComboBox.getSelectionModel().getSelectedItem().getAddress());
            salesDocument.setClient(clientComboBox.getSelectionModel().getSelectedItem());

            saveSalesDocumentButton.setDisable(!isItemTableViewValide() ||
                    salesDocument.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) == 0
                    || clientComboBox.getSelectionModel().getSelectedItem() == null);
        });

        ObservableList<Client> clients = ClientRepository.findAll();
        clientComboBox.setItems(clients);
    }

    private void initSalesDocumentItemsTableView() {
        TableColumn<SalesDocumentItemFormEntry, String> productColumn = new TableColumn<>("Produit");
        TableColumn<SalesDocumentItemFormEntry, String> quantityColumn = new TableColumn<>("Quantité");
        TableColumn<SalesDocumentItemFormEntry, String> unitPriceColumn = new TableColumn<>("Prix unitaire (HT)");
        TableColumn<SalesDocumentItemFormEntry, String> taxRateColumn = new TableColumn<>("Taux TVA");
        TableColumn<SalesDocumentItemFormEntry, String> totalIncludingTaxesColumn = new TableColumn<>("Total (TTC)");
        TableColumn<SalesDocumentItemFormEntry, String> totalExcludingTaxesColumn = new TableColumn<>("Total (HT)");
        TableColumn<SalesDocumentItemFormEntry, String> totalTaxesColumn = new TableColumn<>("Total TVA");
        TableColumn<SalesDocumentItemFormEntry, String> actionColumn = new TableColumn<>("Actions");

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

        salesDocumentItemEntryTableView.getColumns().addAll(
                productColumn,
                quantityColumn,
                unitPriceColumn,
                taxRateColumn,
                totalExcludingTaxesColumn,
                totalTaxesColumn,
                totalIncludingTaxesColumn,
                actionColumn
        );

        salesDocumentItemEntryTableView.setEditable(true);

        // delete
        Callback<TableColumn<SalesDocumentItemFormEntry, String>, TableCell<SalesDocumentItemFormEntry, String>> cellFactory =
                (TableColumn<SalesDocumentItemFormEntry, String> param) -> {
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
                                        SalesDocumentItemFormEntry rowItem = (SalesDocumentItemFormEntry) tableRow.getItem();
                                        salesDocumentItemEntryTableView.getItems().remove(rowItem);
                                        updateTotals();
                                    } catch (Exception ex) {
                                        Logger.getLogger(SalesDocumentController.class.getName()).log(Level.SEVERE, null, ex);
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
            SalesDocumentItemFormEntry entry = event.getTableView().getItems().get(event.getTablePosition().getRow());
            try {
                entry.setQuantity(Integer.parseInt(event.getNewValue()));
                updateItemTotals(entry);
                updateTotals();

                refreshTableView();
            } catch (NumberFormatException e) {
                // Handle the case where the input is not a valid integer
                salesDocumentItemEntryTableView.refresh();
                System.out.println("Invalid input: " + event.getNewValue());
                displayErrorAlert("valeur de quantité incorrecte: " + event.getNewValue());
            }
        });

        unitPriceColumn.setOnEditCommit(event -> {
            SalesDocumentItemFormEntry entry = event.getTableView().getItems().get(event.getTablePosition().getRow());
            try {
                entry.setUnitPriceExcludingTaxes(BigDecimal.valueOf(Double.parseDouble(event.getNewValue())));
                updateItemTotals(entry);
                updateTotals();

                refreshTableView();
            } catch (NumberFormatException e) {
                // Handle the case where the input is not a valid integer
//                entry.setUnitPriceExcludingTaxes(BigDecimal.valueOf(Double.parseDouble(event.getOldValue())));

                salesDocumentItemEntryTableView.refresh();
                System.out.println("Invalid input: " + event.getNewValue());
                displayErrorAlert("valeur de prix unitaire incorrecte: " + event.getNewValue());
            }
        });

    }

    private void initPaymentsTableView() {
        TableColumn<PaymentFormEntry, String> paymentMethodColumn = new TableColumn<>("Mode");
        TableColumn<PaymentFormEntry, String> paymentDateColumn = new TableColumn<>("Date règlement");
        TableColumn<PaymentFormEntry, String> paymentAmountColumn = new TableColumn<>("Montant");
        TableColumn<PaymentFormEntry, String> bankNameColumn = new TableColumn<>("Banque");
        TableColumn<PaymentFormEntry, String> checkDueDateColumn = new TableColumn<>("Date d'échéance");
        TableColumn<PaymentFormEntry, String> checkStatusColumn = new TableColumn<>("Status");
        TableColumn<PaymentFormEntry, String> actionColumn = new TableColumn<>("Actions");

        paymentDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPayment().getPaymentDate().toString()));
        paymentAmountColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPayment().getAmount().toString()));

        paymentMethodColumn.setCellValueFactory(cellData -> {
            String paymentMethod = "";

            if (cellData.getValue().getPayment() instanceof Cash) {
                paymentMethod = "Espèce";
            } else if (cellData.getValue().getPayment() instanceof BankTransfer) {
                paymentMethod = "Virement";
            } else if (cellData.getValue().getPayment() instanceof Check) {
                paymentMethod = "Chèque";
            }

            return new SimpleStringProperty(paymentMethod);
        });

        bankNameColumn.setCellValueFactory(cellData -> {
            String paymentMethod = "";

            if (cellData.getValue().getPayment() instanceof Cash) {
                paymentMethod = "N/A";
            } else if (cellData.getValue().getPayment() instanceof BankTransfer bankTransfer) {
                paymentMethod = bankTransfer.getBankName();
            } else if (cellData.getValue().getPayment() instanceof Check check) {
                paymentMethod = check.getBankName();
            }

            return new SimpleStringProperty(paymentMethod);
        });

        checkDueDateColumn.setCellValueFactory(cellData -> {
            String checkDueDate = "";

            if (cellData.getValue().getPayment() instanceof Cash) {
                checkDueDate = "N/A";
            } else if (cellData.getValue().getPayment() instanceof BankTransfer) {
                checkDueDate = "N/A";
            } else if (cellData.getValue().getPayment() instanceof Check check) {
                if (check.getDueDate() != null) {
                    checkDueDate = check.getDueDate().toString();
                } else {
                    checkDueDate = "N/A";
                }
            }

            return new SimpleStringProperty(checkDueDate);
        });

        checkStatusColumn.setCellValueFactory(cellData -> {
            String checkStatus = "";

            if (cellData.getValue().getPayment() instanceof Cash) {
                checkStatus = "N/A";
            } else if (cellData.getValue().getPayment() instanceof BankTransfer) {
                checkStatus = "N/A";
            } else if (cellData.getValue().getPayment() instanceof Check check) {
                checkStatus = check.getCheckStatus().toString();
            }

            return new SimpleStringProperty(checkStatus);
        });

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
                bankNameColumn,
                checkDueDateColumn,
                checkStatusColumn,
                actionColumn
        );

        paymentsTableView.setEditable(true);
        paymentsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Callback<TableColumn<PaymentFormEntry, String>, TableCell<PaymentFormEntry, String>> cellFactory =
                (TableColumn<PaymentFormEntry, String> param) -> new TableCell<>() {
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
                                    PaymentFormEntry rowItem = (PaymentFormEntry) tableRow.getItem();
                                    paymentsTableView.getItems().remove(rowItem);
                                } catch (Exception ex) {
                                    Logger.getLogger(SalesDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            });

                            editButton.setOnMouseClicked((MouseEvent event) -> {
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/salesmanagement/payment/form-payment.fxml"));
                                Parent root;
                                try {
                                    root = fxmlLoader.load();
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }

                                PaymentController paymentController = fxmlLoader.getController();
                                paymentController.setPaymentsTableView(paymentsTableView);
                                paymentController.setSalesDocumentController(salesDocumentController);
                                TableRow tableRow = (TableRow) editButton.getParent().getParent().getParent();
                                PaymentFormEntry rowItem = (PaymentFormEntry) tableRow.getItem();
                                paymentController.initUpdate(rowItem.getPayment());

                                Stage stage = new Stage();
                                stage.initModality(Modality.APPLICATION_MODAL);
                                stage.setScene(new Scene(root));
                                stage.setResizable(false);
                                stage.showAndWait();
                                updatePaidAndRemainingAmounts();
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

    public void initSalesDocumentUpdate(SalesDocument salesDocumentOriginal) {
        // get a clone of the orignal sales document
        if (salesDocumentOriginal instanceof Invoice invoice) {
            try {
                salesDocument = (SalesDocument) invoice.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        if (salesDocumentOriginal instanceof Quotation quotation) {
            try {
                salesDocument = (SalesDocument) quotation.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        if (salesDocumentOriginal instanceof DeliveryNote deliveryNote) {
            try {
                salesDocument = (SalesDocument) deliveryNote.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        if (salesDocumentOriginal instanceof CreditInvoice creditInvoice) {
            try {
                salesDocument = (SalesDocument) creditInvoice.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        // set commun attributs
        this.issueDateDatePicker.setValue(salesDocumentOriginal.getIssueDate());
        this.totalIncludingTaxesTextField.setText(salesDocumentOriginal.getTotalIncludingTaxes().toString());
        this.totalExcludingTaxesTextField.setText(salesDocumentOriginal.getTotalExcludingTaxes().toString());
        this.totalTaxesTextField.setText(salesDocumentOriginal.getTotalTaxes().toString());

        this.clientComboBox.setValue(salesDocumentOriginal.getClient());
        this.commonCompanyIdentifierTextField.setText(salesDocumentOriginal.getClient().getCommonCompanyIdentifier());
        this.addressTextField.setText(salesDocumentOriginal.getClient().getAddress());

        isEditMode = true;
        originalSalesHasReference = salesDocument.getReference() == null;
        printSalesDocumentButton.setVisible(true);

        printSalesDocumentButton.setOnAction(
                event -> {
                    try {
                        SalesDocumentReportManager salesDocumentReportManager = new SalesDocumentReportManager();
                        salesDocumentReportManager.displaySalesDocumentReport(salesDocument);
                    } catch (Exception ex) {
                        Logger.getLogger(SalesDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
        );

        salesDocumentItemEntryTableView.getItems().clear();
        salesDocumentOriginal.getItems().forEach(invoiceItem -> salesDocumentItemEntryTableView.getItems().add(
                new SalesDocumentItemFormEntry(products, this, invoiceItem)
                )
        );

        refreshTableView();

        saveSalesDocumentButton.setOnAction(e -> {
            try {
                updateSalesDocument();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        if (salesDocumentOriginal instanceof Invoice invoice) {
            this.salesDocumentReferenceLabel.setText(invoice.getReference() == null ? "Facture" : "Facture N° : " + invoice.getReference());
            this.dueDateDatePicker.setValue(invoice.getDueDate());

            salesDocumentStatusComboBox.getItems().clear();
            salesDocumentStatusComboBox.getItems().add(invoice.getStatus());
            salesDocumentStatusComboBox.setValue(invoice.getStatus());

            paymentsTableView.getItems().clear();
            invoice.getPayments().forEach(payment -> paymentsTableView.getItems().add(new PaymentFormEntry(payment)));
        } else if (salesDocumentOriginal instanceof CreditInvoice creditInvoice) {
            this.salesDocumentReferenceLabel.setText(creditInvoice.getReference() == null ? "Facture avoir" : "Facture avoir N° : " + creditInvoice.getReference());

            salesDocumentStatusComboBox.getItems().clear();
            salesDocumentStatusComboBox.getItems().add(creditInvoice.getStatus());
            salesDocumentStatusComboBox.setValue(creditInvoice.getStatus());

            paymentsTableView.getItems().clear();
            creditInvoice.getPayments().forEach(payment -> paymentsTableView.getItems().add(new PaymentFormEntry(payment)));
        } else if (salesDocumentOriginal instanceof Quotation quotation) {
            this.salesDocumentReferenceLabel.setText(quotation.getReference() == null ? "Devis" : "Devis N° : " + quotation.getReference());
            this.dueDateDatePicker.setValue(quotation.getValidUntil());

            salesDocumentStatusComboBox.getItems().clear();
            salesDocumentStatusComboBox.getItems().addAll(QuotationStatus.values());
            salesDocumentStatusComboBox.setValue(quotation.getStatus());
        } else if (salesDocumentOriginal instanceof DeliveryNote deliveryNote) {
            this.salesDocumentReferenceLabel.setText(deliveryNote.getReference() == null ? "Bon de livraison" : "Bon de livraison N° : " + deliveryNote.getReference());

            salesDocumentStatusComboBox.getItems().clear();
            salesDocumentStatusComboBox.getItems().addAll(QuotationStatus.values());
            salesDocumentStatusComboBox.setValue(deliveryNote.getStatus());
        }

        isDraftDocument = isDraftStatus(salesDocumentOriginal);
        updatePaidAndRemainingAmounts();

        if (isDraftDocument) {
            if (salesDocumentOriginal instanceof Quotation) {
                salesDocumentStatusComboBox.setValue(QuotationStatus.DRAFT);
            }
            if (salesDocumentOriginal instanceof DeliveryNote) {
                salesDocumentStatusComboBox.setValue(QuotationStatus.DRAFT);
            }
            if (salesDocumentOriginal instanceof Invoice) {
                salesDocumentStatusComboBox.setValue(InvoiceStatus.DRAFT);
            }
            if (salesDocumentOriginal instanceof CreditInvoice) {
                salesDocumentStatusComboBox.setValue(CreditInvoiceStatus.DRAFT);
            }


        }
    }

    private void refreshTableView() {
        salesDocumentItemEntryTableView.refresh();
    }

    private void updateItemTotals(SalesDocumentItemFormEntry entry) {
        // 1. find new entry totals
        BigDecimal priceExcludingTaxes = entry.getUnitPriceExcludingTaxes().multiply(BigDecimal.valueOf(entry.getQuantity()));
        BigDecimal taxes = priceExcludingTaxes.multiply(entry.getTaxRate());
        BigDecimal priceIncludingTaxes = priceExcludingTaxes.add(taxes);

        // 2. update entry totals
        entry.setTotalExcludingTaxes(priceExcludingTaxes);
        entry.setTotalTaxes(taxes);
        entry.setTotalIncludingTaxes(priceIncludingTaxes);
    }

    public void updateTotals() {
        // update salesdocument totals
        salesDocument.setTotalExcludingTaxes(BigDecimal.ZERO);
        salesDocument.setTotalTaxes(BigDecimal.ZERO);
        salesDocument.setTotalIncludingTaxes(BigDecimal.ZERO);

        for (SalesDocumentItemFormEntry itemEntry : salesDocumentItemEntryTableView.getItems()) {
            salesDocument.setTotalExcludingTaxes(salesDocument.getTotalExcludingTaxes().add(itemEntry.getTotalExcludingTaxes()));
            salesDocument.setTotalTaxes(salesDocument.getTotalTaxes().add(itemEntry.getTotalTaxes()));
            salesDocument.setTotalIncludingTaxes(salesDocument.getTotalIncludingTaxes().add(itemEntry.getTotalIncludingTaxes()));
        }

        // update total display
        totalExcludingTaxesTextField.setText(salesDocument.getTotalExcludingTaxes().toString());
        totalIncludingTaxesTextField.setText(salesDocument.getTotalIncludingTaxes().toString());
        totalTaxesTextField.setText(salesDocument.getTotalTaxes().toString());

        updatePaidAndRemainingAmounts();
    }

    public void updatePaidAndRemainingAmounts() {
        if (salesDocument instanceof Invoice invoice) {
            if (paymentsTableView.getItems().isEmpty()) {
                invoice.setPaidAmount(BigDecimal.ZERO);
            } else {
                BigDecimal paidAmount = BigDecimal.ZERO;

                for (PaymentFormEntry paymentFormEntry : paymentsTableView.getItems()) {
                    if (paymentFormEntry.getPayment() instanceof Check check
                            && check.getCheckStatus() != CheckStatus.CLEARED) {
                        continue;
                    }

                    paidAmount = paidAmount.add(paymentFormEntry.getPayment().getAmount());
                }

                invoice.setPaidAmount(paidAmount);
            }

            paidAmountTextField.setText(invoice.getPaidAmount().toString());
            remainingAmountTextField.setText(invoice.getTotalIncludingTaxes().subtract(invoice.getPaidAmount()).toString());

            paymentsTableView.refresh();
        }
        if (salesDocument instanceof CreditInvoice creditInvoice) {
            if (paymentsTableView.getItems().isEmpty()) {
                creditInvoice.setPaidAmount(BigDecimal.ZERO);
            } else {
                BigDecimal paidAmount = BigDecimal.ZERO;

                for (PaymentFormEntry paymentFormEntry : paymentsTableView.getItems()) {
                    if (paymentFormEntry.getPayment() instanceof Check check
                            && check.getCheckStatus() != CheckStatus.CLEARED) {
                        continue;
                    }

                    paidAmount = paidAmount.add(paymentFormEntry.getPayment().getAmount());
                }

                creditInvoice.setPaidAmount(paidAmount);
            }

            paidAmountTextField.setText(creditInvoice.getPaidAmount().toString());
            remainingAmountTextField.setText(creditInvoice.getTotalIncludingTaxes().subtract(creditInvoice.getPaidAmount()).toString());

            paymentsTableView.refresh();
        }

        updateDocumentStatus();
    }

    private void updateDocumentStatus() {
        saveSalesDocumentButton.setDisable(!isItemTableViewValide() ||
                salesDocument.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) == 0
                || clientComboBox.getSelectionModel().getSelectedItem() == null);

        if (salesDocument instanceof Quotation quotation) {
            if (quotation.getStatus() == QuotationStatus.SENT
                    && quotation.getValidUntil() != null && quotation.getValidUntil().isBefore(LocalDate.now())) {
                quotation.setStatus(QuotationStatus.EXPIRED);
            }

            // update Status Display
            QuotationStatus selectedStatus = (QuotationStatus) salesDocumentStatusComboBox.getSelectionModel().getSelectedItem();

            salesDocumentStatusComboBox.getItems().clear();
            salesDocumentStatusComboBox.getItems().add(quotation.getStatus());

            if (selectedStatus != null) {
                if (!isEditMode) {
                    if (!salesDocumentStatusComboBox.getItems().contains(QuotationStatus.DRAFT)) {
                        salesDocumentStatusComboBox.getItems().add(QuotationStatus.DRAFT);
                    }
                    if (!salesDocumentStatusComboBox.getItems().contains(QuotationStatus.SENT) && quotation.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) != 0) {
                        salesDocumentStatusComboBox.getItems().add(QuotationStatus.SENT);
                    }
                } else {
                    if (isDraftDocument) {
                        salesDocumentStatusComboBox.getItems().add(QuotationStatus.SENT);
                    } else {
                        if (salesDocumentStatusComboBox.getItems().contains(QuotationStatus.SENT)) {
                            salesDocumentStatusComboBox.getItems().addAll(QuotationStatus.ACCEPTED, QuotationStatus.REJECTED);
                        } else {
                            if (!salesDocumentStatusComboBox.getItems().contains(QuotationStatus.REJECTED))
                                salesDocumentStatusComboBox.getItems().add(QuotationStatus.REJECTED);
                            else
                                salesDocumentStatusComboBox.getItems().add(QuotationStatus.ACCEPTED);
                        }
                    }
                }
            }

            if (selectedStatus == QuotationStatus.SENT) {
                salesDocumentStatusComboBox.setValue(QuotationStatus.SENT);
            } else if (selectedStatus == QuotationStatus.DRAFT) {
                salesDocumentStatusComboBox.setValue(QuotationStatus.DRAFT);
            } else {
                salesDocumentStatusComboBox.setValue(quotation.getStatus());
            }
        }

        if (salesDocument instanceof DeliveryNote deliveryNote) {
            // update Status Display
            DeliveryNoteStatus selectedStatus = (DeliveryNoteStatus) salesDocumentStatusComboBox.getSelectionModel().getSelectedItem();

            salesDocumentStatusComboBox.getItems().clear();
            salesDocumentStatusComboBox.getItems().add(deliveryNote.getStatus());

            if (selectedStatus != null) {
                if (!isEditMode) {
                    // insert mode

                    if (!salesDocumentStatusComboBox.getItems().contains(DeliveryNoteStatus.DRAFT)) {
                        salesDocumentStatusComboBox.getItems().add(DeliveryNoteStatus.DRAFT);
                    }

                    if (isItemTableViewValide()
                            && salesDocument.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) != 0
                            && clientComboBox.getSelectionModel().getSelectedItem() != null) {
                        if (!salesDocumentStatusComboBox.getItems().contains(DeliveryNoteStatus.DISPATCHED)) {
                            salesDocumentStatusComboBox.getItems().add(DeliveryNoteStatus.DISPATCHED);
                        }
                        if (!salesDocumentStatusComboBox.getItems().contains(DeliveryNoteStatus.DELIVERED)) {
                            salesDocumentStatusComboBox.getItems().add(DeliveryNoteStatus.DELIVERED);
                        }
                    }
                } else {
                    // update mode
                    salesDocumentStatusComboBox.getItems().remove(deliveryNote.getStatus());

                    if (isDraftDocument) {
                        salesDocumentStatusComboBox.getItems().addAll(DeliveryNoteStatus.DISPATCHED, DeliveryNoteStatus.DELIVERED);
                    } else {
                        salesDocumentStatusComboBox.getItems().addAll(DeliveryNoteStatus.DISPATCHED, DeliveryNoteStatus.DELIVERED, DeliveryNoteStatus.CANCELLED);
                    }
                }
            }

            salesDocumentStatusComboBox.setValue(deliveryNote.getStatus());
        }

        if (salesDocument instanceof Invoice invoice) {
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

            // update Status Display
            InvoiceStatus selectedStatus = (InvoiceStatus) salesDocumentStatusComboBox.getSelectionModel().getSelectedItem();

            salesDocumentStatusComboBox.getItems().clear();
            salesDocumentStatusComboBox.getItems().add(invoice.getStatus());

            if (selectedStatus != null) {
                if (!isEditMode) {
                    if (!salesDocumentStatusComboBox.getItems().contains(InvoiceStatus.DRAFT)) {
                        salesDocumentStatusComboBox.getItems().add(InvoiceStatus.DRAFT);
                    }
                } else {
                    if (isDraftDocument) {
                        salesDocumentStatusComboBox.getItems().add(InvoiceStatus.DRAFT);
                    } else {
                        salesDocumentStatusComboBox.getItems().add(InvoiceStatus.CANCELLED);
                    }
                }
            }

            if (selectedStatus == InvoiceStatus.CANCELLED) {
                salesDocumentStatusComboBox.setValue(InvoiceStatus.CANCELLED);
            } else if (selectedStatus == InvoiceStatus.DRAFT) {
                salesDocumentStatusComboBox.setValue(InvoiceStatus.DRAFT);
            } else {
                salesDocumentStatusComboBox.setValue(invoice.getStatus());
            }

        }

        if (salesDocument instanceof CreditInvoice creditInvoice) {
            if (creditInvoice.getTotalIncludingTaxes().compareTo(creditInvoice.getPaidAmount()) == 0
                    && creditInvoice.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) == 0) {
                creditInvoice.setStatus(CreditInvoiceStatus.DRAFT);
            } else if (creditInvoice.getTotalIncludingTaxes().compareTo(creditInvoice.getPaidAmount()) == 0) {
                creditInvoice.setStatus(CreditInvoiceStatus.FULLY_APPLIED);
            } else if (creditInvoice.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) {
                creditInvoice.setStatus(CreditInvoiceStatus.ISSUED);
            } else {
                creditInvoice.setStatus(CreditInvoiceStatus.PARTIALLY_APPLIED);
            }

            // update Status Display
            CreditInvoiceStatus selectedStatus = (CreditInvoiceStatus) salesDocumentStatusComboBox.getSelectionModel().getSelectedItem();

            salesDocumentStatusComboBox.getItems().clear();
            salesDocumentStatusComboBox.getItems().add(creditInvoice.getStatus());

            if (selectedStatus != null) {
                if (!isEditMode) {
                    if (!salesDocumentStatusComboBox.getItems().contains(CreditInvoiceStatus.DRAFT)) {
                        salesDocumentStatusComboBox.getItems().add(CreditInvoiceStatus.DRAFT);
                    }
                } else {
                    if (isDraftDocument) {
                        // insert mode
                        if (!salesDocumentStatusComboBox.getItems().contains(CreditInvoiceStatus.DRAFT)) {
                            salesDocumentStatusComboBox.getItems().add(CreditInvoiceStatus.DRAFT);
                        }
                    } else {
                        // update mode
                        salesDocumentStatusComboBox.getItems().add(CreditInvoiceStatus.CANCELLED);
                    }
                }
            }

            // select document status
            if (selectedStatus == CreditInvoiceStatus.CANCELLED) {
                salesDocumentStatusComboBox.setValue(CreditInvoiceStatus.CANCELLED);
            } else if (selectedStatus == CreditInvoiceStatus.DRAFT) {
                salesDocumentStatusComboBox.setValue(CreditInvoiceStatus.DRAFT);
            } else {
                salesDocumentStatusComboBox.setValue(creditInvoice.getStatus());
            }

        }
    }

    private boolean isDraftStatus(SalesDocument salesDocument) {
        if (salesDocument instanceof Quotation quotation) {
            return quotation.getStatus() == QuotationStatus.DRAFT;
        } else if (salesDocument instanceof DeliveryNote deliveryNote) {
            return deliveryNote.getStatus() == DeliveryNoteStatus.DRAFT;
        } else if (salesDocument instanceof Invoice invoice) {
            return invoice.getStatus() == InvoiceStatus.DRAFT;
        } else if (salesDocument instanceof CreditInvoice creditInvoice) {
            return creditInvoice.getStatus() == CreditInvoiceStatus.DRAFT;
        }
        return false;
    }

    public void setSalesDocumentType(Class<? extends SalesDocument> documentClass) {
        if (documentClass == Invoice.class) {
            salesDocument = new Invoice();
            salesDocumentReferenceLabel.setText("Facture");

            addPaymentButton.setOnAction(e -> {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/salesmanagement/payment/form-payment.fxml"));
                Parent root;
                try {
                    root = fxmlLoader.load();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                PaymentController paymentController = fxmlLoader.getController();
                paymentController.setSalesDocumentController(this);
                paymentController.refreshPaymentAmount();
                paymentController.setPaymentsTableView(paymentsTableView);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.showAndWait();
            });

            dueDateDatePicker.setOnAction(event -> {
                ((Invoice) salesDocument).setDueDate(dueDateDatePicker.getValue());
                updateDocumentStatus();
            });

            initPaymentsTableView();
            salesDocumentStatusComboBox.getItems().addAll(InvoiceStatus.values());

            updatePaidAndRemainingAmounts();
        }

        if (documentClass == CreditInvoice.class) {
            salesDocument = new CreditInvoice();
            salesDocumentReferenceLabel.setText("Facture Avoir");

            addPaymentButton.setOnAction(e -> {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/salesmanagement/payment/form-payment.fxml"));
                Parent root;
                try {
                    root = fxmlLoader.load();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                PaymentController paymentController = fxmlLoader.getController();
                paymentController.setSalesDocumentController(this);
                paymentController.refreshPaymentAmount();
                paymentController.setPaymentsTableView(paymentsTableView);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.showAndWait();
            });

            ((VBox) dueDateLabel.getParent().getParent()).getChildren().remove(dueDateLabel.getParent());

            initPaymentsTableView();
            salesDocumentStatusComboBox.getItems().addAll(CreditInvoiceStatus.values());
            salesDocumentStatusComboBox.setValue(CreditInvoiceStatus.DRAFT);

            updatePaidAndRemainingAmounts();
        }

        if (documentClass == Quotation.class) {
            salesDocument = new Quotation();
            salesDocumentReferenceLabel.setText("Devis");
            dueDateLabel.setText("Valide jusqu'au");

            HBox parent = (HBox) paymentsHBox.getParent();
            parent.getChildren().remove(paymentsHBox);
            parent.setAlignment(Pos.BOTTOM_RIGHT);

            ((VBox) paidAmountHBox.getParent()).getChildren().removeAll(totalSeparator, paidAmountHBox, remainingAmountHBox);
            salesDocumentItemEntryTableView.setPrefHeight(salesDocumentItemEntryTableView.getPrefHeight() + 100);

            salesDocumentStatusComboBox.getItems().addAll(QuotationStatus.values());
            salesDocumentStatusComboBox.setValue(QuotationStatus.DRAFT);
            updateDocumentStatus();
        }

        if (documentClass == DeliveryNote.class) {
            salesDocument = new DeliveryNote();
            salesDocumentReferenceLabel.setText("Bon de livraison");

            HBox parent = (HBox) paymentsHBox.getParent();
            parent.getChildren().remove(paymentsHBox);
            parent.setAlignment(Pos.BOTTOM_RIGHT);

            ((VBox) paidAmountHBox.getParent()).getChildren().removeAll(totalSeparator, paidAmountHBox, remainingAmountHBox);
            salesDocumentItemEntryTableView.setPrefHeight(salesDocumentItemEntryTableView.getPrefHeight() + 100);

            ((VBox) dueDateLabel.getParent().getParent()).getChildren().remove(dueDateLabel.getParent());


            salesDocumentStatusComboBox.getItems().addAll(DeliveryNoteStatus.values());
            salesDocumentStatusComboBox.setValue(DeliveryNoteStatus.DRAFT);
            updateDocumentStatus();

        }

        salesDocument.setIssueDate(LocalDate.now());

        issueDateDatePicker.setOnAction(event -> {
            salesDocument.setIssueDate(issueDateDatePicker.getValue());
            updateDocumentStatus();
        });
    }

    private void displaySuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Operation effectué avec success");
        alert.showAndWait();
    }

    private void displayErrorAlert() {
        displayErrorAlert("Une erreur est survenue lors de l'opération.");
    }

    private void displayErrorAlert(String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public String getRemainingAmount() {
        return remainingAmountTextField.getText();
    }

    private static class ClientComboCell extends ListCell<Client> {
        @Override
        protected void updateItem(Client client, boolean bln) {
            super.updateItem(client, bln);
            setText(client != null ? client.getName() : null);
        }
    }

}
