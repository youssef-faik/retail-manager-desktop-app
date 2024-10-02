package com.example.salesmanagement.supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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

public class ListSuppliersController implements Initializable {
    private static final int ROWS_PER_PAGE = 13;
    FilteredList<Supplier> filteredList;
    SortedList<Supplier> sortedList;
    ObservableList<Supplier> observableList;
    @FXML
    private TableView<Supplier> supplierTableView;
    @FXML
    private Pagination pagination;
    @FXML
    private Button deleteButton, updateButton, newButton;
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
        newButton.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(3.0), null)));
        ((Text) newButton.getGraphic()).setFill(Color.WHITE);

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initSuppliersTableView();
        refreshSuppliersTable();

        int dataSize = (supplierTableView.getItems().size() / ROWS_PER_PAGE) + 1;
        pagination.setPageCount(dataSize);
        pagination.setPageFactory(this::createPage);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(supplier -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return supplier.getName().toLowerCase().contains(lowerCaseFilter)
                        || (supplier.getCommonCompanyIdentifier() != null && supplier.getCommonCompanyIdentifier().toLowerCase().contains(lowerCaseFilter))
                        || (supplier.getTaxIdentificationNumber() != null && supplier.getTaxIdentificationNumber().toLowerCase().contains(lowerCaseFilter))
                        || supplier.getPhoneNumber().toLowerCase().contains(lowerCaseFilter)
                        || supplier.getAddress().toLowerCase().contains(lowerCaseFilter);

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

        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        supplierTableView.getSelectionModel().clearSelection();
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

            updateButton.setDisable(true);
            deleteButton.setDisable(true);
            supplierTableView.getSelectionModel().clearSelection();
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
                    supplierTableView.getSelectionModel().clearSelection();
                    displaySuccessAlert();
                } else {
                    displayErrorAlert("Cet enregistrement ne peut pas être supprimé, car il est référencé par d'autres enregistrements.");
                }
            }

            updateButton.setDisable(true);
            deleteButton.setDisable(true);
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

        observableList = supplierTableView.getItems();

        filteredList = new FilteredList<>(observableList);
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(supplierTableView.comparatorProperty());
        supplierTableView.setItems(sortedList);

        // Update pagination
        int pageCount = (filteredList.size() / ROWS_PER_PAGE) + 1;
        pagination.setPageCount(pageCount);

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

        List<Supplier> subbedList = filteredList.subList(fromIndex, toIndex);
        supplierTableView.setItems(FXCollections.observableArrayList(subbedList));
        return supplierTableView;
    }

    public TableView<Supplier> getSuppliersTable() {
        return supplierTableView;
    }
}
