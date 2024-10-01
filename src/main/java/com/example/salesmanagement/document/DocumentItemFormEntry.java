package com.example.salesmanagement.document;

import com.example.salesmanagement.product.Product;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;

public class DocumentItemFormEntry {
    private final Pair<String, Product> EMPTY_PRODUCT = new Pair<>(null, null);
    private ComboBox<Pair<String, Product>> productComboBox = new ComboBox<>();
    private DocumentItem documentItem;
    private DocumentController documentController;
    private Product selectedProduct;
    private boolean isChanging = false;
    private TableView<DocumentItemFormEntry> tableView;


    public DocumentItemFormEntry() {
    }

    public DocumentItemFormEntry(
            ObservableList<Product> items,
            DocumentController documentController) {
        this.documentItem = new DocumentItem();
        this.documentController = documentController;
        initProductsComboBox(items);
    }

    public DocumentItemFormEntry(ObservableList<Product> items,
                                 DocumentController documentController,
                                 DocumentItem documentItem,
                                 TableView<DocumentItemFormEntry> tableView) {
        this.tableView = tableView;
        this.documentController = documentController;
        this.documentItem = documentItem;

        initProductsComboBox(items);

        Product product = documentItem.getProduct();

        productComboBox.getItems()
                .stream()
                .filter(stringProductPair -> stringProductPair.getValue() != null && stringProductPair.getValue().equals(product))
                .findFirst()
                .ifPresentOrElse(
                        stringProductPair -> productComboBox.setValue(stringProductPair),
                        () -> {
                            throw new RuntimeException("Product does not exist");
                        }
                );
    }

    private void initProductsComboBox(ObservableList<Product> items) {
        this.productComboBox = new ComboBox<>();
        productComboBox.setCellFactory(x -> new ProductComboCell());
        productComboBox.setButtonCell(new ProductComboCell());
        productComboBox.setMinWidth(350);

        productComboBox.getItems().add(EMPTY_PRODUCT);
        Function<Product, Pair<String, Product>> productObjectFunction = product -> new Pair<>(product.getName(), product);
        productComboBox.getItems().addAll(items.stream().map(productObjectFunction).toList());

        productComboBox.getSelectionModel().selectFirst();

        productComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (isChanging || Objects.equals(newValue, oldValue)) {
                return;
            }

            tableView = tableView != null ? tableView : ((TableRow) productComboBox.getParent().getParent()).getTableView();

            // check if the product was already selected
            if (newValue.getValue() != null) {
                for (DocumentItemFormEntry documentItemFormEntry : tableView.getItems()) {
                    if (documentItemFormEntry.selectedProduct == newValue.getValue()) {
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
            this.documentItem.setProduct(selectedProduct);

            if (selectedProduct == null) {
                this.documentItem.setQuantity(0);
                this.documentItem.setUnitPriceExcludingTaxes(BigDecimal.ZERO);

                this.documentItem.setTotalExcludingTaxes(BigDecimal.ZERO);
                this.documentItem.setTotalIncludingTaxes(BigDecimal.ZERO);
                this.documentItem.setTotalTaxes(BigDecimal.ZERO);

                tableView.refresh();
                documentController.updateTotals();

                return;
            }

            // set new values for InvoiceItem
            this.documentItem.setQuantity(1);
            this.documentItem.setUnitPriceExcludingTaxes(selectedProduct.getSellingPriceExcludingTax());

            // find new document item totals
            BigDecimal priceExcludingTaxes = selectedProduct.getSellingPriceExcludingTax().multiply(BigDecimal.valueOf(this.documentItem.getQuantity()));
            BigDecimal taxes = priceExcludingTaxes.multiply(selectedProduct.getTaxRate().getValue().divide(BigDecimal.valueOf(100)));
            BigDecimal priceIncludingTaxes = priceExcludingTaxes.add(taxes);

            // set new totals for InvoiceItem
            this.documentItem.setTotalExcludingTaxes(priceExcludingTaxes);
            this.documentItem.setTotalIncludingTaxes(priceIncludingTaxes);
            this.documentItem.setTotalTaxes(taxes);

            tableView.refresh();

            documentController.updateTotals();
        });
    }

    public DocumentItem getInvoiceItem() {
        return documentItem;
    }

    public ComboBox<Pair<String, Product>> getProductComboBox() {
        return productComboBox;
    }

    public int getQuantity() {
        return this.documentItem.getQuantity();
    }

    public void setQuantity(int quantity) {
        this.documentItem.setQuantity(quantity);
    }

    public BigDecimal getUnitPriceExcludingTaxes() {
        return this.documentItem.getUnitPriceExcludingTaxes();
    }

    public void setUnitPriceExcludingTaxes(BigDecimal unitPriceExcludingTaxes) {
        this.documentItem.setUnitPriceExcludingTaxes(unitPriceExcludingTaxes);
    }

    public BigDecimal getTaxRate() {
        if (documentItem.getProduct() == null
                || documentItem.getProduct().getTaxRate() == null) {
            return BigDecimal.ZERO;
        }

        return documentItem.getProduct().getTaxRate().getValue().divide(BigDecimal.valueOf(100));
    }

    public BigDecimal getTotalIncludingTaxes() {
        return this.documentItem.getTotalIncludingTaxes();
    }

    public void setTotalIncludingTaxes(BigDecimal totalIncludingTaxes) {
        this.documentItem.setTotalIncludingTaxes(totalIncludingTaxes);
    }

    public BigDecimal getTotalExcludingTaxes() {
        return this.documentItem.getTotalExcludingTaxes();
    }

    public void setTotalExcludingTaxes(BigDecimal totalExcludingTaxes) {
        this.documentItem.setTotalExcludingTaxes(totalExcludingTaxes);
    }

    public BigDecimal getTotalTaxes() {
        return this.documentItem.getTotalTaxes();
    }

    public void setTotalTaxes(BigDecimal totalTaxes) {
        this.documentItem.setTotalTaxes(totalTaxes);
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
