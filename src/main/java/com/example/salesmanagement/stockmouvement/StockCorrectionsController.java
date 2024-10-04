package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.document.DocumentController;
import com.example.salesmanagement.product.Product;
import com.example.salesmanagement.product.ProductRepository;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StockCorrectionsController implements Initializable {
    public Button addProductButton, saveButton;
    public TableView<StockCorrectionFormEntry> stockCorrectionItemEntryTableView;
    private StockCorrection stockCorrection;
    private ObservableList<Product> products;
    private ListStockMouvementsController listStockMouvementsController;

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

        saveButton.setEffect(dropShadow);
        saveButton.setTextFill(Color.color(1, 1, 1));
        saveButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));
        saveButton.setDisable(true);

        stockCorrectionItemEntryTableView.getItems().addListener((ListChangeListener<StockCorrectionFormEntry>) c -> {
            c.next();
            saveButton.setDisable(!isItemTableViewValide());
        });

        products = ProductRepository.findAll();
        stockCorrection = new StockCorrection();

        initDocumentItemsTableView();

        addProductButton.setOnAction(event -> {
            StockCorrectionFormEntry entry = new StockCorrectionFormEntry(products, this);
            stockCorrectionItemEntryTableView.getItems().add(entry);
        });

        saveButton.setOnAction(e -> addStockCorrection());
    }

    private void addStockCorrection() {
        mapStockCorrection();

        Optional<StockCorrection> optionalDocument = StockCorrectionRepository.save(stockCorrection);

        if (optionalDocument.isPresent()) {
            saveButton.setDisable(true);
            addProductButton.setDisable(true);
            stockCorrectionItemEntryTableView.setDisable(true);
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

            String newValueString = event.getNewValue().trim();

            if (newValueString.isBlank()) {
                stockCorrectionItemEntryTableView.refresh();
                displayErrorAlert("Quantité est obligatoire");
                return;
            }

            int quantity;

            try {
                quantity = Integer.parseInt(newValueString);
            } catch (NumberFormatException e) {
                stockCorrectionItemEntryTableView.refresh();
                displayErrorAlert("La quantité ne doit contenir que des chiffres");
                return;
            }

            if (quantity < 0 || quantity > 9_999_999) {
                stockCorrectionItemEntryTableView.refresh();
                displayErrorAlert("la valeur de la quantité doit être comprise entre 0 et 9,999,999");
                return;
            }

            entry.setQuantityToUpdate(quantity);

            if (entry.getStockCorrectionTypeComboBox().getSelectionModel().getSelectedItem() == StockCorrectionType.POSITIVE) {
                entry.setNewQuantity(entry.getCurrentQuantity() + quantity);
            } else {
                entry.setNewQuantity(entry.getCurrentQuantity() - quantity);
            }

            stockCorrectionItemEntryTableView.refresh();
        });
    }

    private boolean isItemTableViewValide() {
        if (stockCorrectionItemEntryTableView.getItems().isEmpty()) {
            return false;
        }

        for (StockCorrectionFormEntry entry : stockCorrectionItemEntryTableView.getItems()) {
            if (entry.getProductComboBox().getSelectionModel().getSelectedItem().getValue() == null) {
                return false;
            }
        }
        return true;
    }

    public void validateForm() {
        saveButton.setDisable(!isItemTableViewValide());
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
