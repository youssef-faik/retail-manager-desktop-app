package com.example.salesmanagement.taxrate;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class TaxRateController implements Initializable {
    @FXML
    public TextField labelTextField, valueTextField;
    @FXML
    public Button saveButton, cancelButton;

    private Long id;
    private ListTaxRatesController listTaxRatesController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DropShadow dropShadow = new DropShadow(
                BlurType.ONE_PASS_BOX,
                Color.color(0.6392, 0.6392, 0.6392, 1.0),
                10.0,
                0,
                0,
                0
        );

        cancelButton.setEffect(dropShadow);
        saveButton.setEffect(dropShadow);
        saveButton.setTextFill(Color.color(1, 1, 1));
        saveButton.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(3.0), null)));

        saveButton.setOnAction(e -> saveTaxRate());
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });
    }

    public void saveTaxRate() {
        TaxRate taxRate = mapTaxRate();
        if (TaxRateRepository.save(taxRate)) {
            clearTextFields();

            if (listTaxRatesController != null) {
                listTaxRatesController.taxRateTableView.getItems().add(taxRate);
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateTaxRate() {
        TaxRate taxRate = mapTaxRate();
        Optional<TaxRate> optionalTaxRate = TaxRateRepository.update(taxRate);

        if (optionalTaxRate.isPresent()) {
            if (listTaxRatesController != null) {
                int index = listTaxRatesController.taxRateTableView.getItems().indexOf(taxRate);

                if (index != -1) {
                    TaxRate oldTaxRate = listTaxRatesController.taxRateTableView.getItems().get(index);

                    listTaxRatesController.taxRateTableView.getItems().remove(oldTaxRate);
                    listTaxRatesController.taxRateTableView.getItems().add(optionalTaxRate.get());
                    listTaxRatesController.taxRateTableView.refresh();
                }
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private TaxRate mapTaxRate() {
        TaxRate taxRate = new TaxRate();

        taxRate.setId(id);
        taxRate.setLabel(labelTextField.getText());
        taxRate.setValue(new BigDecimal(valueTextField.getText()));

        return taxRate;
    }

    private void clearTextFields() {
        labelTextField.clear();
        valueTextField.clear();
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

    public void setListTaxRatesController(ListTaxRatesController listTaxRatesController) {
        this.listTaxRatesController = listTaxRatesController;
    }

    public void initTaxRateUpdate(TaxRate taxRate) {
        this.id = taxRate.getId();
        this.labelTextField.setText(taxRate.getLabel());
        this.valueTextField.setText(taxRate.getValue().toString());

        saveButton.setOnAction(e -> updateTaxRate());
    }
}
