package com.example.gestioncommercial.product;

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
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListProductsController implements Initializable {
    ProductRepository productRepository;
    @FXML
    private TableView<Product> productsTableView;
    @FXML
    private Button deleteButton, newButton, updateButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productRepository = new ProductRepository();

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initProductsTableView();
    }

    public void addProduct(ActionEvent actionEvent) throws IOException, SQLException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("form-product.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
        refreshProductsTable();
    }

    public void updateProduct(ActionEvent actionEvent) throws IOException, SQLException {
        Product selectedProduct = productsTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-product.fxml"));
            Parent root = fxmlLoader.load();

            ProductController productController = fxmlLoader.getController();

            productController.initProductUpdate(selectedProduct);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            refreshProductsTable();
        }
    }

    public void deleteProduct(ActionEvent actionEvent) throws SQLException {
        Product selectedProduct = productsTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer ce produit?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                productRepository.deleteById(selectedProduct.getId());
                refreshProductsTable();
            }
        }
    }

    private void initProductsTableView() {
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<Product, String> nameColumn = new TableColumn<>("Nom");
        TableColumn<Product, String> descriptionColumn = new TableColumn<>("Description");
        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Quantite");
        TableColumn<Product, BigDecimal> purchasePriceExcludingTaxColumn = new TableColumn<>("Prix d'achat (HT)");
        TableColumn<Product, BigDecimal> sellingPriceExcludingTaxColumn = new TableColumn<>("Prix de vente (HT)");
        TableColumn<Product, BigDecimal> taxRateColumn = new TableColumn<>("taux TVA");

        productsTableView.getColumns().addAll(
                idColumn,
                nameColumn,
                descriptionColumn,
                purchasePriceExcludingTaxColumn,
                sellingPriceExcludingTaxColumn,
                taxRateColumn,
                quantityColumn
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
        taxRateColumn.setCellValueFactory(new PropertyValueFactory<>("taxRate"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        productsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            refreshProductsTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshProductsTable() throws SQLException {
        productsTableView.setItems(productRepository.findAll());
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
}
