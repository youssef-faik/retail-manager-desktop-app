package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.document.CreditInvoice;
import com.example.salesmanagement.document.DeliveryNote;
import com.example.salesmanagement.document.Invoice;
import com.example.salesmanagement.document.PurchaseDeliveryNote;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListStockMouvementsController implements Initializable {
    @FXML
    public TableView<StockMouvement> stockMouvementTableView;
    FilteredList<StockMouvement> filteredList;
    SortedList<StockMouvement> sortedList;
    ObservableList<StockMouvement> observableList;
    @FXML
    private Button deleteButton, updateButton;
    @FXML
    private TextField searchTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initStockMovementsTableView();
        refreshStockMovementsTable();

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(stockMouvement -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return stockMouvement.getProduct().getName().toLowerCase().contains(lowerCaseFilter)
                        || stockMouvement.getDateTime().format(DateTimeFormatter.ofPattern("yyyy MMM dd - HH:mm:ss")).toLowerCase().contains(lowerCaseFilter);

            });
        });

    }

    public void addStockMouvement() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-stock-corrections.fxml"));
        Parent root = fxmlLoader.load();

        StockCorrectionsController stockCorrectionsController = fxmlLoader.getController();
        stockCorrectionsController.setListStockMovementsController(this);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
        refreshStockMovementsTable();
    }

    public void updateStockMouvement() throws IOException {
        StockMouvement selectedStockMouvement = stockMouvementTableView.getSelectionModel().getSelectedItem();
        if (selectedStockMouvement != null) {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-stock-correction.fxml"));
            Parent root = fxmlLoader.load();

            StockCorrectionController stockCorrectionController = fxmlLoader.getController();
            stockCorrectionController.setStockMovement(selectedStockMouvement);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            refreshStockMovementsTable();
        }
    }

    public void deleteStockMouvement() {
        StockMouvement selectedStockMouvement = stockMouvementTableView.getSelectionModel().getSelectedItem();
        if (selectedStockMouvement != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer cet enregistrement?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (StockMovementRepository.deleteById(selectedStockMouvement.getId())) {
                    stockMouvementTableView.getItems().remove(selectedStockMouvement);
                    displaySuccessAlert();

                    updateButton.setDisable(true);
                    deleteButton.setDisable(true);
                } else {
                    displayErrorAlert();
                }
            }
        }
    }

    private void initStockMovementsTableView() {
        TableColumn<StockMouvement, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<StockMouvement, String> movementTypeColumn = new TableColumn<>("Type movement");
        TableColumn<StockMouvement, String> productColumn = new TableColumn<>("Produit");
        TableColumn<StockMouvement, String> quantityColumn = new TableColumn<>("Quantité");
        TableColumn<StockMouvement, String> dateTimeColumn = new TableColumn<>("Date");
        TableColumn<StockMouvement, String> movementSourceColumn = new TableColumn<>("Source movement");


        stockMouvementTableView.getColumns().addAll(
                idColumn,
                productColumn,
                movementTypeColumn,
                quantityColumn,
                dateTimeColumn,
                movementSourceColumn
        );

        stockMouvementTableView.setOnMouseClicked(e -> {
            if (stockMouvementTableView.getSelectionModel().getSelectedItem() != null
                    && stockMouvementTableView.getSelectionModel().getSelectedItem().getMovementSource() instanceof StockCorrectionBasedMouvementSource
            ) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        idColumn.setVisible(false);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        movementTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(switch (cellData.getValue().getMovementType()) {
            case STOCK_ENTRY -> "Entrée de stock";
            case OUT_OF_STOCK -> "Sortie de stock";
        }));

        productColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Integer.toString(cellData.getValue().getQuantity())));
        dateTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateTime().format(DateTimeFormatter.ofPattern("yyyy MMM dd - HH:mm:ss"))));
        movementSourceColumn.setCellValueFactory(cellData -> {
            MouvementSource mouvementSource = cellData.getValue().getMovementSource();

            String initialValue = "";
            if (mouvementSource instanceof DocumentBasedMouvementSource documentBasedMouvementSource
                    && documentBasedMouvementSource.getSource() != null) {
                if (documentBasedMouvementSource.getSource() instanceof PurchaseDeliveryNote purchaseDeliveryNote) {
                    initialValue = "Bon de réception ref n° : " + purchaseDeliveryNote.getReference();
                } else if (documentBasedMouvementSource.getSource() instanceof DeliveryNote deliveryNote) {
                    initialValue = "Bon de livraison ref n° : " + deliveryNote.getReference();
                } else if (documentBasedMouvementSource.getSource() instanceof Invoice invoice) {
                    initialValue = "Facture ref n° : " + invoice.getReference();
                } else if (documentBasedMouvementSource.getSource() instanceof CreditInvoice creditInvoice) {
                    initialValue = "Facture d'avoir ref n° : " + creditInvoice.getReference();
                }
            }

            if (mouvementSource instanceof StockCorrectionBasedMouvementSource stockCorrectionBasedMovementSource
                    && stockCorrectionBasedMovementSource.getSource() != null) {
                initialValue = "Correction de stock ref n° : " + stockCorrectionBasedMovementSource.getSource().getId();
            }

            return new SimpleStringProperty(initialValue);
        });

        stockMouvementTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void refreshStockMovementsTable() {
        stockMouvementTableView.getItems().addAll(StockMovementRepository.findAll());

        observableList = stockMouvementTableView.getItems();

        filteredList = new FilteredList<>(observableList);
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(stockMouvementTableView.comparatorProperty());
        stockMouvementTableView.setItems(sortedList);


        stockMouvementTableView.refresh();
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
