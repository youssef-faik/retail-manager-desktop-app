package com.example.salesmanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainController {
    @FXML
    private BorderPane borderPane;

    public void addClient(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("client/form-client.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listClients(ActionEvent actionEvent) throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("client/list-clients.fxml")));
        borderPane.setCenter(pane);
    }

    public void addProduct(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("product/form-product.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listProducts(ActionEvent actionEvent) throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("product/list-products.fxml")));
        borderPane.setCenter(pane);
    }

    public void addCategory(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("category/form-category.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listCategories(ActionEvent actionEvent) throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("category/list-categories.fxml")));
        borderPane.setCenter(pane);
    }

    public void addTaxRate(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("taxrate/form-taxrate.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listTaxRates(ActionEvent actionEvent) throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("taxrate/list-taxrates.fxml")));
        borderPane.setCenter(pane);
    }

    public void addInvoice(ActionEvent actionEvent) throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("invoice/form-invoice.fxml")));
        borderPane.setCenter(pane);
    }

    public void listInvoices(ActionEvent actionEvent) throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("invoice/list-invoices.fxml")));
        borderPane.setCenter(pane);
    }

    public void editConfig() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("configuration/form-config.fxml")));
        borderPane.setCenter(pane);
    }
}

