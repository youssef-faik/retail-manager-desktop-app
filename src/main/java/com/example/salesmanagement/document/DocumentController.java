package com.example.salesmanagement.document;

import com.example.salesmanagement.client.Client;
import com.example.salesmanagement.client.ClientRepository;
import com.example.salesmanagement.payment.*;
import com.example.salesmanagement.product.Product;
import com.example.salesmanagement.product.ProductRepository;
import com.example.salesmanagement.report.DocumentReportManager;
import com.example.salesmanagement.supplier.Supplier;
import com.example.salesmanagement.supplier.SupplierRepository;
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
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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

public class DocumentController implements Initializable {
    @FXML
    public VBox paymentsHBox;

    @FXML
    public HBox statusHBox, remainingAmountHBox, paidAmountHBox, headerHBox;

    @FXML
    private Separator totalSeparator;

    @FXML
    private TextField totalExcludingTaxesTextField, totalTaxesTextField, totalIncludingTaxesTextField, addressTextField, commonCompanyIdentifierTextField, remainingAmountTextField, paidAmountTextField;

    @FXML
    private Label documentDetailsLabel, documentReferenceLabel, dueDateLabel, comboBoxLabel;

    @FXML
    private Button addProductButton, saveDocumentButton, addPaymentButton, printDocumentButton;

    @FXML
    private DatePicker issueDateDatePicker, dueDateDatePicker;

    @FXML
    private ComboBox comboBox;

    @FXML
    private ComboBox documentStatusComboBox;

    @FXML
    private TableView<DocumentItemFormEntry> documentItemEntryTableView;

    @FXML
    private TableView<PaymentFormEntry> paymentsTableView;

    private Document document;
    private ObservableList<Product> products;
    private DocumentController documentController;
    private boolean isEditMode = false, isDraftDocument = false, originalSalesHasReference;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DropShadow dropShadow = new DropShadow(
                BlurType.ONE_PASS_BOX,
                Color.color(0.6392, 0.6392, 0.6392, 1.0),
                10.0,
                0,
                0,
                0
        );

        saveDocumentButton.setEffect(dropShadow);
        saveDocumentButton.setTextFill(Color.color(1, 1, 1));
        saveDocumentButton.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(3.0), null)));

        documentController = this;

        products = ProductRepository.findAll();

        issueDateDatePicker.setValue(LocalDate.now());
        totalExcludingTaxesTextField.setText("0");
        totalIncludingTaxesTextField.setText("0");
        totalTaxesTextField.setText("0");

        initDocumentItemsTableView();
        updatePaidAndRemainingAmounts();

        saveDocumentButton.setDisable(true);

        documentItemEntryTableView.getItems().addListener((ListChangeListener<DocumentItemFormEntry>) c -> {
            c.next();

            if (!c.wasAdded()) {
                updatePaidAndRemainingAmounts();
            }

            saveDocumentButton.setDisable(
                    !isItemTableViewValide()
                            || document.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) == 0
                            || comboBox.getSelectionModel().getSelectedItem() == null
            );
        });

        paymentsTableView.getItems().addListener((ListChangeListener<PaymentFormEntry>) c -> updatePaidAndRemainingAmounts());

        addProductButton.setOnAction(event -> {
            DocumentItemFormEntry entry = new DocumentItemFormEntry(products, this);
            documentItemEntryTableView.getItems().add(entry);
        });

        saveDocumentButton.setOnAction(e -> addDocument());
    }

    private boolean isItemTableViewValide() {
        if (documentItemEntryTableView.getItems().isEmpty()) {
            return false;
        }

        for (DocumentItemFormEntry entry : documentItemEntryTableView.getItems()) {
            if (entry.getProductComboBox().getSelectionModel().getSelectedItem() == null) {
                return false;
            }
        }
        return true;
    }

    private void addDocument() {
        mapDocument();

        Optional<Document> optionalDocument = DocumentRepository.add(document);

        if (optionalDocument.isPresent()) {
            Document document = optionalDocument.get();

            if (document.getReference() != null) {
                this.documentReferenceLabel.setText(documentReferenceLabel.getText() + " N° : " + document.getReference());
            }

            initDocumentUpdateForm(document);

            saveDocumentButton.setText("Modifier");
            Text icon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.EDIT);
            saveDocumentButton.setGraphic(icon);

            saveDocumentButton.setOnAction(
                    e -> {
                        try {
                            updateDocument();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
            );

            printDocumentButton.setVisible(true);
            printDocumentButton.setOnAction(
                    event -> {
                        try {
                            DocumentReportManager documentReportManager = new DocumentReportManager();
                            documentReportManager.displayDocumentReport(document);
                        } catch (Exception ex) {
                            Logger.getLogger(DocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
            );

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateDocument() throws SQLException {
        mapDocument();

        Optional<Document> optionalDocument = DocumentRepository.update(document);

        if (optionalDocument.isPresent()) {
            Document document = optionalDocument.get();
            if (document.getReference() != null && originalSalesHasReference) {
                this.documentReferenceLabel.setText(documentReferenceLabel.getText() + " N° : " + document.getReference());
            }

            isDraftDocument = isDraftStatus(document);
            updatePaidAndRemainingAmounts();
            originalSalesHasReference = document.getReference() == null;

            printDocumentButton.setOnAction(
                    event -> {
                        try {
                            DocumentReportManager documentReportManager = new DocumentReportManager();
                            documentReportManager.displayDocumentReport(document);
                        } catch (Exception ex) {
                            Logger.getLogger(DocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
            );

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private void mapDocument() {
        document.setIssueDate(issueDateDatePicker.getValue());
        document.clearItems();
        documentItemEntryTableView.getItems().forEach(documentItemFormEntry -> document.addItem(documentItemFormEntry.getInvoiceItem()));

        if (document instanceof Quotation quotation) {
            quotation.setValidUntil(dueDateDatePicker.getValue());
            quotation.setStatus((QuotationStatus) documentStatusComboBox.getValue());
        }
        if (document instanceof DeliveryNote deliveryNote) {
            deliveryNote.setStatus((DeliveryNoteStatus) documentStatusComboBox.getValue());
        }
        if (document instanceof PurchaseOrder purchaseOrder) {
            purchaseOrder.setStatus((PurchaseOrderStatus) documentStatusComboBox.getValue());
        }
        if (document instanceof PurchaseDeliveryNote purchaseDeliveryNote) {
            purchaseDeliveryNote.setStatus((PurchaseDeliveryNoteStatus) documentStatusComboBox.getValue());
        }
        if (document instanceof Invoice invoice) {
            invoice.setDueDate(dueDateDatePicker.getValue());
            invoice.setStatus((InvoiceStatus) documentStatusComboBox.getValue());

            Set<Payment> payments = paymentsTableView.getItems()
                    .stream()
                    .map(PaymentFormEntry::getPayment)
                    .collect(Collectors.toSet());

            invoice.setPayments(new HashSet<>(payments));
        }
        if (document instanceof CreditInvoice creditInvoice) {
            creditInvoice.setStatus((CreditInvoiceStatus) documentStatusComboBox.getValue());

            Set<Payment> payments = paymentsTableView.getItems()
                    .stream()
                    .map(PaymentFormEntry::getPayment)
                    .collect(Collectors.toSet());

            creditInvoice.setPayments(new HashSet<>(payments));
        }
    }

    private void initComboBox() {
        comboBox.setEditable(false);

        comboBox.setOnAction(event -> {
            Object selectedItem = comboBox.getSelectionModel().getSelectedItem();

            if (selectedItem instanceof Client client) {
                commonCompanyIdentifierTextField.setText(client.getCommonCompanyIdentifier());
                addressTextField.setText(client.getAddress());
                ((SalesDocument) document).setClient(client);
            } else if (selectedItem instanceof Supplier supplier) {
                commonCompanyIdentifierTextField.setText(supplier.getCommonCompanyIdentifier());
                addressTextField.setText(supplier.getAddress());
                ((PurchaseDocument) document).setSupplier(supplier);
            }

            saveDocumentButton.setDisable(!isItemTableViewValide() ||
                    document.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) == 0 || selectedItem == null);
        });

        if (document instanceof SalesDocument) {
            comboBox.setCellFactory(x -> new ClientComboCell());
            comboBox.setButtonCell(new ClientComboCell());

            ObservableList<Client> clients = ClientRepository.findAll();
            comboBox.setItems(clients);
        } else {
            comboBox.setCellFactory(x -> new SupplierComboCell());
            comboBox.setButtonCell(new SupplierComboCell());

            ObservableList<Supplier> suppliers = SupplierRepository.findAll();
            comboBox.setItems(suppliers);

            comboBoxLabel.setText("Details du fournisseur");
            comboBox.setPromptText("Choisissez un fournisseur");
        }
    }

    private void initDocumentItemsTableView() {
        TableColumn<DocumentItemFormEntry, String> productColumn = new TableColumn<>("Produit");
        TableColumn<DocumentItemFormEntry, String> quantityColumn = new TableColumn<>("Quantité");
        TableColumn<DocumentItemFormEntry, String> unitPriceColumn = new TableColumn<>("Prix unitaire (HT)");
        TableColumn<DocumentItemFormEntry, String> taxRateColumn = new TableColumn<>("Taux TVA");
        TableColumn<DocumentItemFormEntry, String> totalIncludingTaxesColumn = new TableColumn<>("Total (TTC)");
        TableColumn<DocumentItemFormEntry, String> totalExcludingTaxesColumn = new TableColumn<>("Total (HT)");
        TableColumn<DocumentItemFormEntry, String> totalTaxesColumn = new TableColumn<>("Total TVA");
        TableColumn<DocumentItemFormEntry, String> actionColumn = new TableColumn<>("Actions");

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

        documentItemEntryTableView.getColumns().addAll(
                productColumn,
                quantityColumn,
                unitPriceColumn,
                taxRateColumn,
                totalExcludingTaxesColumn,
                totalTaxesColumn,
                totalIncludingTaxesColumn,
                actionColumn
        );

        documentItemEntryTableView.setEditable(true);

        // delete
        Callback<TableColumn<DocumentItemFormEntry, String>, TableCell<DocumentItemFormEntry, String>> cellFactory =
                (TableColumn<DocumentItemFormEntry, String> param) -> {
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
                                        DocumentItemFormEntry rowItem = (DocumentItemFormEntry) tableRow.getItem();
                                        documentItemEntryTableView.getItems().remove(rowItem);
                                        updateTotals();
                                    } catch (Exception ex) {
                                        Logger.getLogger(DocumentController.class.getName()).log(Level.SEVERE, null, ex);
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
            DocumentItemFormEntry entry = event.getTableView().getItems().get(event.getTablePosition().getRow());
            try {
                entry.setQuantity(Integer.parseInt(event.getNewValue()));
                updateItemTotals(entry);
                updateTotals();

                refreshTableView();
            } catch (NumberFormatException e) {
                // Handle the case where the input is not a valid integer
                documentItemEntryTableView.refresh();
                System.out.println("Invalid input: " + event.getNewValue());
                displayErrorAlert("valeur de quantité incorrecte: " + event.getNewValue());
            }
        });

        unitPriceColumn.setOnEditCommit(event -> {
            DocumentItemFormEntry entry = event.getTableView().getItems().get(event.getTablePosition().getRow());
            try {
                entry.setUnitPriceExcludingTaxes(BigDecimal.valueOf(Double.parseDouble(event.getNewValue())));
                updateItemTotals(entry);
                updateTotals();

                refreshTableView();
            } catch (NumberFormatException e) {
                // Handle the case where the input is not a valid integer
                documentItemEntryTableView.refresh();
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
                                    Logger.getLogger(DocumentController.class.getName()).log(Level.SEVERE, null, ex);
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
                                paymentController.setSalesDocumentController(documentController);
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

    public void initDocumentUpdateForm(Document originalDocument) {
        // get a clone of the orignal sales document
        if (originalDocument instanceof PurchaseOrder purchaseOrder) {
            try {
                document = (Document) purchaseOrder.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        if (originalDocument instanceof PurchaseDeliveryNote purchaseDeliveryNote) {
            try {
                document = (Document) purchaseDeliveryNote.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        if (originalDocument instanceof Quotation quotation) {
            try {
                document = (Document) quotation.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        if (originalDocument instanceof DeliveryNote deliveryNote) {
            try {
                document = (Document) deliveryNote.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        if (originalDocument instanceof Invoice invoice) {
            try {
                document = (Document) invoice.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        if (originalDocument instanceof CreditInvoice creditInvoice) {
            try {
                document = (Document) creditInvoice.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        // set commun attributs
        this.issueDateDatePicker.setValue(originalDocument.getIssueDate());
        this.totalIncludingTaxesTextField.setText(originalDocument.getTotalIncludingTaxes().toString());
        this.totalExcludingTaxesTextField.setText(originalDocument.getTotalExcludingTaxes().toString());
        this.totalTaxesTextField.setText(originalDocument.getTotalTaxes().toString());

        // set client/supplier ComboBox
        if (originalDocument instanceof PurchaseDocument purchaseDocument) {
            this.comboBox.setValue(purchaseDocument.getSupplier());
            this.commonCompanyIdentifierTextField.setText(purchaseDocument.getSupplier().getCommonCompanyIdentifier());
            this.addressTextField.setText(purchaseDocument.getSupplier().getAddress());
        } else if (originalDocument instanceof SalesDocument salesDocument) {
            this.comboBox.setValue(salesDocument.getClient());
            this.commonCompanyIdentifierTextField.setText(salesDocument.getClient().getCommonCompanyIdentifier());
            this.addressTextField.setText(salesDocument.getClient().getAddress());
        }

        isEditMode = true;

        originalSalesHasReference = document.getReference() == null;
        printDocumentButton.setVisible(true);

        printDocumentButton.setOnAction(
                event -> {
                    try {
                        DocumentReportManager documentReportManager = new DocumentReportManager();
                        documentReportManager.displayDocumentReport(document);
                    } catch (Exception ex) {
                        Logger.getLogger(DocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
        );

        documentItemEntryTableView.getItems().clear();
        originalDocument.getItems().forEach(
                invoiceItem -> documentItemEntryTableView.getItems().add(
                        new DocumentItemFormEntry(products, this, invoiceItem)
                )
        );

        documentItemEntryTableView.refresh();

        saveDocumentButton.setOnAction(e -> {
            try {
                updateDocument();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        documentStatusComboBox.getItems().clear();

        if (originalDocument instanceof PurchaseOrder purchaseOrder) {
            this.documentReferenceLabel.setText(purchaseOrder.getReference() == null ? "Bon de commande" : "Bon de commande N° : " + purchaseOrder.getReference());

            documentStatusComboBox.getItems().add(purchaseOrder.getStatus());
            documentStatusComboBox.setValue(purchaseOrder.getStatus());
        } else if (originalDocument instanceof PurchaseDeliveryNote purchaseDeliveryNote) {
            this.documentReferenceLabel.setText(purchaseDeliveryNote.getReference() == null ? "Bon de réception" : "Bon de réception N° : " + purchaseDeliveryNote.getReference());

            documentStatusComboBox.getItems().add(purchaseDeliveryNote.getStatus());
            documentStatusComboBox.setValue(purchaseDeliveryNote.getStatus());
        } else if (originalDocument instanceof Quotation quotation) {
            this.documentReferenceLabel.setText(quotation.getReference() == null ? "Devis" : "Devis N° : " + quotation.getReference());
            this.dueDateDatePicker.setValue(quotation.getValidUntil());

            documentStatusComboBox.getItems().add(quotation.getStatus());
            documentStatusComboBox.setValue(quotation.getStatus());
        } else if (originalDocument instanceof DeliveryNote deliveryNote) {
            this.documentReferenceLabel.setText(deliveryNote.getReference() == null ? "Bon de livraison" : "Bon de livraison N° : " + deliveryNote.getReference());

            documentStatusComboBox.getItems().add(deliveryNote.getStatus());
            documentStatusComboBox.setValue(deliveryNote.getStatus());
        } else if (originalDocument instanceof Invoice invoice) {
            this.documentReferenceLabel.setText(invoice.getReference() == null ? "Facture" : "Facture N° : " + invoice.getReference());
            this.dueDateDatePicker.setValue(invoice.getDueDate());

            documentStatusComboBox.getItems().add(invoice.getStatus());
            documentStatusComboBox.setValue(invoice.getStatus());

            paymentsTableView.getItems().clear();
            invoice.getPayments().forEach(payment -> paymentsTableView.getItems().add(new PaymentFormEntry(payment)));
        } else if (originalDocument instanceof CreditInvoice creditInvoice) {
            this.documentReferenceLabel.setText(creditInvoice.getReference() == null ? "Facture avoir" : "Facture avoir N° : " + creditInvoice.getReference());

            documentStatusComboBox.getItems().add(creditInvoice.getStatus());
            documentStatusComboBox.setValue(creditInvoice.getStatus());

            paymentsTableView.getItems().clear();
            creditInvoice.getPayments().forEach(payment -> paymentsTableView.getItems().add(new PaymentFormEntry(payment)));
        }

        isDraftDocument = isDraftStatus(originalDocument);
        updatePaidAndRemainingAmounts();
    }

    private void refreshTableView() {
        documentItemEntryTableView.refresh();
    }

    private void updateItemTotals(DocumentItemFormEntry entry) {
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
        // update document totals
        document.setTotalExcludingTaxes(BigDecimal.ZERO);
        document.setTotalTaxes(BigDecimal.ZERO);
        document.setTotalIncludingTaxes(BigDecimal.ZERO);

        for (DocumentItemFormEntry itemEntry : documentItemEntryTableView.getItems()) {
            document.setTotalExcludingTaxes(document.getTotalExcludingTaxes().add(itemEntry.getTotalExcludingTaxes()));
            document.setTotalTaxes(document.getTotalTaxes().add(itemEntry.getTotalTaxes()));
            document.setTotalIncludingTaxes(document.getTotalIncludingTaxes().add(itemEntry.getTotalIncludingTaxes()));
        }

        // update total display
        totalExcludingTaxesTextField.setText(document.getTotalExcludingTaxes().toString());
        totalIncludingTaxesTextField.setText(document.getTotalIncludingTaxes().toString());
        totalTaxesTextField.setText(document.getTotalTaxes().toString());

        updatePaidAndRemainingAmounts();
    }

    public void updatePaidAndRemainingAmounts() {
        if (document instanceof Invoice invoice) {
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
        if (document instanceof CreditInvoice creditInvoice) {
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
        saveDocumentButton.setDisable(!isItemTableViewValide() ||
                document.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) == 0
                || comboBox.getSelectionModel().getSelectedItem() == null);

        if (document instanceof PurchaseOrder purchaseOrder) {
            // update Status Display
            PurchaseOrderStatus selectedStatus = (PurchaseOrderStatus) documentStatusComboBox.getSelectionModel().getSelectedItem();

            documentStatusComboBox.getItems().clear();
            documentStatusComboBox.getItems().add(purchaseOrder.getStatus());

            if (selectedStatus != null) {
                if (!isEditMode) {
                    // insert mode

                    if (!documentStatusComboBox.getItems().contains(PurchaseOrderStatus.DRAFT)) {
                        documentStatusComboBox.getItems().add(PurchaseOrderStatus.DRAFT);
                    }

                    if (isItemTableViewValide()
                            && document.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) != 0
                            && comboBox.getSelectionModel().getSelectedItem() != null) {
                        if (!documentStatusComboBox.getItems().contains(PurchaseOrderStatus.SENT_TO_SUPPLIER)) {
                            documentStatusComboBox.getItems().add(PurchaseOrderStatus.SENT_TO_SUPPLIER);
                        }
                        if (!documentStatusComboBox.getItems().contains(PurchaseOrderStatus.RECEIVED_ORDER)) {
                            documentStatusComboBox.getItems().add(PurchaseOrderStatus.RECEIVED_ORDER);
                        }
                    }
                } else {
                    // update mode
                    documentStatusComboBox.getItems().remove(purchaseOrder.getStatus());

                    if (isDraftDocument) {
                        documentStatusComboBox.getItems().addAll(PurchaseOrderStatus.DRAFT, PurchaseOrderStatus.SENT_TO_SUPPLIER, PurchaseOrderStatus.RECEIVED_ORDER);
                    } else {
                        documentStatusComboBox.getItems().addAll(PurchaseOrderStatus.SENT_TO_SUPPLIER, PurchaseOrderStatus.RECEIVED_ORDER, PurchaseOrderStatus.CANCELLED);
                    }
                }
            }

            int index = documentStatusComboBox.getItems().indexOf(purchaseOrder.getStatus());
            documentStatusComboBox.getSelectionModel().select(index);
        }

        if (document instanceof PurchaseDeliveryNote purchaseDeliveryNote) {
            // update Status Display
            PurchaseDeliveryNoteStatus selectedStatus = (PurchaseDeliveryNoteStatus) documentStatusComboBox.getSelectionModel().getSelectedItem();

            documentStatusComboBox.getItems().clear();
            documentStatusComboBox.getItems().add(purchaseDeliveryNote.getStatus());

            if (selectedStatus != null) {
                if (!isEditMode) {
                    // insert mode

                    if (!documentStatusComboBox.getItems().contains(PurchaseDeliveryNoteStatus.DRAFT)) {
                        documentStatusComboBox.getItems().add(PurchaseDeliveryNoteStatus.DRAFT);
                    }

                    if (isItemTableViewValide()
                            && document.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) != 0
                            && comboBox.getSelectionModel().getSelectedItem() != null) {
                        if (!documentStatusComboBox.getItems().contains(PurchaseDeliveryNoteStatus.RECEIVED)) {
                            documentStatusComboBox.getItems().add(PurchaseDeliveryNoteStatus.RECEIVED);
                        }
                    }
                } else {
                    // update mode
                    documentStatusComboBox.getItems().remove(purchaseDeliveryNote.getStatus());

                    if (isDraftDocument) {
                        documentStatusComboBox.getItems().addAll(PurchaseDeliveryNoteStatus.DRAFT, PurchaseDeliveryNoteStatus.RECEIVED);
                    } else {
                        documentStatusComboBox.getItems().addAll(PurchaseDeliveryNoteStatus.RECEIVED, PurchaseDeliveryNoteStatus.CANCELLED);
                    }
                }
            }

            int index = documentStatusComboBox.getItems().indexOf(purchaseDeliveryNote.getStatus());
            documentStatusComboBox.getSelectionModel().select(index);
        }

        if (document instanceof Quotation quotation) {
            if (quotation.getStatus() == QuotationStatus.SENT
                    && quotation.getValidUntil() != null && quotation.getValidUntil().isBefore(LocalDate.now())) {
                quotation.setStatus(QuotationStatus.EXPIRED);
            }

            // update Status Display
            QuotationStatus selectedStatus = (QuotationStatus) documentStatusComboBox.getSelectionModel().getSelectedItem();

            documentStatusComboBox.getItems().clear();
            documentStatusComboBox.getItems().add(quotation.getStatus());

            if (selectedStatus != null) {
                if (!isEditMode) {
                    if (!documentStatusComboBox.getItems().contains(QuotationStatus.DRAFT)) {
                        documentStatusComboBox.getItems().add(QuotationStatus.DRAFT);
                    }
                    if (!documentStatusComboBox.getItems().contains(QuotationStatus.SENT) && quotation.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) != 0) {
                        documentStatusComboBox.getItems().add(QuotationStatus.SENT);
                    }
                } else {
                    if (isDraftDocument) {
                        documentStatusComboBox.getItems().add(QuotationStatus.SENT);
                    } else {
                        if (documentStatusComboBox.getItems().contains(QuotationStatus.SENT)) {
                            documentStatusComboBox.getItems().addAll(QuotationStatus.ACCEPTED, QuotationStatus.REJECTED);
                        } else {
                            if (!documentStatusComboBox.getItems().contains(QuotationStatus.REJECTED))
                                documentStatusComboBox.getItems().add(QuotationStatus.REJECTED);
                            else
                                documentStatusComboBox.getItems().add(QuotationStatus.ACCEPTED);
                        }
                    }
                }
            }

            if (selectedStatus == QuotationStatus.SENT) {
                documentStatusComboBox.setValue(QuotationStatus.SENT);
            } else if (selectedStatus == QuotationStatus.DRAFT) {
                documentStatusComboBox.setValue(QuotationStatus.DRAFT);
            } else {
                documentStatusComboBox.setValue(quotation.getStatus());
            }
        }

        if (document instanceof DeliveryNote deliveryNote) {
            // update Status Display
            DeliveryNoteStatus selectedStatus = (DeliveryNoteStatus) documentStatusComboBox.getSelectionModel().getSelectedItem();

            documentStatusComboBox.getItems().clear();
            documentStatusComboBox.getItems().add(deliveryNote.getStatus());

            if (selectedStatus != null) {
                if (!isEditMode) {
                    // insert mode

                    if (!documentStatusComboBox.getItems().contains(DeliveryNoteStatus.DRAFT)) {
                        documentStatusComboBox.getItems().add(DeliveryNoteStatus.DRAFT);
                    }

                    if (isItemTableViewValide()
                            && document.getTotalIncludingTaxes().compareTo(BigDecimal.ZERO) != 0
                            && comboBox.getSelectionModel().getSelectedItem() != null) {
                        if (!documentStatusComboBox.getItems().contains(DeliveryNoteStatus.DISPATCHED)) {
                            documentStatusComboBox.getItems().add(DeliveryNoteStatus.DISPATCHED);
                        }
                        if (!documentStatusComboBox.getItems().contains(DeliveryNoteStatus.DELIVERED)) {
                            documentStatusComboBox.getItems().add(DeliveryNoteStatus.DELIVERED);
                        }
                    }
                } else {
                    // update mode
                    documentStatusComboBox.getItems().remove(deliveryNote.getStatus());

                    if (isDraftDocument) {
                        documentStatusComboBox.getItems().addAll(DeliveryNoteStatus.DISPATCHED, DeliveryNoteStatus.DELIVERED);
                    } else {
                        documentStatusComboBox.getItems().addAll(DeliveryNoteStatus.DISPATCHED, DeliveryNoteStatus.DELIVERED, DeliveryNoteStatus.CANCELLED);
                    }
                }
            }

            int index = documentStatusComboBox.getItems().indexOf(deliveryNote.getStatus());
            documentStatusComboBox.getSelectionModel().select(index);
        }

        if (document instanceof Invoice invoice) {
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
            InvoiceStatus selectedStatus = (InvoiceStatus) documentStatusComboBox.getSelectionModel().getSelectedItem();

            documentStatusComboBox.getItems().clear();
            documentStatusComboBox.getItems().add(invoice.getStatus());

            if (selectedStatus != null) {
                if (!isEditMode) {
                    if (!documentStatusComboBox.getItems().contains(InvoiceStatus.DRAFT)) {
                        documentStatusComboBox.getItems().add(InvoiceStatus.DRAFT);
                    }
                } else {
                    if (isDraftDocument) {
                        documentStatusComboBox.getItems().add(InvoiceStatus.DRAFT);
                    } else {
                        documentStatusComboBox.getItems().add(InvoiceStatus.CANCELLED);
                    }
                }
            }

            int index;
            if (selectedStatus == InvoiceStatus.CANCELLED) {
                index = documentStatusComboBox.getItems().indexOf(InvoiceStatus.CANCELLED);
            } else if (selectedStatus == InvoiceStatus.DRAFT) {
                index = documentStatusComboBox.getItems().indexOf(InvoiceStatus.DRAFT);
            } else {
                index = documentStatusComboBox.getItems().indexOf(invoice.getStatus());
            }

            documentStatusComboBox.getSelectionModel().select(index);
        }

        if (document instanceof CreditInvoice creditInvoice) {
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
            CreditInvoiceStatus selectedStatus = (CreditInvoiceStatus) documentStatusComboBox.getSelectionModel().getSelectedItem();

            documentStatusComboBox.getItems().clear();
            documentStatusComboBox.getItems().add(creditInvoice.getStatus());

            if (selectedStatus != null) {
                if (!isEditMode) {
                    if (!documentStatusComboBox.getItems().contains(CreditInvoiceStatus.DRAFT)) {
                        documentStatusComboBox.getItems().add(CreditInvoiceStatus.DRAFT);
                    }
                } else {
                    if (isDraftDocument) {
                        // insert mode
                        if (!documentStatusComboBox.getItems().contains(CreditInvoiceStatus.DRAFT)) {
                            documentStatusComboBox.getItems().add(CreditInvoiceStatus.DRAFT);
                        }
                    } else {
                        // update mode
                        documentStatusComboBox.getItems().add(CreditInvoiceStatus.CANCELLED);
                    }
                }
            }

            // select document status
            int index;

            if (selectedStatus == CreditInvoiceStatus.CANCELLED) {
                index = documentStatusComboBox.getItems().indexOf(CreditInvoiceStatus.CANCELLED);
            } else if (selectedStatus == CreditInvoiceStatus.DRAFT) {
                index = documentStatusComboBox.getItems().indexOf(CreditInvoiceStatus.DRAFT);
            } else {
                index = documentStatusComboBox.getItems().indexOf(creditInvoice.getStatus());
            }

            documentStatusComboBox.getSelectionModel().select(index);
        }
    }

    private boolean isDraftStatus(Document document) {
        if (document instanceof PurchaseOrder purchaseOrder) {
            return purchaseOrder.getStatus() == PurchaseOrderStatus.DRAFT;
        } else if (document instanceof PurchaseDeliveryNote purchaseDeliveryNote) {
            return purchaseDeliveryNote.getStatus() == PurchaseDeliveryNoteStatus.DRAFT;
        } else if (document instanceof Quotation quotation) {
            return quotation.getStatus() == QuotationStatus.DRAFT;
        } else if (document instanceof DeliveryNote deliveryNote) {
            return deliveryNote.getStatus() == DeliveryNoteStatus.DRAFT;
        } else if (document instanceof Invoice invoice) {
            return invoice.getStatus() == InvoiceStatus.DRAFT;
        } else if (document instanceof CreditInvoice creditInvoice) {
            return creditInvoice.getStatus() == CreditInvoiceStatus.DRAFT;
        }
        return false;
    }

    public void setDocumentType(Class<? extends Document> documentClass) {
        if (documentClass == Invoice.class) {
            document = new Invoice();
            documentReferenceLabel.setText("Facture");
            documentDetailsLabel.setText("Details de la Facture");

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
                ((Invoice) document).setDueDate(dueDateDatePicker.getValue());
                updateDocumentStatus();
            });

            initPaymentsTableView();
            documentStatusComboBox.getItems().addAll(InvoiceStatus.values());

            updatePaidAndRemainingAmounts();
        }

        if (documentClass == CreditInvoice.class) {
            document = new CreditInvoice();
            documentReferenceLabel.setText("Facture Avoir");
            documentDetailsLabel.setText("Details de la Facture avoir");

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
            documentStatusComboBox.getItems().addAll(CreditInvoiceStatus.values());
            documentStatusComboBox.setValue(CreditInvoiceStatus.DRAFT);

            updatePaidAndRemainingAmounts();
        }

        if (documentClass == Quotation.class) {
            document = new Quotation();
            documentReferenceLabel.setText("Devis");
            dueDateLabel.setText("Valide jusqu'au");
            documentDetailsLabel.setText("Details du devis");

            HBox parent = (HBox) paymentsHBox.getParent();
            parent.getChildren().remove(paymentsHBox);
            parent.setAlignment(Pos.BOTTOM_RIGHT);

            ((VBox) paidAmountHBox.getParent()).getChildren().removeAll(totalSeparator, paidAmountHBox, remainingAmountHBox);

            documentStatusComboBox.getItems().addAll(QuotationStatus.values());
            documentStatusComboBox.setValue(QuotationStatus.DRAFT);

            updateDocumentStatus();
        }

        if (documentClass == DeliveryNote.class) {
            document = new DeliveryNote();
            documentReferenceLabel.setText("Bon de livraison");
            documentDetailsLabel.setText("Details du bon de livraison");

            HBox parent = (HBox) paymentsHBox.getParent();
            parent.getChildren().remove(paymentsHBox);
            parent.setAlignment(Pos.BOTTOM_RIGHT);

            ((VBox) paidAmountHBox.getParent()).getChildren().removeAll(totalSeparator, paidAmountHBox, remainingAmountHBox);
            ((VBox) dueDateLabel.getParent().getParent()).getChildren().remove(dueDateLabel.getParent());


            documentStatusComboBox.getItems().addAll(DeliveryNoteStatus.values());
            documentStatusComboBox.setValue(DeliveryNoteStatus.DRAFT);

            updateDocumentStatus();
        }

        if (documentClass == PurchaseOrder.class) {
            document = new PurchaseOrder();
            documentReferenceLabel.setText("Bon de commande");
            documentDetailsLabel.setText("Details du bon de commande");

            HBox parent = (HBox) paymentsHBox.getParent();
            parent.getChildren().remove(paymentsHBox);
            parent.setAlignment(Pos.BOTTOM_RIGHT);

            ((VBox) paidAmountHBox.getParent()).getChildren().removeAll(totalSeparator, paidAmountHBox, remainingAmountHBox);
            ((VBox) dueDateLabel.getParent().getParent()).getChildren().remove(dueDateLabel.getParent());


            documentStatusComboBox.getItems().addAll(PurchaseOrderStatus.values());
            documentStatusComboBox.setValue(PurchaseOrderStatus.DRAFT);

            updateDocumentStatus();
        }

        if (documentClass == PurchaseDeliveryNote.class) {
            document = new PurchaseDeliveryNote();
            documentReferenceLabel.setText("Bon de réception");
            documentDetailsLabel.setText("Details du bon de réception");

            HBox parent = (HBox) paymentsHBox.getParent();
            parent.getChildren().remove(paymentsHBox);
            parent.setAlignment(Pos.BOTTOM_RIGHT);

            ((VBox) paidAmountHBox.getParent()).getChildren().removeAll(totalSeparator, paidAmountHBox, remainingAmountHBox);

            ((VBox) dueDateLabel.getParent().getParent()).getChildren().remove(dueDateLabel.getParent());


            documentStatusComboBox.getItems().addAll(PurchaseDeliveryNoteStatus.values());
            documentStatusComboBox.setValue(PurchaseDeliveryNoteStatus.DRAFT);

            updateDocumentStatus();
        }

        document.setIssueDate(LocalDate.now());

        issueDateDatePicker.setOnAction(event -> {
            document.setIssueDate(issueDateDatePicker.getValue());
            updateDocumentStatus();
        });

        initComboBox();
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

    private static class SupplierComboCell extends ListCell<Supplier> {
        @Override
        protected void updateItem(Supplier supplier, boolean bln) {
            super.updateItem(supplier, bln);
            setText(supplier != null ? supplier.getName() : null);
        }
    }

}
