package com.example.gestioncommercial.product;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProductController implements Initializable {
    @FXML
    private Button saveButton;
    @FXML
    private TextField descriptionTextField, nameTextField, purchasePriceExcludingTaxTextField, quantityTextField, sellingPriceExcludingTaxTextField, taxRateTextField;
    private int id;
    private ProductRepository productRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productRepository = new ProductRepository();

        saveButton.setOnAction(e -> {
            try {
                saveProduct();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void saveProduct() throws SQLException {
        Product product = mapProduct();
        productRepository.save(product);
        displaySuccessAlert();
        clearTextFields();
    }

    public void updateProduct() throws SQLException {
        Product product = mapProduct();
        productRepository.update(product);
        displaySuccessAlert();
    }

    public void initProductUpdate(Product product) {
        this.id = product.getId();
        this.nameTextField.setText(product.getName());
        this.descriptionTextField.setText(product.getDescription());
        this.purchasePriceExcludingTaxTextField.setText(String.valueOf(product.getPurchasePriceExcludingTax()));
        this.sellingPriceExcludingTaxTextField.setText(String.valueOf(product.getSellingPriceExcludingTax()));
        this.quantityTextField.setText(String.valueOf(product.getQuantity()));
        this.taxRateTextField.setText(String.valueOf(product.getTaxRate()));

        saveButton.setOnAction(e -> {
            try {
                updateProduct();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private Product mapProduct() {
        Product product = new Product();
        product.setId(id);
        product.setName(nameTextField.getText());
        product.setDescription(descriptionTextField.getText());
        product.setSellingPriceExcludingTax(new BigDecimal(sellingPriceExcludingTaxTextField.getText()));
        product.setPurchasePriceExcludingTax(new BigDecimal(purchasePriceExcludingTaxTextField.getText()));
        product.setQuantity(Integer.parseInt(quantityTextField.getText()));
        product.setTaxRate(new BigDecimal(taxRateTextField.getText()));
        return product;
    }

    private void clearTextFields() {
        nameTextField.clear();
        descriptionTextField.clear();
        sellingPriceExcludingTaxTextField.clear();
        purchasePriceExcludingTaxTextField.clear();
        quantityTextField.clear();
        taxRateTextField.clear();
    }

    private void displaySuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Operation effectué avec success");
        alert.showAndWait();
    }
}
