package com.example.salesmanagement;

import com.example.salesmanagement.document.*;
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

    public void addSupplier() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("supplier/form-supplier.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listSuppliers() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("supplier/list-suppliers.fxml")));
        borderPane.setCenter(pane);
    }

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

    public void addPurchaseOrder() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(PurchaseOrder.class);

        borderPane.setCenter(pane);
    }

    public void listPurchaseOrders() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(PurchaseOrder.class);

        borderPane.setCenter(pane);
    }

    public void addPurchaseDeliveryNote() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(PurchaseDeliveryNote.class);

        borderPane.setCenter(pane);
    }

    public void listPurchaseDeliveryNotes() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(PurchaseDeliveryNote.class);

        borderPane.setCenter(pane);
    }

    public void listQuotations() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(Quotation.class);

        borderPane.setCenter(pane);
    }

    public void addQuotation() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(Quotation.class);

        borderPane.setCenter(pane);
    }

    public void listDeliveryNotes() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(DeliveryNote.class);

        borderPane.setCenter(pane);
    }

    public void addDeliveryNote() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(DeliveryNote.class);

        borderPane.setCenter(pane);
    }

    public void addInvoice() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(Invoice.class);

        borderPane.setCenter(pane);
    }

    public void listInvoices() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(Invoice.class);

        borderPane.setCenter(pane);
    }

    public void listCreditInvoices() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(CreditInvoice.class);

        borderPane.setCenter(pane);
    }

    public void addCreditInvoice() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(CreditInvoice.class);

        borderPane.setCenter(pane);
    }

    public void editConfig() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("configuration/form-config.fxml")));
        borderPane.setCenter(pane);
    }
}

