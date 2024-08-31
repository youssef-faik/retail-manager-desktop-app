package com.example.salesmanagement.stockmouvement;

import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class StockCorrectionController implements Initializable {

    public TextField productNameTextField, currentQuantityTextField, quantityUpdatedByTextField;
    public ComboBox<MovementType> correctionTypeComboBox;
    public Button saveButton, cancelButton;
    private StockMovement stockMovement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        correctionTypeComboBox.getItems().addAll(MovementType.values());
        correctionTypeComboBox.setCellFactory(x -> new MovementTypeComboCell());
        correctionTypeComboBox.setButtonCell(new MovementTypeComboCell());

        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });

        saveButton.setOnAction(event -> update());
    }

    private void update() {
        Optional<StockMovement> optionalStockMovement = StockMovementRepository.update(stockMovement);

        if (optionalStockMovement.isPresent()) {
            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void setStockMovement(StockMovement stockMovement) {
        this.stockMovement = new StockMovement(
                stockMovement.getId(),
                stockMovement.getProduct(),
                stockMovement.getQuantity(),
                stockMovement.isCanceled(),
                stockMovement.getDateTime(),
                stockMovement.getMovementType(),
                stockMovement.getMovementSource()
        );

        int actualQuantity = stockMovement.getProduct().getQuantity();

        productNameTextField.setText(this.stockMovement.getProduct().getName());
        currentQuantityTextField.setText(Integer.toString(actualQuantity));
        quantityUpdatedByTextField.setText(Integer.toString(this.stockMovement.getQuantity()));
        correctionTypeComboBox.setValue(this.stockMovement.getMovementType());

        quantityUpdatedByTextField.textProperty().addListener((observable, oldValue, newValue) ->
                {
                    int quantityUpdatedBy = Integer.parseInt(newValue);

                    this.stockMovement.setQuantity(quantityUpdatedBy);
                }
        );

        correctionTypeComboBox.setOnAction(event -> {
            MovementType movementType = correctionTypeComboBox.getSelectionModel().getSelectedItem();

            this.stockMovement.setMovementType(movementType);
        });
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

    private static class MovementTypeComboCell extends ListCell<MovementType> {
        @Override
        protected void updateItem(MovementType item, boolean bln) {
            super.updateItem(item, bln);
            setText(item == null ? null : switch (item) {
                case STOCK_ENTRY -> "Entrée de stock";
                case OUT_OF_STOCK -> "Sortie de stock";
            });
        }
    }
}
