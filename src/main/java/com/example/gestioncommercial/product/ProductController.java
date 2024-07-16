package com.example.gestioncommercial.product;

import com.example.gestioncommercial.DataAccessObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ResourceBundle;

public class ProductController implements Initializable {
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField purchasePriceExcludingTaxTextField;
    @FXML
    private TextField quantityTextField;
    @FXML
    private TextField sellingPriceExcludingTaxTextField;
    @FXML
    private TextField taxRateTextField;

    private int id;

    private DataAccessObject dao;

    private static void displaySuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Produit ajouter avec success");
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dao = new DataAccessObject();

    }

    public void addProduct(ActionEvent actionEvent) throws SQLException {
        String insertQuery = getInsertString();
        dao.saveData(insertQuery);
        clearTextFields();
        displaySuccessAlert();
    }

    private void clearTextFields() {
        nameTextField.clear();
        descriptionTextField.clear();
        sellingPriceExcludingTaxTextField.clear();
        purchasePriceExcludingTaxTextField.clear();
        quantityTextField.clear();
        taxRateTextField.clear();
    }

    private String getInsertString() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", symbols);

        String insertQuery = "INSERT INTO Product(name, description, selling_price_excluding_tax, purchase_price_excluding_tax, quantity, tax_rate) VALUES('%s', '%s', %s, %s, %d, %s)"
                .formatted(
                        nameTextField.getText(),
                        descriptionTextField.getText(),
                        df.format(new BigDecimal(sellingPriceExcludingTaxTextField.getText())),
                        df.format(new BigDecimal(purchasePriceExcludingTaxTextField.getText())),
                        Integer.parseInt(quantityTextField.getText()),
                        df.format(new BigDecimal(taxRateTextField.getText()))
                );
        return insertQuery;
    }

    public void initFields(int id, String name, BigDecimal purchasePriceExcludingTax, BigDecimal sellingPriceExcludingTax, String description, int quantity, BigDecimal taxRate) {
        this.id = id;
        this.nameTextField.setText(name);
        this.descriptionTextField.setText(description);
        this.purchasePriceExcludingTaxTextField.setText(String.valueOf(purchasePriceExcludingTax));
        this.sellingPriceExcludingTaxTextField.setText(String.valueOf(sellingPriceExcludingTax));
        this.quantityTextField.setText(String.valueOf(quantity));
        this.taxRateTextField.setText(String.valueOf(taxRate));
    }

    public void updateProduct(ActionEvent actionEvent) throws SQLException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", symbols);

        String updateQuery = "UPDATE Product SET name = '%s', description ='%s', purchase_price_excluding_tax = %s, selling_price_excluding_tax = %s, quantity =%s, tax_rate = %s where id = %d"
                .formatted(
                        nameTextField.getText(),
                        descriptionTextField.getText(),
                        df.format(new BigDecimal(purchasePriceExcludingTaxTextField.getText())),
                        df.format(new BigDecimal(sellingPriceExcludingTaxTextField.getText())),
                        Integer.parseInt(quantityTextField.getText()),
                        df.format(new BigDecimal(taxRateTextField.getText())),
                        id
                );

        dao.saveData(updateQuery);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("produit modifier avec success");
        alert.showAndWait();
    }
}
