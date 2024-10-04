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
    private final TaxRate taxRate = new TaxRate();
    @FXML
    public TextField labelTextField, valueTextField;
    @FXML
    public Button saveButton, cancelButton;
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
        saveButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));

        saveButton.setOnAction(e -> saveTaxRate());
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });
    }

    public void saveTaxRate() {
        TaxRate taxRate = mapTaxRate();

        if (taxRate == null) {
            return;
        }

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

        if (taxRate == null) {
            return;
        }

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

            this.taxRate.setValue(optionalTaxRate.get().getValue());
            this.taxRate.setLabel(optionalTaxRate.get().getLabel());
            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private TaxRate mapTaxRate() {
        String label = labelTextField.getText().trim();

        if (label.isBlank()) {
            displayErrorAlert("Libellé est obligatoire");
            return null;
        }

        Optional<TaxRate> byLabel = TaxRateRepository.findByLabel(label.toLowerCase());

        if (taxRate.getId() == null) {
            if (byLabel.isPresent()) {
                displayErrorAlert("Un taux de taxe avec le même libellé existe déjà");
                return null;
            }
        } else {
            if (!label.equalsIgnoreCase(taxRate.getLabel()) && byLabel.isPresent()) {
                displayErrorAlert("Un taux de taxe avec le même libellé existe déjà");
                return null;
            }
        }

        String value = valueTextField.getText().trim();
        if (value.isBlank()) {
            displayErrorAlert("Valeur est obligatoire");
            return null;
        }

        BigDecimal bigDecimalValue;
        try {
            bigDecimalValue = new BigDecimal(value);
        } catch (NumberFormatException e) {
            displayErrorAlert("la valeur du taux de taxe n'est pas valide");
            return null;
        }

        if (!(bigDecimalValue.compareTo(BigDecimal.ZERO) >= 0
                && bigDecimalValue.compareTo(BigDecimal.valueOf(100L)) <= 0)
        ) {
            displayErrorAlert("la valeur du taux de taxe doit être comprise entre 0 et 100");
            return null;
        }

        Optional<TaxRate> byValue = TaxRateRepository.findByValue(bigDecimalValue);

        if (taxRate.getId() == null) {
            if (byValue.isPresent()) {
                displayErrorAlert("Un taux de taxe avec la même valeur existe déjà");
                return null;
            }
        } else {
            if (byValue.isPresent() && bigDecimalValue.compareTo(taxRate.getValue()) != 0) {
                displayErrorAlert("Un taux de taxe avec la même valeur existe déjà");
                return null;
            }
        }


        TaxRate taxRate = new TaxRate();
        taxRate.setId(this.taxRate.getId());
        taxRate.setLabel(label);
        taxRate.setValue(bigDecimalValue);

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

    private void displayErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
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
        this.taxRate.setId(taxRate.getId());
        this.taxRate.setLabel(taxRate.getLabel());
        this.taxRate.setValue(taxRate.getValue());

        this.labelTextField.setText(this.taxRate.getLabel());
        this.valueTextField.setText(this.taxRate.getValue().toString());

        saveButton.setOnAction(e -> updateTaxRate());
    }
}
