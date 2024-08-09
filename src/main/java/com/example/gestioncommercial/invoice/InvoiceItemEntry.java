package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.product.Product;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableRow;

import java.math.BigDecimal;

public class InvoiceItemEntry {
    private ComboBox<Product> productComboBox = new ComboBox<>();
    private InvoiceItem invoiceItem;
    private InvoiceController invoiceController;

    public InvoiceItemEntry() {
    }

    public InvoiceItemEntry(
            ObservableList<Product> items,
            InvoiceController invoiceController) {
        this.invoiceItem = new InvoiceItem();
        this.invoiceController = invoiceController;
        initProductsComboBox(items);
    }

    public InvoiceItemEntry(ObservableList<Product> items,
                            InvoiceController invoiceController,
                            InvoiceItem invoiceItem) {
        initProductsComboBox(items);
        getProductComboBox().setValue(invoiceItem.getProduct());

        this.invoiceController = invoiceController;
        this.invoiceItem = invoiceItem;
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
            this.invoiceItem.setProduct(product);
            this.invoiceItem.setQuantity(1);
            this.invoiceItem.setUnitPriceExcludingTaxes(product.getSellingPriceExcludingTax());

            // find new invoice item totals
            BigDecimal priceExcludingTaxes = product.getSellingPriceExcludingTax().multiply(BigDecimal.valueOf(this.invoiceItem.getQuantity()));
            BigDecimal taxes = priceExcludingTaxes.multiply(product.getTaxRate());
            BigDecimal priceIncludingTaxes = priceExcludingTaxes.add(taxes);

            // set new totals for InvoiceItem
            this.invoiceItem.setTotalExcludingTaxes(priceExcludingTaxes);
            this.invoiceItem.setTotalIncludingTaxes(priceIncludingTaxes);
            this.invoiceItem.setTotalTaxes(taxes);

            TableRow parent = (TableRow) productComboBox.getParent().getParent();
            parent.getTableView().refresh();

            invoiceController.updateInvoiceTotals();
        });
    }

    public InvoiceItem getInvoiceItem() {
        return invoiceItem;
    }

    public ComboBox<Product> getProductComboBox() {
        return productComboBox;
    }

    public int getQuantity() {
        return this.invoiceItem.getQuantity();
    }

    public void setQuantity(int quantity) {
        this.invoiceItem.setQuantity(quantity);
    }

    public BigDecimal getUnitPriceExcludingTaxes() {
        return this.invoiceItem.getUnitPriceExcludingTaxes();
    }

    public void setUnitPriceExcludingTaxes(BigDecimal unitPriceExcludingTaxes) {
        this.invoiceItem.setUnitPriceExcludingTaxes(unitPriceExcludingTaxes);
    }

    public BigDecimal getTaxRate() {
        return invoiceItem.getProduct() == null ? BigDecimal.ZERO : invoiceItem.getProduct().getTaxRate();
    }

    public BigDecimal getTotalIncludingTaxes() {
        return this.invoiceItem.getTotalIncludingTaxes();
    }

    public void setTotalIncludingTaxes(BigDecimal totalIncludingTaxes) {
        this.invoiceItem.setTotalIncludingTaxes(totalIncludingTaxes);
    }

    public BigDecimal getTotalExcludingTaxes() {
        return this.invoiceItem.getTotalExcludingTaxes();
    }

    public void setTotalExcludingTaxes(BigDecimal totalExcludingTaxes) {
        this.invoiceItem.setTotalExcludingTaxes(totalExcludingTaxes);
    }

    public BigDecimal getTotalTaxes() {
        return this.invoiceItem.getTotalTaxes();
    }

    public void setTotalTaxes(BigDecimal totalTaxes) {
        this.invoiceItem.setTotalTaxes(totalTaxes);
    }

    private static class ProductComboCell extends ListCell<Product> {
        @Override
        protected void updateItem(Product product, boolean bln) {
            super.updateItem(product, bln);
            setText(product != null ? product.getName() : null);
        }
    }
}
