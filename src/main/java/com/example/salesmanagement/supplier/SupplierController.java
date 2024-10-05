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
import java.util.regex.Pattern;

public class SupplierController implements Initializable {
    private final Supplier supplier = new Supplier();
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
        saveButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));

        saveButton.setOnAction(e -> saveClient());
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });
    }

    public void saveClient() {
        Supplier supplier = mapSupplier();

        if (supplier == null) {
            return;
        }

        if (SupplierRepository.save(supplier)) {
            clearTextFields();
            if (listSuppliersController != null) {
                listSuppliersController.getSuppliersObservableList().add(supplier);
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateClient() {
        Supplier supplier = mapSupplier();

        if (supplier == null) {
            return;
        }

        Optional<Supplier> optionalSupplier = SupplierRepository.update(supplier);

        if (optionalSupplier.isPresent()) {
            if (listSuppliersController != null) {
                int index = listSuppliersController.getSuppliersObservableList().indexOf(supplier);

                if (index != -1) {
                    Supplier oldSupplier = listSuppliersController.getSuppliersObservableList().get(index);

                    listSuppliersController.getSuppliersObservableList().remove(oldSupplier);
                    listSuppliersController.getSuppliersObservableList().add(index, optionalSupplier.get());
                    listSuppliersController.supplierTableView.refresh();
                }
            }

            initSupplierUpdate(optionalSupplier.get());
            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private Supplier mapSupplier() {
        String name = nameTextField.getText().trim();

        if (name.isBlank()) {
            displayErrorAlert("Nom du fournisseur est obligatoire");
            return null;
        }

        Optional<Supplier> byName = SupplierRepository.findByName(name.toLowerCase());

        if (supplier.getId() == null) {
            if (byName.isPresent()) {
                displayErrorAlert("Un fournisseur avec le même nom existe déjà");
                return null;
            }
        } else {
            if (!name.equalsIgnoreCase(supplier.getName())) {
                if (byName.isPresent()) {
                    displayErrorAlert("Un fournisseur avec le même nom existe déjà");
                    return null;
                }
            }
        }

        String commonCompanyIdentifier = null;
        if (commonCompanyIdentifierTextField.getText() != null) {
            commonCompanyIdentifier = commonCompanyIdentifierTextField.getText().trim();
            if (commonCompanyIdentifier.isBlank()) {
                commonCompanyIdentifier = null;
            } else {
                if (!Pattern.matches("^\\d+$", commonCompanyIdentifier)) {
                    displayErrorAlert("L'ICE ne doit contenir que des chiffres");
                    return null;
                }

                Optional<Supplier> byICE = SupplierRepository.findByICE(commonCompanyIdentifier);
                if (supplier.getId() == null) {
                    if (byICE.isPresent()) {
                        displayErrorAlert("Un fournisseur avec le même ICE existe déjà");
                        return null;
                    }
                } else {
                    if (!commonCompanyIdentifier.equals(supplier.getCommonCompanyIdentifier())) {
                        if (byICE.isPresent()) {
                            displayErrorAlert("Un fournisseur avec le même ICE existe déjà");
                            return null;
                        }
                    }
                }
            }
        }

        String taxIdentificationNumber = null;
        if (taxIdentificationNumberTextField.getText() != null) {
            taxIdentificationNumber = taxIdentificationNumberTextField.getText().trim();
            if (taxIdentificationNumber.isBlank()) {
                taxIdentificationNumber = null;
            } else {
                if (!Pattern.matches("^\\d+$", taxIdentificationNumber)) {
                    displayErrorAlert("Le numéro d'identification fiscale ne doit contenir que des chiffres");
                    return null;
                }

                Optional<Supplier> byIF = SupplierRepository.findByIF(taxIdentificationNumber);
                if (supplier.getId() == null) {
                    if (byIF.isPresent()) {
                        displayErrorAlert("Un fournisseur avec le même IF existe déjà");
                        return null;
                    }
                } else {
                    if (!taxIdentificationNumber.equals(supplier.getTaxIdentificationNumber())) {
                        if (byIF.isPresent()) {
                            displayErrorAlert("Un fournisseur avec le même IF existe déjà");
                            return null;
                        }
                    }
                }
            }
        }

        Supplier supplier = new Supplier();
        supplier.setId(this.supplier.getId());
        supplier.setName(name);
        supplier.setCommonCompanyIdentifier(commonCompanyIdentifier);
        supplier.setTaxIdentificationNumber(taxIdentificationNumber);
        supplier.setPhoneNumber(phoneNumberTextField.getText() == null ? "" : phoneNumberTextField.getText().trim());
        supplier.setAddress(addressTextArea.getText() == null ? "" : addressTextArea.getText().trim());

        return supplier;
    }

    public void initSupplierUpdate(Supplier supplier) {
        this.supplier.setId(supplier.getId());
        this.supplier.setName(supplier.getName());
        this.supplier.setCommonCompanyIdentifier(supplier.getCommonCompanyIdentifier());
        this.supplier.setTaxIdentificationNumber(supplier.getTaxIdentificationNumber());

        this.nameTextField.setText(this.supplier.getName());
        this.commonCompanyIdentifierTextField.setText(this.supplier.getCommonCompanyIdentifier());
        this.taxIdentificationNumberTextField.setText(this.supplier.getTaxIdentificationNumber());
        this.phoneNumberTextField.setText(this.supplier.getPhoneNumber());
        this.addressTextArea.setText(this.supplier.getAddress());

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

    public void setListSuppliersController(ListSuppliersController listSuppliersController) {
        this.listSuppliersController = listSuppliersController;
    }


}
