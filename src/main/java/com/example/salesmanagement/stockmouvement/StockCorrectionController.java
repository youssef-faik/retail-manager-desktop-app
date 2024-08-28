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
    public ComboBox<MouvementType> correctionTypeComboBox;
    public Button saveButton, cancelButton;
    private StockMouvement stockMouvement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        correctionTypeComboBox.getItems().addAll(MouvementType.values());
        correctionTypeComboBox.setCellFactory(x -> new MovementTypeComboCell());
        correctionTypeComboBox.setButtonCell(new MovementTypeComboCell());

        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });

        saveButton.setOnAction(event -> update());
    }

    private void update() {
        Optional<StockMouvement> optionalStockMovement = StockMovementRepository.update(stockMouvement);

        if (optionalStockMovement.isPresent()) {
            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void setStockMovement(StockMouvement stockMouvement) {
        this.stockMouvement = new StockMouvement(
                stockMouvement.getId(),
                stockMouvement.getProduct(),
                stockMouvement.getQuantity(),
                stockMouvement.isCanceled(),
                stockMouvement.getDateTime(),
                stockMouvement.getMovementType(),
                stockMouvement.getMovementSource()
        );

        int actualQuantity = stockMouvement.getProduct().getQuantity();

        productNameTextField.setText(this.stockMouvement.getProduct().getName());
        currentQuantityTextField.setText(Integer.toString(actualQuantity));
        quantityUpdatedByTextField.setText(Integer.toString(this.stockMouvement.getQuantity()));
        correctionTypeComboBox.setValue(this.stockMouvement.getMovementType());

        quantityUpdatedByTextField.textProperty().addListener((observable, oldValue, newValue) ->
                {
                    int quantityUpdatedBy = Integer.parseInt(newValue);

                    this.stockMouvement.setQuantity(quantityUpdatedBy);
                }
        );

        correctionTypeComboBox.setOnAction(event -> {
            MouvementType mouvementType = correctionTypeComboBox.getSelectionModel().getSelectedItem();

            this.stockMouvement.setMovementType(mouvementType);
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

    private static class MovementTypeComboCell extends ListCell<MouvementType> {
        @Override
        protected void updateItem(MouvementType item, boolean bln) {
            super.updateItem(item, bln);
            setText(item == null ? null : switch (item) {
                case STOCK_ENTRY -> "Entrée de stock";
                case OUT_OF_STOCK -> "Sortie de stock";
            });
        }
    }
}
