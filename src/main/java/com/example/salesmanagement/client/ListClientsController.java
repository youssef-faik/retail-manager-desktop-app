package com.example.salesmanagement.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListClientsController implements Initializable {
    private static final int ROWS_PER_PAGE = 13;
    @FXML
    TableView<Client> clientsTableView;
    private FilteredList<Client> filteredList;
    private SortedList<Client> sortedList;
    private ObservableList<Client> observableList;
    @FXML
    private Button deleteButton, updateButton, newButton;
    @FXML
    private Pagination pagination;
    @FXML
    private TextField searchTextField;

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
        newButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));
        ((Text) newButton.getGraphic()).setFill(Color.WHITE);

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initClientsTableView();
        refreshClientsTable();

        int dataSize = (clientsTableView.getItems().size() / ROWS_PER_PAGE) + 1;
        pagination.setPageCount(dataSize);
        pagination.setPageFactory(this::createPage);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(client -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return client.getName().toLowerCase().contains(lowerCaseFilter)
                        || (client.getCommonCompanyIdentifier() != null && client.getCommonCompanyIdentifier().toLowerCase().contains(lowerCaseFilter))
                        || (client.getTaxIdentificationNumber() != null && client.getTaxIdentificationNumber().toLowerCase().contains(lowerCaseFilter))
                        || client.getPhoneNumber().toLowerCase().contains(lowerCaseFilter)
                        || client.getAddress().toLowerCase().contains(lowerCaseFilter);

            });

            // Update pagination after filtering
            int pageCount = (filteredList.size() / ROWS_PER_PAGE) + 1;
            pagination.setPageCount(pageCount);
            // Reset to first page after filter change
            pagination.setCurrentPageIndex(0);
            // Update the table view with the new first page
            createPage(0);
        });
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
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        clientsTableView.getSelectionModel().clearSelection();
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
            updateButton.setDisable(true);
            deleteButton.setDisable(true);
            clientsTableView.getSelectionModel().clearSelection();
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
                    clientsTableView.getSelectionModel().clearSelection();
                    displaySuccessAlert();
                } else {
                    displayErrorAlert("Cet enregistrement ne peut pas être supprimé, car il est référencé par d'autres enregistrements.");
                }
            }

            updateButton.setDisable(true);
            deleteButton.setDisable(true);
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
        clientsTableView.getItems().clear();
        clientsTableView.getItems().addAll(ClientRepository.findAll());

        observableList = clientsTableView.getItems();

        filteredList = new FilteredList<>(observableList);
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(clientsTableView.comparatorProperty());
        clientsTableView.setItems(sortedList);

        // Update pagination
        int pageCount = (filteredList.size() / ROWS_PER_PAGE) + 1;
        pagination.setPageCount(pageCount);

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

    private void displayErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredList.size());

        List<Client> subbedList = filteredList.subList(fromIndex, toIndex);
        clientsTableView.setItems(FXCollections.observableArrayList(subbedList));
        return clientsTableView;
    }

    public ObservableList<Client> getClientsObservableList() {
        return clientsTableView.getItems();
    }
}
