package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.document.CreditInvoice;
import com.example.salesmanagement.document.DeliveryNote;
import com.example.salesmanagement.document.Invoice;
import com.example.salesmanagement.document.PurchaseDeliveryNote;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListStockMouvementsController implements Initializable {
    private static final int ROWS_PER_PAGE = 17;
    @FXML
    public TableView<StockMovement> stockMovementTableView;
    FilteredList<StockMovement> filteredList;
    SortedList<StockMovement> sortedList;
    ObservableList<StockMovement> observableList;
    @FXML
    private Button deleteButton, updateButton, newButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private Pagination pagination;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

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
        newButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));
        ((Text) newButton.getGraphic()).setFill(Color.WHITE);

        initStockMovementsTableView();
        refreshStockMovementsTable();

        int dataSize = (stockMovementTableView.getItems().size() / ROWS_PER_PAGE) + 1;
        pagination.setPageCount(dataSize);
        pagination.setPageFactory(this::createPage);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(stockMouvement -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return stockMouvement.getProduct().getName().toLowerCase().contains(lowerCaseFilter)
                        || stockMouvement.getDateTime().format(DateTimeFormatter.ofPattern("yyyy MMM dd - HH:mm:ss")).toLowerCase().contains(lowerCaseFilter);

            });

            // Update pagination after filtering
            int pageCount = (filteredList.size() / ROWS_PER_PAGE) + 1;
            pagination.setPageCount(pageCount);
            // Reset to first page after filter change
            pagination.setCurrentPageIndex(0);
            // Update the table view with the new first page
            createPage(0);
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
        StockMovement selectedStockMovement = stockMovementTableView.getSelectionModel().getSelectedItem();
        if (selectedStockMovement != null) {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-stock-correction.fxml"));
            Parent root = fxmlLoader.load();

            StockCorrectionController stockCorrectionController = fxmlLoader.getController();
            stockCorrectionController.setStockMovement(selectedStockMovement);
            stockCorrectionController.setStockMovementTableView(stockMovementTableView);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            stockMovementTableView.getSelectionModel().clearSelection();
        }
    }

    public void deleteStockMouvement() {
        StockMovement selectedStockMovement = stockMovementTableView.getSelectionModel().getSelectedItem();
        if (selectedStockMovement != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer cet enregistrement?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (StockMovementRepository.deleteById(selectedStockMovement.getId())) {
                    refreshStockMovementsTable();
                    updateButton.setDisable(true);
                    deleteButton.setDisable(true);

                    displaySuccessAlert();
                } else {
                    displayErrorAlert();
                }
            }
        }
    }

    private void initStockMovementsTableView() {
        TableColumn<StockMovement, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<StockMovement, String> movementTypeColumn = new TableColumn<>("Type movement");
        TableColumn<StockMovement, String> productColumn = new TableColumn<>("Produit");
        TableColumn<StockMovement, String> quantityColumn = new TableColumn<>("Quantité");
        TableColumn<StockMovement, String> dateTimeColumn = new TableColumn<>("Date");
        TableColumn<StockMovement, String> movementSourceColumn = new TableColumn<>("Source movement");


        stockMovementTableView.getColumns().addAll(
                idColumn,
                productColumn,
                movementTypeColumn,
                quantityColumn,
                dateTimeColumn,
                movementSourceColumn
        );

        stockMovementTableView.setOnMouseClicked(e -> {
            if (stockMovementTableView.getSelectionModel().getSelectedItem() != null
                    && stockMovementTableView.getSelectionModel().getSelectedItem().getMovementSource() instanceof StockCorrectionBasedMovementSource
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
            MovementSource movementSource = cellData.getValue().getMovementSource();

            String initialValue = "";
            if (movementSource instanceof DocumentBasedMovementSource documentBasedMouvementSource
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

            if (movementSource instanceof StockCorrectionBasedMovementSource stockCorrectionBasedMovementSource
                    && stockCorrectionBasedMovementSource.getSource() != null) {
                initialValue = "Correction de stock ref n° : " + stockCorrectionBasedMovementSource.getSource().getId();
            }

            return new SimpleStringProperty(initialValue);
        });

        stockMovementTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void refreshStockMovementsTable() {
        stockMovementTableView.setItems(FXCollections.observableArrayList(StockMovementRepository.findAll()));

        observableList = stockMovementTableView.getItems();

        filteredList = new FilteredList<>(observableList);
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(stockMovementTableView.comparatorProperty());
        stockMovementTableView.setItems(sortedList);

        // Update pagination
        int pageCount = (filteredList.size() / ROWS_PER_PAGE) + 1;
        pagination.setPageCount(pageCount);

        stockMovementTableView.refresh();
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

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredList.size());

        List<StockMovement> subbedList = filteredList.subList(fromIndex, toIndex);
        stockMovementTableView.setItems(FXCollections.observableArrayList(subbedList));
        return stockMovementTableView;
    }
}
