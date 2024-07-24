package com.example.gestioncommercial.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    @FXML
    private Button saveButton;
    @FXML
    private TextField addressTextField, commonCompanyIdentifierTextField, nameTextField, phoneNumberTextField, taxIdentificationNumberTextField;
    private int id;
    private ClientRepository clientRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientRepository = new ClientRepository();

        saveButton.setOnAction(e -> {
            try {
                saveClient();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void saveClient() throws SQLException {
        Client client = mapClient();
        clientRepository.save(client);
        displaySuccessAlert();
        clearTextFields();
    }

    public void updateClient() throws SQLException {
        Client client = mapClient();
        clientRepository.update(client);
        displaySuccessAlert();
    }

    public void initClientUpdate(Client client) {
        this.id = client.getId();
        this.nameTextField.setText(client.getName());
        this.phoneNumberTextField.setText(client.getPhoneNumber());
        this.addressTextField.setText(client.getAddress());
        this.commonCompanyIdentifierTextField.setText(client.getCommonCompanyIdentifier());
        this.taxIdentificationNumberTextField.setText(client.getTaxIdentificationNumber());

        saveButton.setOnAction(e -> {
            try {
                updateClient();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
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
}
