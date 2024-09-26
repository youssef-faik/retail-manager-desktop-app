package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.product.Product;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableRow;

public class StockCorrectionFormEntry {
    private ComboBox<Product> productComboBox = new ComboBox<>();
    private ComboBox<StockCorrectionType> stockCorrectionTypeComboBox = new ComboBox<>();
    private StockCorrectionItem stockCorrectionItem;
    private Integer newQuantity;
    private Integer quantityToUpdate;


    public StockCorrectionFormEntry() {
    }

    public StockCorrectionFormEntry(ObservableList<Product> items) {
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
        this.productComboBox = new ComboBox<>(items);

        productComboBox.setCellFactory(x -> new ProductComboCell());
        productComboBox.setButtonCell(new ProductComboCell());
        productComboBox.setPromptText("Sélectionnez un produit");
        productComboBox.setMinWidth(300);

        productComboBox.setOnAction(event -> {
            Product product = productComboBox.getSelectionModel().getSelectedItem();

            // set new values for InvoiceItem
            this.stockCorrectionItem.setProduct(product);
            setCurrentQuantity(product.getQuantity());
            setQuantityToUpdate(0);
            stockCorrectionTypeComboBox.getSelectionModel().select(StockCorrectionType.POSITIVE);
            setNewQuantity(getCurrentQuantity());

            TableRow parent = (TableRow) productComboBox.getParent().getParent();
            parent.getTableView().refresh();
        });
    }

    public StockCorrectionItem getStockCorrectionItem() {
        return stockCorrectionItem;
    }

    public ComboBox<Product> getProductComboBox() {
        return productComboBox;
    }

    public ComboBox<StockCorrectionType> getStockCorrectionTypeComboBox() {
        return stockCorrectionTypeComboBox;
    }

    public void setStockCorrectionTypeComboBox(ComboBox<StockCorrectionType> stockCorrectionTypeComboBox) {
        this.stockCorrectionTypeComboBox = stockCorrectionTypeComboBox;
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

    private static class ProductComboCell extends ListCell<Product> {
        @Override
        protected void updateItem(Product product, boolean bln) {
            super.updateItem(product, bln);
            setText(product != null ? product.getName() : null);
        }
    }
}
