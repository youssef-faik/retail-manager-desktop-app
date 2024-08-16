package com.example.salesmanagement.taxrate;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListTaxRatesController implements Initializable {
    @FXML
    public Button newButton, updateButton, deleteButton;
    @FXML
    public TableView<TaxRate> taxRateTableView;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initTaxRatesTableView();
    }

    public void addTaxRate(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-taxrate.fxml"));
        Parent root = fxmlLoader.load();

        TaxRateController taxRateController = fxmlLoader.getController();
        taxRateController.setListTaxRatesController(this);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void updateTaxRate(ActionEvent actionEvent) throws IOException {
        TaxRate selectedTaxRate = taxRateTableView.getSelectionModel().getSelectedItem();
        if (selectedTaxRate != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-taxrate.fxml"));
            Parent root = fxmlLoader.load();

            TaxRateController taxRateController = fxmlLoader.getController();
            taxRateController.setListTaxRatesController(this);
            taxRateController.initTaxRateUpdate(selectedTaxRate);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        }
    }

    public void deleteTaxRate(ActionEvent actionEvent) {
        TaxRate selectedTaxRate = taxRateTableView.getSelectionModel().getSelectedItem();
        if (selectedTaxRate != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez-vous supprimer ce taux de TVA?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (TaxRateRepository.deleteById(selectedTaxRate.getId())) {
                    taxRateTableView.getItems().remove(selectedTaxRate);
                    displaySuccessAlert();
                } else {
                    displayErrorAlert();
                }

                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        }
    }

    private void initTaxRatesTableView() {
        TableColumn<TaxRate, String> labelColumn = new TableColumn<>("Libellé");
        TableColumn<TaxRate, String> valueColumn = new TableColumn<>("Valeur");

        labelColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        taxRateTableView.getColumns().addAll(labelColumn, valueColumn);

        taxRateTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        taxRateTableView.setOnMouseClicked(e -> {
            if (taxRateTableView.getSelectionModel().getSelectedItem() != null) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        taxRateTableView.setItems(TaxRateRepository.findAll());
        taxRateTableView.refresh();
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
}
