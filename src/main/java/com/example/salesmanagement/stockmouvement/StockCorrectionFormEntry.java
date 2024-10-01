package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.product.Product;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.util.Objects;
import java.util.function.Function;

public class StockCorrectionFormEntry {
    private final Pair<String, Product> EMPTY_PRODUCT = new Pair<>(null, null);
    private final ComboBox<StockCorrectionType> stockCorrectionTypeComboBox = new ComboBox<>();
    private ComboBox<Pair<String, Product>> productComboBox = new ComboBox<>();
    private StockCorrectionItem stockCorrectionItem;
    private Integer newQuantity;
    private Integer quantityToUpdate;
    private boolean isChanging = false;
    private Product selectedProduct;
    private StockCorrectionsController stockCorrectionsController;


    public StockCorrectionFormEntry(ObservableList<Product> items,
                                    StockCorrectionsController stockCorrectionsController) {
        this.stockCorrectionsController = stockCorrectionsController;
        this.stockCorrectionItem = new StockCorrectionItem();
        newQuantity = quantityToUpdate = 0;

        initProductsComboBox(items);
        initStockCorrectionTypeComboBox();
    }

    private void initStockCorrectionTypeComboBox() {
        stockCorrectionTypeComboBox.getItems().addAll(StockCorrectionType.values());
        stockCorrectionTypeComboBox.getSelectionModel().selectFirst();

        stockCorrectionTypeComboBox.setOnAction(event -> {
            StockCorrectionType stockCorrectionType = stockCorrectionTypeComboBox.getSelectionModel().getSelectedItem();

            if (stockCorrectionType == StockCorrectionType.POSITIVE) {
                setNewQuantity(getCurrentQuantity() + quantityToUpdate);
            } else {
                setNewQuantity(getCurrentQuantity() - quantityToUpdate);
            }

            TableRow parent = (TableRow) productComboBox.getParent().getParent();
            parent.getTableView().refresh();
        });
    }

    private void initProductsComboBox(ObservableList<Product> items) {
        this.productComboBox = new ComboBox<>();
        productComboBox.setCellFactory(x -> new ProductComboCell());
        productComboBox.setButtonCell(new ProductComboCell());
        productComboBox.setMinWidth(300);

        productComboBox.getItems().add(EMPTY_PRODUCT);
        Function<Product, Pair<String, Product>> productObjectFunction = product -> new Pair<>(product.getName(), product);
        productComboBox.getItems().addAll(items.stream().map(productObjectFunction).toList());

        productComboBox.getSelectionModel().selectFirst();

        productComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (isChanging || Objects.equals(newValue, oldValue)) {
                return;
            }

            TableView<StockCorrectionFormEntry> tableView = ((TableRow) productComboBox.getParent().getParent()).getTableView();

            // check if the product was already selected
            if (newValue.getValue() != null) {
                for (StockCorrectionFormEntry stockCorrectionFormEntry : tableView.getItems()) {
                    if (stockCorrectionFormEntry.selectedProduct == newValue.getValue()) {
                        // Set the flag to true before changing the value
                        isChanging = true;

                        // Set the selected item back to its previous value
                        productComboBox.setValue(oldValue);

                        // Reset the flag after changing the value
                        isChanging = false;

                        displayErrorAlert("Le produit est déjà choisi");
                        return;
                    }
                }
            }

            selectedProduct = newValue.getValue();
            this.stockCorrectionItem.setProduct(selectedProduct);

            if (selectedProduct == null) {
                setCurrentQuantity(0);
                setQuantityToUpdate(0);
                setNewQuantity(0);

                tableView.refresh();
                stockCorrectionsController.validateForm();

                return;
            }

            setCurrentQuantity(selectedProduct.getQuantity());
            setQuantityToUpdate(0);
            setNewQuantity(getCurrentQuantity());
            stockCorrectionTypeComboBox.getSelectionModel().select(StockCorrectionType.POSITIVE);

            tableView.refresh();
            stockCorrectionsController.validateForm();
        });
    }

    public StockCorrectionItem getStockCorrectionItem() {
        return stockCorrectionItem;
    }

    public ComboBox<Pair<String, Product>> getProductComboBox() {
        return productComboBox;
    }

    public ComboBox<StockCorrectionType> getStockCorrectionTypeComboBox() {
        return stockCorrectionTypeComboBox;
    }

    public Integer getQuantityToUpdate() {
        return quantityToUpdate;
    }

    public void setQuantityToUpdate(Integer quantityToUpdate) {
        this.quantityToUpdate = quantityToUpdate;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public int getCurrentQuantity() {
        return this.stockCorrectionItem.getQuantity();
    }

    public void setCurrentQuantity(int quantity) {
        this.stockCorrectionItem.setQuantity(quantity);
    }

    private void displayErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class ProductComboCell extends ListCell<Pair<String, Product>> {
        @Override
        protected void updateItem(Pair<String, Product> pair, boolean bln) {
            super.updateItem(pair, bln);
            setText(pair == null ? null : (pair.getValue() != null ? pair.getValue().getName() : "Choisissez un produit"));
        }
    }
}
