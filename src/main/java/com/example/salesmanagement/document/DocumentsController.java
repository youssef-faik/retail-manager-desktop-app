package com.example.salesmanagement.document;

import com.example.salesmanagement.report.DocumentReportManager;
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
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import net.sf.jasperreports.engine.JRException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentsController implements Initializable {
    Class<? extends Document> formClass;
    FilteredList<Document> filteredList;
    SortedList<Document> sortedList;
    ObservableList<Document> observableList;

    @FXML
    private TableView<Document> documentsTableView;

    @FXML
    private Button deleteButton, updateButton, newButton;

    @FXML
    private ComboBox<Pair<Class<? extends Document>, String>> docsListComboBox;

    @FXML
    private TextField searchTextField;

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

        updateButton.setEffect(dropShadow);
        deleteButton.setEffect(dropShadow);
        newButton.setEffect(dropShadow);
        newButton.setTextFill(Color.color(1, 1, 1));
        newButton.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(3.0), null)));
        ((Text) newButton.getGraphic()).setFill(Color.WHITE);

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        docsListComboBox.setCellFactory(x -> new DocumentComboCell());
        docsListComboBox.setButtonCell(new DocumentComboCell());


        docsListComboBox.getItems().addAll(
                new Pair<>(PurchaseOrder.class, "Bons de commande"),
                new Pair<>(PurchaseDeliveryNote.class, "Bons de réception"),
                new Pair<>(Quotation.class, "Devis"),
                new Pair<>(DeliveryNote.class, "Bons de livraison"),
                new Pair<>(Invoice.class, "Factures"),
                new Pair<>(CreditInvoice.class, "Factures avoir")
        );

        docsListComboBox.setOnAction(event -> load(docsListComboBox.getSelectionModel().getSelectedItem().getKey()));

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

                if (salesDocument instanceof SalesDocument document) {
                    return document.getClient().getName().toLowerCase().contains(lowerCaseFilter);
                } else {
                    return ((PurchaseDocument) salesDocument).getSupplier().getName().toLowerCase().contains(lowerCaseFilter);
                }

            });
        });
    }

    private void load(Class<? extends Document> documentClass) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("documents.fxml"));

        VBox pane = null;
        try {
            pane = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DocumentsController documentController = fxmlLoader.getController();
        documentController.setFormClass(documentClass);

        BorderPane borderPane = (BorderPane) documentsTableView.getScene().getRoot();
        borderPane.setCenter(pane);
    }

    public void addDocument(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(formClass);

        Button button = (Button) actionEvent.getSource();
        BorderPane borderPane = (BorderPane) button.getScene().getRoot();
        borderPane.setCenter(pane);
    }

    public void updateDocument() throws IOException {
        Document selectedDocument = documentsTableView.getSelectionModel().getSelectedItem();

        if (selectedDocument != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-document.fxml"));
            Parent root = fxmlLoader.load();

            DocumentController documentController = fxmlLoader.getController();

            Document salesDocument = DocumentRepository.findById(selectedDocument.getId()).orElseThrow();
            documentController.setDocumentType(formClass);
            documentController.initDocumentUpdateForm(salesDocument);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            stage.showAndWait();

            loadDocumentsData(formClass);
        }
    }

    public void deleteDocument() {
        Document selectedDocument = documentsTableView.getSelectionModel().getSelectedItem();

        if (selectedDocument != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer ce document?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (DocumentRepository.deleteById(selectedDocument.getId())) {
                    loadDocumentsData(formClass);
                    displaySuccessAlert();
                } else {
                    displayErrorAlert();
                }
            }

            updateButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    public void setFormClass(Class<? extends Document> formClass) {
        this.formClass = formClass;

        initDocumentsTableView(formClass);
        loadDocumentsData(formClass);

        docsListComboBox.setValue(docsListComboBox.getItems().stream().filter(o -> o.getKey() == formClass).findFirst().get());
    }

    private <T extends Document> void initDocumentsTableView(Class<T> aClass) {
        TableColumn<T, String> idColumn = new TableColumn<>("ID");
        TableColumn<T, String> referenceColumn = new TableColumn<>("Référence");
        TableColumn<T, String> issueDateColumn = new TableColumn<>("Date d'émission");
        TableColumn<T, String> totalIncludingTaxesColumn = new TableColumn<>("Total (TTC)");
        TableColumn<T, String> actionColumn = new TableColumn<>("Actions");
        TableColumn<T, String> statusColumn = new TableColumn<>("Status");
        TableColumn<T, String> clientColumn = null;
        TableColumn<T, String> paidAmountColumn = null;
        TableColumn<T, String> remainingAmountTaxesColumn = null;
        TableColumn<T, String> dueDateColumn = null;
        TableColumn<T, String> validUntilColumn = null;

        if (aClass == Quotation.class) {
            validUntilColumn = new TableColumn<>("Valide jusqu'au");
            validUntilColumn.setCellValueFactory(cellData -> new SimpleStringProperty(((Quotation) cellData.getValue()).getValidUntil() == null ? "N/A" : ((Quotation) cellData.getValue()).getValidUntil().toString()));
            clientColumn = new TableColumn<>("Client");
            clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(((Quotation) cellData.getValue()).getClient().getName()));
        }

        if (aClass == DeliveryNote.class) {
            clientColumn = new TableColumn<>("Client");
            clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(((DeliveryNote) cellData.getValue()).getClient().getName()));
        }

        if (aClass == Invoice.class) {
            paidAmountColumn = new TableColumn<>("Montant payé");
            remainingAmountTaxesColumn = new TableColumn<>("Montant restant");
            dueDateColumn = new TableColumn<>("Date d'échéance");

            remainingAmountTaxesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTotalIncludingTaxes().subtract(((Invoice) cellData.getValue()).getPaidAmount()).toPlainString()));
            paidAmountColumn.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
            dueDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(((Invoice) cellData.getValue()).getDueDate() == null ? "N/A" : ((Invoice) cellData.getValue()).getDueDate().toString()));
            clientColumn = new TableColumn<>("Client");
            clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(((Invoice) cellData.getValue()).getClient().getName()));
        }

        if (aClass == CreditInvoice.class) {
            paidAmountColumn = new TableColumn<>("Montant payé");
            remainingAmountTaxesColumn = new TableColumn<>("Montant restant");

            paidAmountColumn.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
            remainingAmountTaxesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTotalIncludingTaxes().subtract(((CreditInvoice) cellData.getValue()).getPaidAmount()).toPlainString()));
            clientColumn = new TableColumn<>("Client");
            clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(((CreditInvoice) cellData.getValue()).getClient().getName()));
        }

        if (formClass == PurchaseOrder.class) {
            clientColumn = new TableColumn<>("Fournisseur");
            clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(((PurchaseOrder) cellData.getValue()).getSupplier().getName()));
        }

        if (formClass == PurchaseDeliveryNote.class) {
            clientColumn = new TableColumn<>("Fournisseur");
            clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(((PurchaseDeliveryNote) cellData.getValue()).getSupplier().getName()));
        }


        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        issueDateColumn.setCellValueFactory(new PropertyValueFactory<>("issueDate"));

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

        documentsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        documentsTableView.getColumns().addAll(
                (TableColumn<Document, ?>) idColumn,
                (TableColumn<Document, ?>) referenceColumn,
                (TableColumn<Document, ?>) clientColumn,
                (TableColumn<Document, ?>) issueDateColumn,
                (TableColumn<Document, ?>) statusColumn
        );

        if (aClass == Quotation.class) {
            documentsTableView.getColumns().addAll(
                    (TableColumn<Document, ?>) totalIncludingTaxesColumn,
                    (TableColumn<Document, ?>) validUntilColumn
            );
        }

        if (aClass == DeliveryNote.class) {
            documentsTableView.getColumns().addAll(
                    (TableColumn<Document, ?>) totalIncludingTaxesColumn
            );
        }
        if (aClass == Invoice.class) {
            documentsTableView.getColumns().addAll(
                    (TableColumn<Document, ?>) dueDateColumn,
                    (TableColumn<Document, ?>) totalIncludingTaxesColumn,
                    (TableColumn<Document, ?>) paidAmountColumn,
                    (TableColumn<Document, ?>) remainingAmountTaxesColumn
            );
        }

        if (aClass == CreditInvoice.class) {
            documentsTableView.getColumns().addAll(
                    (TableColumn<Document, ?>) totalIncludingTaxesColumn,
                    (TableColumn<Document, ?>) paidAmountColumn,
                    (TableColumn<Document, ?>) remainingAmountTaxesColumn
            );
        }


        documentsTableView.getColumns().addAll((TableColumn<Document, ?>) actionColumn);

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
                                        TableRow<Document> tableRow = (TableRow<Document>) printButton.getParent().getParent().getParent();
                                        Document selectedDocument = tableRow.getItem();

                                        Document document = DocumentRepository.findById(selectedDocument.getId()).orElseThrow();

                                        try {
                                            DocumentReportManager documentReportManager = new DocumentReportManager();
                                            documentReportManager.displayDocumentReport(document);
                                        } catch (JRException e) {
                                            throw new RuntimeException(e);
                                        }

                                    } catch (Exception ex) {
                                        Logger.getLogger(DocumentController.class.getName()).log(Level.SEVERE, null, ex);
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

        documentsTableView.setOnMouseClicked(e -> {
            if (documentsTableView.getSelectionModel().getSelectedItem() != null) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }

    private <T extends Document> void loadDocumentsData(Class<T> aClass) {
        List<T> all = DocumentRepository.findAll(aClass);
        documentsTableView.setItems(FXCollections.observableArrayList());
        all.forEach(document -> documentsTableView.getItems().add(document));

        observableList = documentsTableView.getItems();

        filteredList = new FilteredList<>(observableList);
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(documentsTableView.comparatorProperty());
        documentsTableView.setItems(sortedList);

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

    private static class DocumentComboCell extends ListCell<Pair<Class<? extends Document>, String>> {
        @Override
        protected void updateItem(Pair<Class<? extends Document>, String> pair, boolean bln) {
            super.updateItem(pair, bln);
            setText(pair != null ? pair.getValue() : null);
            setFont(Font.font(null, FontWeight.BOLD, 14));

        }
    }
}
