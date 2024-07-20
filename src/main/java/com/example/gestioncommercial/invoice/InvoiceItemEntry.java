package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.product.Product;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableRow;

import java.math.BigDecimal;

public class InvoiceItemEntry {
    private ComboBox<Product> productComboBox = new ComboBox<>();
    private int quantity;
    private BigDecimal unitPriceExcludingTaxes;
    private BigDecimal taxRate;
    private BigDecimal totalExcludingTaxes;
    private BigDecimal totalIncludingTaxes;
    private BigDecimal totalTaxes;

    private InvoiceController invoiceController;

    public InvoiceItemEntry() {
    }

    public InvoiceItemEntry(
            ObservableList<Product> items,
            InvoiceController invoiceController) {

        this.invoiceController = invoiceController;
        initProductsComboBox(items);

        this.quantity = 0;
        this.unitPriceExcludingTaxes = BigDecimal.ZERO;
        this.taxRate = BigDecimal.ZERO;
        this.totalExcludingTaxes = BigDecimal.ZERO;
        this.totalIncludingTaxes = BigDecimal.ZERO;
        this.totalTaxes = BigDecimal.ZERO;
    }

    public InvoiceItemEntry(ObservableList<Product> items,
                            InvoiceController invoiceController,
                            InvoiceItem invoiceItem) {

        this.invoiceController = invoiceController;
        initProductsComboBox(items);
        getProductComboBox().setValue(invoiceItem.getProduct());

        this.quantity = invoiceItem.getQuantity();
        this.unitPriceExcludingTaxes = invoiceItem.getUnitPriceExcludingTaxes();
        this.taxRate = invoiceItem.getProduct().getTaxRate();
        this.totalExcludingTaxes = invoiceItem.getTotalExcludingTaxes();
        this.totalIncludingTaxes = invoiceItem.getTotalIncludingTaxes();
        this.totalTaxes = invoiceItem.getTotalTaxes();
    }

    private void initProductsComboBox(ObservableList<Product> items) {
        this.productComboBox = new ComboBox<>(items);
        productComboBox.setCellFactory(x -> new ProductComboCell());
        productComboBox.setButtonCell(new ProductComboCell());
        productComboBox.setPromptText("Sélectionnez produit");
        productComboBox.setMinWidth(350);

        productComboBox.setOnAction(event -> {
            Product product = productComboBox.getSelectionModel().getSelectedItem();
            // set new values for InvoiceItemEntry
            this.quantity = 1;
            this.unitPriceExcludingTaxes = product.getSellingPriceExcludingTax();
            this.taxRate = product.getTaxRate();

            // find new invoice item totals
            BigDecimal priceExcludingTaxes = product.getSellingPriceExcludingTax().multiply(BigDecimal.valueOf(this.quantity));
            BigDecimal taxes = priceExcludingTaxes.multiply(product.getTaxRate());
            BigDecimal priceIncludingTaxes = priceExcludingTaxes.add(taxes);

            // set new totals for InvoiceItemEntry
            this.totalExcludingTaxes = priceExcludingTaxes;
            this.totalIncludingTaxes = priceIncludingTaxes;
            this.totalTaxes = taxes;

            TableRow parent = (TableRow) productComboBox.getParent().getParent();
            parent.getTableView().refresh();

            invoiceController.updateInvoiceTotals();
        });
    }

    public ComboBox<Product> getProductComboBox() {
        return productComboBox;
    }

    public void setProductComboBox(ComboBox<Product> productComboBox) {
        this.productComboBox = productComboBox;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPriceExcludingTaxes() {
        return unitPriceExcludingTaxes;
    }

    public void setUnitPriceExcludingTaxes(BigDecimal unitPriceExcludingTaxes) {
        this.unitPriceExcludingTaxes = unitPriceExcludingTaxes;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTotalIncludingTaxes() {
        return totalIncludingTaxes;
    }

    public void setTotalIncludingTaxes(BigDecimal totalIncludingTaxes) {
        this.totalIncludingTaxes = totalIncludingTaxes;
    }

    public BigDecimal getTotalExcludingTaxes() {
        return totalExcludingTaxes;
    }

    public void setTotalExcludingTaxes(BigDecimal totalExcludingTaxes) {
        this.totalExcludingTaxes = totalExcludingTaxes;
    }

    public BigDecimal getTotalTaxes() {
        return totalTaxes;
    }

    public void setTotalTaxes(BigDecimal totalTaxes) {
        this.totalTaxes = totalTaxes;
    }

    private static class ProductComboCell extends ListCell<Product> {
        @Override
        protected void updateItem(Product product, boolean bln) {
            super.updateItem(product, bln);
            setText(product != null ? product.getName() : null);
        }
    }
}
