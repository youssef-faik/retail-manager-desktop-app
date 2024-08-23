package com.example.salesmanagement.supplier;

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
import java.util.Optional;
import java.util.ResourceBundle;

public class ListSuppliersController implements Initializable {
    @FXML
    private TableView<Supplier> supplierTableView;

    @FXML
    private Button deleteButton, updateButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initSuppliersTableView();
        refreshSuppliersTable();
    }

    public void addSupplier() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-supplier.fxml"));
        Parent root = fxmlLoader.load();

        SupplierController supplierController = fxmlLoader.getController();
        supplierController.setListSuppliersController(this);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void updateSupplier() throws IOException {
        Supplier selectedSupplier = supplierTableView.getSelectionModel().getSelectedItem();
        if (selectedSupplier != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-supplier.fxml"));
            Parent root = fxmlLoader.load();

            SupplierController supplierController = fxmlLoader.getController();
            supplierController.initSupplierUpdate(selectedSupplier);
            supplierController.setListSuppliersController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        }
    }

    public void deleteSupplier() {
        Supplier selectedSupplier = supplierTableView.getSelectionModel().getSelectedItem();
        if (selectedSupplier != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer ce fournisseur?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (SupplierRepository.deleteById(selectedSupplier.getId())) {
                    supplierTableView.getItems().remove(selectedSupplier);
                    displaySuccessAlert();
                } else {
                    displayErrorAlert();
                }
            }
        }
    }

    private void initSuppliersTableView() {
        TableColumn<Supplier, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<Supplier, String> nameColumn = new TableColumn<>("Nom");
        TableColumn<Supplier, String> addressColumn = new TableColumn<>("Adresse");
        TableColumn<Supplier, String> phoneNumberColumn = new TableColumn<>("Telephone");
        TableColumn<Supplier, String> commonCompanyIdentifierColumn = new TableColumn<>("ICE");
        TableColumn<Supplier, String> taxIdentificationNumberColumn = new TableColumn<>("IF");


        supplierTableView.getColumns().addAll(
                idColumn,
                nameColumn,
                addressColumn,
                phoneNumberColumn,
                commonCompanyIdentifierColumn,
                taxIdentificationNumberColumn
        );

        supplierTableView.setOnMouseClicked(e -> {
            if (supplierTableView.getSelectionModel().getSelectedItem() != null) {
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

        supplierTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void refreshSuppliersTable() {
        supplierTableView.setItems(SupplierRepository.findAll());
        supplierTableView.refresh();
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

    public TableView<Supplier> getSuppliersTable() {
        return supplierTableView;
    }
}
