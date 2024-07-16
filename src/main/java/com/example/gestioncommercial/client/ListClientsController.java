package com.example.gestioncommercial.client;

import com.example.gestioncommercial.DataAccessObject;
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
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListClientsController implements Initializable {
    @FXML
    private TableView<Client> clientsTableView;

    @FXML
    private Button deleteButton;

    @FXML
    private Button newButton;

    @FXML
    private Button updateButton;

    private DataAccessObject dao;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dao = new DataAccessObject();

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        TableColumn<Client, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<Client, String> nameColumn = new TableColumn<>("Nom");
        TableColumn<Client, String> addressColumn = new TableColumn<>("Adresse");
        TableColumn<Client, String> phoneNumberColumn = new TableColumn<>("Telephone");
        TableColumn<Client, String> commonCompanyIdentifierColumn = new TableColumn<>("ICE");
        TableColumn<Client, String> taxIdentificationNumberColumn = new TableColumn<>("IF");


        clientsTableView.getColumns().addAll(
                idColumn,
                nameColumn,
                addressColumn,
                phoneNumberColumn,
                commonCompanyIdentifierColumn,
                taxIdentificationNumberColumn
        );

        clientsTableView.setOnMouseClicked(e -> {
            if(clientsTableView.getSelectionModel().getSelectedItem() != null) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            }else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setVisible(false);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        commonCompanyIdentifierColumn.setCellValueFactory(new PropertyValueFactory<>("commonCompanyIdentifier"));
        taxIdentificationNumberColumn.setCellValueFactory(new PropertyValueFactory<>("taxIdentificationNumber"));

        clientsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            getAllClients();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void getAllClients() throws SQLException {
        String clientQuery = "SELECT * FROM Client";
        clientsTableView.setItems(dao.getClients(clientQuery));
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    public void addClient(ActionEvent actionEvent) throws IOException, SQLException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("add-client.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
        getAllClients();
    }

    public void updateClient(ActionEvent actionEvent) throws IOException, SQLException {
        Client selectedClient = clientsTableView.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("update-client.fxml"));
            Parent root = fxmlLoader.load();

            ClientController clientController = fxmlLoader.getController();

            clientController.initFields(
                    selectedClient.getId(),
                    selectedClient.getName(),
                    selectedClient.getPhoneNumber(),
                    selectedClient.getAddress(),
                    selectedClient.getCommonCompanyIdentifier(),
                    selectedClient.getTaxIdentificationNumber()
            );

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            getAllClients();
        }
    }

    public void deleteClient(ActionEvent actionEvent) throws SQLException {
        Client selectedClient = clientsTableView.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer cet client?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                String deleteClientQuery = "DELETE FROM Client WHERE id = " + selectedClient.getId();
                dao.saveData(deleteClientQuery);
                getAllClients();
            }
        }
    }
}
