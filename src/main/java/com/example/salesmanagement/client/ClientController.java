package com.example.salesmanagement.client;

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

public class ClientController implements Initializable {
    private final Client client = new Client();
    private ListClientsController listClientsController;

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
        Client client = mapClient();

        if (client == null) {
            return;
        }

        if (ClientRepository.save(client)) {
            clearTextFields();
            if (listClientsController != null) {
                listClientsController.getClientsTable().getItems().add(client);
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateClient() {
        Client client = mapClient();

        if (client == null) {
            return;
        }

        Optional<Client> optionalClient = ClientRepository.update(client);

        if (optionalClient.isPresent()) {
            if (listClientsController != null) {
                int index = listClientsController.getClientsTable().getItems().indexOf(client);

                if (index != -1) {
                    Client oldClient = listClientsController.getClientsTable().getItems().get(index);

                    listClientsController.getClientsTable().getItems().remove(oldClient);
                    listClientsController.getClientsTable().getItems().add(optionalClient.get());
                }
            }

            initClientUpdate(optionalClient.get());
            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private Client mapClient() {
        String name = nameTextField.getText().trim();

        if (name.isBlank()) {
            displayErrorAlert("Nom du client est obligatoire");
            return null;
        }

        Optional<Client> byName = ClientRepository.findByName(name.toLowerCase());

        if (client.getId() == null) {
            if (byName.isPresent()) {
                displayErrorAlert("Un client avec le même nom existe déjà");
                return null;
            }
        } else {
            if (!name.equalsIgnoreCase(client.getName()) && byName.isPresent()) {
                displayErrorAlert("Un client avec le même nom existe déjà");
                return null;
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

                Optional<Client> byICE = ClientRepository.findByICE(commonCompanyIdentifier);
                if (client.getId() == null) {
                    if (byICE.isPresent()) {
                        displayErrorAlert("Un client avec le même ICE existe déjà");
                        return null;
                    }
                } else {
                    if (!commonCompanyIdentifier.equals(client.getCommonCompanyIdentifier()) && byICE.isPresent()) {
                        displayErrorAlert("Un client avec le même ICE existe déjà");
                        return null;
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

                Optional<Client> byIF = ClientRepository.findByIF(taxIdentificationNumber);
                if (client.getId() == null) {
                    if (byIF.isPresent()) {
                        displayErrorAlert("Un client avec le même IF existe déjà");
                        return null;
                    }
                } else {
                    if (!taxIdentificationNumber.equals(client.getTaxIdentificationNumber()) && byIF.isPresent()) {
                        displayErrorAlert("Un client avec le même IF existe déjà");
                        return null;
                    }
                }
            }
        }


        Client client = new Client();
        client.setId(this.client.getId());
        client.setName(name);
        client.setCommonCompanyIdentifier(commonCompanyIdentifier);
        client.setTaxIdentificationNumber(taxIdentificationNumber);
        client.setPhoneNumber(phoneNumberTextField.getText() == null ? "" : phoneNumberTextField.getText().trim());
        client.setAddress(addressTextArea.getText() == null ? "" : addressTextArea.getText().trim());

        return client;
    }

    public void initClientUpdate(Client client) {
        this.client.setId(client.getId());
        this.client.setName(client.getName());
        this.client.setCommonCompanyIdentifier(client.getCommonCompanyIdentifier() == null ? "" : client.getCommonCompanyIdentifier());
        this.client.setTaxIdentificationNumber(client.getTaxIdentificationNumber() == null ? "" : client.getTaxIdentificationNumber());
        this.client.setPhoneNumber(client.getPhoneNumber() == null ? "" : client.getPhoneNumber());

        this.nameTextField.setText(this.client.getName());
        this.commonCompanyIdentifierTextField.setText(this.client.getCommonCompanyIdentifier());
        this.taxIdentificationNumberTextField.setText(this.client.getTaxIdentificationNumber());
        this.phoneNumberTextField.setText(this.client.getPhoneNumber());
        this.addressTextArea.setText(this.client.getAddress());

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

    private void displayErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setListClientsController(ListClientsController listClientsController) {
        this.listClientsController = listClientsController;
    }

}
