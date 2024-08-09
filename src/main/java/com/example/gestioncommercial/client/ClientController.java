package com.example.gestioncommercial.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    private Long id;
    private ListClientsController listClientsController;

    @FXML
    private Button saveButton;
    @FXML
    private TextField addressTextField, commonCompanyIdentifierTextField, nameTextField, phoneNumberTextField, taxIdentificationNumberTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveButton.setOnAction(e -> saveClient());
    }

    public void saveClient() {
        Client client = mapClient();
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

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private Client mapClient() {
        Client client = new Client();
        client.setId(id);
        client.setName(nameTextField.getText());
        client.setPhoneNumber(phoneNumberTextField.getText());
        client.setAddress(addressTextField.getText());
        client.setCommonCompanyIdentifier(commonCompanyIdentifierTextField.getText());
        client.setTaxIdentificationNumber(taxIdentificationNumberTextField.getText());

        return client;
    }

    public void initClientUpdate(Client client) {
        this.id = client.getId();
        this.nameTextField.setText(client.getName());
        this.phoneNumberTextField.setText(client.getPhoneNumber());
        this.addressTextField.setText(client.getAddress());
        this.commonCompanyIdentifierTextField.setText(client.getCommonCompanyIdentifier());
        this.taxIdentificationNumberTextField.setText(client.getTaxIdentificationNumber());

        saveButton.setOnAction(e -> updateClient());
    }

    private void clearTextFields() {
        nameTextField.clear();
        phoneNumberTextField.clear();
        addressTextField.clear();
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

    public void setListClientsController(ListClientsController listClientsController) {
        this.listClientsController = listClientsController;
    }


}
