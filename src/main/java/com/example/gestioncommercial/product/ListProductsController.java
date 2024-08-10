package com.example.gestioncommercial.product;

import javafx.beans.property.SimpleStringProperty;
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
import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListProductsController implements Initializable {
    @FXML
    private TableView<Product> productsTableView;
    @FXML
    private Button deleteButton, updateButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initProductsTableView();
    }

    public void addProduct(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-product.fxml"));
        Parent root = fxmlLoader.load();

        ProductController productController = fxmlLoader.getController();
        productController.setListProductsController(this);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void updateProduct(ActionEvent actionEvent) throws IOException {
        Product selectedProduct = productsTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-product.fxml"));
            Parent root = fxmlLoader.load();

            ProductController productController = fxmlLoader.getController();
            productController.setListProductsController(this);
            productController.initProductUpdate(selectedProduct);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        }
    }

    public void deleteProduct(ActionEvent actionEvent) {
        Product selectedProduct = productsTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer ce produit?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (ProductRepository.deleteById(selectedProduct.getId())) {
                    productsTableView.getItems().remove(selectedProduct);
                    displaySuccessAlert();
                } else {
                    displayErrorAlert();
                }

                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        }
    }

    private void initProductsTableView() {
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<Product, String> nameColumn = new TableColumn<>("Nom");
        TableColumn<Product, String> descriptionColumn = new TableColumn<>("Description");
        TableColumn<Product, BigDecimal> purchasePriceExcludingTaxColumn = new TableColumn<>("Prix d'achat (HT)");
        TableColumn<Product, BigDecimal> sellingPriceExcludingTaxColumn = new TableColumn<>("Prix de vente (HT)");
        TableColumn<Product, String> taxRateColumn = new TableColumn<>("Taux TVA");
        TableColumn<Product, String> categoryColumn = new TableColumn<>("Catégorie");

        productsTableView.getColumns().addAll(
                idColumn,
                nameColumn,
                descriptionColumn,
                categoryColumn,
                purchasePriceExcludingTaxColumn,
                sellingPriceExcludingTaxColumn,
                taxRateColumn
        );

        productsTableView.setOnMouseClicked(e -> {
            if (productsTableView.getSelectionModel().getSelectedItem() != null) {
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
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        sellingPriceExcludingTaxColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPriceExcludingTax"));
        purchasePriceExcludingTaxColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePriceExcludingTax"));
        categoryColumn.setCellValueFactory(cellData ->
                {
                    if (cellData.getValue().getCategory() != null) {
                        return new SimpleStringProperty(cellData.getValue().getCategory().getName());
                    }
                    return new SimpleStringProperty("N/A");
                }
        );

        taxRateColumn.setCellValueFactory(cellData ->
                {
                    if (cellData.getValue().getTaxRate() != null) {
                        return new SimpleStringProperty(cellData.getValue().getTaxRate().getValue() + " %");
                    }
                    return new SimpleStringProperty("N/A");
                }
        );

        productsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        productsTableView.setItems(ProductRepository.findAll());
        productsTableView.refresh();
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

    public TableView<Product> getProductsTable() {
        return productsTableView;
    }
}
