package com.example.salesmanagement;

import com.example.salesmanagement.salesdocument.*;
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

    public void addClient() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("client/form-client.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listClients() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("client/list-clients.fxml")));
        borderPane.setCenter(pane);
    }

    public void addProduct() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("product/form-product.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listProducts() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("product/list-products.fxml")));
        borderPane.setCenter(pane);
    }

    public void addCategory() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("category/form-category.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listCategories() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("category/list-categories.fxml")));
        borderPane.setCenter(pane);
    }

    public void addTaxRate() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("taxrate/form-taxrate.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listTaxRates() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("taxrate/list-taxrates.fxml")));
        borderPane.setCenter(pane);
    }

    public void listQuotations() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("salesdocument/list-sales-documents.fxml"));
        VBox pane = fxmlLoader.load();

        SalesDocumentsController salesDocumentsController = fxmlLoader.getController();
        salesDocumentsController.setFormClass(Quotation.class);

        borderPane.setCenter(pane);
    }

    public void addQuotation() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("salesdocument/form-sales-document.fxml"));
        VBox pane = fxmlLoader.load();

        SalesDocumentController salesDocumentController = fxmlLoader.getController();
        salesDocumentController.setSalesDocumentType(Quotation.class);

        borderPane.setCenter(pane);
    }

    public void listDeliveryNotes() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("salesdocument/list-sales-documents.fxml"));
        VBox pane = fxmlLoader.load();

        SalesDocumentsController salesDocumentsController = fxmlLoader.getController();
        salesDocumentsController.setFormClass(DeliveryNote.class);

        borderPane.setCenter(pane);
    }

    public void addDeliveryNote() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("salesdocument/form-sales-document.fxml"));
        VBox pane = fxmlLoader.load();

        SalesDocumentController salesDocumentController = fxmlLoader.getController();
        salesDocumentController.setSalesDocumentType(DeliveryNote.class);

        borderPane.setCenter(pane);
    }

    public void addInvoice() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("salesdocument/form-sales-document.fxml"));
        VBox pane = fxmlLoader.load();

        SalesDocumentController salesDocumentController = fxmlLoader.getController();
        salesDocumentController.setSalesDocumentType(Invoice.class);

        borderPane.setCenter(pane);
    }

    public void listInvoices() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("salesdocument/list-sales-documents.fxml"));
        VBox pane = fxmlLoader.load();

        SalesDocumentsController salesDocumentsController = fxmlLoader.getController();
        salesDocumentsController.setFormClass(Invoice.class);

        borderPane.setCenter(pane);
    }

    public void listCreditInvoices() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("salesdocument/list-sales-documents.fxml"));
        VBox pane = fxmlLoader.load();

        SalesDocumentsController salesDocumentsController = fxmlLoader.getController();
        salesDocumentsController.setFormClass(CreditInvoice.class);

        borderPane.setCenter(pane);
    }

    public void addCreditInvoice() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("salesdocument/form-sales-document.fxml"));
        VBox pane = fxmlLoader.load();

        SalesDocumentController salesDocumentController = fxmlLoader.getController();
        salesDocumentController.setSalesDocumentType(CreditInvoice.class);

        borderPane.setCenter(pane);
    }

    public void editConfig() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("configuration/form-config.fxml")));
        borderPane.setCenter(pane);
    }
}

