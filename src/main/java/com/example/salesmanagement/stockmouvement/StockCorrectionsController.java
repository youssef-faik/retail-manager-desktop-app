package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.document.DocumentController;
import com.example.salesmanagement.product.Product;
import com.example.salesmanagement.product.ProductRepository;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StockCorrectionsController implements Initializable {
    public Button addProductButton, saveStockCorrectionButton;
    public TableView<StockCorrectionFormEntry> stockCorrectionItemEntryTableView;
    private StockCorrection stockCorrection;
    private ObservableList<Product> products;
    private ListStockMouvementsController listStockMouvementsController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        products = ProductRepository.findAll();
        stockCorrection = new StockCorrection();

        initDocumentItemsTableView();

        addProductButton.setOnAction(event -> {
            StockCorrectionFormEntry entry = new StockCorrectionFormEntry(products);
            stockCorrectionItemEntryTableView.getItems().add(entry);
        });

        saveStockCorrectionButton.setOnAction(e -> addStockCorrection());
    }

    private void addStockCorrection() {
        mapStockCorrection();

        Optional<StockCorrection> optionalDocument = StockCorrectionRepository.save(stockCorrection);

        if (optionalDocument.isPresent()) {
            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }


    private void mapStockCorrection() {
        stockCorrectionItemEntryTableView.getItems().forEach(item -> {
            StockCorrectionItem stockCorrectionItem = item.getStockCorrectionItem();
            stockCorrectionItem.setQuantity(item.getQuantityToUpdate());
            stockCorrectionItem.setCorrectionType(item.getStockCorrectionTypeComboBox().getSelectionModel().getSelectedItem());
            stockCorrection.addItem(stockCorrectionItem);
        });
    }

    private void initDocumentItemsTableView() {
        TableColumn<StockCorrectionFormEntry, String> productColumn = new TableColumn<>("Produit");
        TableColumn<StockCorrectionFormEntry, String> currentQuantityColumn = new TableColumn<>("La quantité actuelle");
        TableColumn<StockCorrectionFormEntry, String> correctionTypeColumn = new TableColumn<>("Type correction");
        TableColumn<StockCorrectionFormEntry, String> quantityToUpdateColumn = new TableColumn<>("Quantité");
        TableColumn<StockCorrectionFormEntry, String> newQuantityColumn = new TableColumn<>("Nouvelle Quantité");
        TableColumn<StockCorrectionFormEntry, String> actionColumn = new TableColumn<>("Action");

        stockCorrectionItemEntryTableView.setEditable(true);
        stockCorrectionItemEntryTableView.getColumns().addAll(
                productColumn,
                currentQuantityColumn,
                correctionTypeColumn,
                quantityToUpdateColumn,
                newQuantityColumn,
                actionColumn
        );

        productColumn.setCellValueFactory(new PropertyValueFactory<>("productComboBox"));
        correctionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("stockCorrectionTypeComboBox"));
        currentQuantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Integer.toString(cellData.getValue().getCurrentQuantity())));
        quantityToUpdateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Integer.toString(cellData.getValue().getQuantityToUpdate())));
        newQuantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Integer.toString(cellData.getValue().getNewQuantity())));

        productColumn.setReorderable(false);

        currentQuantityColumn.setMinWidth(120);
        currentQuantityColumn.setMaxWidth(120);
        currentQuantityColumn.setResizable(false);
        currentQuantityColumn.setReorderable(false);

        correctionTypeColumn.setMinWidth(100);
        correctionTypeColumn.setMaxWidth(100);
        correctionTypeColumn.setResizable(false);
        correctionTypeColumn.setReorderable(false);

        quantityToUpdateColumn.setMinWidth(80);
        quantityToUpdateColumn.setMaxWidth(80);
        quantityToUpdateColumn.setResizable(false);
        quantityToUpdateColumn.setReorderable(false);

        newQuantityColumn.setMinWidth(110);
        newQuantityColumn.setMaxWidth(110);
        newQuantityColumn.setResizable(false);
        newQuantityColumn.setReorderable(false);

        actionColumn.setMinWidth(100);
        actionColumn.setMaxWidth(100);
        actionColumn.setReorderable(false);

        // delete
        Callback<TableColumn<StockCorrectionFormEntry, String>, TableCell<StockCorrectionFormEntry, String>> cellFactory =
                (TableColumn<StockCorrectionFormEntry, String> param) -> {
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
                                        StockCorrectionFormEntry rowItem = (StockCorrectionFormEntry) tableRow.getItem();
                                        stockCorrectionItemEntryTableView.getItems().remove(rowItem);

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

        // edit quantity
        quantityToUpdateColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        quantityToUpdateColumn.setOnEditCommit(event -> {
            StockCorrectionFormEntry entry = event.getTableView().getItems().get(event.getTablePosition().getRow());
            try {
                int quantityToUpdate = Integer.parseInt(event.getNewValue());
                entry.setQuantityToUpdate(quantityToUpdate);

                if (entry.getStockCorrectionTypeComboBox().getSelectionModel().getSelectedItem() == StockCorrectionType.POSITIVE) {
                    entry.setNewQuantity(entry.getCurrentQuantity() + quantityToUpdate);
                } else {
                    entry.setNewQuantity(entry.getCurrentQuantity() - quantityToUpdate);
                }

                stockCorrectionItemEntryTableView.refresh();
            } catch (NumberFormatException e) {
                // Handle the case where the input is not a valid integer
                stockCorrectionItemEntryTableView.refresh();
                System.out.println("Invalid input: " + event.getNewValue());
                displayErrorAlert("valeur de quantité incorrecte: " + event.getNewValue());
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

    public void setListStockMovementsController(ListStockMouvementsController listStockMouvementsController) {
        this.listStockMouvementsController = listStockMouvementsController;
    }

}
