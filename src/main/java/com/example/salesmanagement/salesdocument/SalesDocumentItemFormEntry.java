package com.example.salesmanagement.salesdocument;

import com.example.salesmanagement.product.Product;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableRow;

import java.math.BigDecimal;

public class SalesDocumentItemFormEntry {
    private ComboBox<Product> productComboBox = new ComboBox<>();
    private SalesDocumentItem salesDocumentItem;
    private SalesDocumentController salesDocumentController;

    public SalesDocumentItemFormEntry() {
    }

    public SalesDocumentItemFormEntry(
            ObservableList<Product> items,
            SalesDocumentController salesDocumentController) {
        this.salesDocumentItem = new SalesDocumentItem();
        this.salesDocumentController = salesDocumentController;
        initProductsComboBox(items);
    }

    public SalesDocumentItemFormEntry(ObservableList<Product> items,
                                      SalesDocumentController salesDocumentController,
                                      SalesDocumentItem salesDocumentItem) {
        initProductsComboBox(items);
        getProductComboBox().setValue(salesDocumentItem.getProduct());

        this.salesDocumentController = salesDocumentController;
        this.salesDocumentItem = salesDocumentItem;
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
            this.salesDocumentItem.setProduct(product);
            this.salesDocumentItem.setQuantity(1);
            this.salesDocumentItem.setUnitPriceExcludingTaxes(product.getSellingPriceExcludingTax());

            // find new salesdocument item totals
            BigDecimal priceExcludingTaxes = product.getSellingPriceExcludingTax().multiply(BigDecimal.valueOf(this.salesDocumentItem.getQuantity()));
            BigDecimal taxes = priceExcludingTaxes.multiply(product.getTaxRate().getValue().divide(BigDecimal.valueOf(100)));
            BigDecimal priceIncludingTaxes = priceExcludingTaxes.add(taxes);

            // set new totals for InvoiceItem
            this.salesDocumentItem.setTotalExcludingTaxes(priceExcludingTaxes);
            this.salesDocumentItem.setTotalIncludingTaxes(priceIncludingTaxes);
            this.salesDocumentItem.setTotalTaxes(taxes);

            TableRow parent = (TableRow) productComboBox.getParent().getParent();
            parent.getTableView().refresh();

            salesDocumentController.updateTotals();
        });
    }

    public SalesDocumentItem getInvoiceItem() {
        return salesDocumentItem;
    }

    public ComboBox<Product> getProductComboBox() {
        return productComboBox;
    }

    public int getQuantity() {
        return this.salesDocumentItem.getQuantity();
    }

    public void setQuantity(int quantity) {
        this.salesDocumentItem.setQuantity(quantity);
    }

    public BigDecimal getUnitPriceExcludingTaxes() {
        return this.salesDocumentItem.getUnitPriceExcludingTaxes();
    }

    public void setUnitPriceExcludingTaxes(BigDecimal unitPriceExcludingTaxes) {
        this.salesDocumentItem.setUnitPriceExcludingTaxes(unitPriceExcludingTaxes);
    }

    public BigDecimal getTaxRate() {
        if (salesDocumentItem.getProduct() == null
                || salesDocumentItem.getProduct().getTaxRate() == null) {
            return BigDecimal.ZERO;
        }

        return salesDocumentItem.getProduct().getTaxRate().getValue().divide(BigDecimal.valueOf(100));
    }

    public BigDecimal getTotalIncludingTaxes() {
        return this.salesDocumentItem.getTotalIncludingTaxes();
    }

    public void setTotalIncludingTaxes(BigDecimal totalIncludingTaxes) {
        this.salesDocumentItem.setTotalIncludingTaxes(totalIncludingTaxes);
    }

    public BigDecimal getTotalExcludingTaxes() {
        return this.salesDocumentItem.getTotalExcludingTaxes();
    }

    public void setTotalExcludingTaxes(BigDecimal totalExcludingTaxes) {
        this.salesDocumentItem.setTotalExcludingTaxes(totalExcludingTaxes);
    }

    public BigDecimal getTotalTaxes() {
        return this.salesDocumentItem.getTotalTaxes();
    }

    public void setTotalTaxes(BigDecimal totalTaxes) {
        this.salesDocumentItem.setTotalTaxes(totalTaxes);
    }

    private static class ProductComboCell extends ListCell<Product> {
        @Override
        protected void updateItem(Product product, boolean bln) {
            super.updateItem(product, bln);
            setText(product != null ? product.getName() : null);
        }
    }
}
