package com.example.gestioncommercial.client;

import com.example.gestioncommercial.DataAccessObject;
import javafx.event.ActionEvent;
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
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private TextField addressTextField;
    @FXML
    private TextField commonCompanyIdentifierTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private TextField taxIdentificationNumberTextField;

    private int id;


    private DataAccessObject dao;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dao = new DataAccessObject();
    }


    public void addClient() throws SQLException {
        String insertClientQuery = "INSERT INTO Client(name, phone_number, address, common_company_identifier, tax_identification_number) VALUES('%s', '%s', '%s', '%s', '%s')"
                .formatted(
                        nameTextField.getText(),
                        phoneNumberTextField.getText(),
                        addressTextField.getText(),
                        commonCompanyIdentifierTextField.getText(),
                        taxIdentificationNumberTextField.getText()
                );

        dao.saveData(insertClientQuery);

        nameTextField.clear();
        phoneNumberTextField.clear();
        addressTextField.clear();
        commonCompanyIdentifierTextField.clear();
        taxIdentificationNumberTextField.clear();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Client ajouter avec success");
        alert.showAndWait();
    }

    public void updateClient(ActionEvent actionEvent) throws SQLException {
        String updateClientQuery = "UPDATE Client SET name = '%s', phone_number ='%s', address ='%s', common_company_identifier ='%s', tax_identification_number ='%s' where id = '%d'"
                .formatted(
                        nameTextField.getText(),
                        phoneNumberTextField.getText(),
                        addressTextField.getText(),
                        commonCompanyIdentifierTextField.getText(),
                        taxIdentificationNumberTextField.getText(),
                        id
                );

        dao.saveData(updateClientQuery);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Client modifier avec success");
        alert.showAndWait();
    }

    public void initFields(int id, String name, String phoneNumber, String address, String commonCompanyIdentifier, String taxIdentificationNumber) {
        this.id = id;
        this.nameTextField.setText(name);
        this.phoneNumberTextField.setText(phoneNumber);
        this.addressTextField.setText(address);
        this.commonCompanyIdentifierTextField.setText(commonCompanyIdentifier);
        this.taxIdentificationNumberTextField.setText(taxIdentificationNumber);
    }

}
