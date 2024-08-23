package com.example.salesmanagement.document;

import com.example.salesmanagement.product.Product;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableRow;

import java.math.BigDecimal;

public class DocumentItemFormEntry {
    private ComboBox<Product> productComboBox = new ComboBox<>();
    private DocumentItem documentItem;
    private DocumentController documentController;

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
                                 DocumentItem documentItem) {
        initProductsComboBox(items);
        getProductComboBox().setValue(documentItem.getProduct());

        this.documentController = documentController;
        this.documentItem = documentItem;
    }

    private void initProductsComboBox(ObservableList<Product> items) {
        this.productComboBox = new ComboBox<>(items);

        productComboBox.setCellFactory(x -> new ProductComboCell());
        productComboBox.setButtonCell(new ProductComboCell());
        productComboBox.setPromptText("Sélectionnez un produit");
        productComboBox.setMinWidth(350);

        productComboBox.setOnAction(event -> {
            Product product = productComboBox.getSelectionModel().getSelectedItem();
            // set new values for InvoiceItem
            this.documentItem.setProduct(product);
            this.documentItem.setQuantity(1);
            this.documentItem.setUnitPriceExcludingTaxes(product.getSellingPriceExcludingTax());

            // find new document item totals
            BigDecimal priceExcludingTaxes = product.getSellingPriceExcludingTax().multiply(BigDecimal.valueOf(this.documentItem.getQuantity()));
            BigDecimal taxes = priceExcludingTaxes.multiply(product.getTaxRate().getValue().divide(BigDecimal.valueOf(100)));
            BigDecimal priceIncludingTaxes = priceExcludingTaxes.add(taxes);

            // set new totals for InvoiceItem
            this.documentItem.setTotalExcludingTaxes(priceExcludingTaxes);
            this.documentItem.setTotalIncludingTaxes(priceIncludingTaxes);
            this.documentItem.setTotalTaxes(taxes);

            TableRow parent = (TableRow) productComboBox.getParent().getParent();
            parent.getTableView().refresh();

            documentController.updateTotals();
        });
    }

    public DocumentItem getInvoiceItem() {
        return documentItem;
    }

    public ComboBox<Product> getProductComboBox() {
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

    private static class ProductComboCell extends ListCell<Product> {
        @Override
        protected void updateItem(Product product, boolean bln) {
            super.updateItem(product, bln);
            setText(product != null ? product.getName() : null);
        }
    }
}
