package com.example.salesmanagement.salesdocument;

import com.example.salesmanagement.report.SalesDocumentReportManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.sf.jasperreports.engine.JRException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SalesDocumentsController implements Initializable {
    @FXML
    public Label formLabel;

    Class<? extends SalesDocument> formClass;
    FilteredList<SalesDocument> filteredList;
    SortedList<SalesDocument> sortedList;
    ObservableList<SalesDocument> observableList;

    @FXML
    private TableView<SalesDocument> salesDocumentsTableView;
    @FXML
    private Button deleteButton, updateButton;
    @FXML
    private TextField searchTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(salesDocument -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                try {
                    Long value = Long.valueOf(searchTextField.getText());

                    if (Objects.equals(salesDocument.getReference(), value)) {
                        return true;
                    }
                } catch (Exception ignored) {
                }

                return salesDocument.getClient().getName().toLowerCase().contains(lowerCaseFilter);

            });
        });
    }

    public void addSalesDocument(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-sales-document.fxml"));
        VBox pane = fxmlLoader.load();

        SalesDocumentController salesDocumentController = fxmlLoader.getController();
        salesDocumentController.setSalesDocumentType(formClass);

        Button button = (Button) actionEvent.getSource();
        BorderPane borderPane = (BorderPane) button.getScene().getRoot();
        borderPane.setCenter(pane);
    }

    public void updateSalesDocument() throws IOException {
        SalesDocument selectedSalesDocument = salesDocumentsTableView.getSelectionModel().getSelectedItem();
        if (selectedSalesDocument != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-sales-document.fxml"));
            Parent root = fxmlLoader.load();

            SalesDocumentController salesDocumentController = fxmlLoader.getController();

            SalesDocument salesDocument = SalesDocumentRepository.findById(selectedSalesDocument.getId()).orElseThrow();
            salesDocumentController.setSalesDocumentType(formClass);
            salesDocumentController.initSalesDocumentUpdate(salesDocument);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            loadSalesDocumentsData(formClass);
        }
    }

    public void deleteSalesDocument() {
        SalesDocument selectedSalesDocument = salesDocumentsTableView.getSelectionModel().getSelectedItem();
        if (selectedSalesDocument != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer ce document?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {

                if (SalesDocumentRepository.deleteById(selectedSalesDocument.getId())) {
                    loadSalesDocumentsData(formClass);
                    updateButton.setDisable(true);
                    deleteButton.setDisable(true);
                    displaySuccessAlert();
                } else {
                    displayErrorAlert();
                }

            }
        }
    }

    public void setFormClass(Class<? extends SalesDocument> formClass) {
        this.formClass = formClass;

        initSalesDocumentsTableView(formClass);
        loadSalesDocumentsData(formClass);

        if (formClass == Quotation.class) {
            formLabel.setText("Devis");
        }

        if (formClass == DeliveryNote.class) {
            formLabel.setText("Bons de livraison");
        }

        if (formClass == Invoice.class) {
            formLabel.setText("Factures");
        }

        if (formClass == CreditInvoice.class) {
            formLabel.setText("Factures avoir");
        }
    }

    private <T extends SalesDocument> void initSalesDocumentsTableView(Class<T> aClass) {
        TableColumn<T, String> idColumn = new TableColumn<>("ID");
        TableColumn<T, String> referenceColumn = new TableColumn<>("Référence");
        TableColumn<T, String> clientColumn = new TableColumn<>("Client");
        TableColumn<T, String> issueDateColumn = new TableColumn<>("Date d'émission");
        TableColumn<T, String> totalIncludingTaxesColumn = new TableColumn<>("Total (TTC)");
        TableColumn<T, String> actionColumn = new TableColumn<>("Actions");
        TableColumn<T, String> statusColumn = new TableColumn<>("Status");
        TableColumn<T, String> paidAmountColumn = null;
        TableColumn<T, String> remainingAmountTaxesColumn = null;
        TableColumn<T, String> dueDateColumn = null;
        TableColumn<T, String> validUntilColumn = null;

        if (aClass == Invoice.class) {
            paidAmountColumn = new TableColumn<>("Montant payé");
            remainingAmountTaxesColumn = new TableColumn<>("Montant restant");
            dueDateColumn = new TableColumn<>("Date d'échéance");

            remainingAmountTaxesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTotalIncludingTaxes().subtract(((Invoice) cellData.getValue()).getPaidAmount()).toPlainString()));
            paidAmountColumn.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
            dueDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(((Invoice) cellData.getValue()).getDueDate() == null ? "N/A" : ((Invoice) cellData.getValue()).getDueDate().toString()));
        }
        if (aClass == CreditInvoice.class) {
            paidAmountColumn = new TableColumn<>("Montant payé");
            remainingAmountTaxesColumn = new TableColumn<>("Montant restant");

            paidAmountColumn.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
            remainingAmountTaxesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTotalIncludingTaxes().subtract(((CreditInvoice) cellData.getValue()).getPaidAmount()).toPlainString()));
        }

        if (aClass == Quotation.class) {
            validUntilColumn = new TableColumn<>("Valide jusqu'au");
            validUntilColumn.setCellValueFactory(cellData -> new SimpleStringProperty(((Quotation) cellData.getValue()).getValidUntil() == null ? "N/A" : ((Quotation) cellData.getValue()).getValidUntil().toString()));

        }

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        issueDateColumn.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClient().getName()));
        totalIncludingTaxesColumn.setCellValueFactory(new PropertyValueFactory<>("totalIncludingTaxes"));
        referenceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReference() == null ? "N/A" : cellData.getValue().getReference().toString()));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        idColumn.setVisible(false);

        referenceColumn.setMinWidth(70);
        referenceColumn.setMaxWidth(70);
        referenceColumn.setReorderable(false);

        actionColumn.setMinWidth(90);
        actionColumn.setMaxWidth(90);
        actionColumn.setResizable(false);
        actionColumn.setReorderable(false);

        salesDocumentsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        salesDocumentsTableView.getColumns().addAll(
                (TableColumn<SalesDocument, ?>) idColumn,
                (TableColumn<SalesDocument, ?>) referenceColumn,
                (TableColumn<SalesDocument, ?>) clientColumn,
                (TableColumn<SalesDocument, ?>) issueDateColumn,
                (TableColumn<SalesDocument, ?>) statusColumn
        );

        if (aClass == Quotation.class) {
            salesDocumentsTableView.getColumns().addAll(
                    (TableColumn<SalesDocument, ?>) totalIncludingTaxesColumn,
                    (TableColumn<SalesDocument, ?>) validUntilColumn
            );
        }

        if (aClass == DeliveryNote.class) {
            salesDocumentsTableView.getColumns().addAll(
                    (TableColumn<SalesDocument, ?>) totalIncludingTaxesColumn
            );
        }
        if (aClass == Invoice.class) {
            salesDocumentsTableView.getColumns().addAll(
                    (TableColumn<SalesDocument, ?>) dueDateColumn,
                    (TableColumn<SalesDocument, ?>) totalIncludingTaxesColumn,
                    (TableColumn<SalesDocument, ?>) paidAmountColumn,
                    (TableColumn<SalesDocument, ?>) remainingAmountTaxesColumn
            );
        }

        if (aClass == CreditInvoice.class) {
            salesDocumentsTableView.getColumns().addAll(
                    (TableColumn<SalesDocument, ?>) totalIncludingTaxesColumn,
                    (TableColumn<SalesDocument, ?>) paidAmountColumn,
                    (TableColumn<SalesDocument, ?>) remainingAmountTaxesColumn
            );
        }


        salesDocumentsTableView.getColumns().addAll((TableColumn<SalesDocument, ?>) actionColumn);

        Callback<TableColumn<T, String>, TableCell<T, String>> cellFactory =
                (TableColumn<T, String> param) -> {
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
                                Button printButton = new Button("Imprimer");
                                Text icon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.PRINT);
                                printButton.setGraphic(icon);


                                printButton.setOnMouseClicked((MouseEvent event) -> {
                                    try {
                                        @SuppressWarnings("unchecked")
                                        TableRow<SalesDocument> tableRow = (TableRow<SalesDocument>) printButton.getParent().getParent().getParent();
                                        SalesDocument selectedSalesDocument = tableRow.getItem();

                                        SalesDocument salesDocument = SalesDocumentRepository.findById(selectedSalesDocument.getId()).orElseThrow();

                                        try {
                                            SalesDocumentReportManager salesDocumentReportManager = new SalesDocumentReportManager();
                                            salesDocumentReportManager.displaySalesDocumentReport(salesDocument);
                                        } catch (JRException e) {
                                            throw new RuntimeException(e);
                                        }

                                    } catch (Exception ex) {
                                        Logger.getLogger(SalesDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                });

                                HBox actionsHBox = new HBox(printButton);
                                actionsHBox.setStyle("-fx-alignment:center");
                                setGraphic(actionsHBox);

                                setText(null);
                            }
                        }

                    };
                };

        actionColumn.setCellFactory(cellFactory);

        salesDocumentsTableView.setOnMouseClicked(e -> {
            if (salesDocumentsTableView.getSelectionModel().getSelectedItem() != null) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }

    private <T extends SalesDocument> void loadSalesDocumentsData(Class<T> aClass) {
        List<T> all = SalesDocumentRepository.findAll(aClass);
        salesDocumentsTableView.setItems(FXCollections.observableArrayList());
        all.forEach(salesDocument -> salesDocumentsTableView.getItems().add(salesDocument));

        observableList = salesDocumentsTableView.getItems();

        filteredList = new FilteredList<>(observableList);
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(salesDocumentsTableView.comparatorProperty());
        salesDocumentsTableView.setItems(sortedList);

        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void displaySuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Operation effectué avec success");
        alert.showAndWait();
    }

    private void displayErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Une erreur est survenue lors de l'opération.");
        alert.showAndWait();
    }

}
