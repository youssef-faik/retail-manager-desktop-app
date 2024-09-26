package com.example.salesmanagement.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListClientsController implements Initializable {
    @FXML
    private TableView<Client> clientsTableView;

    @FXML
    private Button deleteButton, updateButton, newButton;

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

        updateButton.setEffect(dropShadow);
        deleteButton.setEffect(dropShadow);
        newButton.setEffect(dropShadow);
        newButton.setTextFill(Color.color(1, 1, 1));
        newButton.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(3.0), null)));
        ((Text) newButton.getGraphic()).setFill(Color.WHITE);

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initClientsTableView();
        refreshClientsTable();
    }

    public void addClient(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-client.fxml"));
        Parent root = fxmlLoader.load();

        ClientController clientController = fxmlLoader.getController();
        clientController.setListClientsController(this);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void updateClient(ActionEvent actionEvent) throws IOException {
        Client selectedClient = clientsTableView.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-client.fxml"));
            Parent root = fxmlLoader.load();

            ClientController clientController = fxmlLoader.getController();
            clientController.initClientUpdate(selectedClient);
            clientController.setListClientsController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        }
    }

    public void deleteClient(ActionEvent actionEvent) {
        Client selectedClient = clientsTableView.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer cet client?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (ClientRepository.deleteById(selectedClient.getId())) {
                    clientsTableView.getItems().remove(selectedClient);
                    displaySuccessAlert();
                } else {
                    displayErrorAlert();
                }
            }
        }
    }

    private void initClientsTableView() {
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
            if (clientsTableView.getSelectionModel().getSelectedItem() != null) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
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
    }

    public void refreshClientsTable() {
        clientsTableView.setItems(ClientRepository.findAll());
        clientsTableView.refresh();
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
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

    public TableView<Client> getClientsTable() {
        return clientsTableView;
    }
}
