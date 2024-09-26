package com.example.salesmanagement.supplier;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class SupplierController implements Initializable {
    private Long id;
    private ListSuppliersController listSuppliersController;

    @FXML
    private Button saveButton, cancelButton;
    @FXML
    private TextField commonCompanyIdentifierTextField, nameTextField, phoneNumberTextField, taxIdentificationNumberTextField;
    @FXML
    private TextArea addressTextArea;

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

        saveButton.setOnAction(e -> saveClient());
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });
    }

    public void saveClient() {
        Supplier supplier = mapSupplier();
        if (SupplierRepository.save(supplier)) {
            clearTextFields();
            if (listSuppliersController != null) {
                listSuppliersController.getSuppliersTable().getItems().add(supplier);
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateClient() {
        Supplier supplier = mapSupplier();
        Optional<Supplier> optionalSupplier = SupplierRepository.update(supplier);

        if (optionalSupplier.isPresent()) {
            if (listSuppliersController != null) {
                int index = listSuppliersController.getSuppliersTable().getItems().indexOf(supplier);

                if (index != -1) {
                    Supplier oldSupplier = listSuppliersController.getSuppliersTable().getItems().get(index);

                    listSuppliersController.getSuppliersTable().getItems().remove(oldSupplier);
                    listSuppliersController.getSuppliersTable().getItems().add(optionalSupplier.get());
                }
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private Supplier mapSupplier() {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setName(nameTextField.getText());
        supplier.setPhoneNumber(phoneNumberTextField.getText());
        supplier.setAddress(addressTextArea.getText());
        supplier.setCommonCompanyIdentifier(commonCompanyIdentifierTextField.getText());
        supplier.setTaxIdentificationNumber(taxIdentificationNumberTextField.getText());

        return supplier;
    }

    public void initSupplierUpdate(Supplier supplier) {
        this.id = supplier.getId();
        this.nameTextField.setText(supplier.getName());
        this.phoneNumberTextField.setText(supplier.getPhoneNumber());
        this.addressTextArea.setText(supplier.getAddress());
        this.commonCompanyIdentifierTextField.setText(supplier.getCommonCompanyIdentifier());
        this.taxIdentificationNumberTextField.setText(supplier.getTaxIdentificationNumber());

        saveButton.setOnAction(e -> updateClient());
    }

    private void clearTextFields() {
        nameTextField.clear();
        phoneNumberTextField.clear();
        addressTextArea.clear();
        commonCompanyIdentifierTextField.clear();
        taxIdentificationNumberTextField.clear();
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

    public void setListSuppliersController(ListSuppliersController listSuppliersController) {
        this.listSuppliersController = listSuppliersController;
    }


}
