package com.example.gestioncommercial.product;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductController implements Initializable {
    private Long id;
    private ListProductsController listProductsController;

    @FXML
    private Button saveButton;
    @FXML
    private TextField descriptionTextField, nameTextField, purchasePriceExcludingTaxTextField, quantityTextField, sellingPriceExcludingTaxTextField, taxRateTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveButton.setOnAction(e -> saveProduct());
    }

    public void saveProduct() {
        Product product = mapProduct();
        if (ProductRepository.save(product)) {
            clearTextFields();

            if (listProductsController != null) {
                listProductsController.getProductsTable().getItems().add(product);
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateProduct() {
        Product product = mapProduct();
        Optional<Product> optionalProduct = ProductRepository.update(product);

        if (optionalProduct.isPresent()) {
            if (listProductsController != null) {
                int index = listProductsController.getProductsTable().getItems().indexOf(product);

                if (index != -1) {
                    Product oldProduct = listProductsController.getProductsTable().getItems().get(index);

                    listProductsController.getProductsTable().getItems().remove(oldProduct);
                    listProductsController.getProductsTable().getItems().add(optionalProduct.get());
                }
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private Product mapProduct() {
        Product product = new Product();

        product.setId(id);
        product.setName(nameTextField.getText());
        product.setDescription(descriptionTextField.getText());
        product.setSellingPriceExcludingTax(new BigDecimal(sellingPriceExcludingTaxTextField.getText()));
        product.setPurchasePriceExcludingTax(new BigDecimal(purchasePriceExcludingTaxTextField.getText()));
        product.setQuantity(Integer.parseInt(quantityTextField.getText()));
        product.setTaxRate(new BigDecimal(taxRateTextField.getText()).divide(BigDecimal.valueOf(100)));

        return product;
    }

    public void initProductUpdate(Product product) {
        this.id = product.getId();
        this.nameTextField.setText(product.getName());
        this.descriptionTextField.setText(product.getDescription());
        this.purchasePriceExcludingTaxTextField.setText(String.valueOf(product.getPurchasePriceExcludingTax()));
        this.sellingPriceExcludingTaxTextField.setText(String.valueOf(product.getSellingPriceExcludingTax()));
        this.quantityTextField.setText(String.valueOf(product.getQuantity()));
        this.taxRateTextField.setText(String.valueOf(product.getTaxRate().multiply(BigDecimal.valueOf(100L))));

        saveButton.setOnAction(e -> updateProduct());
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

    private void displayErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Une erreur est survenue lors de l'opération.");
        alert.showAndWait();
    }

    public void setListProductsController(ListProductsController listProductsController) {
        this.listProductsController = listProductsController;
    }
}
