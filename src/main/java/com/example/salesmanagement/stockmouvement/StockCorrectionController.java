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
    public TextField newQuantityTextField;
    private StockMovement stockMovement;
    private int originalQuantityUpdatedBy;
    private TableView<StockMovement> stockMovementTableView;

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
        String newValueString = quantityUpdatedByTextField.getText().trim();

        if (newValueString.isBlank()) {
            displayErrorAlert("Quantité est obligatoire");
            return;
        }

        int quantity;

        try {
            quantity = Integer.parseInt(newValueString);
        } catch (NumberFormatException e) {
            displayErrorAlert("La quantité ne doit contenir que des chiffres");
            return;
        }

        if (quantity < 0 || quantity > 9_999_999) {
            displayErrorAlert("la valeur de la quantité doit être comprise entre 0 et 9,999,999");
            return;
        }

        Optional<StockMovement> optionalStockMovement = StockMovementRepository.update(stockMovement);

        if (optionalStockMovement.isPresent()) {
            Optional<StockMovement> stockMovementOptional = stockMovementTableView.getItems().stream().filter(stockMovement1 -> stockMovement1.getId() == optionalStockMovement.get().getId()).findFirst();
            stockMovementOptional.ifPresent(stockMovement1 -> {
                stockMovement1.setQuantity(quantity);
                stockMovement1.setMovementType(stockMovement.getMovementType());
                stockMovementTableView.refresh();
            });

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void setStockMovement(StockMovement stockMovement) {
        originalQuantityUpdatedBy = stockMovement.getQuantity();
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
        newQuantityTextField.setText(Integer.toString(actualQuantity));

        quantityUpdatedByTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String newValueString = newValue.trim();

            if (newValueString.isBlank()) {
                newQuantityTextField.setText("Valeur erronée");
                return;
            }

            int quantity;

            try {
                quantity = Integer.parseInt(newValueString);
            } catch (NumberFormatException e) {
                newQuantityTextField.setText("Valeur erronée");
                return;
            }

            if (quantity < 0 || quantity > 9_999_999) {
                newQuantityTextField.setText("Valeur erronée");
                return;
            }

            this.stockMovement.setQuantity(quantity);

            newQuantityTextField.setText(Integer.toString(
                    this.stockMovement.getMovementType() == MovementType.OUT_OF_STOCK ?
                            actualQuantity - originalQuantityUpdatedBy - this.stockMovement.getQuantity()
                            : actualQuantity - originalQuantityUpdatedBy + this.stockMovement.getQuantity()));
                }
        );

        correctionTypeComboBox.setOnAction(event -> {
            MovementType movementType = correctionTypeComboBox.getSelectionModel().getSelectedItem();
            newQuantityTextField.setText(Integer.toString(
                    movementType == MovementType.OUT_OF_STOCK ?
                            actualQuantity - originalQuantityUpdatedBy - this.stockMovement.getQuantity()
                            : actualQuantity - originalQuantityUpdatedBy + this.stockMovement.getQuantity()));

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

    public void setStockMovementTableView(TableView<StockMovement> stockMovementTableView) {
        this.stockMovementTableView = stockMovementTableView;
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
